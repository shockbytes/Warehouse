package at.shockbytes.warehouse

import at.shockbytes.warehouse.ledger.LedgerTest
import at.shockbytes.warehouse.ledger.PersistentLedgerEngineTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    BoxSyncTest::class,
    LedgerTest::class,
    WarehouseLeaderBoxSwitchTest::class,
    WarehouseSynchronizationTest::class,
    WarehouseMigrationTest::class,
    PersistentLedgerEngineTest::class
)
class CoreTests
