package com.saratms.risetodo.Database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;

/**
 * Created by Sarah Al-Shamy on 20/11/2018.
 */
@Database(entities = {Todo.class}, version = 2)
public abstract class TodoDatabase extends RoomDatabase {

    private static volatile TodoDatabase INSTANCE;
    public abstract TodoDAO getTodoDAO();

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE todo_table "
                    + "ADD COLUMN voice_note_path TEXT");

            database.execSQL("ALTER TABLE todo_table "
                    + "ADD COLUMN reminder_time TEXT");
        }
    };

    public static TodoDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (TodoDatabase.class) {
                if (INSTANCE == null) {

                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), TodoDatabase.class, "todoDatabase")
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
