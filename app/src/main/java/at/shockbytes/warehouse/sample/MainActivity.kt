package at.shockbytes.warehouse.sample

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import at.shockbytes.warehouse.IdentityMapper
import at.shockbytes.warehouse.R
import at.shockbytes.warehouse.Warehouse
import at.shockbytes.warehouse.WarehouseConfiguration
import at.shockbytes.warehouse.box.file.FileBox
import at.shockbytes.warehouse.box.file.GsonFileSerializer
import at.shockbytes.warehouse.box.memory.InMemoryBox
import at.shockbytes.warehouse.box.log.LogBox
import at.shockbytes.warehouse.realm.RealmBox
import at.shockbytes.warehouse.sample.realm.RealmMessageMapper
import at.shockbytes.warehouse.truck.BatchTruck
import at.shockbytes.warehouse.truck.SingleCargoTruck
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.realm.Realm
import io.realm.RealmConfiguration
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()

    private lateinit var warehouse: Warehouse<Message>

    private val config: RealmConfiguration by lazy {
        RealmConfiguration.Builder()
            .name("messages.realm")
            .schemaVersion(1L)
            .deleteRealmIfMigrationNeeded()
            .allowQueriesOnUiThread(true)
            .allowWritesOnUiThread(true)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Timber.plant(Timber.DebugTree())
        Realm.init(this)

        warehouse = Warehouse(
            boxes = listOf(
                LogBox.withTag("LogBox"),
                RealmBox.fromRealm(config, mapper = RealmMessageMapper, idProperty = "id", idSelector = { it.id }),
                InMemoryBox.default(),
                FileBox.fromContext(
                    applicationContext,
                    fileName = "filename.json",
                    mapper = IdentityMapper(),
                    idSelector = { it.id },
                    fileSerializer = GsonFileSerializer()
                )
                /*
                FirebaseBox.fromDatabase(
                    FirebaseDatabase.getInstance().reference.database,
                    reference = "/test",
                    idSelector = { it.id }
                )
                 */
            ),
            trucks = listOf(
                SingleCargoTruck { message ->
                    showToast(message.toString())
                },
                BatchTruck(batchSize = 2) { messages ->
                    showToast("$messages ready to be processed!")
                }
            ),
            WarehouseConfiguration(leaderBox = InMemoryBox.NAME)
        )

        store {
            update {
                getAll()
            }
        }
    }

    private fun update(onComplete: () -> Unit) {

        val msg = Message("random-id-1", "Oh, it's me!", "This is a message")

        warehouse.update(msg)
            .subscribe({
                Timber.e("BOX: Successfully updated all boxes!")
                onComplete()
            }, { throwable ->
                Timber.e(throwable)
            })
            .addTo(compositeDisposable)
    }

    private fun store(onComplete: () -> Unit) {
        warehouse.store(Message("random-id-1", "You", "This is a message"))
            .subscribe({
                Timber.e("BOX: Successfully stored in all boxes!")
                onComplete()
            }, { throwable ->
                Timber.e(throwable)
            })
            .addTo(compositeDisposable)
    }

    private fun getAll() {

        warehouse.getAllFor<RealmBox<*, Message>>()
            .subscribe({ messages ->
                Timber.e("Get all!")
                Timber.e(messages.toString())
            }, { throwable ->
                Timber.e(throwable)
            })
            .addTo(compositeDisposable)

        // warehouse.getAllForClass(RealmBox::class.java)

        /*
        warehouse.getAll { box ->
            box is RealmBox<*, *>
        }
        */

        // warehouse.getAll()
    }

    private fun getSingleElementForId(id: String) {

        warehouse["id"].subscribe { messages ->
            showToast("${messages.size} loaded for id")
        }
    }


    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}
