package lightr.config

import lightr.data.ScoredMember

class GlobalHistoryState {
    var historyUseTypeMapper: String? = null
    var historyUsePath: MutableSet<ScoredMember> = HashSet()
    var namespace: String? = null
}
