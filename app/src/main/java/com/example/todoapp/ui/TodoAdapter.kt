package com.example.todoapp.ui

import android.graphics.Paint
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.data.Todo
import com.example.todoapp.databinding.ItemTodoBinding

class TodoAdapter(
    private val onDelete: (Todo) -> Unit,
    private val onCheck: (Todo) -> Unit,
    private val onTogglePriority: (Todo) -> Unit
) : ListAdapter<Todo, TodoAdapter.TodoViewHolder>(TodoDiffCallback()) {

    inner class TodoViewHolder(val binding: ItemTodoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        TodoViewHolder(
            ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val todo = getItem(position)
        val context = holder.itemView.context

        with(holder.binding) {

            tvTitle.text = todo.title

            // Priority star — always visible, filled if high priority, outline if not
            if (todo.isHighPriority) {
                ivPriority.setImageResource(R.drawable.ic_star)
            } else {
                ivPriority.setImageResource(R.drawable.ic_star_outline)
            }
            // Dim the star for completed tasks
            ivPriority.alpha = if (todo.isCompleted) 0.4f else 1.0f

            // Tap star to toggle priority
            ivPriority.setOnClickListener { view ->
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                onTogglePriority(todo.copy(isHighPriority = !todo.isHighPriority))
            }

            // Completed styling: strikethrough, dim, muted text color
            if (todo.isCompleted) {
                tvTitle.paintFlags = tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvTitle.setTextColor(ContextCompat.getColor(context, R.color.completed_text))
                tvTitle.alpha = 0.7f
                cardTodo.alpha = 0.75f
            } else {
                tvTitle.paintFlags = tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tvTitle.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                tvTitle.alpha = 1.0f
                cardTodo.alpha = 1.0f
            }

            // Checkbox — remove listener before setting to avoid loop
            cbDone.setOnCheckedChangeListener(null)
            cbDone.isChecked = todo.isCompleted

            cbDone.setOnCheckedChangeListener { view, checked ->
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                onCheck(todo.copy(isCompleted = checked))
            }

            // Delete button
            btnDelete.setOnClickListener { view ->
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                onDelete(todo)
            }

            // Long-press also toggles priority (keep as secondary gesture)
            root.setOnLongClickListener { view ->
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                onTogglePriority(todo.copy(isHighPriority = !todo.isHighPriority))
                true
            }
        }
    }

    class TodoDiffCallback : DiffUtil.ItemCallback<Todo>() {
        override fun areItemsTheSame(oldItem: Todo, newItem: Todo) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Todo, newItem: Todo) = oldItem == newItem
    }
}
