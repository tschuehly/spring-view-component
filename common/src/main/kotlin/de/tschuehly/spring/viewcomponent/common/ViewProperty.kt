package de.tschuehly.spring.viewcomponent.common

data class ViewProperty(
    val propertyName: String,
    val propertyValue: Any?
) {
    companion object {
        @JvmStatic
        fun of(propertyName: String, propertyValue: Any): ViewProperty {
            return ViewProperty(propertyName, propertyValue)
        }
    }

}


fun Array<out ViewProperty>.toMap(): Map<String, Any?> {
    return this.associate {
        Pair(it.propertyName, it.propertyValue)
    }
}
infix fun <B : Any> String.toProperty(that: B?) = ViewProperty(this, that)