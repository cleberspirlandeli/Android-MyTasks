package com.example.mytasks.adapter

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mytasks.R
import com.example.mytasks.listener.TaskListener
import com.example.mytasks.service.model.TaskModel
import java.text.SimpleDateFormat

class ListTasksAdapter : RecyclerView.Adapter<TaskViewHolder>() {

    private var mList: List<TaskModel> = arrayListOf()
    private lateinit var mListener: TaskListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val item =
            LayoutInflater.from(parent.context).inflate(R.layout.adapter_list_tasks, parent, false)
        return TaskViewHolder(item, mListener)
    }

    override fun getItemCount(): Int {
        return mList.count()
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bindData(mList[position])
    }

    fun attachListener(listener: TaskListener) {
        mListener = listener
    }

    fun updateList(list: List<TaskModel>) {
        mList = list
        notifyDataSetChanged()
    }

}

class TaskViewHolder(itemView: View, val listener: TaskListener) :
    RecyclerView.ViewHolder(itemView) {

    private val formatDate = SimpleDateFormat("dd/MM/yyyy")
    private val formatTime = SimpleDateFormat("HH:mm")
    private lateinit var date: String
    private lateinit var time: String

    private var mCardViewTask: CardView = itemView.findViewById(R.id.card_view_task)

    private var mImagePriority: ImageView = itemView.findViewById(R.id.img_priority_task)
    private var mImagePhoto: ImageView = itemView.findViewById(R.id.img_photo_task)

    private var mDate: TextView = itemView.findViewById(R.id.txt_date_task)
    private var mName: TextView = itemView.findViewById(R.id.txt_name_task)
    private var mDescription: TextView = itemView.findViewById(R.id.txt_description_task)

    private var mBtnDelete: ImageView = itemView.findViewById(R.id.img_btn_delete_task)
    private var mBtnUndo: Button = itemView.findViewById(R.id.btn_undo_task)
    private var mBtnDone: Button = itemView.findViewById(R.id.btn_done_task)

    val myDrawable =
        ContextCompat.getDrawable(itemView.context, R.drawable.ic_baseline_radio_button_checked_24)

    fun bindData(task: TaskModel) {

        if (task.priority == 0) {
            myDrawable?.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN)
            mImagePriority.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN)
        } else if (task.priority == 1) {
            myDrawable?.setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN)
            mImagePriority.setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN)
        } else {
            myDrawable?.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
            mImagePriority.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
        }

        if (task.image.isNullOrEmpty())
            mImagePhoto.setBackgroundResource(R.drawable.ic_baseline_hide_image_24)
        else {
            Glide.with(itemView.context).load(task.image).into(mImagePhoto)
        }

        date = formatDate.format(task.date)
        time = formatTime.format(task.date)

        mDate.text = "${date}  ${time}"
        mName.text = task.task
        mDescription.text = task.description

        // Events
        mCardViewTask.setOnClickListener {
            Toast.makeText(itemView.context, "Agora abro outra tela", Toast.LENGTH_SHORT).show()
        }

        mBtnDelete.setOnClickListener {
            AlertDialog.Builder(itemView.context)
                .setTitle(R.string.remove_task)
                .setMessage(R.string.confirme_remove_task)
                .setPositiveButton(R.string.yes) { dialog, which ->
                    listener.onDeleteTaskClick("1")
                }
                .setNeutralButton(R.string.cancel, null)
                .show()
            true
        }

        mBtnUndo.setOnClickListener {
            AlertDialog.Builder(itemView.context)
                .setTitle(R.string.undo_task)
                .setMessage(R.string.confirme_remove_task)
                .setPositiveButton(R.string.yes) { dialog, which ->
                    listener.onUndoTaskClick("1")
                }
                .setNeutralButton(R.string.cancel, null)
                .show()
            true
        }

        mBtnDone.setOnClickListener {
            AlertDialog.Builder(itemView.context)
                .setTitle(R.string.done_task)
                .setMessage(R.string.confirme_done_task)
                .setPositiveButton(R.string.yes) { dialog, which ->
                    listener.onDoneTaskClick("1")
                }
                .setNeutralButton(R.string.cancel, null)
                .show()
            true
        }

    }

}