package moe.ore.txhook.hook

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedBridge.log
import moe.ore.txhook.catching.FromSource
import moe.ore.txhook.helper.fastTry
import moe.ore.txhook.xposed.afterHook
import moe.ore.txhook.xposed.hookMethod
import java.io.File

object StartupHook {
    const val TX_FULL_TAG = "tx_full_tag"

    @JvmStatic
    var sec_static_stage_inited = false
    private var firstStageInit = false

    fun doInit(fromSource: FromSource, classLoader: ClassLoader?) {
        if (firstStageInit) return
        fastTry {
            val startup = afterHook(51) { param -> fastTry {
                val clz = param.thisObject.javaClass.classLoader!!
                    .loadClass("com.tencent.common.app.BaseApplicationImpl")
                val field = clz.declaredFields.first { it.type == clz }
                val app: Context? = field.get(null) as? Context
                log("hook注入成功！")

                if (app != null) {
                    execStartupInit(fromSource, app)
                }
            }.onFailure { log(it) } }

            val loadDex = classLoader!!.loadClass("com.tencent.mobileqq.startup.step.LoadDex")
            loadDex.declaredMethods
                .filter { it.returnType.equals(java.lang.Boolean.TYPE) && it.parameterTypes.isEmpty() }
                .forEach {
                    XposedBridge.hookMethod(it, startup)
                }
            firstStageInit = true
        }.onFailure { log(it) }

        hookMethod("com.tencent.mobileqq.qfix.QFixApplication", classLoader, "attachBaseContext", Context::class.java)?.before {
            deleteDirIfNecessaryNoThrow(it.args[0] as Context)
        }
    }

    private fun execStartupInit(fromSource: FromSource, ctx: Context) {
        log("进入可执行状态！！！！！")
        if (sec_static_stage_inited) return
        val classLoader: ClassLoader = ctx.classLoader ?: throw AssertionError("ERROR: classLoader == null")
        if ("true" == System.getProperty(TX_FULL_TAG)) {
            // reload join
            return
        }
        System.setProperty(TX_FULL_TAG, "true")
        injectClassLoader(classLoader)

        HostInfo.init(ctx as Application)
        Initiator.init(classLoader)

        MainHook(ctx, fromSource)

        sec_static_stage_inited = true
        deleteDirIfNecessaryNoThrow(ctx)
    }

    private fun injectClassLoader(classLoader: ClassLoader?) {
        requireNotNull(classLoader) { "classLoader == null" }
        try {
            val fParent = ClassLoader::class.java.declaredFields.first { it.name == "parent" }
            fParent.isAccessible = true
            val mine = StartupHook::class.java.classLoader!! // 获取我的loader
            var curr = fParent[mine] as ClassLoader? // 获取我的父loader
            if (curr == null) curr = XposedBridge::class.java.classLoader // 如果我的父loader不存在，那么就获取xposed的loader（解决虚拟空间运行问题）
            if (curr!!.javaClass.name != HybridClassLoader::class.java.name) {
                // fParent[mine] = HybridClassLoader(curr, classLoader)
                // 尝试修复bug
            }
        } catch (e: Exception) {
            log(e)
        }
    }

    private fun deleteDirIfNecessaryNoThrow(ctx: Context) {
        if (Build.VERSION.SDK_INT >= 24) deleteFile(File(ctx.dataDir, "app_qqprotect"))
        // if (File(ctx.filesDir, "qn_disable_hot_patch").exists()) deleteFile(ctx.getFileStreamPath("hotpatch"))
        // 禁用qq热更新
    }

    private fun deleteFile(file: File): Boolean {
        if (!file.exists()) return false
        if (file.isFile) {
            file.delete()
        } else if (file.isDirectory) {
            file.listFiles()?.forEach { deleteFile(it) }
            file.delete()
        }
        return !file.exists()
    }
}