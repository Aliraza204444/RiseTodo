package com.saratms.risetodo;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.saratms.risetodo.Database.Todo;

import java.util.List;

public class MyDiffCallback extends DiffUtil.Callback{

    List<Todo> oldTodos;
    List<Todo> newTodos;

    public MyDiffCallback(List<Todo> oldTodos, List<Todo> newTodos) {
        this.newTodos = newTodos;
        this.oldTodos = oldTodos;
    }

    @Override
    public int getOldListSize() {
        return oldTodos == null ? 0 : oldTodos.size();
    }

    @Override
    public int getNewListSize() {
        return newTodos == null ? 0 : newTodos.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldTodos.get(oldItemPosition).getId() == newTodos.get(newItemPosition).getId();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldTodos.get(oldItemPosition).equals(newTodos.get(newItemPosition));
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        //you can return particular field for changed item.
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}