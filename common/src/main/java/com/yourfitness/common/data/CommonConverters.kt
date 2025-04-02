package com.yourfitness.common.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yourfitness.common.network.dto.Gender
import com.yourfitness.common.network.dto.Packages

object CommonConverters {

    @TypeConverter
    fun fromString(value: String?): List<String>? {
        val listType = object : TypeToken<List<String?>>() {}.type
        return if (value == null) null else Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<String?>?): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun toGender(value: String?): Gender? {
        return value?.let { enumValueOf<Gender>(it) }
    }

    @TypeConverter
    fun fromGender(value: Gender?): String? {
        return value?.name
    }

    @TypeConverter
    fun fromPackagesString(value: String?): List<Packages>? {
        val listType = object : TypeToken<List<Packages>?>() {}.type
        return if (value == null) null else Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromPackagesList(list: List<Packages>?): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}
