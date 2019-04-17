package com.saratms.risetodo.Communicators;

import com.saratms.risetodo.Database.Todo;

/**
 * Created by Sarah Al-Shamy on 11/02/2019.
 */

public interface TodoCheckListener {

    void onCheck(Todo todo);
}
