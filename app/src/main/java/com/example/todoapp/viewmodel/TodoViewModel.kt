package com.example.todoapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.Todo
import com.example.todoapp.data.TodoDatabase
import kotlinx.coroutines.launch

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = TodoDatabase.getDatabase(application).todoDao()

    private val searchQuery = MutableLiveData("")
    private val filterType = MutableLiveData(0) // 0=All, 1=Active, 2=Done, 3=High Priority

    private val filterState = MutableLiveData<Pair<String, Int>>()

    init {
        // Trigger initial data load
        updateFilter()
    }

    val allTodos: LiveData<List<Todo>> = filterState.switchMap { (query, filter) ->
        dao.getTodosFiltered(query, filter)
    }

    fun search(query: String) {
        searchQuery.value = query
        updateFilter()
    }

    fun setFilter(type: Int) {
        filterType.value = type
        updateFilter()
    }

    private fun updateFilter() {
        filterState.value = Pair(searchQuery.value ?: "", filterType.value ?: 0)
    }

    fun insert(todo: Todo) = viewModelScope.launch { dao.insert(todo) }
    fun delete(todo: Todo) = viewModelScope.launch { dao.delete(todo) }
    fun update(todo: Todo) = viewModelScope.launch { dao.update(todo) }
    fun deleteCompleted() = viewModelScope.launch { dao.deleteCompleted() }
}
