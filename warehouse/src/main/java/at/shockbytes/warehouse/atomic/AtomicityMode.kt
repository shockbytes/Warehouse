package at.shockbytes.warehouse.atomic

enum class AtomicityMode(val atomicityEnforcer: AtomicityEnforcer?) {
    ATOMIC_READ(AtomicReadEnforcer),
    ATOMIC_WRITE(AtomicWriteEnforcer),
    ATOMIC_READ_WRITE(AtomicReadWriteEnforcer),
    NONE(null)
}