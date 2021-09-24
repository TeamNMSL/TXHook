package moe.ore.txhook.more

import android.app.Activity
import com.haoge.easyandroid.easy.EasyPermissions

object Permissions {
    // 判断是否拥有权限
    fun hasPermission(activity: Activity, vararg permissions: String): Boolean {
        permissions.forEach {
            if(!EasyPermissions.isPermissionGranted(it, activity)) {
                return false
            }
        }
        return true
    }

}