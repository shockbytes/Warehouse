package at.shockbytes.warehouse.firebase

import com.google.android.gms.tasks.Task
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.subjects.Subject

fun <T, K> Subject<List<T>>.fromFirebase(
    dbRef: DatabaseReference,
    clazz: Class<T>,
    changedChildKeySelector: (T) -> K,
    cancelHandler: ((DatabaseError) -> Unit)? = null
) {
    dbRef.listen(this, clazz, changedChildKeySelector, cancelHandler)
}

inline fun <T, K> DatabaseReference.listen(
    relay: Subject<List<T>>,
    clazz: Class<T>,
    crossinline changedChildKeySelector: (T) -> K,
    noinline cancelHandler: ((DatabaseError) -> Unit)? = null
) {

    val cache = mutableListOf<T>()

    this.addChildEventListener(object : ChildEventListener {

        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

            dataSnapshot.getValue(clazz)?.let { value ->
                cache.add(value)
                relay.onNext(cache)
            }
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

            dataSnapshot.getValue(clazz)?.let { value ->

                val changedValueSelector = changedChildKeySelector(value)
                val index = cache.indexOfFirst { v ->
                    changedValueSelector == changedChildKeySelector(v)
                }

                if (index > -1) {
                    cache[index] = value
                    relay.onNext(cache)
                } else {
                    cancelHandler?.invoke(
                        DatabaseError.fromException(
                            IndexOutOfBoundsException("Could not find changed index of value $value")
                        )
                    )
                }
            }
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

            dataSnapshot.getValue(clazz)?.let { value ->
                cache.remove(value)
                relay.onNext(cache)
            }
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) = Unit

        override fun onCancelled(databaseError: DatabaseError) {
            cancelHandler?.invoke(databaseError)
        }
    })
}

fun <T> FirebaseDatabase.insertValue(
    reference: String,
    value: T
) {
    getReference(reference).push().setValue(value)
}

fun <T> FirebaseDatabase.updateValue(reference: String, childId: String, value: T): Completable {
    return getReference(reference)
        .child(childId)
        .setValue(value)
        .toCompletable()
}

fun FirebaseDatabase.removeChildValue(reference: String, childId: String): Completable {
    return getReference(reference)
        .child(childId)
        .removeValue()
        .toCompletable()
}

fun FirebaseDatabase.removeReference(reference: String): Completable {
    return getReference(reference)
        .removeValue()
        .toCompletable()
}

private fun <T> Task<T>.toCompletable(): Completable {
    return Completable.create { emitter ->
        this
            .addOnSuccessListener {
                emitter.onComplete()
            }
            .addOnFailureListener { exception ->
                emitter.onError(exception)
            }
    }
}
