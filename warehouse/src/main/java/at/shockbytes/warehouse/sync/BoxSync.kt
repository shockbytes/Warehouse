package at.shockbytes.warehouse.sync

import at.shockbytes.warehouse.box.Box
import at.shockbytes.warehouse.box.BoxId
import at.shockbytes.warehouse.ledger.BoxOperation
import at.shockbytes.warehouse.ledger.Ledger
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.kotlin.concatAll

class BoxSync<E>(
    private val leaderBoxId: BoxId,
    private val boxes: List<Box<E>>
) {

    private val leaderBox: Box<E> = findLeaderFromConfig()

    private val follower = boxes.toMutableSet()
        .apply {
            remove(leaderBox)
        }

    private fun findLeaderFromConfig(): Box<E> {
        return boxes.find { it.id == leaderBoxId }
            ?: throw IllegalStateException("Could not find LeaderBox with name $leaderBoxId")
    }

    fun syncWithLedger(
        ledger: Ledger<E>,
        exceptMigrationSource: Box<E>? = null
    ): Completable {
        return follower
            // Do not sync migrationSource if that was the trigger of this operation
            .filterNot { it.id == exceptMigrationSource?.id }
            .map { box ->
                synchronizeBoxWithLedger(box, ledger)
            }
            .concatAll()
    }


    fun synchronizeLeader(ledger: Ledger<E>): Completable {
        return synchronizeBoxWithLedger(leaderBox, ledger)
    }

    private fun synchronizeBoxWithLedger(box: Box<E>, ledger: Ledger<E>): Completable {
        return ledger.operationsSince(box.currentState)
            .map { blocks ->
                blocks
                    // Each box takes care of their own init operation
                    .filterNot { it.data is BoxOperation.InitOperation }
                    .map { it.data }
            }
            .flatMapCompletable { operations ->
                if (operations.isNotEmpty()) {
                    box.syncOperations(operations)
                } else {
                    Completable.complete()
                }
            }
    }
}