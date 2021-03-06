package at.shockbytes.warehouse.sample

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.shockbytes.warehouse.R
import at.shockbytes.warehouse.Warehouse
import at.shockbytes.warehouse.WarehouseConfiguration
import at.shockbytes.warehouse.box.Box
import at.shockbytes.warehouse.box.BoxId
import at.shockbytes.warehouse.box.log.LogBoxEngine
import at.shockbytes.warehouse.box.memory.InMemoryBoxEngine
import at.shockbytes.warehouse.firebase.FirebaseBoxEngine
import at.shockbytes.warehouse.firebase.FirebaseBoxEngineConfiguration
import at.shockbytes.warehouse.ledger.Ledger
import at.shockbytes.warehouse.realm.RealmBoxEngine
import at.shockbytes.warehouse.realm.RealmIdSelector
import at.shockbytes.warehouse.sample.firebase.FirebaseMessageMapper
import at.shockbytes.warehouse.sample.realm.RealmMessageMapper
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.realm.Realm
import io.realm.RealmConfiguration
import timber.log.Timber
import java.io.File
import java.lang.Math.random
import kotlin.math.absoluteValue

class MainActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()

    private lateinit var warehouse: Warehouse<Message>

    private val rvLedger: RecyclerView
        get() = findViewById(R.id.rv_ledger)

    private val rvContent: RecyclerView
        get() = findViewById(R.id.rv_box_content)

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

        setupWarehouse()

        setupViews()
    }

    private fun setupWarehouse() {
        val sharedLedger = Ledger.inMemory<Message>()

        /*
        val sharedLedger = Ledger.fromEngine<Message>(
            PersistentLedgerEngine(
                chain = FileBasedPersistentLedgerSource(
                    file = filesDir.child("chain.lef"),
                ),
                head = FileBasedPersistentLedgerSource(
                    file = filesDir.child("head.lef"),
                ),
                mapper = PersistentLedgerMessageMapper()
            )
        )
        */

        warehouse = Warehouse.new(
            boxes = listOf(
                Box.defaultFrom(
                    RealmBoxEngine.fromRealm(
                        config,
                        mapper = RealmMessageMapper,
                        realmIdSelector = RealmIdSelector(
                            idProperty = "id",
                            idSelector = { it.id }
                        )
                    ),
                ),
                Box.defaultFrom(
                    LogBoxEngine.withTag("LogBox"),
                ),
                Box.defaultFrom(
                    InMemoryBoxEngine.default(),
                ),
                Box.defaultFrom(
                    FirebaseBoxEngine.fromDatabase(
                        config = FirebaseBoxEngineConfiguration(
                            database = FirebaseDatabase.getInstance().reference.database,
                            reference = "/messages",
                            id = BoxId.of("fb-messages")
                        ),
                        cancelHandler = { error -> Timber.e(error.toException()) },
                        idSelector = { it.id },
                        mapper = FirebaseMessageMapper
                    ),
                )
            ),
            sharedLedger,
            WarehouseConfiguration(leaderBoxId = BoxId.of("realm-android"))
        )

        sharedLedger.onLedgerEvents()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { block ->
                (rvLedger.adapter as LedgerAdapter).apply {
                    add(block)
                    rvLedger.smoothScrollToPosition(itemCount - 1)
                }
            }
    }

    private fun setupViews() {
        rvLedger.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = LedgerAdapter(mutableListOf())
        }

        rvContent.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = ContentAdapter(mutableListOf())
        }

        warehouse.getAll()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({ messages ->
                (rvContent.adapter as ContentAdapter).apply {
                    setData(messages)
                    if (messages.isNotEmpty()) {
                        rvContent.smoothScrollToPosition(messages.count() - 1)
                    }
                }
            }, { throwable ->
                Timber.e(throwable)
            })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_create -> {
                val id = "id_${random().absoluteValue.times(1_000_000).toInt()}"
                store(Message(id, "You", "This is a message")) {
                    runOnUiThread {
                        showToast("Message with $id stored")
                    }
                }
            }
            R.id.menu_delete -> {
                val latest = (rvContent.adapter as ContentAdapter).lastItem()
                delete(latest) {
                    runOnUiThread {
                        showToast("Message with ${latest.id} deleted")
                    }
                }
            }
            R.id.menu_reset -> {

                warehouse.reset().subscribe {
                    runOnUiThread {
                        showToast("All boxes reset")
                    }
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun update(onComplete: () -> Unit) {

        val msg = Message("random-id-1", "Oh, it's me!", "This is a message")

        warehouse.update(msg)
            .subscribe(
                {
                    Timber.e("BOX: Successfully updated all boxes!")
                    onComplete()
                },
                { throwable ->
                    Timber.e(throwable)
                }
            )
            .addTo(compositeDisposable)
    }

    private fun store(
        message: Message,
        onComplete: () -> Unit
    ) {
        warehouse.store(message)
            .subscribe(
                {
                    Timber.e("BOX: Successfully stored in all boxes!")
                    onComplete()
                },
                { throwable ->
                    Timber.e(throwable)
                }
            )
            .addTo(compositeDisposable)
    }

    private fun delete(
        message: Message,
        onComplete: () -> Unit
    ) {
        warehouse.delete(message)
            .subscribe(
                {
                    Timber.e("BOX: Successfully deleted in all boxes!")
                    onComplete()
                },
                { throwable ->
                    Timber.e(throwable)
                }
            )
            .addTo(compositeDisposable)
    }

    private fun getAll() {

        warehouse.getAll()
            .subscribe(
                { messages ->
                    Timber.e("Get all!")
                    // Timber.e(messages.toString())
                },
                { throwable ->
                    Timber.e(throwable)
                }
            )
            .addTo(compositeDisposable)
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}
