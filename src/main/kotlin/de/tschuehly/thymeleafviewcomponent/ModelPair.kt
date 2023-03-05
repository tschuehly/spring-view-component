package de.tschuehly.thymeleafviewcomponent

data class ModelPair(
    val propertyName: String,
    val propertyValue: Any
) {
}

infix fun <A : String, B : Any> A.toModel(that: B) = ModelPair(this, that)

fun Array<out ModelPair>.toMap(): Map<String, Any> {
    return this.associate {
        Pair(it.propertyName, it.propertyValue)
    }
}