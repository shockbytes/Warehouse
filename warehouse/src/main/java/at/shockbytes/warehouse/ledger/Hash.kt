package at.shockbytes.warehouse.ledger

import kotlinx.serialization.Serializable

@Serializable
data class Hash(val value: String) {

    companion object {
        fun empty(): Hash = Hash("")
    }
}
