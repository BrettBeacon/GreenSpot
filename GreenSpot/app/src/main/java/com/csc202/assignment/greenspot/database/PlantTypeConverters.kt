package com.csc202.assignment.greenspot.database

import androidx.room.TypeConverter
import java.util.Date

class PlantTypeConverters {
    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }

    @TypeConverter
    fun toDate(millisSinceEpoch: Long): Date {
        return Date(millisSinceEpoch)
    }
}