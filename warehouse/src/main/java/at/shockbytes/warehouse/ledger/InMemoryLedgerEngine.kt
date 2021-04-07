package at.shockbytes.warehouse.ledger

import at.shockbytes.warehouse.util.singleOf
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

class InMemoryLedgerEngine<E> : LedgerEngine<E> {

    private val chain = mutableListOf<LedgerBlock<E>>()

    private val lastHash: String?
        get() = chain.lastOrNull()?.hash

    override fun entries(): Single<List<LedgerBlock<E>>> {
        return singleOf { chain }
    }

    override fun store(operation: BoxOperation<E>): Completable {

        // TODO This is messy
        val last = lastHash
        return if (last != null) {
            chain.add(LedgerBlock(last, operation))
            Completable.complete()
        } else {
            // TODO Handle very first block
            Completable.error(Throwable("test"))
        }
    }

}
