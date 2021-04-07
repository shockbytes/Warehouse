package at.shockbytes.warehouse.sample

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import at.shockbytes.warehouse.IdentityMapper
import at.shockbytes.warehouse.R
import at.shockbytes.warehouse.Warehouse
import at.shockbytes.warehouse.WarehouseConfiguration
import at.shockbytes.warehouse.box.Box
import at.shockbytes.warehouse.box.memory.InMemoryBoxEngine
import at.shockbytes.warehouse.box.log.LogBoxEngine
import at.shockbytes.warehouse.firebase.FirebaseBoxEngine
import at.shockbytes.warehouse.ledger.Ledger
import at.shockbytes.warehouse.realm.RealmBoxEngine
import at.shockbytes.warehouse.sample.realm.RealmMessageMapper
import com.google.firebase.database.FirebaseDatabase
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

        val sharedLedger = Ledger.inMemory<Message>()

        warehouse = Warehouse(
            boxes = listOf(
                Box(
                    LogBoxEngine.withTag("LogBox"),
                    sharedLedger
                ),
                Box(
                    RealmBoxEngine.fromRealm(
                        config,
                        mapper = RealmMessageMapper,
                        idProperty = "id",
                        idSelector = { it.id }
                    ),
                    sharedLedger
                ),
                Box(
                    InMemoryBoxEngine.default(),
                    sharedLedger
                ),
                Box(
                    FirebaseBoxEngine.fromDatabase(
                        FirebaseDatabase.getInstance().reference.database,
                        reference = "/messages",
                        idSelector = { it.id },
                        cancelHandler = { error -> Timber.e(error.toException()) },
                        mapper = IdentityMapper()
                    ),
                    sharedLedger
                )
            ),
            WarehouseConfiguration(leaderBox = InMemoryBoxEngine.NAME)
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

        warehouse.getAll()
            .subscribe({ messages ->
                Timber.e("Get all!")
                Timber.e(messages.toString())
            }, { throwable ->
                Timber.e(throwable)
            })
            .addTo(compositeDisposable)
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
