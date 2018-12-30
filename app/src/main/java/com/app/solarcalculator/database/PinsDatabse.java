package com.app.solarcalculator.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.app.solarcalculator.models.Pins;

@Database(entities = {Pins.class}, version = 1)
public abstract class PinsDatabse extends RoomDatabase {
    static volatile PinsDatabse db;

    public static PinsDatabse getDatabase(Context context) {
        if (db == null) {
            synchronized (PinsDatabse.class) {
                if (db == null) {
                    db = Room.databaseBuilder(context,
                            PinsDatabse.class, "pins-database").build();
                }
            }
        }
        return db;
    }

    public abstract PinsDao pinsDao();
}