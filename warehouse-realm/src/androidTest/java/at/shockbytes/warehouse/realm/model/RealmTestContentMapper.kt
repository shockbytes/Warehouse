package at.shockbytes.warehouse.realm.model

import at.shockbytes.warehouse.Mapper


object RealmTestContentMapper : Mapper<RealmTestContent, TestContent>() {

    override fun mapTo(data: RealmTestContent): TestContent {
        return data.toTestContent()
    }

    override fun mapFrom(data: TestContent): RealmTestContent {
        return RealmTestContent(
            id = data.id,
            content = data.content
        )
    }
}
