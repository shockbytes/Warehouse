package at.shockbytes.warehouse.state

import at.shockbytes.warehouse.ledger.Hash

class InMemoryStatePreserver : StatePreserver {

    private var currentState: Hash = Hash("")

    override fun getCurrentState(): Hash = currentState

    override fun updateHash(hash: Hash) {
        currentState = hash
    }
}