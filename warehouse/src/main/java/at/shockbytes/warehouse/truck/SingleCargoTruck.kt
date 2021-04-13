package at.shockbytes.warehouse.truck

/**
 * Truck, that handles every single emitted item separately with the [action] function.
 */
class SingleCargoTruck<T>(private val action: (T) -> Unit) : Truck<T> {

    override fun loadCargo(value: T) {
        action(value)
    }
}
