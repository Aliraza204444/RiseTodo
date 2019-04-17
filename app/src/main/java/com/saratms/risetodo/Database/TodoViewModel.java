package com.saratms.risetodo.Database;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

/**
 * Created by Sarah Al-Shamy on 11/12/2018.
 */

public class TodoViewModel extends AndroidViewModel {

    private LiveData<List<Todo>> mAllTodos;
    private LiveData<List<Todo>> mNoDateTodos;
    private TodoRepository mRepository;

    public TodoViewModel (Application application) {
        super(application);
        mRepository = new TodoRepository(application);
        mAllTodos = mRepository.getAllTodos();
        mNoDateTodos = mRepository.getAllNoDateTodos();
    }

    public LiveData<List<Todo>> getAllTodos() { return mAllTodos; }

    public LiveData<List<Todo>> getAllNoDateTodos(){return mNoDateTodos;}

    public void insert(Todo todo) { mRepository.insert(todo); }

    public void delete(Todo todo) { mRepository.delete(todo); }

    public void update(Todo todo){mRepository.update(todo);}

    public Long getLastId(){return mRepository.getLastInsretedTodoId();}


}
