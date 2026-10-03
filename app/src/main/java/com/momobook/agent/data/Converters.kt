package com.momobook.agent.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun toType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromType(type: TransactionType): String = type.name
}
