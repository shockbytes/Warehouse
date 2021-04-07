package at.shockbytes.warehouse.ledger

import org.apache.commons.codec.digest.DigestUtils

data class LedgerBlock<E>(
    val previousHash: String,
    val data: BoxOperation<E>,
) {

    val hash: String = DigestUtils.sha256Hex((previousHash + data).toByteArray())
}

