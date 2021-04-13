package at.shockbytes.warehouse

import at.shockbytes.warehouse.box.Box
import at.shockbytes.warehouse.box.BoxId
import at.shockbytes.warehouse.box.memory.InMemoryBoxEngine
import at.shockbytes.warehouse.ledger.Ledger
import at.shockbytes.warehouse.model.Content
import at.shockbytes.warehouse.state.box.TransientBoxActivationDelegate
import at.shockbytes.warehouse.state.head.TransientLedgerHeadState
import org.junit.Before
import org.junit.Test

class WarehouseSynchronizationTest {

    private val testData = listOf(
        Content("1", "1"),
        Content("2", "2"),
        Content("3", "3")
    )

    private lateinit var warehouse: Warehouse<Content>

    @Before
    fun setup() {

        warehouse = Warehouse.new(
            boxes = listOf(
                Box(
                    InMemoryBoxEngine.custom("in-memory.1", IdentityMapper()) { it.id },
                    TransientLedgerHeadState(),
                    TransientBoxActivationDelegate(defaultValue = true)
                ),
                Box(
                    InMemoryBoxEngine.custom("in-memory.2", IdentityMapper()) { it.id },
                    TransientLedgerHeadState(),
                    TransientBoxActivationDelegate(defaultValue = true)
                )
            ),
            ledger = Ledger.inMemory(),
            WarehouseConfiguration(leaderBoxId = BoxId.of("in-memory.1"))
        )
    }

    @Test
    fun `test disable box and fill other box with data`() {

        warehouse.setBoxEnabled(BoxId.of("in-memory.2"), isEnabled = false).blockingAwait()

        warehouse.store(testData[0]).blockingAwait()
        warehouse.store(testData[1]).blockingAwait()
        warehouse.store(testData[2]).blockingAwait()

        warehouse[BoxId.of("in-memory.1")]
            .test()
            .assertValue { storedContent ->
                storedContent == testData
            }

        warehouse[BoxId.of("in-memory.2")]
            .test()
            .assertValue { storedContent ->
                storedContent.isEmpty()
            }
    }

    @Test
    fun `test disable box and enabled after first two fills`() {

        warehouse.setBoxEnabled(BoxId.of("in-memory.2"), isEnabled = false).blockingAwait()

        warehouse.store(testData[0]).blockingAwait()
        warehouse.store(testData[1]).blockingAwait()

        warehouse.setBoxEnabled(BoxId.of("in-memory.2"), isEnabled = true).blockingAwait()

        warehouse.store(testData[2]).blockingAwait()

        warehouse[BoxId.of("in-memory.1")]
            .test()
            .assertValue { storedContent ->
                storedContent == testData
            }

        warehouse[BoxId.of("in-memory.2")]
            .test()
            .assertValue { storedContent ->
                storedContent == testData
            }
    }

    @Test
    fun `test enable box, then disable, then enabled afterwards`() {

        warehouse.setBoxEnabled(BoxId.of("in-memory.2"), isEnabled = true).blockingAwait()

        warehouse.store(testData[0]).blockingAwait()
        warehouse.setBoxEnabled(BoxId.of("in-memory.2"), isEnabled = false).blockingAwait()

        warehouse.store(testData[1]).blockingAwait()
        warehouse.store(testData[2]).blockingAwait()
        warehouse.setBoxEnabled(BoxId.of("in-memory.2"), isEnabled = true).blockingAwait()

        warehouse[BoxId.of("in-memory.1")]
            .test()
            .assertValue { storedContent ->
                storedContent == testData
            }

        warehouse[BoxId.of("in-memory.2")]
            .test()
            .assertValue { storedContent ->
                storedContent == testData
            }
    }
}
