package moe.ore.txhook.hook

import android.content.Context
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers
import kotlinx.io.core.discardExact
import kotlinx.io.core.readBytes
import moe.ore.txhook.catching.FromService
import moe.ore.txhook.catching.FromSource
import moe.ore.txhook.catching.ToService
import moe.ore.txhook.datas.ProtocolDatas
import moe.ore.txhook.hook.Initiator.load
import moe.ore.txhook.xposed.callMethod
import moe.ore.txhook.xposed.hookMethod
import moe.ore.txhook.helper.*


object MainHook {
    private val desc = "一个一劳永逸的工具罢了。TXHook群里面有个内鬼，这个人不是很喜欢我，我也不想理他，谢谢你，陌生人！想来想去，过过往往，总是被针对，仔细想想其实原因在我，但是我并无不妥！！"

    private var isInit: Boolean = false
    // private val bytesClz = Class.forName("[B")
    private val codecClazz = load("com.tencent.qphone.base.util.CodecWarpper")!!
    // private val fromService = load("com.tencent.qphone.base.remote.FromServiceMsg")!!
    private val ecdhClz = load("oicq.wlogin_sdk.tools.EcdhCrypt")
    private val oicqUtilClz = load("oicq.wlogin_sdk.tools.util")

    private fun input(ctx: Context, fromSource: FromSource) {
        hookFirst(fromSource)
        hookSendPacket(fromSource)
        hookData(ctx)
    }

    private fun hookData(ctx: Context) {
        ecdhClz.hookMethod("get_c_pub_key")?.after {
            ProtocolDatas.setPubKey(JavaCaster.castToBytes(it.result) ?: EMPTY_BYTE_ARRAY)
        }
        ecdhClz.hookMethod("set_c_pub_key")?.before {
            ProtocolDatas.setPubKey(JavaCaster.castToBytes(it.args[0]) ?: EMPTY_BYTE_ARRAY)
        }
        ecdhClz.hookMethod("set_g_share_key")?.before {
            ProtocolDatas.setShareKey(JavaCaster.castToBytes(it.args[0]) ?: EMPTY_BYTE_ARRAY)
        }
        ecdhClz.hookMethod("get_g_share_key")?.after {
            ProtocolDatas.setShareKey(JavaCaster.castToBytes(it.result) ?: EMPTY_BYTE_ARRAY)
        }

        codecClazz.hookMethod("setAccountKey")?.before { param ->
            /*
        uin: String,
        a1: ByteArray,
        a2: ByteArray,
        a3: ByteArray,
        d1: ByteArray,
        d2: ByteArray,
        s2: ByteArray,
        key: ByteArray,
        cookie: ByteArray,
        unknwon: String
             */
            if (param.args.size == 10) {
                val uin = (param.args[0] as? String) ?: ""
                val a1 = JavaCaster.castToBytes(param.args[1]) ?: EMPTY_BYTE_ARRAY
                val a2 = JavaCaster.castToBytes(param.args[2]) ?: EMPTY_BYTE_ARRAY
                val a3 = JavaCaster.castToBytes(param.args[3]) ?: EMPTY_BYTE_ARRAY
                val d1 = JavaCaster.castToBytes(param.args[4]) ?: EMPTY_BYTE_ARRAY
                val d2 = JavaCaster.castToBytes(param.args[5]) ?: EMPTY_BYTE_ARRAY
                val s2 = JavaCaster.castToBytes(param.args[6]) ?: EMPTY_BYTE_ARRAY
                val key = JavaCaster.castToBytes(param.args[7]) ?: EMPTY_BYTE_ARRAY
                val cookie = JavaCaster.castToBytes(param.args[8]) ?: EMPTY_BYTE_ARRAY
                ProtocolDatas.setId("$uin-AccountKey", newBuilder().apply {
                    writeStringWithShortLen(uin)
                    writeBytesWithShortLen(a1)
                    writeBytesWithShortLen(a2)
                    writeBytesWithShortLen(a3)
                    writeBytesWithShortLen(d1)
                    writeBytesWithShortLen(d2)
                    writeBytesWithShortLen(s2)
                    writeBytesWithShortLen(key)
                    writeBytesWithShortLen(cookie)
                }.toByteArray())
            }
        }

        hookExec(ctx)
    }

