package at.shockbytes.warehouse

import at.shockbytes.warehouse.box.Box
import at.shockbytes.warehouse.box.BoxId
import at.shockbytes.warehouse.ledger.BoxOperation
import at.shockbytes.warehouse.ledger.Ledger
import at.shockbytes.warehouse.sync.BoxSync
import at.shockbytes.warehouse.sync.MigrationHandler
import at.shockbytes.warehouse.util.asCompletable
import at.shockbytes.warehouse.util.merge
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

class WarehouseImplementation<E> internal constructor(
    private val boxes: List<Box<E>>,
    private val ledger: Ledger<E>,
    private val config: WarehouseConfiguration
) : Warehouse<E> {

    private val boxSync = BoxSync(config.leaderBoxId, boxes)
    private val migrationHandler = MigrationHandler(findBoxById(config.migrationSource))

    init {
        // TODO .blockingAwait is not nice, how can we make this more beautiful?
        migrationHandler.checkForMigrations(ledger, boxSync)
            .blockingAwait()
    }

    override fun forceBoxSynchronization(): Completable {
        return boxSync.syncWithLedger(ledger)
    }

    override fun store(
        value: E,
        writePredicate: (Box<E>) -> Boolean
    ): Completable {
        return performCompletableWriteBoxAction(writePredicate) { box ->
            box.store(value)
                .andThen(ledger.storeOperation(BoxOperation.StoreOperation(value)))
                .doOnSuccess(box::updateHash)
                .asCompletable()
        }
    }

    override fun update(
        value: E,
        writePredicate: (Box<E>) -> Boolean,
    ): Completable {
        return performCompletableWriteBoxAction(writePredicate) { box ->
            box.update(value)
                .andThen(ledger.storeOperation(BoxOperation.UpdateOperation(value)))
                .doOnSuccess(box::updateHash)
                .asCompletable()
        }
    }

    override fun delete(
        value: E,
        writePredicate: (Box<E>) -> Boolean,
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
            .filter { it.isEnabled }
            .map(action)
            .merge()
    }

    override operator fun get(id: BoxId): Observable<List<E>> {
        return boxes
            .firstOrNull { box ->
                box.id == id
            }
            ?.getAll()
            ?: Observable.empty()
    }

    /**
     * TODO This needs to be REAL reactive - check this
     */
    override fun getAll(): Observable<List<E>> {
        val readPredicate: (Box<E>) -> Boolean = { box -> box.id == config.leaderBoxId }
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
            .filter { it.isEnabled }
            .map(action)
            .merge()
    }

    override fun resetBox(id: BoxId): Completable {
        return findBoxById(id)?.reset()
            ?: Completable.error(Throwable("Box with ID ${id.value} not found"))
    }

    override fun reset(): Completable {
        return boxes
            .map { box ->
                box.reset()
            }
            .merge()
    }

    /**
     * Enabling a box requires a synchronization afterwards
     */
    override fun setBoxEnabled(id: BoxId, isEnabled: Boolean): Completable {

        findBoxById(id)?.isEnabled = isEnabled

        return if (isEnabled) {
            forceBoxSynchronization()
        } else {
            Completable.complete()
        }
    }

    private fun findBoxById(id: BoxId?): Box<E>? {
        return boxes
            .find { box ->
                box.id == id
            }
    }
}
