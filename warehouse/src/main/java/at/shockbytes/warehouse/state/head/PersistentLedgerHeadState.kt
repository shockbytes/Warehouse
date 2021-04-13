package at.shockbytes.warehouse.state.head

import android.content.SharedPreferences
import at.shockbytes.warehouse.ledger.Hash

class PersistentLedgerHeadState(
    private val sharedPreferences: SharedPreferences
) : LedgerHeadState {

    override fun headHash(): Hash {
        return sharedPreferences.getString(PREFS_HASH, "")
            ?.let(::Hash)
            ?: Hash.empty()
    }

    override fun updateHead(hash: Hash) {
        sharedPreferences.edit().putString(PREFS_HASH, hash.value).apply()
    }

    companion object {

        private const val PREFS_HASH = "prefs_hash_state_preserver"
    }
}
