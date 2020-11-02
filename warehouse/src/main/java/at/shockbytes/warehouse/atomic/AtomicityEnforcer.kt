package at.shockbytes.warehouse.atomic

interface AtomicityEnforcer {

    val enforceAtomicRead: Boolean

    val enforceAtomicWrite: Boolean
}
