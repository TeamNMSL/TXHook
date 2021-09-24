package moe.ore.txhook

import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import moe.ore.txhook.databinding.ActivityPacketInfoBinding
import moe.ore.txhook.datas.PacketInfoData
import moe.ore.txhook.more.BaseActivity
import moe.ore.txhook.ui.main.PacketPagerAdapter

class PacketInfoActivity: BaseActivity() {
    private lateinit var binding: ActivityPacketInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPacketInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val data = intent.getParcelableExtra<PacketInfoData>("data")!!

        val packetPagerAdapter = PacketPagerAdapter(this, supportFragmentManager, data)
        val viewPager: ViewPager = binding.viewPager

        viewPager.adapter = packetPagerAdapter
        val tabs: TabLayout = binding.tabs

        tabs.setupWithViewPager(viewPager)
    }




}