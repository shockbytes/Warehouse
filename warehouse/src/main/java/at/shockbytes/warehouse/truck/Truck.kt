package at.shockbytes.warehouse.truck

/**
 *
 * The truck transports new data out of the warehouse to any thinkable destination.
 *
 * This class can be used to perform side effects every time new data arrives.
 *
 * TODO Docs
 */
interface Truck<T> {

    fun loadCargo(value: T)
}
