package com.android.stressy.dataclass.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "highApp")
data class HighAppData (
    @PrimaryKey(autoGenerate = true) val id:Int,
    @ColumnInfo(name = "cate") val cate: Int
){

}