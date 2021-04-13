package at.shockbytes.warehouse.util

fun <T> List<T>.indexOfFirstOrNull(
    predicate: (T) -> Boolean
): Int? {
    val index = indexOfFirst(predicate)
    return if (index > -1) index else null
}

fun <T> List<T>.firstOrDefault(default: T): T {
    return firstOrNull() ?: default
}
