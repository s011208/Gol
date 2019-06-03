package yhh.com.gol.activity.settings.fragment

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import yhh.com.gol.activity.settings.fragment.domain.State
import yhh.com.gol.libs.dagger2.PerFragment
import javax.inject.Inject

@PerFragment
class SettingsFragmentPresenter @Inject constructor(
    private val view: SettingsFragment,
    private val model: SettingsFragmentModel
) {
    private val compositeDisposable = CompositeDisposable()

    fun create() {
        compositeDisposable += model
            .getVersionCode()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe (
                {
                    view.render(State.UpdateVersionCode(it.toString()))
                },
                {
                    Timber.w(it, "failed to get version code")
                }
            )

        compositeDisposable += model
            .getVersionName()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe (
                {
                    view.render(State.UpdateVersionName(it))
                },
                {
                    Timber.w(it, "failed to get version name")
                }
            )
    }

    fun destroy() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
    }
}