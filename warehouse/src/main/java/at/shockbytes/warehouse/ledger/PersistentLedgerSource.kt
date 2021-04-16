package at.shockbytes.warehouse.ledger

interface PersistentLedgerSource {

    fun write(data: String)

    fun append(data: String)

    fun firstLine(): String

    fun <T> elements(block: (Sequence<String>) -> T): T
}
