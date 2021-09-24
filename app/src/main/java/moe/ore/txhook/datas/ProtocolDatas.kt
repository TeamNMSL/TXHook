package moe.ore.txhook.datas

import android.os.Environment
import de.robv.android.xposed.XposedBridge.log
import moe.ore.tars.TarsBase
import moe.ore.tars.TarsInputStream
import moe.ore.tars.TarsOutputStream
import moe.ore.txhook.catching.FromService
import moe.ore.txhook.catching.FromSource
import moe.ore.txhook.catching.PacketService
import moe.ore.txhook.catching.ToService
import moe.ore.txhook.helper.EMPTY_BYTE_ARRAY
import moe.ore.txhook.helper.ThreadManager
import moe.ore.txhook.helper.fastTry
import moe.ore.util.FileUtil
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

object ProtocolDatas {
    private var lastCheckTime: Long = 0

    private val dataPath = Environment.getExternalStorageDirectory().absolutePath + "/TXHook/"

    private val PATH_VALUE = dataPath + "values/"

    private val PATH_APPID = PATH_VALUE + "appid"
    private val PATH_MAX_PACKAGE_SIZE = PATH_VALUE + "package_size"
    private val PATH_SERVICE = dataPath + "service/"

    fun setId(name: String, key: ByteArray?) {
        fastTry {
            FileUtil.saveFile(PATH_VALUE + name, key ?: EMPTY_BYTE_ARRAY)
        }.onFailure { it.printStackTrace() }
    }

    fun getId(name: String): ByteArray {
        return File(PATH_VALUE + name).also {
            if (!it.exists()) setId(name, EMPTY_BYTE_ARRAY)
        }.readBytes()
    }

    fun setNetType(key: Int) = setId("net_type", key.toString().toByteArray())

    fun getNetType(): Int = String(getId("net_type")).let {
        if (it.isNotEmpty()) it.toInt() else -1
    }

    fun setAndroidId(key: String?) = setId("aid", key?.toByteArray())

    fun getAndroidId(): String = String(getId("aid"))

    fun setSsid(key: String?) = setId("ssid", key?.toByteArray())

    fun getSsid(): String = String(getId("ssid"))

    fun setBSsid(key: String?) = setId("bssid", key?.toByteArray())

    fun getBSsid(): String = String(getId("bssid"))

    fun setMac(key: String?) = setId("mac", key?.toByteArray())

    fun getMac(): String = String(getId("mac"))

    fun setReleaseTime(key: String?) = setId("release_time", key?.toByteArray())

    fun getReleaseTime(): String = String(getId("release_time"))

    fun setSVNVersion(key: String?) = setId("svn_version", key?.toByteArray())

    fun getSVNVersion(): String = String(getId("svn_version"))

    fun setWloginLogDir(key: String?) = setId("WloginLogDir", key?.toByteArray())

    fun getWloginLogDir(): String = String(getId("WloginLogDir"))

    fun setKsid(key: ByteArray?) = setId("ksid", key)

    fun getKsid(): ByteArray = getId("ksid")

    fun setGUID(key: ByteArray?) = setId("guid", key)

    fun getGUID(): ByteArray = getId("guid")

    /**
     * appid的设置与获取
     */
    fun setAppId(appId: Int) {
        FileUtil.saveFile(PATH_APPID, appId.toString())
    }

    fun getAppId(): Int {
        return File(PATH_APPID).also {
            if (!it.exists()) setAppId(0)
        }.readText().toInt()
    }

    fun setPubKey(key: ByteArray?) = setId("pubkey", key)

    fun getPubKey(): ByteArray = getId("pubkey")

    fun setQIMEI(key: ByteArray?) = setId("qimei", key)

    fun getQIMEI(): ByteArray = getId("qimei")

    fun setShareKey(key: ByteArray?) = setId("sharekey", key)

    fun getShareKey(): ByteArray = getId("sharekey")

    fun setMaxPackageSize(size: Int) {
        FileUtil.saveFile(PATH_MAX_PACKAGE_SIZE, size.toString())
    }

    fun getMaxPackageSize(): Int {
        return File(PATH_MAX_PACKAGE_SIZE).also {
            if (!it.exists()) setMaxPackageSize(0)
        }.readText().toInt()
    }

