package com.csc202.assignment.greenspot

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity
data class Plant(
    @PrimaryKey val id: UUID,
    val title: String,
    val place: String,
    val date: Date,
    val isShared: Boolean,
    val photoFileName: String? = null,
    val latDouble: Double? = null,
    val longDouble: Double? = null
)
