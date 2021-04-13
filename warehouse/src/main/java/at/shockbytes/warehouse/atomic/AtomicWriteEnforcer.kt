package at.shockbytes.warehouse.atomic

object AtomicWriteEnforcer : AtomicityEnforcer {
    override val enforceAtomicRead: Boolean = false
    override val enforceAtomicWrite: Boolean = true
}
