package at.shockbytes.warehouse.ledger

import kotlinx.serialization.Serializable
import org.apache.commons.codec.digest.DigestUtils

@Serializable
data class LedgerBlock<E>(
    val previousHash: String,
    val data: BoxOperation<E>,
) {

    val hash: Hash = Hash(DigestUtils.sha256Hex((previousHash + data).toByteArray()))
}
