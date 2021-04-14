package at.shockbytes.warehouse

/**
 * Helper class for BoxEngine implementations where the internal representation is the same
 * as the external representation (for example for in-memory or Firebase use cases)
 */
class IdentityMapper<T> : Mapper<T, T>() {

    override fun mapTo(data: T): T = data

    override fun mapFrom(data: T): T = data
}
