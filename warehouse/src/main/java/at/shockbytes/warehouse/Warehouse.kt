package at.shockbytes.warehouse

import at.shockbytes.warehouse.box.Box
import at.shockbytes.warehouse.truck.Truck
import at.shockbytes.warehouse.util.completableOf
import at.shockbytes.warehouse.util.merge
import at.shockbytes.warehouse.util.toObservableFromIterable
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

class Warehouse<E>(
    private val boxes: List<Box<*, E>>,
    private val trucks: List<Truck<E>> = listOf(),
    // TODO Incorporate config
    private val config: WarehouseConfiguration = WarehouseConfiguration()
) {

    init {
        syncLeader()
    }

    // TODO Use dedicated `BoxSync` class for this
    private fun syncLeader() {
        findLeaderFromConfig()
            ?.let(::syncBoxesWithLeaders)
    }

    private fun findLeaderFromConfig(): Box<*, E>? {
        return config.leaderBox
            ?.let { leaderBoxName ->
                boxes.find { it.name == leaderBoxName }
            }
    }

    private fun syncBoxesWithLeaders(leader: Box<*, E>) {
        boxes.toMutableSet()
            .apply {
                remove(leader)
            }
            .forEach { follower ->
                follower.syncWith(leader)
            }
    }

    /**
     * Public Api
     */
    fun store(
        value: E,
        writePredicate: (Box<*, E>) -> Boolean = { true }
    ): Completable {
        return performCompletableWriteBoxAction(writePredicate) { box ->
            box.store(value)
        }.andThen(updateTrucks(value))
    }

    private fun updateTrucks(value: E): Completable {
        return trucks
            .map { truck ->
                completableOf {
                    truck.loadCargo(value)
                }
            }
            .merge()
    }

    /**
     * Public Api
     */
    fun update(
        value: E,
        writePredicate: (Box<*, E>) -> Boolean = { true },
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
        writePredicate: (Box<*, E>) -> Boolean = { true },
    ): Completable {
        return performCompletableWriteBoxAction(writePredicate) { box ->
            box.delete(value)
        }
    }

    private fun performCompletableWriteBoxAction(
        writePredicate: (Box<*, E>) -> Boolean,
        action: (Box<*, E>) -> Completable
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
        readPredicate: (Box<*, E>) -> Boolean = { true },
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

    // TODO Rethink getAll method...

    // TODO Public Api?
    inline fun <reified K : Box<*, E>> getAllFor(): Observable<List<E>> {
        return getAllForClass(K::class.java)
    }

    // TODO Public Api?
    fun <K : Box<*, E>> getAllForClass(c: Class<K>): Observable<List<E>> {
        return boxes.filterIsInstance(c).first().getAll()
    }

    /**
     * Public API
     */
    fun getAll(
        readPredicate: (Box<*, E>) -> Boolean = { true }
    ): Observable<List<E>> {
        return performObservableReadBoxAction(readPredicate) { box ->
            box.getAll()
        }
    }

    private fun performObservableReadBoxAction(
        readPredicate: (Box<*, E>) -> Boolean,
        action: (Box<*, E>) -> Observable<List<E>>
    ): Observable<List<E>> {
        return boxes
            .filter(readPredicate)
            .map(action)
            .merge()
    }
}
