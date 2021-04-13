package at.shockbytes.warehouse.state.box

import android.content.SharedPreferences
import kotlin.reflect.KProperty

class PersistentBoxActivationDelegate(
    private val preferences: SharedPreferences,
    private val key: String,
    private val defaultValue: Boolean = false
) : BoxActivationDelegate {

    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean {
        return preferences.getBoolean(key, defaultValue)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }
}