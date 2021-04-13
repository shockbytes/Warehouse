package at.shockbytes.warehouse.sync

import at.shockbytes.warehouse.ledger.BoxOperation

sealed class MigrationAction<E> {

    data class Migration<E>(
        val migrationOperation: BoxOperation.MigrateOperation<E>
    ) : MigrationAction<E>()

    class NoMigration<E> : MigrationAction<E>()
}
