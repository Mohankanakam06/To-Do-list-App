package com.example.todoapp.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TodoDao {
    // Dynamic search and filter query
    @Query("""
        SELECT * FROM todo_table 
        WHERE (title LIKE '%' || :searchQuery || '%') 
        AND (:filterType = 0 
            OR (:filterType = 1 AND isCompleted = 0) 
            OR (:filterType = 2 AND isCompleted = 1) 
            OR (:filterType = 3 AND isHighPriority = 1 AND isCompleted = 0))
        ORDER BY 
        isCompleted ASC, 
        isHighPriority DESC,
        createdAt DESC
    """)
    fun getTodosFiltered(searchQuery: String, filterType: Int): LiveData<List<Todo>>

    @Query("SELECT * FROM todo_table ORDER BY isCompleted ASC, isHighPriority DESC, createdAt DESC")
    fun getAllTodos(): LiveData<List<Todo>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(todo: Todo)

    @Delete
    suspend fun delete(todo: Todo)

    @Update
    suspend fun update(todo: Todo)

    @Query("DELETE FROM todo_table WHERE isCompleted = 1")
    suspend fun deleteCompleted()
}
