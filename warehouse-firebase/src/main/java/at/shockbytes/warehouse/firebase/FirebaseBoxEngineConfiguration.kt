package at.shockbytes.warehouse.firebase

import at.shockbytes.warehouse.box.BoxId
import com.google.firebase.database.FirebaseDatabase

data class FirebaseBoxEngineConfiguration(
    val database: FirebaseDatabase,
    val reference: String,
    val id: BoxId = BoxId.of(DEFAULT_NAME),
    val useDefaultFirebaseId: Boolean = false
) {

    companion object {
        const val DEFAULT_NAME = "firebase"
    }
}