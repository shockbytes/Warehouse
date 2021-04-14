package at.shockbytes.warehouse

import at.shockbytes.warehouse.box.BoxId

sealed class BoxUpdateAction {

    data class ChangeLeaderBox(val newLeaderBoxId: BoxId): BoxUpdateAction()

    data class ChangeActivationState(val boxId: BoxId, val isEnabled: Boolean): BoxUpdateAction()
}
