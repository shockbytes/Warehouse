package at.shockbytes.warehouse

import at.shockbytes.warehouse.box.Box
import at.shockbytes.warehouse.sync.BoxSync
import at.shockbytes.warehouse.truck.Truck
import at.shockbytes.warehouse.util.completableOf
import at.shockbytes.warehouse.util.merge
import at.shockbytes.warehouse.util.toObservableFromIterable
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

/**
* - Box uses BoxEngine
* - Box uses Ledger which is a blockchain that stores all new Operations in a file
* - BoxSync uses Ledger to determine which Operations are delta
* - Take these delta operations and apply it to unsynced box
 */
class Warehouse<E>(
    private val boxes: List<Box<E>>,
    private val config: WarehouseConfiguration = WarehouseConfiguration()
) {

    private val boxSync: BoxSync<E> = BoxSync(config.leaderBox!!, boxes)

    init {
        boxSync.sync()
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
