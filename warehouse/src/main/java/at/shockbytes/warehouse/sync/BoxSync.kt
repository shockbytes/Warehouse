package at.shockbytes.warehouse.sync

import at.shockbytes.warehouse.box.Box

class BoxSync<E>(
    private val leaderBoxName: String,
    private val follower: List<Box<E>>
) {

    private val leaderBox = findLeaderFromConfig()

    private fun findLeaderFromConfig(): Box<E> {
        return follower.find { it.name == leaderBoxName }
            ?: throw IllegalStateException("Could not find LeaderBox with name $leaderBoxName")
    }

    fun sync() {
        follower.toMutableSet()
            .apply {
                remove(leaderBox)
            }
            .forEach { follower ->
                follower.syncWith(leaderBox)
            }
    }
}