package at.shockbytes.warehouse

import at.shockbytes.warehouse.box.Box
import at.shockbytes.warehouse.box.BoxId
import at.shockbytes.warehouse.box.memory.InMemoryBoxEngine
import at.shockbytes.warehouse.ledger.Ledger
import at.shockbytes.warehouse.model.Content
import at.shockbytes.warehouse.state.box.TransientBoxActivationDelegate
import at.shockbytes.warehouse.state.head.TransientLedgerHeadState
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class WarehouseLeaderBoxSwitchTest {

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
    fun `test switch leader box while both boxes are enabled`() {

        warehouse.store(testData[0]).blockingAwait()
        warehouse.store(testData[1]).blockingAwait()
        warehouse.store(testData[2]).blockingAwait()

        Assert.assertTrue(warehouse.leaderBoxId == BoxId.of("in-memory.1"))

        warehouse.updateBoxState(BoxUpdateAction.ChangeLeaderBox(BoxId.of("in-memory.2")))
            .blockingAwait()

        Assert.assertTrue(warehouse.leaderBoxId == BoxId.of("in-memory.2"))

        warehouse[BoxId.of("in-memory.1")].test()
            .assertValue { data ->
                data == testData
            }

        warehouse[BoxId.of("in-memory.2")].test()
            .assertValue { data ->
                data == testData
            }
    }

    @Test
    fun `test only one box, enable second box, switch leader box`() {

        warehouse.updateBoxState(
            BoxUpdateAction.ChangeActivationState(
                BoxId.of("in-memory.2"),
                isEnabled = false
            )
        )

        warehouse.store(testData[0]).blockingAwait()
        warehouse.store(testData[1]).blockingAwait()
        warehouse.store(testData[2]).blockingAwait()

        Assert.assertTrue(warehouse.leaderBoxId == BoxId.of("in-memory.1"))

        warehouse[BoxId.of("in-memory.1")].test()
            .assertValue { data ->
                data == testData
            }

        warehouse[BoxId.of("in-memory.2")].test()
            .assertValue { data ->
                data.isEmpty()
            }

        warehouse.updateBoxState(BoxUpdateAction.ChangeLeaderBox(BoxId.of("in-memory.2")))
            .blockingAwait()

        Assert.assertTrue(warehouse.leaderBoxId == BoxId.of("in-memory.2"))

        warehouse[BoxId.of("in-memory.1")].test()
            .assertValue { data ->
                data == testData
            }

        warehouse[BoxId.of("in-memory.2")].test()
            .assertValue { data ->
                data == testData
            }
    }
}
