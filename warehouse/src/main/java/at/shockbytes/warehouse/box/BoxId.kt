package at.shockbytes.warehouse.box

data class BoxId protected constructor(val value: String) {

    companion object {

        fun of(id: String) = BoxId(id)
    }
}
