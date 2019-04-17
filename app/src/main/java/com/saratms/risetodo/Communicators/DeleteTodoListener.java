package com.saratms.risetodo.Communicators;

import com.saratms.risetodo.Database.Todo;

/**
 * Created by Sarah Al-Shamy on 11/12/2018.
 */

public interface DeleteTodoListener {

    void onDelete(Todo todo);
}
