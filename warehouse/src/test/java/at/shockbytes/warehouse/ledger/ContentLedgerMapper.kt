package at.shockbytes.warehouse.ledger

import at.shockbytes.warehouse.Mapper
import at.shockbytes.warehouse.model.Content
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

class ContentLedgerMapper : Mapper<String, LedgerBlock<Content>>() {

    private val json: Json = Json {
        serializersModule = SerializersModule {
            polymorphic(Any::class) {
                subclass(Content::class, Content.serializer())
            }
        }
    }

    override fun mapTo(data: String): LedgerBlock<Content> {
        return json.decodeFromString(data)
    }

    override fun mapFrom(data: LedgerBlock<Content>): String {
        return json.encodeToString(data)
    }
}