    private fun hookExec(ctx: Context) {
        ProtocolDatas.setQIMEI(JavaCaster.castToBytes(oicqUtilClz!!.callMethod("get_qimei", ctx)))
        ProtocolDatas.setGUID(JavaCaster.castToBytes(oicqUtilClz.callMethod("generateGuid", ctx)))
        ProtocolDatas.setKsid(JavaCaster.castToBytes(oicqUtilClz.callMethod("get_ksid", ctx)))
        ProtocolDatas.setKsid(JavaCaster.castToBytes(oicqUtilClz.callMethod("get_ksid", ctx)))
        ProtocolDatas.setWloginLogDir(oicqUtilClz.callMethod("getLogDir", ctx) as? String)
        ProtocolDatas.setSVNVersion(oicqUtilClz.callMethod("getSvnVersion") as? String)
        ProtocolDatas.setReleaseTime(oicqUtilClz.callMethod("get_release_time") as? String)
        ProtocolDatas.setMac( String(JavaCaster.castToBytes(oicqUtilClz.callMethod("get_mac_addr", ctx)) ?: EMPTY_BYTE_ARRAY) )
        ProtocolDatas.setBSsid( String(JavaCaster.castToBytes(oicqUtilClz.callMethod("get_bssid_addr", ctx)) ?: EMPTY_BYTE_ARRAY) )
        ProtocolDatas.setSsid( String(JavaCaster.castToBytes(oicqUtilClz.callMethod("get_ssid_addr", ctx)) ?: EMPTY_BYTE_ARRAY) )
        ProtocolDatas.setAndroidId( String(JavaCaster.castToBytes(oicqUtilClz.callMethod("get_android_id", ctx)) ?: EMPTY_BYTE_ARRAY) )
        ProtocolDatas.setNetType( oicqUtilClz.callMethod("get_network_type", ctx) as Int )
    }

    private fun hookReceivePacket(fromSource: FromSource, clazz: Class<*>) {
        clazz.hookMethod("onResponse")?.after { param ->
            val fromObject = param.args[1]

            val buf = JavaCaster.castToBytes(XposedHelpers.callMethod(fromObject, "getWupBuffer")) ?: EMPTY_BYTE_ARRAY
            val command = (XposedHelpers.callMethod(fromObject, "getServiceCmd") as? String) ?: ""
            val qq = (XposedHelpers.callMethod(fromObject, "getUin") as? String) ?: ""
            val ssoSeq = (XposedHelpers.callMethod(fromObject, "getRequestSsoSeq") as? Int) ?: 0
            val msgCookie = JavaCaster.castToBytes(XposedHelpers.callMethod(fromObject, "getMsgCookie")) ?: EMPTY_BYTE_ARRAY

            ProtocolDatas.addService(FromService(fromSource, qq.toLong(), ssoSeq, command, buf, System.currentTimeMillis(), msgCookie))
        }
    }

