package yhh.com.gol.activity.settings.fragment.domain

sealed class State {

    data class UpdateVersionName(val text: String) : State()
    data class UpdateVersionCode(val text: String) : State()
}