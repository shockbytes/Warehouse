package at.shockbytes.warehouse

/**
 * Author:  Martin Macheiner
 * Date:    12.06.2018
 */
abstract class Mapper<Internal, External> {

    abstract fun mapTo(data: Internal): External

    abstract fun mapFrom(data: External): Internal

    fun mapListTo(data: List<Internal>): List<External> {
        return data.map { mapTo(it) }
    }

    fun mapListFrom(data: List<External>): List<Internal> {
        return data.map { mapFrom(it) }
    }
}
