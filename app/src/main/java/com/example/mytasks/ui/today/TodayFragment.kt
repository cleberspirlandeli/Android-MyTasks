package com.example.mytasks.ui.today

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mytasks.R
import com.example.mytasks.adapter.ListTasksAdapter
import com.example.mytasks.listener.TaskListener
import com.example.mytasks.service.model.TaskModel
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore


class TodayFragment : Fragment() {

    private lateinit var mViewModel: TodayViewModel
    private lateinit var mListener: TaskListener


    private val mAdapter = ListTasksAdapter()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mViewModel =
            ViewModelProvider(this).get(TodayViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_today, container, false)

        val recyclerToday = root.findViewById<RecyclerView>(R.id.recyclerToday)
        recyclerToday.layoutManager = LinearLayoutManager(context)
        recyclerToday.adapter = mAdapter

        mListener = object : TaskListener {
            override fun onViewTaskClick(id: String) {
                mViewModel.getTaskById(id)
            }

            override fun onDeleteTaskClick(id: String) {
                mViewModel.deleteTask(id)
            }

            override fun onDoneTaskClick(id: String) {
                mViewModel.completeTask(id)
            }

            override fun onUndoTaskClick(id: String) {
                mViewModel.undoTask(id)
            }
        }

        observe()
        return root
    }

    override fun onResume() {
        super.onResume()
        mAdapter.attachListener(mListener)
        mViewModel.getListTasks()
    }

    private fun observe() {
        mViewModel.listTasks.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                mAdapter.updateList(it)
            }
        })
    }

}