package moe.ore.txhook.hook

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage
import moe.ore.txhook.catching.FromSource

class HookEntry: IXposedHookLoadPackage {
    companion object {
        const val PACKAGE_NAME_QQ = "com.tencent.mobileqq"
        const val PACKAGE_NAME_QQ_INTERNATIONAL = "com.tencent.mobileqqi"
        const val PACKAGE_NAME_QQ_LITE = "com.tencent.qqlite"
        const val PACKAGE_NAME_TIM = "com.tencent.tim"
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        when(lpparam.packageName) {
            PACKAGE_NAME_QQ -> { // 普通QQHook
                StartupHook.doInit(FromSource.MOBILE_QQ, lpparam.classLoader)
            }
        }


    }
}