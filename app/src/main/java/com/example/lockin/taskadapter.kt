package com.example.lockin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private val list: MutableList<Task>,
    private val onTaskClick: (Int, Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.findViewById<TextView>(R.id.taskTitle)
        val time = view.findViewById<TextView>(R.id.taskTime)
        val status = view.findViewById<TextView>(R.id.taskStatus)
        val muteIcon = view.findViewById<ImageView>(R.id.muteIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val item = list[position]
        holder.title.text = item.title
        holder.time.text = item.time
        holder.status.text = item.status
        
        holder.muteIcon.visibility = if (item.isMuted) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            onTaskClick(position, item)
        }
    }

    override fun getItemCount() = list.size

    fun addTask(task: Task) {
        list.add(0, task)
        notifyItemInserted(0)
    }
    
    fun removeTask(position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, list.size)
    }
    
    fun updateTask(position: Int, task: Task) {
        list[position] = task
        notifyItemChanged(position)
    }
}
