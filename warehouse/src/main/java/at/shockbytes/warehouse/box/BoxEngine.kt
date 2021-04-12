package at.shockbytes.warehouse.box

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface BoxEngine<I, E> {

    val id: BoxId

    fun getSingleElement(id: String): Single<E>
    fun getAll(): Observable<List<E>>
    fun store(value: E): Completable
    fun update(value: E): Completable
    fun delete(value: E): Completable
    fun reset(): Completable
}