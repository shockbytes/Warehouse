package at.shockbytes.warehouse.state.head

import at.shockbytes.warehouse.ledger.Hash

interface LedgerHeadState {

    fun headHash(): Hash

    fun updateHead(hash: Hash)
}
