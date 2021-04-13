package at.shockbytes.warehouse.realm

import io.realm.RealmQuery
import java.util.Date

data class RealmIdSelector<I, ID>(
    val idProperty: String,
    val idSelector: (I) -> ID
) {

    fun equalToByValue(realmQuery: RealmQuery<I>, value: I): RealmQuery<I> {
        return equalToById(realmQuery, idSelector(value))
    }

    fun <ID> equalToById(realmQuery: RealmQuery<I>, id: ID): RealmQuery<I> {
        return resolveQuery(realmQuery, id)
    }

    private fun <ID> resolveQuery(realmQuery: RealmQuery<I>, id: ID): RealmQuery<I> {
        return when (id) {
            is Int -> realmQuery.equalTo(idProperty, id)
            is Double -> realmQuery.equalTo(idProperty, id)
            is Long -> realmQuery.equalTo(idProperty, id)
            is Short -> realmQuery.equalTo(idProperty, id)
            is String -> realmQuery.equalTo(idProperty, id)
            is Byte -> realmQuery.equalTo(idProperty, id)
            is Date -> realmQuery.equalTo(idProperty, id)
            else -> throw IllegalStateException("")
        }
    }
}
