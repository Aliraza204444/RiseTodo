package com.saratms.risetodo.Database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by Sarah Al-Shamy on 20/11/2018.
 */

@Dao
public interface TodoDAO {

    @Insert
    long insertTodo(Todo todo);

    @Query("SELECT * From todo_table WHERE todo_duedate NOT NULL OR todo_duedate != '' ")
    LiveData<List<Todo>> getAllTodos();

    @Query("SELECT * From todo_table WHERE todo_duedate is NULL OR todo_duedate = '' ")
    LiveData<List<Todo>> getNoDateTodos();

    @Query("SELECT * From todo_table")
    List<Todo> getTodos();

    @Delete
    void deleteTodo(Todo todo);

    @Update
    void updateTodo(Todo todo);
}
