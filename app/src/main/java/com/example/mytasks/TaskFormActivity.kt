package com.example.mytasks

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.mytasks.common.ConnectivityManager
import com.example.mytasks.common.ProgressBarLoadingActivity
import com.example.mytasks.common.ProgressBarLoadingFragment
import com.example.mytasks.common.Receiver
import com.example.mytasks.common.constants.CountAdMob
import com.example.mytasks.common.constants.ScreenFilterConstants
import com.example.mytasks.common.constants.TaskConstants
import com.example.mytasks.service.model.TaskModel
import com.example.mytasks.service.repository.AdMobPreferences
import com.example.mytasks.ui.today.TodayViewModel
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.protobuf.Empty
import kotlinx.android.synthetic.main.activity_task_form.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.AbstractCollection


class TaskFormActivity : AppCompatActivity(),
    View.OnClickListener,
    DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener,
    CompoundButton.OnCheckedChangeListener {

    private var _dayOfMonth = 0
    private var _month = 0
    private var _year = 0
    private var _hour = 0
    private var _minute = 0

    lateinit var db: FirebaseFirestore
    private var items = arrayOf<String>()

    private var mRewardedAd: RewardedAd? = null

    private final var TAG = "TaskFormActivityADMOB"

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private lateinit var taskRef: StorageReference
    private lateinit var taskSaved: TaskModel

    private lateinit var dialog: Dialog
    private var imageBitmap: Bitmap? = null
    private lateinit var view: View
    private lateinit var progressButton: ProgressButton

    private lateinit var mTodayViewModel: TodayViewModel
    private lateinit var mAdMobPreferences: AdMobPreferences

    private lateinit var mLoading: ProgressBarLoadingActivity
    private lateinit var alarmManager: AlarmManager
    private var notificationId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_form)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        mTodayViewModel =
            ViewModelProvider(this).get(TodayViewModel::class.java)

        mLoading = ProgressBarLoadingActivity(this)

        view = btnSave

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        items = resources.getStringArray(R.array.priorities_array)
        val adapter = ArrayAdapter(baseContext, R.layout.option_item_priority, items)

        // Set make default value
        autoCompletePriority.setText(adapter.getItem(0).toString(), false)
        autoCompletePriority.setAdapter(adapter);

        db = Firebase.firestore
        auth = Firebase.auth
        storage = FirebaseStorage.getInstance()
        storageRef = storage.getReferenceFromUrl("gs://mytasks-d9a0d.appspot.com/mytasks/task")
        user = auth.currentUser

        mAdMobPreferences = AdMobPreferences(baseContext)

        taskSaved = TaskModel()

        btn_delete.visibility = View.INVISIBLE

        listeners()
        setActualDateAndHours()

        val bundle = intent.extras
        if (bundle != null) {
            setFormByTask()
        }

        configDialog()

        showBannerAdMob()

    }

    private fun showBannerAdMob() {
        adViewBanner.visibility = View.GONE

        val adRequest = AdRequest.Builder().build()
        adViewBanner.loadAd(adRequest)

        val rnds = (1..10).random()

        if (rnds > 7) {
            tilDescription.setPadding(0, 0, 0, 170)
            adViewBanner.visibility = View.VISIBLE
        }

    }

    private fun setFormByTask() {
        var task: TaskModel = intent.extras?.get("extra_task") as TaskModel
        var task_id: String = intent.extras?.get("extra_task_id") as String
        taskSaved = task
        taskSaved.id = task_id
        notificationId = task.notificationId!!

        val taskDate = SimpleDateFormat("dd/MM/yyyy").format(task.date)
        val taskTime = SimpleDateFormat("HH:mm").format(task.date)

        items = resources.getStringArray(R.array.priorities_array)
        val adapter = ArrayAdapter(baseContext, R.layout.option_item_priority, items)

        // Set make default value
        autoCompletePriority.setText(adapter.getItem(task.priority!!).toString(), false)
        autoCompletePriority.setAdapter(adapter);

        txtTask.setText(task.task)
        swtComplete.isChecked = task.complete!!
        txtDescription.setText(task.description)


        var data = Calendar.getInstance()
        data.timeInMillis = task.date!!
        var dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm");
        var hoje = dateFormat.format(data.time);
        btnDate.text = hoje

        _year = data.get(Calendar.YEAR)
        _month = data.get(Calendar.MONTH)
        _dayOfMonth = data.get(Calendar.DAY_OF_MONTH)
        _hour = data.get(Calendar.HOUR_OF_DAY)
        _minute = data.get(Calendar.MINUTE)

    }

    override fun onResume() {
        super.onResume()

        items = resources.getStringArray(R.array.priorities_array)
        val adapter = ArrayAdapter(baseContext, R.layout.option_item_priority, items)

        // Set make default value
        autoCompletePriority.setAdapter(adapter);
    }

    private fun configDialog() {
        dialog = Dialog(this)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_custom_galery)

        val btnCancel = dialog.findViewById<Button>(R.id.btn_dialog_cancel)
        val btnCamera = dialog.findViewById<ConstraintLayout>(R.id.constraint_layout_dialog_camera)
        val btnGallery =
            dialog.findViewById<ConstraintLayout>(R.id.constraint_layout_dialog_gallery)

        btnCancel.setOnClickListener(this)
        btnCamera.setOnClickListener(this)
        btnGallery.setOnClickListener(this)
    }

    private fun setActualDateAndHours() {
        getDateTimeCalendar()


        var dataAtual = Calendar.getInstance()
        dataAtual.set(Calendar.HOUR_OF_DAY, Date().hours + 1)
        var data = dataAtual.getTime()

        var dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm");

        var hoje = dateFormat.format(data);

        _hour = dataAtual.get(Calendar.HOUR_OF_DAY)

        btnDate.text = hoje
    }

    private fun listeners() {
        swtComplete.setOnCheckedChangeListener(this)
        btnDate.setOnClickListener(this)
        btnSave.setOnClickListener(this)
        cvPicture.setOnClickListener(this)
        btn_delete.setOnClickListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnDate -> openDatePicker()
            R.id.btnSave -> save()
            R.id.cvPicture -> openDialogFromImage()
            R.id.btn_dialog_cancel -> dialog.dismiss()
            R.id.constraint_layout_dialog_gallery -> openGallery()
            R.id.constraint_layout_dialog_camera -> openCamera()
            R.id.btn_delete -> setDefaultImageDelete()
            else -> { // Note the block
                print("x is neither 1 nor 2")
            }
        }
    }

    private fun setDefaultImageDelete() {
        btn_delete.visibility = View.INVISIBLE
        imageTask.layoutParams.width = 220
        imageTask.layoutParams.height = 220
        imageTask.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_add_photo_alternate_24))

        imageBitmap?.recycle()
    }

    private fun openCamera() {
        if (ActivityCompat.checkSelfPermission(
                baseContext,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 111)
        } else {
            callCamera()
        }
    }

    private fun callCamera() {
        dialog.dismiss()
        val CODE_PERMISSION_ACTION_IMAGE_CAPTURE = 101
        var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CODE_PERMISSION_ACTION_IMAGE_CAPTURE)
    }

    private fun openGallery() {
        val PERMISSION_CODE = 1001

        if (ActivityCompat.checkSelfPermission(
                baseContext,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED
        ) {
            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_CODE)
        } else {
            callGallery()
        }

    }

    private fun callGallery() {
        dialog.dismiss()
        val IMAGE_PICK_CODE = 1000
        var intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun openDialogFromImage() {
        dialog.show()
    }

    private fun save() {
        progressButton = ProgressButton(baseContext, btnSave)
        progressButton.buttonActivated()

        var adRequest = AdRequest.Builder().build()

        if (invalidationForm())
            return progressButton.buttonFinish()

        RewardedAd.load(
            this,
            "ca-app-pub-3940256099942544/5224354917",
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mRewardedAd = null
                    progressButton.buttonFinish()
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    mRewardedAd = rewardedAd
                    saveImage()
                }
            })


    }

    private fun createNotification(task: TaskModel) {
        val intent = Intent(baseContext, Receiver::class.java)
        intent.putExtra("task", task.task)
        intent.putExtra("notificationId", task.notificationId)
        val pendingIntent = PendingIntent.getBroadcast(
            baseContext,
            task.notificationId!!,
            intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val oneHourBefore = 1 * 60 * 60 * 1000
        val time = task.date!! - oneHourBefore

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent)
    }

    private fun showVideoAdMob() {

        mRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad was dismissed.")

                val toast: Toast

                if (taskSaved.id != null) {
                    toast = Toast.makeText(
                        baseContext,
                        R.string.task_updated_successfully,
                        Toast.LENGTH_SHORT
                    )
                } else {
                    toast = Toast.makeText(
                        baseContext,
                        R.string.task_successfully_created,
                        Toast.LENGTH_SHORT
                    )
                }

                toast.setGravity(Gravity.BOTTOM, 0, 40)
                toast.view = layoutInflater.inflate(R.layout.toast_layout, null)

                finish()
                progressButton.buttonFinish()

                toast.show()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                finish()
                progressButton.buttonFinish()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed fullscreen content.")
                // Called when ad is dismissed.
                // Don't set the ad reference to null to avoid showing the ad a second time.
                mRewardedAd = null
                finish()
                progressButton.buttonFinish()
            }
        }

        val countAdMob = mAdMobPreferences.getInt(CountAdMob.TYPE.INSERTED)
        if (countAdMob > 7) {
            if (mRewardedAd != null) {
                mRewardedAd?.show(this, OnUserEarnedRewardListener() {
                    fun onUserEarnedReward(rewardItem: RewardItem) {
//                        var rewardAmount = rewardItem.amount
//                        var rewardType = rewardItem.getType()
//                        Log.d("TAG", "User earned the reward.")
                    }
                })
                mAdMobPreferences.putInt(CountAdMob.TYPE.INSERTED, 1)
            } else {
                val toast: Toast

                if (taskSaved.id != null) {
                    toast = Toast.makeText(
                        baseContext,
                        R.string.task_updated_successfully,
                        Toast.LENGTH_SHORT
                    )
                } else {
                    toast = Toast.makeText(
                        baseContext,
                        R.string.task_successfully_created,
                        Toast.LENGTH_SHORT
                    )
                }

                toast.setGravity(Gravity.BOTTOM, 0, 40)
                toast.view = layoutInflater.inflate(R.layout.toast_layout, null)

                finish()
                progressButton.buttonFinish()

                toast.show()
            }
        } else {
            mAdMobPreferences.putInt(CountAdMob.TYPE.INSERTED, countAdMob + 1)

            val toast: Toast

            if (taskSaved.id != null) {
                toast = Toast.makeText(
                    baseContext,
                    R.string.task_updated_successfully,
                    Toast.LENGTH_SHORT
                )
            } else {
                toast = Toast.makeText(
                    baseContext,
                    R.string.task_successfully_created,
                    Toast.LENGTH_SHORT
                )
            }

            toast.setGravity(Gravity.BOTTOM, 0, 40)
            toast.view = layoutInflater.inflate(R.layout.toast_layout, null)

            finish()
            progressButton.buttonFinish()

            toast.show()
        }
        return
    }

    private fun saveImage() {

        if (imageBitmap == null || imageBitmap!!.isRecycled) {
            if (taskSaved.id != null) {
                return updateTaskInFirebase()
            }
            return saveTaskInFirebase()
        }

        getDateTimeCalendar()
        val randomString = java.util.UUID.randomUUID().toString()
        var namePhoto = "${randomString}_${_dayOfMonth}-${_month}-${_year}.jpg"

        taskRef = storageRef.child(namePhoto);

        val baos = ByteArrayOutputStream()
        imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = taskRef.putBytes(data)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            taskRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result.toString()

                if (taskSaved.id != null) {
                    updateTaskInFirebase(downloadUri, namePhoto)
                } else {
                    saveTaskInFirebase(downloadUri, namePhoto)
                }
            } else {
                // Handle failures
                // ...
            }
        }

    }

    private fun updateTaskInFirebase(uriImage: String = "", namePhoto: String = "") {

        if (!uriImage.isNullOrEmpty()) {
            taskSaved.image?.let { mTodayViewModel.deletePhoto(it) }
        }

        val calendar = Calendar.getInstance()
        calendar[Calendar.YEAR] = _year
        calendar[Calendar.MONTH] = _month
        calendar[Calendar.DAY_OF_MONTH] = _dayOfMonth
        calendar[Calendar.HOUR_OF_DAY] = _hour
        calendar[Calendar.MINUTE] = _minute
        calendar[Calendar.SECOND] = 0
        var date = calendar.time
        val timestamp: Long = calendar.timeInMillis


        var priorityIndex = items.indexOf(autoCompletePriority.text.toString())

        val task = TaskModel(
            user.uid,
            txtTask.text.toString(),
            priorityIndex,
            swtComplete.isChecked,
            timestamp,
            txtDescription.text.toString(),
            uriImage,
            namePhoto,
            notificationId
        )

        db.collection("tasks")
            .document(taskSaved.id!!)
            .set(task)
            .addOnSuccessListener { documentReference ->
                Log.d(
                    "addOnSuccessListener",
                    "DocumentSnapshot added with ID: ${documentReference}"
                )

                createNotification(task)

                showVideoAdMob()

            }
            .addOnFailureListener { e ->
                Log.w("addOnFailureListener", "Error adding document", e)
            }

    }

    private fun invalidationForm(): Boolean {
        var error = false

        if (txtTask.length() < 2) {
            error = true

            tilTask.error = resources.getString(R.string.task_error)
            txtTask.isFocused
        } else {
            tilTask.error = null
        }

        if (!ConnectivityManager().isNetworkAvailable(baseContext)) {
            error = true

            Snackbar.make(view, "Sem Internet", Snackbar.LENGTH_LONG).show()
        }

        return error
    }

    private fun saveTaskInFirebase(uriImage: String = "", namePhoto: String = "") {

        val calendar = Calendar.getInstance()
        calendar[Calendar.YEAR] = _year
        calendar[Calendar.MONTH] = _month
        calendar[Calendar.DAY_OF_MONTH] = _dayOfMonth
        calendar[Calendar.HOUR_OF_DAY] = _hour
        calendar[Calendar.MINUTE] = _minute
        calendar[Calendar.SECOND] = 0
        var date = calendar.time
        val timestamp: Long = calendar.timeInMillis

//        val currentDate = SimpleDateFormat("dd/MM/yyyy").format(timestamp)
//        val currentTime = SimpleDateFormat("HH:mm").format(timestamp)

        var priorityIndex = items.indexOf(autoCompletePriority.text.toString())

        val task = TaskModel(
            user.uid,
            txtTask.text.toString(),
            priorityIndex,
            swtComplete.isChecked,
            timestamp,
            txtDescription.text.toString(),
            uriImage,
            namePhoto,
            Random().nextInt(Int.MAX_VALUE)
        )

        db.collection("tasks")
            .add(task)
            .addOnSuccessListener { documentReference ->
                Log.d(
                    "addOnSuccessListener",
                    "DocumentSnapshot added with ID: ${documentReference.id}"
                )

                createNotification(task)

                showVideoAdMob()
            }
            .addOnFailureListener { e ->
                Log.w("addOnFailureListener", "Error adding document", e)
            }

    }

    private fun getDateTimeCalendar() {
        val cal: Calendar = Calendar.getInstance()
        _dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        _month = cal.get(Calendar.MONTH)
        _year = cal.get(Calendar.YEAR)
        _hour = cal.get(Calendar.HOUR_OF_DAY)
        _minute = cal.get(Calendar.MINUTE)
    }

    private fun openDatePicker() {
        getDateTimeCalendar()

        var datePickerDialog = DatePickerDialog(this, this, _year, _month, _dayOfMonth)
        datePickerDialog.datePicker.minDate = Calendar.getInstance().timeInMillis
        datePickerDialog.show()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        getDateTimeCalendar()

        _year = year
        _month = month
        _dayOfMonth = dayOfMonth

        TimePickerDialog(this, this, _hour, _minute, true).show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        _hour = hourOfDay
        _minute = minute

        var date =
            "${formatNumber(_dayOfMonth)}/${formatNumber(_month + 1)}/${_year} ${formatNumber(
                _hour
            )}:${formatNumber(_minute)}"

        btnDate.text = date
    }

    private fun formatNumber(number: Int): String {
        return if (number == 0) "00" else if (number < 10) "0${number}" else "${number}"
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView?.id) {
            R.id.swtComplete -> {
                if (swtComplete.isChecked)
                    imgTask.setImageResource(R.drawable.ic_baseline_task_alt_24)
                else
                    imgTask.setImageResource(R.drawable.ic_baseline_highlight_off_24)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            callCamera()
        }

        if (requestCode == 1001 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults.isNotEmpty()) {
            callGallery()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101 && data != null) {
            var picture: Bitmap? = data.getParcelableExtra<Bitmap>("data")

            imageBitmap = picture!!

            imageTask.layoutParams.width = ActionBar.LayoutParams.MATCH_PARENT
            imageTask.layoutParams.height = ActionBar.LayoutParams.MATCH_PARENT
            imageTask.setImageBitmap(picture)

            btn_delete.visibility = View.VISIBLE
            return
        }

        if (requestCode == 1000 && resultCode == Activity.RESULT_OK && data != null) {
            val source = data.data?.let { ImageDecoder.createSource(this.contentResolver, it) }
            val picture = source?.let { ImageDecoder.decodeBitmap(it) }

            imageBitmap = picture!!

            imageTask.layoutParams.width = ActionBar.LayoutParams.MATCH_PARENT
            imageTask.layoutParams.height = ActionBar.LayoutParams.MATCH_PARENT
            imageTask.setImageBitmap(picture)

            btn_delete.visibility = View.VISIBLE
            return
        }
    }
}

