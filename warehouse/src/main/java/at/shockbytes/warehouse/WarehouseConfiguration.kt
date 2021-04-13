package at.shockbytes.warehouse

import at.shockbytes.warehouse.box.BoxId

data class WarehouseConfiguration(
    val leaderBoxId: BoxId,
    val migrationSource: BoxId? = null
)
