package at.shockbytes.warehouse.ledger

import at.shockbytes.warehouse.Mapper
import at.shockbytes.warehouse.util.completableOf
import at.shockbytes.warehouse.util.singleOf
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

class PersistentLedgerEngine<E>(
    private val chain: PersistentLedgerSource,
    private val head: PersistentLedgerSource,
    private val mapper: Mapper<String, LedgerBlock<E>>
) : LedgerEngine<E> {

    init {
        storeBlock(LedgerBlock("", BoxOperation.InitOperation()))
    }

    override val last: LedgerBlock<E>
        get() = head.firstLine().let(mapper::mapTo)

    private val lastHash: String
        get() = last.hash.value

    override fun entries(): Single<List<LedgerBlock<E>>> {
        return singleOf {
            chain.elements { lines ->
                lines
                    .filter { it.isNotEmpty() }
                    .map(mapper::mapTo)
                    .toList()
            }
        }
    }

    override fun store(operation: BoxOperation<E>): Completable {
        return completableOf {
            storeBlock(LedgerBlock(lastHash, operation))
        }
    }

    private fun storeBlock(block: LedgerBlock<E>) {

        val internalRepresentation = mapper.mapFrom(block)

        // Write to chain
        chain.append(internalRepresentation)

        // Write to head
        head.write(internalRepresentation)
    }
}
