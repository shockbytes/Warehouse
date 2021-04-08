package at.shockbytes.warehouse

import at.shockbytes.warehouse.ledger.BoxOperation
import at.shockbytes.warehouse.ledger.Hash
import at.shockbytes.warehouse.ledger.Ledger
import at.shockbytes.warehouse.ledger.LedgerBlock
import at.shockbytes.warehouse.model.Content
import org.junit.Before
import org.junit.Test


class LedgerTest {

    private lateinit var ledger: Ledger<Content>

    @Before
    fun setup() {
        ledger = Ledger.inMemory()
    }

    @Test
    fun `test storeOperations`() {

        val events = ledger.onLedgerEvents().test()

        val createOperation = BoxOperation.StoreOperation(Content("1", "test"))
        val updateOperation = BoxOperation.UpdateOperation(Content("1", "test1"))

        ledger.storeOperation(createOperation).blockingGet()
        ledger.storeOperation(updateOperation).blockingGet()

        events
            .assertValueAt(
                0,
                LedgerBlock(
                    "ae5f08f3e82b2f877413045dee9f0dcb56a6460e2f5305ada45639181ee42075",
                    createOperation
                )
            )
            .assertValueAt(
                1,
                LedgerBlock(
                    "2314fbc77e1e7d5b62e73906970ff613268f78d7f3e9e557ab948b298c6538de",
                    updateOperation
                )
            )
    }

    @Test
    fun `test operationsSince with valid hash`() {

        populateWithData()

        ledger.operationsSince(Hash("01a1f7c743874f7902a520c6d9e1231e609a7e7f9ad218239f01cce26ad7f368"))
            .test()
            .assertValue(
                listOf(
                    LedgerBlock(
                        "01a1f7c743874f7902a520c6d9e1231e609a7e7f9ad218239f01cce26ad7f368",
                        BoxOperation.StoreOperation(Content("2", "content"))
                    ),
                    LedgerBlock(
                        "9ff10f0939f66aa52c460a03a711bb7bfea4e7d7d369d5e435d342a207eaf713",
                        BoxOperation.StoreOperation(Content("3", "is there"))
                    )
                )
            )
    }

    @Test
    fun `test operationsSince with hash that's not stored in the ledger`() {

        populateWithData()

        ledger.operationsSince(Hash("kdckcmsldkcmsdlckm"))
            .test()
            .assertValue(
                listOf(
                    LedgerBlock("", BoxOperation.InitOperation()),
                    LedgerBlock(
                        "ae5f08f3e82b2f877413045dee9f0dcb56a6460e2f5305ada45639181ee42075",
                        BoxOperation.StoreOperation(Content("1", "test"))
                    ),
                    LedgerBlock(
                        "2314fbc77e1e7d5b62e73906970ff613268f78d7f3e9e557ab948b298c6538de",
                        BoxOperation.UpdateOperation(Content("1", "test1"))
                    ),
                    LedgerBlock(
                        "01a1f7c743874f7902a520c6d9e1231e609a7e7f9ad218239f01cce26ad7f368",
                        BoxOperation.StoreOperation(Content("2", "content"))
                    ),
                    LedgerBlock(
                        "9ff10f0939f66aa52c460a03a711bb7bfea4e7d7d369d5e435d342a207eaf713",
                        BoxOperation.StoreOperation(Content("3", "is there"))
                    )
                )
            )
    }

    @Test
    fun `test operationsSince with empty hash`() {

        populateWithData()

        ledger.operationsSince(Hash.empty())
            .test()
            .assertValue(
                listOf(
                    LedgerBlock("", BoxOperation.InitOperation()),
                    LedgerBlock(
                        "ae5f08f3e82b2f877413045dee9f0dcb56a6460e2f5305ada45639181ee42075",
                        BoxOperation.StoreOperation(Content("1", "test"))
                    ),
                    LedgerBlock(
                        "2314fbc77e1e7d5b62e73906970ff613268f78d7f3e9e557ab948b298c6538de",
                        BoxOperation.UpdateOperation(Content("1", "test1"))
                    ),
                    LedgerBlock(
                        "01a1f7c743874f7902a520c6d9e1231e609a7e7f9ad218239f01cce26ad7f368",
                        BoxOperation.StoreOperation(Content("2", "content"))
                    ),
                    LedgerBlock(
                        "9ff10f0939f66aa52c460a03a711bb7bfea4e7d7d369d5e435d342a207eaf713",
                        BoxOperation.StoreOperation(Content("3", "is there"))
                    )
                )
            )
    }


    @Test
    fun `test allOperations with empty ledger`() {

        ledger.allOperations()
            .test()
            .assertValue(
                listOf(
                    LedgerBlock("", BoxOperation.InitOperation())
                )
            )
    }

    @Test
    fun `test allOperations with filled ledger`() {

        populateWithData()

        ledger.allOperations()
            .test()
            .assertValue(
                listOf(
                    LedgerBlock("", BoxOperation.InitOperation()),
                    LedgerBlock(
                        "ae5f08f3e82b2f877413045dee9f0dcb56a6460e2f5305ada45639181ee42075",
                        BoxOperation.StoreOperation(Content("1", "test"))
                    ),
                    LedgerBlock(
                        "2314fbc77e1e7d5b62e73906970ff613268f78d7f3e9e557ab948b298c6538de",
                        BoxOperation.UpdateOperation(Content("1", "test1"))
                    ),
                    LedgerBlock(
                        "01a1f7c743874f7902a520c6d9e1231e609a7e7f9ad218239f01cce26ad7f368",
                        BoxOperation.StoreOperation(Content("2", "content"))
                    ),
                    LedgerBlock(
                        "9ff10f0939f66aa52c460a03a711bb7bfea4e7d7d369d5e435d342a207eaf713",
                        BoxOperation.StoreOperation(Content("3", "is there"))
                    )
                )
            )
    }

    private fun populateWithData() {

        val createOperation = BoxOperation.StoreOperation(Content("1", "test"))
        val updateOperation = BoxOperation.UpdateOperation(Content("1", "test1"))
        val createOperation1 = BoxOperation.StoreOperation(Content("2", "content"))
        val createOperation2 = BoxOperation.StoreOperation(Content("3", "is there"))

        ledger.storeOperation(createOperation).blockingGet()
        ledger.storeOperation(updateOperation).blockingGet()
        ledger.storeOperation(createOperation1).blockingGet()
        ledger.storeOperation(createOperation2).blockingGet()
    }

}
