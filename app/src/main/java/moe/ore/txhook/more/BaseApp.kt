package moe.ore.txhook.more

import android.app.Activity
import android.app.Application
import android.content.SharedPreferences
import android.os.Bundle
import com.haoge.easyandroid.EasyAndroid
import com.haoge.easyandroid.easy.*
import com.xuexiang.xui.XUI

val logger: EasyLog by lazy { EasyLog.DEFAULT } // 可直接调用式logger
val toast: EasyToast by lazy { EasyToast.DEFAULT }
lateinit var config: Config

open class BaseApp: Application() {
    open fun isDebug(): Boolean = false
    open fun needLog(): Boolean = isDebug()

    override fun onCreate() {
        super.onCreate()

        EasyAndroid.init(this) // 初始化easy android
        XUI.init(this) // 初始化xui

        EasyLog.DEFAULT.enable = needLog() // 是否允许打印日志

        config = EasySharedPreferences.load(Config::class.java)

        registerActivityLifecycleCallbacks(ActivityLifeListener)
    }
}

@PreferenceRename("txhook_config")
class Config: PreferenceSupport() {
    var isFirst: Boolean = false

    var maxPacketSize: Int = 600

    var changeViewRefresh: Boolean = false
}

object ActivityLifeListener: Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        ActivityCollector.addActivity(activity)
    }

    override fun onActivityStarted(activity: Activity) {

    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {


    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {
        ActivityCollector.removeActivity(activity)
    }
}