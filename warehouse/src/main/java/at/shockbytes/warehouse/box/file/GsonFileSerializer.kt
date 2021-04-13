package at.shockbytes.warehouse.box.file

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GsonFileSerializer<T>(private val gson: Gson = Gson()) : FileSerializer<T> {

    override fun serializeToString(values: List<T>): String {
        return gson.toJson(values)
    }

    override fun deserializeFromString(str: String): List<T> {
        return gson.fromJson(str, object : TypeToken<T>() {}.type)
    }
}
