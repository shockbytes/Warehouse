package at.shockbytes.warehouse.sample.realm

import at.shockbytes.warehouse.sample.Message
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RealmMessage(
    @PrimaryKey var id: String = "",
    var recipient: String = "",
    var content: String = ""
) : RealmObject() {

    fun toMessage(): Message {
        return Message(id, recipient, content)
    }
}
