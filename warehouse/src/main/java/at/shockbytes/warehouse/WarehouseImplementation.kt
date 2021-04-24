package at.shockbytes.warehouse

import at.shockbytes.warehouse.box.Box
import at.shockbytes.warehouse.box.BoxId
import at.shockbytes.warehouse.ledger.BoxOperation
import at.shockbytes.warehouse.ledger.Hash
import at.shockbytes.warehouse.ledger.Ledger
import at.shockbytes.warehouse.sync.BoxSync
import at.shockbytes.warehouse.sync.MigrationHandler
import at.shockbytes.warehouse.util.asCompletable
import at.shockbytes.warehouse.util.completableOf
import at.shockbytes.warehouse.util.merge
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableSource
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class WarehouseImplementation<E> internal constructor(
    private val boxes: List<Box<E>>,
    private val ledger: Ledger<E>,
    private var config: WarehouseConfiguration
) : Warehouse<E> {

    override val leaderBoxId: BoxId
        get() = config.leaderBoxId

    private val boxSync: BoxSync<E>
        get() = BoxSync(config.leaderBoxId, boxes)

    private val migrationHandler: MigrationHandler<E>
        get() = MigrationHandler(findBoxById(config.migrationSource), config.leaderBoxId)

    init {
        // TODO .blockingAwait is not nice, how can we make this more beautiful?
        migrationCheck().blockingAwait()
    }

    private fun migrationCheck(): Completable {
        return migrationHandler.checkForMigrations(ledger, boxSync)
    }

    override fun forceBoxSynchronization(): Completable {
        return boxSync.syncWithLedger(ledger)
    }

    override fun store(
        value: E,
        writePredicate: (Box<E>) -> Boolean
    ): Completable {
        return performCompletableWriteBoxAction(writePredicate) { box ->
            box.store(value).asCompletable()
        }.andThen(updateLedger(BoxOperation.StoreOperation(value)))
    }

    override fun update(
        value: E,
        writePredicate: (Box<E>) -> Boolean,
    ): Completable {
        return performCompletableWriteBoxAction(writePredicate) { box ->
            box.update(value)
        }.andThen(updateLedger(BoxOperation.UpdateOperation(value)))
    }

    override fun delete(
        value: E,
        writePredicate: (Box<E>) -> Boolean,
    ): Completable {
        return performCompletableWriteBoxAction(writePredicate) { box ->
            box.delete(value)
        }.andThen(updateLedger(BoxOperation.DeleteOperation(value)))
    }

    private fun updateLedger(operation: BoxOperation<E>): CompletableSource {
        return ledger.storeOperation(operation)
            .flatMapCompletable(::updateHashOfAllBoxes)
    }

    private fun updateHashOfAllBoxes(hash: Hash): Completable {
        return boxes
            .map { box ->
                completableOf {
                    box.updateHash(hash)
                }
            }
            .merge()
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

    override fun <ID> getItemById(id: ID): Single<E> {
        val readPredicate: (Box<E>) -> Boolean = { box ->
            box.id == config.leaderBoxId && box.isEnabled
        }

        val leaderBox = boxes
            .find(readPredicate)
            ?: return Single.error(IllegalStateException("No LeaderBox available!"))

        return leaderBox.getSingleElement(id)
    }

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
            .andThen(ledger.storeOperation(BoxOperation.ResetOperation()).asCompletable())
    }

    /**
     * Enabling a box requires a synchronization afterwards
     */
    override fun updateBoxState(update: BoxUpdateAction): Completable {
        return when (update) {
            is BoxUpdateAction.ChangeActivationState -> {
                handleActivationStateChanges(update.boxId, update.isEnabled)
            }
            is BoxUpdateAction.ChangeLeaderBox -> {
                handleChangeLeaderBoxAction(update.newLeaderBoxId)
            }
        }
    }

    private fun handleActivationStateChanges(id: BoxId, isEnabled: Boolean): Completable {
        findBoxById(id)?.isEnabled = isEnabled

        return if (isEnabled) {
            forceBoxSynchronization()
        } else {
            Completable.complete()
        }
    }

    private fun handleChangeLeaderBoxAction(newLeaderBoxId: BoxId): Completable {
        config = config.copy(leaderBoxId = newLeaderBoxId)

        // Always enable new leader
        findBoxById(newLeaderBoxId)
            ?.let { box ->
                box.isEnabled = true
            }
            ?: throw IllegalStateException("There is no new leader")

        return boxSync.synchronizeLeader(ledger)
    }

    private fun findBoxById(id: BoxId?): Box<E>? {
        return boxes
            .find { box ->
                box.id == id
            }
    }
}
