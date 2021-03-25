package com.example.mytasks.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mytasks.R
import com.example.mytasks.listener.TaskListener
import com.example.mytasks.model.TaskModel

class ListTasksAdapter: RecyclerView.Adapter<TaskViewHolder>() {

    private var mTasks: List<TaskModel> = arrayListOf()
    private lateinit var mListener: TaskListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val item =
            LayoutInflater.from(parent.context).inflate(R.layout.fragment_today, parent, false)
        return TaskViewHolder(item, mListener)
    }

    override fun getItemCount(): Int {
        return mTasks.count()
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bindData(mTasks[position])
    }

    fun attachListener(listener: TaskListener) {
        mListener = listener
    }

    fun updateList(list: List<TaskModel>) {
        mTasks = list
        notifyDataSetChanged()
    }

}

class TaskViewHolder(itemView: View, val listener: TaskListener) : RecyclerView.ViewHolder(itemView) {


    fun bindData(task: TaskModel) {


    }

}