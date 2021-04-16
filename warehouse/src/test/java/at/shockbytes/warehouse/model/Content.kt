package at.shockbytes.warehouse.model

import kotlinx.serialization.Serializable

@Serializable
data class Content(
    val id: String,
    val content: String
)
