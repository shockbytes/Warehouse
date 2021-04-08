package at.shockbytes.warehouse.state

import android.content.SharedPreferences
import at.shockbytes.warehouse.ledger.Hash

class SharedPreferencesStatePreserver(
    private val sharedPreferences: SharedPreferences
) : StatePreserver {

    override fun getCurrentState(): Hash {
        return sharedPreferences.getString(PREFS_HASH, "")
            ?.let(::Hash)
            ?: Hash.empty()
    }

    override fun updateHash(hash: Hash) {
        sharedPreferences.edit().putString(PREFS_HASH, hash.value).apply()
    }

    companion object {

        private const val PREFS_HASH = "prefs_hash_state_preserver"
    }
}