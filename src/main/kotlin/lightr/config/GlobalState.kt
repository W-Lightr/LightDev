package lightr.config

import lightr.data.TypeMappingUnit

class GlobalState {
    var typeMappingGroupMap: MutableMap<String, Collection<TypeMappingUnit>> = HashMap()
}
