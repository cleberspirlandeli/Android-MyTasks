package com.example.mytasks.ui.done

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mytasks.R
import com.example.mytasks.adapter.ListTasksAdapter
import com.example.mytasks.common.ProgressBarLoadingFragment
import com.example.mytasks.common.constants.ScreenFilterConstants
import com.example.mytasks.listener.TaskListener
import com.example.mytasks.service.model.TaskModel
import com.example.mytasks.ui.today.TodayViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class DoneFragment : Fragment() {

    // FIREBASE
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser

    private lateinit var mTodayViewModel: TodayViewModel
    private lateinit var mListener: TaskListener

    private lateinit var mLoading: ProgressBarLoadingFragment

    private val mAdapter = ListTasksAdapter()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        mTodayViewModel =
                ViewModelProvider(this).get(TodayViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_today, container, false)

        auth = Firebase.auth
        user = auth.currentUser

        mLoading = ProgressBarLoadingFragment(this)

        val recyclerToday = root.findViewById<RecyclerView>(R.id.recyclerToday)
        recyclerToday.layoutManager = LinearLayoutManager(context)
        recyclerToday.adapter = mAdapter

        mListener = object : TaskListener {
            override fun onViewTaskClick(task: TaskModel) {

            }

            override fun onDeleteTaskClick(task: TaskModel) {
                mTodayViewModel.delete(task, ScreenFilterConstants.SCREEN.DONE)
            }

            override fun onChangeCompleteTaskClick(id: String, statusTask: Boolean) {
                mTodayViewModel.onChangeCompleteTaskClick(id, statusTask, ScreenFilterConstants.SCREEN.DONE)
            }

        }

        observe()
        return root
    }

    override fun onResume() {
        super.onResume()
        mAdapter.attachListener(mListener)
        mTodayViewModel.getListDoneTasks()
    }

    private fun observe() {
        mTodayViewModel.isLoading.observe(viewLifecycleOwner, Observer {
            if (it) {
                mLoading.startLoading()
            } else {
                mLoading.endLoading()
            }
        })

        mTodayViewModel.isLoading.observe(viewLifecycleOwner, Observer {
            if (it) {
                mLoading.startLoading()
            } else {
                mLoading.endLoading()
            }
        })

        mTodayViewModel.listTasks.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                mAdapter.updateList(it)
            }
        })

        mTodayViewModel.validation.observe(viewLifecycleOwner, Observer {
            if (it.isSuccess()) {
                val layout = layoutInflater.inflate(R.layout.toast_layout, null)
                val textV = layout.findViewById<TextView>(R.id.txt_toast)
                textV.setText(it.getSuccessMessage())

                val toast =
                    Toast(context)
                toast.setGravity(Gravity.BOTTOM, 0, 40)
                toast.setDuration(Toast.LENGTH_SHORT)
                toast.view = layout
                toast.show()
            } else {
                Toast.makeText(context, it.getErrorMessage(), Toast.LENGTH_SHORT).show()
            }
        })
    }
}