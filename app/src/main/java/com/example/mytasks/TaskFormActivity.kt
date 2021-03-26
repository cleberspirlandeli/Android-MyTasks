package com.example.mytasks

import android.Manifest
import android.app.*
import android.content.Intent
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
import com.example.mytasks.common.ConnectivityManager
import com.example.mytasks.service.model.TaskModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_task_form.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


class TaskFormActivity : AppCompatActivity(),
    View.OnClickListener,
    DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener,
    CompoundButton.OnCheckedChangeListener {

    private var day = 0
    private var month = 0
    private var year = 0
    private var hour = 0
    private var minute = 0

    private var savedDay = 0
    private var savedMonth = 0
    private var savedYear = 0
    private var savedHour = 0
    private var savedMinute = 0

    lateinit var db: FirebaseFirestore
    private var items = arrayOf<String>()

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private lateinit var taskRef: StorageReference
    private lateinit var taskImageRef: StorageReference

    private lateinit var dialog: Dialog
    private var imageBitmap: Bitmap? = null
    private lateinit var view: View
    private lateinit var progressButton: ProgressButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_form)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        view = btnSave

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



        btn_delete.visibility = View.INVISIBLE

        listeners()
        setActualDateAndHours()
        configDialog()
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
            R.id.btnSave -> saveTask()
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveTask() {

        progressButton = ProgressButton(baseContext, btnSave)
        progressButton.buttonActivated()

        if (invalidationForm()) {
            return progressButton.buttonFinish()
        }

        if (imageBitmap == null || imageBitmap!!.isRecycled)
            return saveTaskInFirebase()

        getDateTimeCalendar()
        val randomString = java.util.UUID.randomUUID().toString()

        // Create a reference to "mountains.jpg"
        taskRef = storageRef.child("${randomString}_${day}-${month}-${year}.jpg");

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
                saveTaskInFirebase(downloadUri)
            } else {
                // Handle failures
                // ...
            }
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

    private fun saveTaskInFirebase(uriImage: String = "") {

        val calendar = Calendar.getInstance()
        calendar[Calendar.YEAR] = savedYear
        calendar[Calendar.MONTH] = savedMonth
        calendar[Calendar.DAY_OF_MONTH] = savedDay
        calendar[Calendar.HOUR_OF_DAY] = savedHour
        calendar[Calendar.MINUTE] = savedMinute
        calendar[Calendar.SECOND] = 0
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
            uriImage
        )

        db.collection("tasks")
            .add(task)
            .addOnSuccessListener { documentReference ->
                Log.d(
                    "addOnSuccessListener",
                    "DocumentSnapshot added with ID: ${documentReference.id}"
                )

                val toast =
                    Toast.makeText(this, R.string.task_successfully_created, Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
                toast.view = layoutInflater.inflate(R.layout.toast_layout, null)

                progressButton.buttonFinish()
                finish()

                toast.show()
            }
            .addOnFailureListener { e ->
                Log.w("addOnFailureListener", "Error adding document", e)
            }

    }

    private fun getDateTimeCalendar() {
        val cal: Calendar = Calendar.getInstance()
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year = cal.get(Calendar.YEAR)
        hour = cal.get(Calendar.HOUR_OF_DAY)
        minute = cal.get(Calendar.MINUTE)
    }

    private fun openDatePicker() {
        getDateTimeCalendar()

        var datePickerDialog = DatePickerDialog(this, this, year, month, day)
        datePickerDialog.datePicker.minDate = Calendar.getInstance().timeInMillis
        datePickerDialog.show()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedYear = year
        savedMonth = month
        savedDay = dayOfMonth

        getDateTimeCalendar()
        TimePickerDialog(this, this, hour, minute, true).show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        savedHour = hourOfDay
        savedMinute = minute

        var date =
            "${formatNumber(savedDay)}/${formatNumber(savedMonth + 1)}/${savedYear} ${formatNumber(
                savedHour
            )}:${formatNumber(savedMinute)}"

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

