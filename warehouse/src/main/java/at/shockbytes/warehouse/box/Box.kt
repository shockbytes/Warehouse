package at.shockbytes.warehouse.box

import at.shockbytes.warehouse.ledger.BoxOperation
import at.shockbytes.warehouse.ledger.Hash
import at.shockbytes.warehouse.ledger.Ledger
import at.shockbytes.warehouse.state.box.BoxActivationDelegate
import at.shockbytes.warehouse.state.box.TransientBoxActivationDelegate
import at.shockbytes.warehouse.state.head.LedgerHeadState
import at.shockbytes.warehouse.state.head.TransientLedgerHeadState
import at.shockbytes.warehouse.sync.MigrationAction
import at.shockbytes.warehouse.util.asCompletable
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.concatAll

class Box<E>(
    private val boxEngine: BoxEngine<*, E>,
    private val headState: LedgerHeadState,
    activationDelegate: BoxActivationDelegate
) {

    var isEnabled: Boolean by activationDelegate

    val id: BoxId
        get() = boxEngine.id

    val currentState: Hash
        get() = headState.headHash()

    fun updateHash(hash: Hash) {
        headState.updateHead(hash)
    }

    operator fun <ID> get(id: ID): Single<E> {
        return getSingleElement(id)
    }

    fun <ID> getSingleElement(id: ID): Single<E> = boxEngine.getElementForIdType(id)

    fun getAll(): Observable<List<E>> = boxEngine.getAll()

    fun store(value: E): Single<E> {
        return boxEngine.store(value)
    }

    fun storeBatch(values: List<E>): Completable {
        return values
            .map { value ->
                boxEngine.store(value).asCompletable()
            }
            .concatAll()
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
        println("${this.id.value} syncing ${operations.size} operations...")

        return operations
            .map { operation ->
                operation.perform(this)
            }
            .concatAll()
    }

    /**
     * A migration is required in case that there is data stored in the engine, but the ledger
     * says that there should be no data available --> Data is available from previous installation.
     */
    fun requiresMigration(ledger: Ledger<E>): Single<MigrationAction<E>> {
        return Observable
            .zip(
                boxEngine.getAll(),
                ledger.isEmpty.toObservable(),
                { data: List<E>, isLedgerEmpty: Boolean ->

                    if (data.isNotEmpty() && isLedgerEmpty) {
                        MigrationAction.Migration(BoxOperation.MigrateOperation(data))
                    } else {
                        MigrationAction.NoMigration()
                    }
                }
            )
            .first(MigrationAction.NoMigration())
    }

    companion object {

        fun <E> defaultFrom(boxEngine: BoxEngine<*, E>): Box<E> {
            return Box(
                boxEngine,
                TransientLedgerHeadState(),
                TransientBoxActivationDelegate(defaultValue = true)
            )
        }
    }
}
