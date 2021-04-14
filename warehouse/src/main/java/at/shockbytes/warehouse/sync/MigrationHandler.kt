package at.shockbytes.warehouse.sync

import at.shockbytes.warehouse.box.Box
import at.shockbytes.warehouse.box.BoxId
import at.shockbytes.warehouse.ledger.Ledger
import at.shockbytes.warehouse.util.asCompletable
import io.reactivex.rxjava3.core.Completable

class MigrationHandler<E>(
    private val migrationBox: Box<E>?,
    private val leaderBoxId: BoxId
) {

    /**
     * 1.) Check if the box from config.migrationSource requires a migration
     * 2.) Let the ledger write an migration operation
     * 3.) Force the leading box to perform the migration
     * 4.) Call forceBoxSynchronization() to synchronize all boxes
     */
    fun checkForMigrations(ledger: Ledger<E>, boxSync: BoxSync<E>): Completable {

        // There might be no migrationBox, that's okay
        return if (migrationBox != null && migrationBox.id != leaderBoxId) {
            migrationBox.requiresMigration(ledger) // 1.
                .flatMapCompletable { action: MigrationAction<E> ->
                    handleMigrationAction(action, ledger, boxSync)
                }
        } else {
            Completable.complete()
        }
    }

    private fun handleMigrationAction(
        action: MigrationAction<E>,
        ledger: Ledger<E>,
        boxSync: BoxSync<E>
    ): Completable {
        return when (action) {
            is MigrationAction.Migration -> {
                ledger.storeOperation(action.migrationOperation).asCompletable() // 2.
                    .andThen(boxSync.synchronizeLeader(ledger)) // 3.
                    .andThen(
                        // 4.
                        boxSync.syncWithLedger(
                            ledger,
                            exceptMigrationSource = migrationBox
                        )
                    )
            }
            is MigrationAction.NoMigration -> Completable.complete()
        }
    }
}
