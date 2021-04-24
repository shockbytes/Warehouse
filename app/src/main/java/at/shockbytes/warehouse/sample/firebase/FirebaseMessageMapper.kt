package at.shockbytes.warehouse.sample.firebase

import at.shockbytes.warehouse.Mapper
import at.shockbytes.warehouse.sample.Message

object FirebaseMessageMapper : Mapper<FirebaseMessage, Message>() {

    override fun mapTo(data: FirebaseMessage): Message {
        return data.toMessage()
    }

    override fun mapFrom(data: Message): FirebaseMessage {
        return FirebaseMessage(
            id = data.id,
            recipient = data.recipient,
            content = data.content
        )
    }
}
