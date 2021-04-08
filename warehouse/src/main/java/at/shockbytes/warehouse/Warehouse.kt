package at.shockbytes.warehouse

import at.shockbytes.warehouse.box.Box
import at.shockbytes.warehouse.ledger.BoxOperation
import at.shockbytes.warehouse.ledger.Ledger
import at.shockbytes.warehouse.sync.BoxSync
import at.shockbytes.warehouse.util.asCompletable
import at.shockbytes.warehouse.util.merge
import at.shockbytes.warehouse.util.toObservableFromIterable
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

class Warehouse<E>(
    private val boxes: List<Box<E>>,
    private val ledger: Ledger<E>,
    private val config: WarehouseConfiguration
) {

    private val boxSync: BoxSync<E> = BoxSync(config.leaderBox, boxes)

    init {
        boxSync.syncWithLedger(ledger).blockingAwait() // TODO Not nice
    }

    /**
     * Public Api
     */
    fun store(
        value: E,
        writePredicate: (Box<E>) -> Boolean = { true }
    ): Completable {
        return performCompletableWriteBoxAction(writePredicate) { box ->
            box.store(value)
                .andThen(ledger.storeOperation(BoxOperation.StoreOperation(value)))
                .doOnSuccess(box::updateHash)
                .asCompletable()
        }
    }

    /**
     * Public Api
     */
    fun update(
        value: E,
        writePredicate: (Box<E>) -> Boolean = { true },
    ): Completable {
        return performCompletableWriteBoxAction(writePredicate) { box ->
            box.update(value)
                .andThen(ledger.storeOperation(BoxOperation.UpdateOperation(value)))
                .doOnSuccess(box::updateHash)
                .asCompletable()
        }
    }

    /**
     * Public Api
     */
    fun delete(
        value: E,
        writePredicate: (Box<E>) -> Boolean = { true },
    ): Completable {
        return performCompletableWriteBoxAction(writePredicate) { box ->
            box.delete(value)
                .andThen(ledger.storeOperation(BoxOperation.DeleteOperation(value)))
                .doOnSuccess(box::updateHash)
                .asCompletable()
        }
    }

    private fun performCompletableWriteBoxAction(
        writePredicate: (Box<E>) -> Boolean,
        action: (Box<E>) -> Completable
    ): Completable {
        return boxes
            .filter(writePredicate)
            .map(action)
            .merge()
    }

    /**
     * Public Api
     */
    operator fun get(
        id: String,
        readPredicate: (Box<E>) -> Boolean = { true },
    ): Observable<List<E>> {
        return boxes
            .filter(readPredicate)
            .map { box ->
                box[id].toObservable()
            }
            .toObservableFromIterable()
            .flatMap { it }
            .toList()
            .toObservable()
    }

    /**
     * TODO This needs to be REAL reactive
     * Public API
     */
    fun getAll(): Observable<List<E>> {
        val readPredicate: (Box<E>) -> Boolean = {box -> box.name == config.leaderBox }
        return performObservableReadBoxAction(readPredicate) { box ->
            box.getAll()
        }
    }

    private fun performObservableReadBoxAction(
        readPredicate: (Box<E>) -> Boolean,
        action: (Box<E>) -> Observable<List<E>>
    ): Observable<List<E>> {
        return boxes
            .filter(readPredicate)
            .map(action)
            .merge()
    }
}
