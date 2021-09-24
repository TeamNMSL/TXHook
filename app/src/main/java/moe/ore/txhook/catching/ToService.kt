package moe.ore.txhook.catching

import moe.ore.txhook.helper.EMPTY_BYTE_ARRAY

data class ToService(
    val fromSource: FromSource,
    val uin: Long,
    val seq: Int,
    val cmd: String,
    val buffer: ByteArray,
    val time: Long,
    val sessionId: ByteArray,
): PacketService(false, true) {
    var encodeType: Byte = 0
    var packetType: Int = 0

    var firstToken: ByteArray = EMPTY_BYTE_ARRAY
    var secondToken: ByteArray = EMPTY_BYTE_ARRAY
}
