package lightr.interfaces

fun interface ITypeMapperMatch {
    fun match(rule: String, input: String): Boolean
}
