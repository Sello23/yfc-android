package com.yourfitness.coach.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yourfitness.coach.network.dto.Bonuses
import com.yourfitness.common.domain.models.WorkTimeDto

object Converters {

    @TypeConverter
    fun fromWorkTimeString(value: String?): List<WorkTimeDto> {
        val listType = object : TypeToken<List<WorkTimeDto>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromWorkTimeList(list: List<WorkTimeDto>?): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromBonusesString(value: String?): List<Bonuses>? {
        val listType = object : TypeToken<List<Bonuses>?>() {}.type
        return if (value == null) null else Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromBonusesList(list: List<Bonuses>?): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}
