package com.yourfitness.pt.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yourfitness.pt.data.entity.EducationEntity
import com.yourfitness.pt.data.entity.ProfileInfo

object EducationConverters {

    @TypeConverter
    fun fromEducationString(value: String?): List<EducationEntity>? {
        val listType = object : TypeToken<List<EducationEntity?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromEducationList(list: List<EducationEntity?>?): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}

object ProfileInfoConverters {

    @TypeConverter
    fun fromProfileInfoString(value: String?): ProfileInfo {
        val listType = object : TypeToken<ProfileInfo>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromProfileInfoObject(data: ProfileInfo): String {
        val gson = Gson()
        return gson.toJson(data)
    }
}
