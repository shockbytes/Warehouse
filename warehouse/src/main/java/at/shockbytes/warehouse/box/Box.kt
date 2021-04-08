package at.shockbytes.warehouse.box

import at.shockbytes.warehouse.ledger.BoxOperation
import at.shockbytes.warehouse.ledger.Hash
import at.shockbytes.warehouse.state.StatePreserver
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.concatAll

class Box<E>(
    private val boxEngine: BoxEngine<*, E>,
    private val statePreserver: StatePreserver
    ) {

    val name: String
        get() = boxEngine.name

    val currentState: Hash
        get() = statePreserver.getCurrentState()

    fun updateHash(hash: Hash) {
        statePreserver.updateHash(hash)
    }

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

    fun syncOperations(operations: List<BoxOperation<E>>): Completable {
        return operations
            .map { operation ->
                operation.perform(this)
            }
            .concatAll()
    }
}
