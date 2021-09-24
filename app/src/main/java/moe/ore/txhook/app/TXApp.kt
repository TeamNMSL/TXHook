package moe.ore.txhook.app

import android.widget.ListView
import androidx.constraintlayout.widget.ConstraintLayout
import moe.ore.txhook.R
import moe.ore.txhook.catching.PacketService
import moe.ore.txhook.databinding.FragmentCatchBinding
import moe.ore.txhook.more.BaseApp

class TXApp: BaseApp() {
    override fun isDebug(): Boolean {
        return true
    }

    companion object {
        val catchingList = arrayListOf<PacketService>()
        lateinit var catching: FragmentCatchBinding

        /**
         * 获取抓包列表
         */
        fun getCatchingList(): ListView {
            val catchingRoot = catching.multipleStatusView.contentView as ConstraintLayout
            return catchingRoot.findViewById(R.id.catch_list)
        }
    }
}