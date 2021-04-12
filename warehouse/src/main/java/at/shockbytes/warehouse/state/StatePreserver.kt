package at.shockbytes.warehouse.state

import at.shockbytes.warehouse.ledger.Hash

/**
 * TODO Better namings
 */
interface StatePreserver {

    fun getCurrentState(): Hash

    fun updateHash(hash: Hash)
}
