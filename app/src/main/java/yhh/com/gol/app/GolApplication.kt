package yhh.com.gol.app

import android.app.Application
import android.util.Log
import timber.log.Timber
import yhh.com.gol.activity.main.controller.ConwayRule
import yhh.com.gol.activity.main.controller.v2.GameRunner

class GolApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(object : Timber.DebugTree() {
            override fun isLoggable(tag: String?, priority: Int): Boolean {
                return when (tag) {
                    GameRunner::class.java.simpleName -> // adb -s emulator-5554 shell setprop log.tag.GameRunner V
                        Log.isLoggable(GameRunner::class.java.simpleName, Log.DEBUG)
                    ConwayRule::class.java.simpleName -> // adb -s emulator-5554 shell setprop log.tag.ConwayRule V
                        Log.isLoggable(ConwayRule::class.java.simpleName, Log.DEBUG)
                    else -> true
                }
            }
        })
    }
}