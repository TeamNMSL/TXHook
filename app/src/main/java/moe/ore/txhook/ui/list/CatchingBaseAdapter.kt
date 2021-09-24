package moe.ore.txhook.ui.list

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.haoge.easyandroid.EasyAndroid
import com.xuexiang.xui.widget.imageview.RadiusImageView
import moe.ore.txhook.R
import moe.ore.txhook.catching.PacketService
import moe.ore.txhook.more.dateToString
import moe.ore.txhook.more.fileSizeToString
import java.util.*
import kotlin.collections.ArrayList

import com.xuexiang.xui.widget.textview.badge.Badge
import com.xuexiang.xui.widget.textview.badge.BadgeView
import moe.ore.txhook.more.config
import moe.ore.txhook.more.getColor


class CatchingBaseAdapter(
    private val services: ArrayList<PacketService>
): BaseAdapter() {
    fun setItemFirst(services: List<PacketService>) {
        this.services.clear()
        this.services.addAll(services.let {
            if (it.size >= config.maxPacketSize + 10) it.slice(0 .. config.maxPacketSize) else it
        })
    }

    override fun getCount(): Int = services.size

    override fun getItem(position: Int): PacketService {
        return services[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val root: View
        val viewHolder: ViewHolder
        if (convertView == null) {
            root = LayoutInflater.from(EasyAndroid.getApplicationContext()).inflate(R.layout.list_catching_packet, parent, false)
            val iconLayout: View = root.findViewById(R.id.icon_layout)
            viewHolder = ViewHolder(
                root = root,
                iconLayout = iconLayout,
                icon = root.findViewById(R.id.icon),
                cmd = root.findViewById(R.id.cmd_name),
                time = root.findViewById(R.id.operator_time),
                uin = root.findViewById(R.id.uin),
                seq = root.findViewById(R.id.seq),
                size = root.findViewById(R.id.size),
                badge = BadgeView(EasyAndroid.getApplicationContext()).bindTarget(iconLayout)
            )
            root.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
            root = convertView
        }

        val service = getItem(position)
        if (service.from) {
            val from = service.toFromService()
            viewHolder.cmd.text = from.cmd.let {
                if (it.length <= 25) it else it.substring(0, 25) + "..."
            }
            viewHolder.time.text = dateToString(Date(from.time), "HH:mm:ss")
            viewHolder.uin.text = from.uin.toString()
            viewHolder.seq.text = from.seq.toString()
            viewHolder.size.text = fileSizeToString(from.buffer.size.toLong())
            viewHolder.badge.apply {
                setBadgePadding(1f, true)
                // setBadgeTextSize(7.5f, true)
                // setGravityOffset(-8f, 0f, true)
                badgeText = " receive "
                badgeTextColor = Color.WHITE
                badgeGravity = Gravity.BOTTOM or Gravity.START
                badgeBackgroundColor = EasyAndroid.getColor(R.color.tx_receive)
                isShowShadow = false
            }
        } else {
            val to = service.toToService()
            viewHolder.cmd.text = to.cmd.let {
                if (it.length <= 25) it else it.substring(0, 25) + "..."
            }
            viewHolder.time.text = dateToString(Date(to.time), "HH:mm:ss")
            viewHolder.uin.text = to.uin.toString()
            viewHolder.seq.text = to.seq.toString()
            viewHolder.size.text = fileSizeToString(to.buffer.size.toLong())
            viewHolder.badge.apply {
                setBadgePadding(1f, true)
                // setBadgeTextSize(7.5f, true)
                // setGravityOffset(-8f, 0f, true)
                badgeText = " send "
                badgeTextColor = Color.WHITE
                badgeGravity = Gravity.BOTTOM or Gravity.START
                badgeBackgroundColor = EasyAndroid.getColor(R.color.tx_send)
                isShowShadow = false
            }
        }

        return root
    }

    data class ViewHolder(
        val root: View,
        val iconLayout: View,
        val icon: RadiusImageView,
        val cmd: TextView,
        val time: TextView,
        val uin: TextView,
        val seq: TextView,
        val size: TextView,
        val badge: Badge
    )
}