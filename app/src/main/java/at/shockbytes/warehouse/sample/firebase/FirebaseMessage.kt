package at.shockbytes.warehouse.sample.firebase

import at.shockbytes.warehouse.firebase.FirebaseStorable
import at.shockbytes.warehouse.sample.Message
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

data class FirebaseMessage(
    val id: String = "",
    val recipient: String = "",
    val content: String = ""
) : FirebaseStorable{

    fun toMessage(): Message {
        return Message(id, recipient, content)
    }

    override fun copyWithNewId(newId: String): FirebaseStorable {
        return copy(id = newId)
    }
}
