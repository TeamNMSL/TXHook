package moe.ore.txhook.more

import com.haoge.easyandroid.easy.EasyToast
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorRes
import com.haoge.easyandroid.EasyAndroid

import com.xuexiang.xui.widget.popupwindow.bar.CookieBar
import moe.ore.txhook.R
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

@JvmOverloads
fun Context.toast(message: String, type: Int = Toast.LENGTH_SHORT) = Toast.makeText(this, message, type).show()

fun Context.copyText(msg: String) {
    val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val mClipData = ClipData.newPlainText("Label", msg)
    cm.setPrimaryClip(mClipData)
    toast("复制成功")
}

fun EasyAndroid.getColor(@ColorRes id: Int): Int {
    return getApplicationContext().getColor(id)
}

fun EasyToast.show(input: CharSequence) {
    show(input.toString())
}

object CookieBars {
    fun cookieBar(context: Activity?, title: String, msg: String, button: String, listener: View.OnClickListener? = null) {
        CookieBar.builder(context)
            .setTitle(title)
            .setMessage(msg)
            .setDuration(3000)
            .setBackgroundColor(R.color.tx_cookiebar)
            .setActionColor(R.color.white)
            .setTitleColor(R.color.white)
            .setAction(button, listener)
            .show()
    }
}

fun dateToString(data: Date, formatType: String): String {
    return SimpleDateFormat(formatType, Locale.ROOT).format(data)
}

fun fileSizeToString(size: Long): String {
    val kiloByte = size / 1024
    if (kiloByte < 1) {
        return size.toString() + "b"
    }
    val megaByte = kiloByte / 1024
    if (megaByte < 1) {
        val result1 = BigDecimal(kiloByte.toString())
        return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString().toString() + "kb"
    }
    val gigaByte = megaByte / 1024
    if (gigaByte < 1) {
        val result2 = BigDecimal(megaByte.toString())
        return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString().toString() + "mb"
    }
    val teraBytes = gigaByte / 1024
    if (teraBytes < 1) {
        val result3 = BigDecimal(gigaByte.toString())
        return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString().toString() + "gb"
    }
    val result4 = BigDecimal(teraBytes)
    return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString().toString() + "tb"
}