package at.shockbytes.warehouse

/**
 * TODO Docs
 */
class IdentityMapper<T> : Mapper<T, T>() {

    override fun mapTo(data: T): T = data

    override fun mapFrom(data: T): T = data
}
