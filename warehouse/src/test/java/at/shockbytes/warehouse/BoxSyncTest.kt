package at.shockbytes.warehouse

import at.shockbytes.warehouse.box.Box
import at.shockbytes.warehouse.box.BoxId
import at.shockbytes.warehouse.box.memory.InMemoryBoxEngine
import at.shockbytes.warehouse.ledger.BoxOperation
import at.shockbytes.warehouse.ledger.Ledger
import at.shockbytes.warehouse.model.Content
import at.shockbytes.warehouse.state.box.TransientBoxActivationDelegate
import at.shockbytes.warehouse.state.head.TransientLedgerHeadState
import at.shockbytes.warehouse.sync.BoxSync
import org.junit.Assert
import org.junit.Test

class BoxSyncTest {

    @Test
    fun `test BoxSync`() {

        val leader = Box.defaultFrom<Content>(
            InMemoryBoxEngine.custom("in-memory.1", IdentityMapper()) { it.id },
        )

        leader.store(Content("1", "test")).blockingAwait()
        leader.update(Content("1", "test1")).blockingAwait()
        leader.delete(Content("1", "test1")).blockingAwait()
        leader.store(Content("2", "this is it")).blockingAwait()

        val ledger = Ledger.inMemory<Content>()
        ledger.storeOperation(BoxOperation.StoreOperation(Content("1", "test"))).blockingGet()
        ledger.storeOperation(BoxOperation.UpdateOperation(Content("1", "test1"))).blockingGet()
        ledger.storeOperation(BoxOperation.DeleteOperation(Content("1", "test"))).blockingGet()
        val lastHash = ledger.storeOperation(BoxOperation.StoreOperation(Content("2", "this is it"))).blockingGet()

        leader.updateHash(lastHash)

        val follower = Box.defaultFrom<Content>(
            InMemoryBoxEngine.custom("in-memory.2", IdentityMapper()) { it.id },
        )

        val boxes = listOf(leader, follower)

        val boxSync = BoxSync(
            BoxId.of("in-memory.1"),
            boxes
        )

        boxSync.syncWithLedger(ledger).blockingAwait()

        Assert.assertTrue(leader["2"].blockingGet() == Content("2", "this is it"))
        Assert.assertTrue(follower["2"].blockingGet() == Content("2", "this is it"))

    }

}