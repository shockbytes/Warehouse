package at.shockbytes.warehouse.ledger

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface LedgerEngine<E> {

    fun entries(): Single<List<LedgerBlock<E>>>

    fun store(operation: BoxOperation<E>): Completable
}