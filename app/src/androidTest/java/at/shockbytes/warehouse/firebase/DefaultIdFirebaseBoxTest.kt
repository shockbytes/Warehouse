package at.shockbytes.warehouse.firebase

import androidx.test.platform.app.InstrumentationRegistry
import at.shockbytes.warehouse.Mapper
import at.shockbytes.warehouse.box.Box
import at.shockbytes.warehouse.box.BoxId
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

class DefaultIdFirebaseBoxTest {

    data class Content(
        val firebaseId: String = "",
        val id: String = "",
        val content: String = ""
    )

    data class FirebaseContent(
        val firebaseId: String = "",
        val id: String = "",
        val content: String = ""
    ) : FirebaseStorable {
        override fun copyWithNewId(newId: String): FirebaseStorable = copy(firebaseId = newId)
    }

    class TestMapper : Mapper<FirebaseContent, Content>() {
        override fun mapTo(data: FirebaseContent): Content {
            return Content(data.firebaseId, data.id, data.content)
        }

        override fun mapFrom(data: Content): FirebaseContent {
            return FirebaseContent(data.firebaseId, data.id, data.content)
        }
    }

    private val firebaseBox = Box.defaultFrom(
        FirebaseBoxEngine.fromDatabase(
            config = FirebaseBoxEngineConfiguration(
                database = database,
                reference = "/content",
                useDefaultFirebaseId = true,
                id = BoxId.of("fb-content-test"),
            ),
            cancelHandler = { error -> println(error.toException()) },
            idSelector = { it.firebaseId },
            mapper = TestMapper(),
        )
    )

    @Before
    fun setup() {
        // Clear box for every run
        firebaseBox.reset().blockingAwait()
    }

    @Test
    fun storeInFirebase() {

        firebaseBox.store(Content("test", "content")).blockingGet()
        firebaseBox.store(Content("test1", "content")).blockingGet()

        firebaseBox.getAll()
            .test()
            .assertValue { content ->
                content.size == 2
            }
            .assertValue { content ->
                content[0].firebaseId.isNotEmpty() && content[1].firebaseId.isNotEmpty()
            }
    }

    @Test
    fun deleteFromFirebase() {

        val data = Content("test", "content")

        val stored = firebaseBox.store(data).blockingGet()

        firebaseBox.getAll()
            .test()
            .assertValueAt(0) { content ->
                content.size == 1
            }

        firebaseBox.delete(stored).blockingAwait()

        firebaseBox.getAll()
            .test()
            .assertValueAt(0) { content ->
                content.isEmpty()
            }
    }

    @Test
    fun updateEntityOnFirebase() {

        val data = Content("test", "content")
        val updated = data.copy(content = "this is an updated content")

        firebaseBox.store(data).blockingGet()
        firebaseBox.update(updated).blockingAwait()

        firebaseBox.getSingleElement("test")
            .test()
            .assertValue { value ->
                value == updated
            }
    }

    companion object {

        @JvmStatic
        private lateinit var database: FirebaseDatabase

        @BeforeClass
        @JvmStatic
        fun setupBeforeClass() {
            FirebaseApp.initializeApp(InstrumentationRegistry.getInstrumentation().targetContext)
            database = FirebaseDatabase.getInstance().reference.database
        }
    }
}
