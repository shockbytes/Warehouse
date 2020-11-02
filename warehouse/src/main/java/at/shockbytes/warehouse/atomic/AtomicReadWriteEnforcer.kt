package at.shockbytes.warehouse.atomic

object AtomicReadWriteEnforcer : AtomicityEnforcer {
    override val enforceAtomicRead: Boolean = true
    override val enforceAtomicWrite: Boolean = true
}
