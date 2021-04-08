package at.shockbytes.warehouse

import at.shockbytes.warehouse.atomic.AtomicityMode

data class WarehouseConfiguration(
    val atomicityMode: AtomicityMode = AtomicityMode.NONE,
    val leaderBox: String
)