package lightr.util

import lightr.data.TypeMappingUnit

@Suppress("unused")
class MapperUtil(private val typeMappingUnits: Collection<TypeMappingUnit>) {

    fun tryTransformTo(input: String): String?{
       return TemplateUtil.convertType(input, typeMappingUnits)
    }

}
