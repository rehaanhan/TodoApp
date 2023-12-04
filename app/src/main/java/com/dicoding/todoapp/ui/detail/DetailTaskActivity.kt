package com.dicoding.todoapp.ui.detail

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dicoding.todoapp.R
import com.dicoding.todoapp.data.Task
import com.dicoding.todoapp.data.TaskDatabase
import com.dicoding.todoapp.data.TaskRepository
import com.dicoding.todoapp.utils.DateConverter
import com.dicoding.todoapp.utils.TASK_ID

class DetailTaskActivity : AppCompatActivity() {

    private lateinit var taskViewModel: DetailTaskViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        //TODO 11 : Show detail task and implement delete action

        val taskId = intent.getIntExtra(TASK_ID, 0)

        val taskRepository = TaskRepository(TaskDatabase.getInstance(this).taskDao())
        taskViewModel = ViewModelProvider(this).get(DetailTaskViewModel::class.java)

        if (taskId != null) {
            taskViewModel.setTaskId(taskId)
        }

        taskViewModel.task.observe(this, { task ->
            task?.let {
                showDetailTask(task)
            }
        })

        findViewById<Button>(R.id.btn_delete_task).setOnClickListener{
            deleteTask()
        }
    }

    private fun showDetailTask(task: Task){
        findViewById<TextView>(R.id.detail_ed_title).text = task.title
        findViewById<TextView>(R.id.detail_ed_description).text = task.description
        findViewById<TextView>(R.id.detail_ed_due_date).text = DateConverter.convertMillisToString(task.dueDateMillis)
    }

    private fun deleteTask(){
        taskViewModel.deleteTask()

        finish()
    }
}