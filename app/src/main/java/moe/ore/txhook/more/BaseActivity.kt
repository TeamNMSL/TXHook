package moe.ore.txhook.more

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.haoge.easyandroid.easy.EasyPermissions
import com.haoge.easyandroid.easy.PermissionAlwaysDenyNotifier
import com.xuexiang.xui.XUI
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import moe.ore.txhook.R
import kotlin.system.exitProcess

open class BaseActivity: AppCompatActivity() {
    open fun needInitTheme(): Boolean = false // 是否动态初始化主题
    open fun requiredPermission(): Array<String> = arrayOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        if(needInitTheme()) {
            XUI.initTheme(this)
        }

        XUI.initFontStyle("fonts/Mono.ttf")

        super.onCreate(savedInstanceState)

        // ActivityCollector.addActivity(this)
    }

    /**
     * 当权限全部拥有时执行block
     */
    protected fun requestPermission(block: (() -> Unit)? = null) {
        val permissions = requiredPermission()
        if (!Permissions.hasPermission(this, *permissions)) {
            EasyPermissions.create(*permissions).rational { permission, chain ->
                val alert = AlertDialog.Builder(this).setTitle("权限申请说明").setMessage("应用需要此权限：\n$permission")
                    .setNegativeButton("拒绝") { _, _ -> exitProcess(1) }
                    .setPositiveButton("同意") { _, _ -> chain.process() }
                    .create()
                alert.setOnShowListener {
                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.tx_appbar_color))
                    alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
                }
                alert.show()
                return@rational true
            }.callback {
                requestPermission(block)
            }.alwaysDenyNotifier(object : PermissionAlwaysDenyNotifier() {
                override fun onAlwaysDeny(permissions: Array<String>, activity: Activity) {
                    val alert = AlertDialog.Builder(activity).setTitle("权限申请提醒").setMessage("以下部分权限已被默认拒绝，请前往设置页将其打开:\n\n")
                        .setPositiveButton("确定") { _, _ -> goSetting(activity) }
                        .setNegativeButton("退出") { _, _ -> exitProcess(1) }
                        .create()
                    alert.setOnShowListener {
                        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(
                            R.color.tx_appbar_color))
                        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
                    }
                    alert.show()
                }
            }).request(this)
        } else {
            block?.invoke()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onDestroy() {
        super.onDestroy()
        // ActivityCollector.removeActivity(this)
    }
}