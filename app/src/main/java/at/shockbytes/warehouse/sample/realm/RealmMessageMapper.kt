package at.shockbytes.warehouse.sample.realm

import at.shockbytes.warehouse.Mapper
import at.shockbytes.warehouse.sample.Message

object RealmMessageMapper : Mapper<RealmMessage, Message>() {

    override fun mapTo(data: RealmMessage): Message {
        return data.toMessage()
    }

    override fun mapFrom(data: Message): RealmMessage {
        return RealmMessage(
            id = data.id,
            recipient = data.recipient,
            content = data.content
        )
    }
}
