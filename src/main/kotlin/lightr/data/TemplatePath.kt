package lightr.data

class TemplatePath : Comparable<TemplatePath> {
    var templateName: String? = null
    var path: String? = null

    constructor(templateName: String, path: String) {
        this.templateName = templateName
        this.path = path
    }

    @Deprecated(message = "Framework internal use only", level = DeprecationLevel.ERROR)
    constructor()

    override fun compareTo(other: TemplatePath): Int {
        return templateName?.compareTo(other.templateName ?: "") ?: 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TemplatePath

        return templateName == other.templateName
    }

    override fun hashCode(): Int {
        return templateName?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "$templateName -> $path"
    }
}
