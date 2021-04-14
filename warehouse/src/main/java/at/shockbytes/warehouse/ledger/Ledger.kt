package at.shockbytes.warehouse.ledger

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject

class Ledger<E> private constructor(
    private val ledgerEngine: LedgerEngine<E>
) {

    private val ledgerEventSource = PublishSubject.create<LedgerBlock<E>>()

    /**
     * The ledger is empty if there are no blocks, or there is one InitOperation block
     */
    val isEmpty: Single<Boolean>
        get() = ledgerEngine.entries()
            .map { blocks ->
                blocks.isEmpty() || blocks.firstOrNull()?.data is BoxOperation.InitOperation
            }

    fun onLedgerEvents(): Observable<LedgerBlock<E>> = ledgerEventSource

    fun storeOperation(operation: BoxOperation<E>): Single<Hash> {
        return ledgerEngine.store(operation)
            .doOnComplete {
                ledgerEventSource.onNext(ledgerEngine.last)
            }
            .toSingle {
                ledgerEngine.last.hash
            }
    }

    fun allOperations(): Single<List<LedgerBlock<E>>> = ledgerEngine.entries()

    fun operationsSince(hash: Hash): Single<List<LedgerBlock<E>>> {
        return ledgerEngine.entries()
            .map { entries ->

                val index = entries.indexOfFirst { block ->
                    block.hash == hash
                }

                if (index > -1) {
                    entries.subList(index.inc(), entries.size)
                } else {
                    // If not found, return everything because nothing is stored
                    entries
                }
            }
    }

    companion object {

        fun <E> inMemory(): Ledger<E> {
            return Ledger(InMemoryLedgerEngine())
        }
    }
}
