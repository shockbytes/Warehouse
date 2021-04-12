package at.shockbytes.warehouse.realm.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RealmTestContent(
    @PrimaryKey var id: Int = -1,
    var content: String = ""
): RealmObject() {

    fun toTestContent(): TestContent {
        return TestContent(id, content)
    }
}