    fun addService(service: PacketService) {
        if (service.from) {
            val from = service.toFromService()
            FileUtil.saveFile("$PATH_SERVICE${from.seq}.from", Packet().apply {
                this.fromSource = from.fromSource
                this.uin = from.uin
                this.seq = from.seq
                this.cmd = from.cmd
                this.buffer = from.buffer
                this.time = from.time
                this.sessionId = from.sessionId
            }.toByteArray())
            setId("last_uin", from.uin.toString().toByteArray())
        } else {
            val to = service.toToService()
            FileUtil.saveFile("$PATH_SERVICE${to.seq}.to", Packet().apply {
                this.fromSource = to.fromSource
                this.uin = to.uin
                this.seq = to.seq
                this.cmd = to.cmd
                this.buffer = to.buffer
                this.time = to.time
                this.sessionId = to.sessionId
            }.toByteArray())
            setId("last_uin", to.uin.toString().toByteArray())
        }
        ThreadManager.getInstance(0).addTask {
            // 创建线程任务，清理多余的包
            if (lastCheckTime + 1000 * 15 <= System.currentTimeMillis()) {
                val servicesFile = File(PATH_SERVICE)
                if (servicesFile.exists()) {
                    val serviceFiles = servicesFile.listFiles()
                    if (serviceFiles?.size ?: 0 > 1000) {
                        fastTry {
                            val sortList = serviceFiles!!.toMutableList()

                            Collections.sort(sortList, Comparator { f1, f2 ->
                                val f1t = f1.lastModified()
                                val f2t = f2.lastModified()
                                // val f1Seq = f1.name.split(".")[0].toInt()
                                // val f2Seq = f2.name.split(".")[0].toInt()
                                return@Comparator if (f1t > f2t) -1 else if (f2t > f1t) 1 else 0
                            })

                            for (i in 1000..sortList.size) {
                                sortList[i].delete()
                            }
                        }.onFailure { log(it) }
                    }
                }
                lastCheckTime = System.currentTimeMillis()
            }
        }
    }

    fun clearService() {
        val servicesFile = File(PATH_SERVICE)
        if (servicesFile.exists()) {
            servicesFile.deleteRecursively()
        }
    }

    fun getServices(): List<PacketService> {
        val servicesFile = File(PATH_SERVICE)
        if (servicesFile.exists()) {
            val serviceFiles = servicesFile.listFiles()
            fastTry {
                val result = ArrayList<PacketService>()
                val sortList = serviceFiles!!.toMutableList()

                Collections.sort(sortList, Comparator { f1, f2 ->
                    val f1t = f1.lastModified()
                    val f2t = f2.lastModified()
                    // val f1Seq = f1.name.split(".")[0].toInt()
                    // val f2Seq = f2.name.split(".")[0].toInt()
                    return@Comparator if (f1t > f2t) -1 else if (f2t > f1t) 1 else 0
                })

                sortList.forEach {
                    val packet = Packet().parse(it.readBytes())
                    if (it.name.endsWith("to")) {
                        result.add(ToService(
                            packet.fromSource,
                            packet.uin,
                            packet.seq,
                            packet.cmd,
                            packet.buffer,
                            packet.time,
                            packet.sessionId
                        ))
                    } else {
                        result.add(FromService(
                            packet.fromSource,
                            packet.uin,
                            packet.seq,
                            packet.cmd,
                            packet.buffer,
                            packet.time,
                            packet.sessionId
                        ))
                    }
                }
                return result
            }.onFailure { it.printStackTrace() }
        }
        return emptyList()
    }

}

class Packet: TarsBase() {
    var fromSource: FromSource = FromSource.MOBILE_QQ
    var uin: Long = 0
    var seq: Int = 0
    var cmd: String = ""
    var buffer: ByteArray = EMPTY_BYTE_ARRAY
    var time: Long = 0
    var sessionId: ByteArray = EMPTY_BYTE_ARRAY

    var encodeType: Byte = 0
    var packetType: Int = 0

    var firstToken: ByteArray = EMPTY_BYTE_ARRAY
    var secondToken: ByteArray = EMPTY_BYTE_ARRAY

    override fun writeTo(output: TarsOutputStream) {
        output.write(fromSource.name, 0)
        output.write(uin, 1)
        output.write(seq, 2)
        output.write(cmd, 3)
        output.write(buffer, 4)
        output.write(time, 5)
        output.write(sessionId, 6)
        // output.write(fromSource, 1)
    }

    override fun readFrom(input: TarsInputStream) {
        fromSource = FromSource.valueOf(input.readString(0, true))
        uin = input.read(uin, 1, false)
        seq = input.read(seq, 2, false)
        cmd = input.read(cmd, 3, false)
        buffer = input.read(buffer, 4, false)
        time = input.read(time, 5, false)
        sessionId = input.read(sessionId, 6, false)
        // uin = input.read(uin, 1, false)
    }

    fun parse(byteArray: ByteArray): Packet {
        readFrom(TarsInputStream(byteArray))
        return this
    }
}
