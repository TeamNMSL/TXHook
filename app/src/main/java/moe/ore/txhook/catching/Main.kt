package moe.ore.txhook.catching

import moe.ore.txhook.helper.EMPTY_BYTE_ARRAY

abstract class PacketService(
    val from: Boolean,
    val to: Boolean
) {
    fun toToService() = this as ToService

    fun toFromService() = this as FromService
}

enum class FromSource(val packetName: String) {
    MOBILE_QQ(""),
    QQ_LITE(""),
    TIM("")
}

data class Data(
    val name: String,
    val type: DataType = DataType.NUM,
    val key: ByteArray = EMPTY_BYTE_ARRAY,
    val int: Long = 0
)

enum class DataType {
    NUM,
    BYTES,
    STR
}