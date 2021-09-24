package moe.ore.txhook.datas

import android.os.Parcel
import android.os.Parcelable
import moe.ore.txhook.catching.FromSource
import moe.ore.txhook.helper.EMPTY_BYTE_ARRAY

class PacketInfoData() : Parcelable {
    constructor(parcel: Parcel) : this() {
        fromSource = FromSource.valueOf(parcel.readString()!!)
        uin = parcel.readLong()
        seq = parcel.readInt()
        cmd = parcel.readString()!!
        bufferSize = parcel.readInt()
        buffer = ByteArray(bufferSize).also {
            parcel.readByteArray(it)
        }
        time = parcel.readLong()
        sessionSize = parcel.readInt()
        sessionId = ByteArray(sessionSize).also {
            parcel.readByteArray(it)
        }
        encodeType = parcel.readByte()
        packetType = parcel.readInt()

        firstTokenSize = parcel.readInt()
        firstToken = ByteArray(firstTokenSize).also {
            parcel.readByteArray(it)
        }

        secondTokenSize = parcel.readInt()
        secondToken = ByteArray(secondTokenSize).also {
            parcel.readByteArray(it)
        }
    }

    var fromSource: FromSource = FromSource.MOBILE_QQ
    var uin: Long = 0
    var seq: Int = 0
    var cmd: String = ""
    var bufferSize: Int = 0
    var buffer: ByteArray = EMPTY_BYTE_ARRAY
    var time: Long = 0
    var sessionSize: Int = 0
    var sessionId: ByteArray = EMPTY_BYTE_ARRAY

    var encodeType: Byte = 0
    var packetType: Int = 0

    var firstTokenSize: Int = 0
    var firstToken: ByteArray = EMPTY_BYTE_ARRAY
    var secondTokenSize: Int = 0
    var secondToken: ByteArray = EMPTY_BYTE_ARRAY

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(fromSource.name)
        parcel.writeLong(uin)
        parcel.writeInt(seq)
        parcel.writeString(cmd)
        parcel.writeInt(bufferSize)
        parcel.writeByteArray(buffer)
        parcel.writeLong(time)
        parcel.writeInt(sessionSize)
        parcel.writeByteArray(sessionId)
        parcel.writeByte(encodeType)
        parcel.writeInt(packetType)
        parcel.writeInt(firstTokenSize)
        parcel.writeByteArray(firstToken)
        parcel.writeInt(secondTokenSize)
        parcel.writeByteArray(secondToken)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PacketInfoData> {
        override fun createFromParcel(parcel: Parcel): PacketInfoData {
            return PacketInfoData(parcel)
        }

        override fun newArray(size: Int): Array<PacketInfoData?> {
            return arrayOfNulls(size)
        }
    }
}