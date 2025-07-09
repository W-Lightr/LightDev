package lightr.config

import lightr.data.TemplatePath
import lightr.data.TypeMappingUnit

class GlobalState {
    var typeMappingGroupMap: MutableMap<String, Collection<TypeMappingUnit>> = HashMap()
    var templatePaths: MutableSet<TemplatePath> = HashSet()
}
