package at.shockbytes.warehouse

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    BoxSyncTest::class,
    LedgerTest::class,
    WarehouseTest::class,
    WarehouseSynchronizationTest::class,
    WarehouseMigrationTest::class
)
class CoreTests