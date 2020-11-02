package at.shockbytes.warehouse.atomic

object AtomicReadEnforcer : AtomicityEnforcer {
    override val enforceAtomicRead: Boolean = true
    override val enforceAtomicWrite: Boolean = false
}
