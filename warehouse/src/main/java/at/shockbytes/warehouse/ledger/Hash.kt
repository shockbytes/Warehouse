package at.shockbytes.warehouse.ledger

data class Hash(val value: String) {

    companion object {
        fun empty(): Hash = Hash("")
    }
}
