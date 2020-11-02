package at.shockbytes.warehouse.box

import at.shockbytes.warehouse.Mapper
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

abstract class Box<I, E>(
    protected val mapper: Mapper<I, E>,
    protected val idSelector: (I) -> String
) {

    abstract val name: String

    operator fun get(id: String): Single<E> {
        return getSingleElement(id)
    }

    abstract fun getSingleElement(id: String): Single<E>

    abstract fun getAll(): Observable<List<E>>

    abstract fun store(value: E): Completable

    abstract fun update(value: E): Completable

    abstract fun delete(value: E): Completable

    fun syncWith(leader: Box<*, E>): Completable {
        TODO("Implement sync mechanism!")
    }
}
