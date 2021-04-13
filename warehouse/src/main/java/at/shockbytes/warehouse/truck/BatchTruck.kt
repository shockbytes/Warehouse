package at.shockbytes.warehouse.truck

/**
 * Truck, that handles every N emitted items (whereas N = [batchSize]) with the [action] function.
 * Please note that the algorithm does NOT implement a sliding window, every window does not overlap
 * with the previous window. No value will appear twice in two different batch windows.
 */
class BatchTruck<T>(
    cacheImplementation: MutableList<T> = mutableListOf(),
    private val batchSize: Int,
    private val action: (List<T>) -> Unit,
) : Truck<T> {

    /**
     * The actual implementation of the cache can be controlled with the (cacheImplementation)
     * constructor parameter. This might be interesting if a thread-safe implementation of the
     * cache is requested.
     */
    private val cache: MutableList<T> = cacheImplementation

    override fun loadCargo(value: T) {

        if (cache.size.dec() == batchSize) {
            action(cache)
            cache.clear()
        }

        cache.add(value)
    }
}
