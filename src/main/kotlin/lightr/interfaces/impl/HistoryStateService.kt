package lightr.interfaces.impl

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import lightr.config.GlobalHistoryState
import lightr.data.ScoredMember


@State(
    name = "HistoryStateService",
    storages = [Storage(value = "TemplatelightrGlobalHistoryState.xml")]
)
class HistoryStateService : PersistentStateComponent<GlobalHistoryState?> {
    private var state = GlobalHistoryState()

    override fun getState(): GlobalHistoryState {
        return state
    }

    override fun loadState(state: GlobalHistoryState) {
        if (state.historyUsePath.size > 10) {
            val v = HashSet<ScoredMember>()
            state.historyUsePath.stream().limit(10).forEach { v.add(it) }
            state.historyUsePath = v
        }
        this.state = state
    }

    companion object {
        @JvmStatic
        fun getInstance(): HistoryStateService {
            return ApplicationManager.getApplication().getService(HistoryStateService::class.java)
        }
    }
}
