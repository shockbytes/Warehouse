package at.shockbytes.warehouse.ledger

import java.io.File

class FileBasedPersistentLedgerSource(
    private val file: File
) : PersistentLedgerSource {

    override fun write(data: String) = file.writeText(data)

    override fun append(data: String) = file.appendText(data.plus(System.lineSeparator()))

    override fun firstLine(): String = file.useLines { it.first() }

    override fun <T> elements(block: (Sequence<String>) -> T): T {
        return file.useLines(block = block)
    }
}
