package moe.ore.txhook.catching

data class FromService(
    val fromSource: FromSource,
    val uin: Long,
    val seq: Int,
    val cmd: String,
    val buffer: ByteArray,
    val time: Long,
    val sessionId: ByteArray,
): PacketService(true, false) {
    var encodeType: Byte = 0
    var packetType: Int = 0


}
