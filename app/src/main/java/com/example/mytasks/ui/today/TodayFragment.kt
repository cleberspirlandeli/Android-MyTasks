package com.example.mytasks.ui.today

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mytasks.R
import com.example.mytasks.adapter.ListTasksAdapter
import com.example.mytasks.listener.TaskListener
import com.example.mytasks.service.model.TaskModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class TodayFragment : Fragment() {

    // FIREBASE
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser


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

        auth = Firebase.auth
        user = auth.currentUser

        val recyclerToday = root.findViewById<RecyclerView>(R.id.recyclerToday)
        recyclerToday.layoutManager = LinearLayoutManager(context)
        recyclerToday.adapter = mAdapter

        mListener = object : TaskListener {
            override fun onViewTaskClick(id: String) {
                mViewModel.getTaskById(id)
            }

            override fun onDeleteTaskClick(task: TaskModel) {
                mViewModel.delete(task)
            }

            override fun onChangeCompleteTaskClick(id: String, statusTask: Boolean) {
                mViewModel.onChangeCompleteTaskClick(id, statusTask)
            }

        }

        observe()
        return root
    }

    override fun onResume() {
        super.onResume()
        mAdapter.attachListener(mListener)
        mViewModel.getListTasks(user.uid)
    }

    private fun observe() {
        mViewModel.listTasks.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                mAdapter.updateList(it)
            }
        })

        mViewModel.validation.observe(viewLifecycleOwner, Observer {
            if (it.isSuccess()) {
                Toast.makeText(context, it.getSuccessMessage(), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, it.getErrorMessage(), Toast.LENGTH_SHORT).show()
            }
        })
    }

}