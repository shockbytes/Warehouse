package at.shockbytes.warehouse.firebase

interface FirebaseStorable {

    fun copyWithNewId(newId: String): FirebaseStorable
}