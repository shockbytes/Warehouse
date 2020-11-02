package at.shockbytes.warehouse.box.file

interface FileSerializer<T> {

    fun serializeToString(values: List<T>): String

    fun deserializeFromString(str: String): List<T>
}