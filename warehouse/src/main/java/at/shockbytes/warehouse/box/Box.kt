package at.shockbytes.warehouse.box

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class Box<E>(private val boxEngine: BoxEngine<*, E>) {

    val name: String
        get() = boxEngine.name

    operator fun get(id: String): Single<E> {
        return getSingleElement(id)
    }

    fun getSingleElement(id: String): Single<E> = boxEngine.getSingleElement(id)

    fun getAll(): Observable<List<E>> = boxEngine.getAll()

    fun store(value: E): Completable {
        return boxEngine.store(value)
    }

    fun update(value: E): Completable {
        return boxEngine.update(value)
    }

    fun delete(value: E): Completable {
        return boxEngine.delete(value)
    }

    fun syncWith(leader: Box<E>): Completable {
        // TODO Implement this method
        return Completable.complete()
    }
}
