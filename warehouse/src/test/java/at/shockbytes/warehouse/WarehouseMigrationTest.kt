package at.shockbytes.warehouse

import at.shockbytes.warehouse.box.Box
import at.shockbytes.warehouse.box.BoxId
import at.shockbytes.warehouse.box.memory.InMemoryBoxEngine
import at.shockbytes.warehouse.ledger.BoxOperation
import at.shockbytes.warehouse.ledger.Ledger
import at.shockbytes.warehouse.model.Content
import org.junit.Test

class WarehouseMigrationTest {

    private val testData = listOf(
        Content("1", "1"),
        Content("2", "2"),
        Content("3", "3")
    )

    @Test
    fun `test migration happy path`() {

        val warehouse = Warehouse.new(
            boxes = listOf(
                Box.defaultFrom(
                    InMemoryBoxEngine.custom("in-memory.1", IdentityMapper()) { it.id },
                ),
                Box.defaultFrom(
                    InMemoryBoxEngine.withData(
                        "in-memory.2",
                        IdentityMapper(),
                        idSelector = { it.id },
                        data = testData
                    )
                )
            ),
            ledger = Ledger.inMemory(),
            WarehouseConfiguration(
                leaderBoxId = BoxId.of("in-memory.1"),
                migrationSource = BoxId.of("in-memory.2")
            )
        )

        warehouse[BoxId.of("in-memory.2")]
            .test()
            .assertValue { storedContent ->
                storedContent == testData
            }

        warehouse[BoxId.of("in-memory.1")]
            .test()
            .assertValue { storedContent ->
                storedContent == testData
            }
    }

    @Test
    fun `test migration happens only once`() {

        val boxes = listOf(
            Box.defaultFrom(
                InMemoryBoxEngine.custom("in-memory.1", IdentityMapper()) { it.id },
            ),
            Box.defaultFrom(
                InMemoryBoxEngine.withData(
                    "in-memory.2",
                    IdentityMapper(),
                    idSelector = { it.id },
                    data = testData
                )
            )
        )

        val ledger = Ledger.inMemory<Content>()

        var warehouse = Warehouse.new(
            boxes = boxes,
            ledger = ledger,
            WarehouseConfiguration(
                leaderBoxId = BoxId.of("in-memory.1"),
                migrationSource = BoxId.of("in-memory.2")
            )
        )

        // Simply reinitialize to trigger another migration check
        warehouse = Warehouse.new(
            boxes = boxes,
            ledger = ledger,
            WarehouseConfiguration(
                leaderBoxId = BoxId.of("in-memory.1"),
                migrationSource = BoxId.of("in-memory.2")
            )
        )

        ledger.allOperations()
            .test()
            .assertValue { blocks ->
                blocks[0].data is BoxOperation.InitOperation<*> && blocks[1].data is BoxOperation.MigrateOperation<*>
            }

        warehouse[BoxId.of("in-memory.2")]
            .test()
            .assertValue { storedContent ->
                storedContent == testData
            }

        warehouse[BoxId.of("in-memory.1")]
            .test()
            .assertValue { storedContent ->
                storedContent == testData
            }
    }
}
