package at.shockbytes.warehouse.ledger

class DummyPersistentLedgerSource(
    private val storage: MutableList<String> = mutableListOf()
) : PersistentLedgerSource {

    override fun write(data: String) {
        storage.clear()
        storage.add(data)
    }

    override fun append(data: String) {
        storage.add(data)
    }

    override fun firstLine(): String {
        return storage.first()
    }

    override fun <T> elements(block: (Sequence<String>) -> T): T {
        return block(storage.asSequence())
    }
}
