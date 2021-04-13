package at.shockbytes.warehouse.state.head

import at.shockbytes.warehouse.ledger.Hash

class TransientLedgerHeadState : LedgerHeadState {

    private var currentState: Hash = Hash("")

    override fun headHash(): Hash = currentState

    override fun updateHead(hash: Hash) {
        currentState = hash
    }
}