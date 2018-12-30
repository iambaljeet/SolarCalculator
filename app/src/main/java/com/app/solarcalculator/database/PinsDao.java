package com.app.solarcalculator.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.app.solarcalculator.models.Pins;

import java.util.List;

@Dao
public interface PinsDao {
    @Query("SELECT * FROM pins_table")
    List<Pins> getAllPins();

    @Query("SELECT * FROM pins_table WHERE uid IN (:pinIds)")
    List<Pins> loadPinsByIds(int[] pinIds);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long[] insertPin(Pins... pins);

    @Delete
    void deletePin(Pins pins);
}