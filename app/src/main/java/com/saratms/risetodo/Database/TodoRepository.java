package com.saratms.risetodo.Database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

/**
 * Created by Sarah Al-Shamy on 11/12/2018.
 */

public class TodoRepository {

    private TodoDAO mTodoDao;
    protected Long lastInsertedTodoId;
    private LiveData<List<Todo>> mAllTodos;
    private LiveData<List<Todo>> mAllNoDateTodos;
    private List<Todo> mTodos;

    TodoRepository(Application application) {
        TodoDatabase db = TodoDatabase.getDatabase(application);
        mTodoDao = db.getTodoDAO();
        mAllTodos = mTodoDao.getAllTodos();
        mAllNoDateTodos = mTodoDao.getNoDateTodos();
    }

    LiveData<List<Todo>> getAllTodos() {
        return mAllTodos;
    }
    LiveData<List<Todo>> getAllNoDateTodos(){return mAllNoDateTodos;}

    public Long getLastInsretedTodoId(){
        return lastInsertedTodoId;
    }

    public void insert(Todo todo) {
        new InsertTodoAsyncTask(mTodoDao).execute(todo);
    }

    private class InsertTodoAsyncTask extends AsyncTask<Todo, Void, Long> {

        private TodoDAO mAsyncTaskDao;

        InsertTodoAsyncTask(TodoDAO dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Long doInBackground(Todo... todo) {
            Long id = mAsyncTaskDao.insertTodo(todo[0]);
            return id;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            lastInsertedTodoId = aLong;
        }
    }

    public void delete(Todo todo) {
        new DeleteTodoAsyncTask(mTodoDao).execute(todo);
    }

    private class DeleteTodoAsyncTask extends AsyncTask<Todo, Void, Void> {

        private TodoDAO mAsyncTaskDao;

        DeleteTodoAsyncTask(TodoDAO dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Todo... todo) {
            mAsyncTaskDao.deleteTodo(todo[0]);
            return null;
        }

    }


    public void update(Todo todo) {new UpdateTodoAsyncTask(mTodoDao).execute(todo);}

    private class UpdateTodoAsyncTask extends AsyncTask<Todo, Void, Void> {

        private TodoDAO mAsyncTaskDao;

        UpdateTodoAsyncTask(TodoDAO dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Todo... todo) {
            mAsyncTaskDao.updateTodo(todo[0]);
            return null;
        }

    }

}
