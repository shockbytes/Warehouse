package at.shockbytes.warehouse.realm

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import at.shockbytes.warehouse.TestActivity
import at.shockbytes.warehouse.box.Box
import at.shockbytes.warehouse.realm.model.RealmTestContentMapper
import at.shockbytes.warehouse.realm.model.TestContent
import at.shockbytes.warehouse.state.head.TransientLedgerHeadState
import io.realm.Realm
import io.realm.RealmConfiguration
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class AndroidRealmBoxTest {

    private lateinit var box: Box<TestContent>

    @Before
    fun setup() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)

        box = Box(
            RealmBoxEngine.fromRealm(
                RealmConfiguration.Builder()
                    .inMemory()
                    .name("in-memory.test.realm")
                    .build(),
                mapper = RealmTestContentMapper,
                realmIdSelector = RealmIdSelector(
                    idProperty = "id",
                    idSelector = { it.id }
                )
            ),
            TransientLedgerHeadState(),
            isEnabled = true
        )
    }

    @Test
    fun test() {

        val scenario = ActivityScenario.launch(TestActivity::class.java)

        scenario.onActivity {

            box.store(TestContent(id = 12, content = "This is a content")).blockingAwait()

            box.getAll()
                .test()
                .assertValueAt(0, listOf())
                .assertValueAt(1, listOf(TestContent(id = 12, content = "This is a content")))


        }
    }
}
