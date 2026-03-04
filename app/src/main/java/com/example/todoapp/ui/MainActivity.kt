package com.example.todoapp.ui

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.example.todoapp.R
import com.example.todoapp.data.Todo
import com.example.todoapp.databinding.ActivityMainBinding
import com.example.todoapp.viewmodel.TodoViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private val viewModel: TodoViewModel by viewModels()
    private lateinit var adapter: TodoAdapter
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge status bar — use WindowCompat (no deprecated calls)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        @Suppress("DEPRECATION")
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_dark)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupInputArea()
        setupSearchAndFilter()
        observeTodos()
    }

    // ───────── RecyclerView + Swipe-to-delete ─────────

    private fun setupRecyclerView() {
        adapter = TodoAdapter(
            onDelete = { todo -> deleteTodoWithUndo(todo) },
            onCheck = { todo -> viewModel.update(todo) },
            onTogglePriority = { todo ->
                viewModel.update(todo)
                if (todo.isHighPriority) {
                    showSnackbar("★ Marked as urgent")
                } else {
                    showSnackbar("Removed urgent mark")
                }
            }
        )

        binding.recyclerView.apply {
            this.adapter = this@MainActivity.adapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
        }

        // Premium swipe-to-delete with rounded background + icon
        val deleteColor = ContextCompat.getColor(this, R.color.swipe_delete_bg)
        val deletePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = deleteColor }
        val deleteIcon = ContextCompat.getDrawable(this, R.drawable.ic_delete)!!
        val iconTint = ContextCompat.getColor(this, android.R.color.white)

        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                val pos = viewHolder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val todo = adapter.currentList[pos]
                    deleteTodoWithUndo(todo)
                }
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val cornerRadius = 16f * resources.displayMetrics.density

                if (dX > 0) {
                    // Swiping right
                    val rect = RectF(
                        itemView.left.toFloat(), itemView.top.toFloat(),
                        itemView.left + dX, itemView.bottom.toFloat()
                    )
                    c.drawRoundRect(rect, cornerRadius, cornerRadius, deletePaint)

                    // Draw icon centered vertically in swipe area
                    val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2
                    val iconLeft = itemView.left + iconMargin
                    deleteIcon.setTint(iconTint)
                    deleteIcon.setBounds(
                        iconLeft, itemView.top + iconMargin,
                        iconLeft + deleteIcon.intrinsicWidth, itemView.bottom - iconMargin
                    )
                    if (dX > deleteIcon.intrinsicWidth + iconMargin) deleteIcon.draw(c)

                } else if (dX < 0) {
                    // Swiping left
                    val rect = RectF(
                        itemView.right + dX, itemView.top.toFloat(),
                        itemView.right.toFloat(), itemView.bottom.toFloat()
                    )
                    c.drawRoundRect(rect, cornerRadius, cornerRadius, deletePaint)

                    val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2
                    val iconRight = itemView.right - iconMargin
                    deleteIcon.setTint(iconTint)
                    deleteIcon.setBounds(
                        iconRight - deleteIcon.intrinsicWidth, itemView.top + iconMargin,
                        iconRight, itemView.bottom - iconMargin
                    )
                    if (-dX > deleteIcon.intrinsicWidth + iconMargin) deleteIcon.draw(c)
                }

                // Fade the card as it's swiped
                val alpha = 1f - (Math.abs(dX) / itemView.width.toFloat()) * 0.4f
                itemView.alpha = alpha

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                viewHolder.itemView.alpha = 1f
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.recyclerView)
    }

    // ───────── Search & Filter ─────────

    private fun setupSearchAndFilter() {
        binding.btnSearch.setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            TransitionManager.beginDelayedTransition(
                binding.appBarLayout, AutoTransition().setDuration(250)
            )
            binding.searchContainer.visibility = View.VISIBLE
            binding.etSearch.requestFocus()
            showKeyboard(binding.etSearch)
        }

        binding.btnSearchClose.setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            TransitionManager.beginDelayedTransition(
                binding.appBarLayout, AutoTransition().setDuration(200)
            )
            binding.searchContainer.visibility = View.GONE
            binding.etSearch.text?.clear()
            viewModel.search("")
            hideKeyboard()
        }

        binding.etSearch.addTextChangedListener { text ->
            viewModel.search(text.toString().trim())
        }

        binding.chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            group.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            val filterType = when (checkedIds.first()) {
                R.id.chipActive -> 1
                R.id.chipDone -> 2
                R.id.chipUrgent -> 3
                else -> 0
            }
            viewModel.setFilter(filterType)
        }
    }

    // ───────── Input Area ─────────

    private fun setupInputArea() {
        binding.btnAdd.isEnabled = false
        binding.etTodo.addTextChangedListener { text ->
            binding.btnAdd.isEnabled = !text.isNullOrBlank()
            // Subtle alpha change on the button
            binding.btnAdd.alpha = if (text.isNullOrBlank()) 0.5f else 1.0f
        }
        binding.btnAdd.alpha = 0.5f

        binding.btnAdd.setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            addTodo()
        }

        binding.etTodo.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addTodo()
                true
            } else false
        }

        // Clear completed — Material dialog with custom theme
        binding.btnClearCompleted.setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            MaterialAlertDialogBuilder(this, R.style.AlertDialog_TodoApp)
                .setTitle("Clear Completed")
                .setMessage("Are you sure you want to delete all completed tasks? This cannot be undone.")
                .setPositiveButton("Clear") { _, _ ->
                    viewModel.deleteCompleted()
                    showSnackbar("Completed tasks cleared ✓")
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun addTodo() {
        val text = binding.etTodo.text.toString().trim()
        if (text.isNotEmpty()) {
            val isHighPriority = binding.chipGroupFilter.checkedChipId == R.id.chipUrgent
            viewModel.insert(Todo(title = text, isHighPriority = isHighPriority))
            binding.etTodo.text?.clear()
            hideKeyboard()
            showSnackbar("Task added ✓")
        }
    }

    // ───────── Observe Data ─────────

    private fun observeTodos() {
        viewModel.allTodos.observe(this) { todos ->
            adapter.submitList(todos)

            val hasItems = todos.isNotEmpty()
            binding.recyclerView.visibility = if (hasItems) View.VISIBLE else View.GONE
            binding.emptyStateLayout.visibility = if (hasItems) View.GONE else View.VISIBLE

            // Dynamic empty state messaging
            if (!hasItems) {
                val (title, subtitle) = when (binding.chipGroupFilter.checkedChipId) {
                    R.id.chipActive -> "No active tasks" to "Complete some or add new ones"
                    R.id.chipDone -> "Nothing completed yet" to "Check off tasks to see them here"
                    R.id.chipUrgent -> "No urgent tasks" to "Long-press a task to mark as urgent"
                    else -> "All caught up!" to "Add a new task to get started"
                }
                binding.tvEmptyState.text = title
                binding.tvEmptySubtext.text = subtitle
            }

            // Clear-completed visibility
            val hasCompleted = todos.any { it.isCompleted }
            val showClear = binding.chipGroupFilter.checkedChipId == R.id.chipAll ||
                    binding.chipGroupFilter.checkedChipId == R.id.chipDone
            binding.btnClearCompleted.visibility =
                if (hasCompleted && showClear) View.VISIBLE else View.GONE

            // Task counter
            val active = todos.count { !it.isCompleted }
            val total = todos.size
            binding.tvTaskCount.text = if (total == 0) "" else "$active of $total tasks remaining"
            binding.tvTaskCount.visibility = if (total > 0) View.VISIBLE else View.GONE
        }
    }

    // ───────── Helpers ─────────

    private fun deleteTodoWithUndo(todo: Todo) {
        viewModel.delete(todo)
        Snackbar.make(binding.root, "\"${todo.title}\" deleted", Snackbar.LENGTH_LONG)
            .setAction("UNDO") { viewModel.insert(todo) }
            .setActionTextColor(ContextCompat.getColor(this, R.color.accent))
            .show()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
    }

    private fun showKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}
