package moe.ore.txhook.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import moe.ore.txhook.R
import moe.ore.txhook.datas.PacketInfoData

private val TAB_TITLES = arrayOf(
    R.string.info,
    R.string.content,
    R.string.tab_analyse,
)

class PacketPagerAdapter(private val context: Context, fm: FragmentManager, private val data: PacketInfoData) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return PacketFragment.newInstance(position + 1, data)
    }

    override fun getPageTitle(position: Int): CharSequence {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Show 2 total pages.
        return TAB_TITLES.size
    }
}