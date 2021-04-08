package at.shockbytes.warehouse.sync

import at.shockbytes.warehouse.box.Box
import at.shockbytes.warehouse.ledger.Ledger
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.kotlin.concatAll

class BoxSync<E>(
    private val leaderBoxName: String,
    private val boxes: List<Box<E>>
) {

    private val leaderBox = findLeaderFromConfig()

    private fun findLeaderFromConfig(): Box<E> {
        return boxes.find { it.name == leaderBoxName }
            ?: throw IllegalStateException("Could not find LeaderBox with name $leaderBoxName")
    }

    fun syncWithLedger(ledger: Ledger<E>): Completable {

        val follower = boxes.toMutableSet()
            .apply {
                remove(leaderBox)
            }

        return follower
            .map { box ->
                ledger.operationsSince(box.currentState)
                    .map { blocks ->
                        blocks.map { it.data }
                    }
                    .flatMapCompletable { operations ->
                        box.syncOperations(operations)
                    }
            }
            .concatAll()
    }
}