    private fun hookSendPacket(fromSource: FromSource) {
        codecClazz.hookMethod("encodeRequest")?.after { param ->
            val result = JavaCaster.castToBytes(param.result)
            when (param.args.size) {
                17 -> {
                    val seq = param.args[0] as Int
                    // val imei = (param.args[1] as String?) ?: ""
                    // val version = param.args[3] as String
                    val cmd = (param.args[5] as? String) ?: ""
                    val sessionId = (param.args[6] as ByteArray?) ?: EMPTY_BYTE_ARRAY
                    val uin  = param.args[9] as String
                    // val qImei = String((param.args[14] as ByteArray?) ?: EMPTY_BYTE_ARRAY)
                    val buffer = param.args[15] as ByteArray
                    // val out = param.result as ByteArray

                    val to = ToService(fromSource, uin.toLong(), seq, cmd, buffer, System.currentTimeMillis(), sessionId)

                    fastTry {
                        val reader = result.toByteReadPacket()
                        reader.discardExact(4) // 去掉4字节的长度
                        to.packetType = reader.readInt()
                        to.encodeType = reader.readByte()

                        // to.firstToken = reader.readBytes(reader.readInt() -  4)

                        reader.closeQuietly()
                    }

                    ProtocolDatas.addService(to)
                }
                14 -> {
                    val seq = param.args[0] as Int
                    // val imei = param.args[1] as String
                    // val version = param.args[3] as String
                    val cmd = (param.args[5] as? String) ?: ""
                    val sessionId = (param.args[6] as? ByteArray) ?: EMPTY_BYTE_ARRAY
                    val uin  = param.args[9] as String
                    // val qImei = String((param.args[14] as ByteArray?) ?: EMPTY_BYTE_ARRAY)
                    val buffer = param.args[12] as ByteArray
                    // val out = param.result as ByteArray

                    val to = ToService(fromSource, uin.toLong(), seq, cmd, buffer, System.currentTimeMillis(), sessionId)

                    fastTry {
                        val reader = result.toByteReadPacket()
                        reader.discardExact(4) // 去掉4字节的长度
                        to.packetType = reader.readInt()
                        to.encodeType = reader.readByte()

                        // to.firstToken = reader.readBytes(reader.readInt() -  4)

                        reader.closeQuietly()
                    }

                    ProtocolDatas.addService(to)
                }
                16 -> {
                    val seq = param.args[0] as Int
                    // val imei = param.args[1] as String
                    // val version = param.args[3] as String
                    val cmd = (param.args[5] as? String) ?: ""
                    val sessionId = (param.args[6] as? ByteArray) ?: EMPTY_BYTE_ARRAY
                    val uin  = param.args[9] as String
                    // val qImei = String((param.args[14] as ByteArray?) ?: EMPTY_BYTE_ARRAY)
                    val buffer = param.args[14] as ByteArray
                    // val out = param.result as ByteArray

                    val to = ToService(fromSource, uin.toLong(), seq, cmd, buffer, System.currentTimeMillis(), sessionId)

                    fastTry {
                        val reader = result.toByteReadPacket()
                        reader.discardExact(4) // 去掉4字节的长度
                        to.packetType = reader.readInt()
                        to.encodeType = reader.readByte()

                        // to.firstToken = reader.readBytes(reader.readInt() -  4)

                        reader.closeQuietly()
                    }

                    ProtocolDatas.addService(to)
                }
                else -> {
                    log("hook到了个不知道什么东西")
                }
            }
        }
    }

    private fun hookFirst(fromSource: FromSource) {
        // QQ 8.8.28开始修改了载入参数
        codecClazz.hookMethod("init")?.before {
            if (it.args.size >= 2) {
                it.args[1] = true // 强制打开调试模式
                // if (it.args.size >= 3) it.args[2] = true test version
                if (!isInit) {
                    val thisClass = it.thisObject.javaClass
                    // hookReceivePacket(thisClass, bytesClz, fromService)
                    hookReceivePacket(fromSource, thisClass)
                    isInit = true
                }
            }
        }?.after {
            // 【废弃】构建ws长连接作为数据交互
            ProtocolDatas.setAppId(codecClazz.callMethod("getAppid") as Int)
            ProtocolDatas.setMaxPackageSize(codecClazz.callMethod("getMaxPackageSize") as Int)
        }

        codecClazz.hookMethod("onReceData")?.after {
            if (!isInit) {
                hookReceivePacket(fromSource, it.thisObject.javaClass)
                isInit = true
            }
        }
    }

    operator fun invoke(ctx: Context, fromSource: FromSource) {
        fastTry {
            log("TXHook 开始载入，载入进行中！！！")
            input(ctx, fromSource)
        }.onFailure { log(it) }
    }
}

/*
Integer.TYPE, // seq 0
            String::class.java, // androidId / imei 1
            String::class.java, // 2
            String::class.java, // 3 apkVersion
            String::class.java, // 4
            String::class.java, // cmd 5
            bytesClz, // 6 sessionId/ msgCookie
            Integer.TYPE, // 7 appid
            Integer.TYPE, // 8 appid2
            String::class.java, // uin
            java.lang.Byte.TYPE, // 10
            java.lang.Byte.TYPE, // 11
            java.lang.Byte.TYPE, // 12
            bytesClz, // 13
            bytesClz, // 14 qimei
            bytesClz, // 15 buffer
            java.lang.Boolean.TYPE, // 16
 */