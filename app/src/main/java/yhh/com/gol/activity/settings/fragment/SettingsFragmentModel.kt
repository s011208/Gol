package yhh.com.gol.activity.settings.fragment

import android.content.Context
import android.content.pm.PackageManager
import io.reactivex.Single
import yhh.com.gol.libs.dagger2.PerFragment
import javax.inject.Inject

@PerFragment
class SettingsFragmentModel @Inject constructor(
    private val context: Context,
    private val packageManager: PackageManager) {

    fun getVersionName() = Single.fromCallable {
        return@fromCallable packageManager.getPackageInfo(context.packageName, 0).versionName
    }

    fun getVersionCode() = Single.fromCallable {
        @Suppress("DEPRECATION")
        return@fromCallable packageManager.getPackageInfo(context.packageName, 0).versionCode
    }
}