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
    private val statePreserver: StatePreserver,
    var isEnabled: Boolean = true
) {

    val id: BoxId
        get() = boxEngine.id

    val currentState: Hash
        get() = statePreserver.getCurrentState()

    fun updateHash(hash: Hash) {
        statePreserver.updateHash(hash)
    }

    operator fun <ID> get(id: ID): Single<E> {
        return getSingleElement(id)
    }

    fun <ID> getSingleElement(id: ID): Single<E> = boxEngine.getElementForIdType(id)

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

    fun reset(): Completable {
        return boxEngine.reset()
    }

    fun syncOperations(operations: List<BoxOperation<E>>): Completable {
        return operations
            .map { operation ->
                operation.perform(this)
            }
            .concatAll()
    }
}
