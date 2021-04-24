package at.shockbytes.warehouse.box

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface BoxEngine<I, E> {

    val id: BoxId

    fun <ID> getElementForIdType(internalId: ID): Single<E>
    fun getAll(): Observable<List<E>>
    fun store(value: E): Single<E>
    fun update(value: E): Completable
    fun delete(value: E): Completable
    fun reset(): Completable
}
