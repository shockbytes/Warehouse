package at.shockbytes.warehouse.ledger

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

class Ledger<E> private constructor(
    private val ledgerEngine: LedgerEngine<E>
) {

    fun storeOperation(operation: BoxOperation<E>): Completable = ledgerEngine.store(operation)

    fun getEntriesSince(hash: String): Single<List<LedgerBlock<E>>> {
        return ledgerEngine.entries()
            .map { entries ->
                // TODO This can be optimized
                val index = entries.indexOfFirst { block ->
                    block.hash == hash
                }

                // Found
                if (index > -1) {
                    entries.subList(index, entries.size)
                } else {
                    listOf()
                }
            }
    }

    companion object {

        fun <E> inMemory(): Ledger<E> {
            return Ledger(InMemoryLedgerEngine())
        }
    }
}