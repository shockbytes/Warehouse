package at.shockbytes.warehouse.ledger

import at.shockbytes.warehouse.util.singleOf
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

class InMemoryLedgerEngine<E> : LedgerEngine<E> {

    private val chain = mutableListOf<LedgerBlock<E>>()

    override val last: LedgerBlock<E>
        get() = chain.last()

    private val lastHash: String
        get() = last.hash.value

    init {
        chain.add(LedgerBlock("", BoxOperation.InitOperation()))
    }

    override fun entries(): Single<List<LedgerBlock<E>>> {
        return singleOf { chain }
    }

    override fun store(operation: BoxOperation<E>): Completable {
        chain.add(LedgerBlock(lastHash, operation))
        return Completable.complete()
    }
}
