package moe.ore.txhook.more

import android.app.Activity

object ActivityCollector {
    private val sActivities: MutableList<Activity> = ArrayList()


    fun addActivity(activity: Activity) {
        sActivities.add(activity)
    }

    fun removeActivity(activity: Activity) {
        sActivities.remove(activity)
    }

    fun finishAll() {
        for (activity in sActivities) {
            if (!activity.isFinishing) {
                activity.finish()
            }
        }
        sActivities.clear()
    }
}