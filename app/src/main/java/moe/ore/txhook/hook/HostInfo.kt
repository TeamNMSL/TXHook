package moe.ore.txhook.hook

import android.app.Application
import android.os.Environment
import java.io.File

object HostInfo {
    @JvmStatic
    lateinit var application: Application

    fun init(application: Application) {
        HostInfo.application = application
    }
}