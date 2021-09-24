package moe.ore.txhook.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.haoge.easyandroid.EasyAndroid
import moe.ore.txhook.R
import moe.ore.txhook.catching.Data
import moe.ore.txhook.catching.DataType
import moe.ore.txhook.helper.toHexString
import kotlin.collections.ArrayList

class DataInfoAdapter(
    private val keyList: ArrayList<Data>
): BaseAdapter() {
    override fun getCount(): Int {
        return keyList.size
    }

    override fun getItem(position: Int): Data {
        return keyList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val root: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            root = LayoutInflater.from(EasyAndroid.getApplicationContext()).inflate(R.layout.list_data_info, parent, false)
            viewHolder = ViewHolder(
                root = root,
                name = root.findViewById(R.id.name),
                key = root.findViewById(R.id.key),
            )
            root.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
            root = convertView
        }

        val key = getItem(position)
        viewHolder.name.text = key.name

        when (key.type) {
            DataType.NUM -> {
                viewHolder.key.text = key.int.toString()
            }
            DataType.BYTES -> {
                viewHolder.key.text = key.key.toHexString()
            }
            else -> {
                viewHolder.key.text = String(key.key)
            }
        }

        return root
    }

    data class ViewHolder(
        val root: View,
        val name: TextView,
        val key: TextView
    )

}