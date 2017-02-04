package co.kenrg.kagelang.model

import co.kenrg.kagelang.KageParser
import co.kenrg.kagelang.tree.types.KGType
import co.kenrg.kagelang.tree.types.asKGType

data class TypeIdentifier(val name: String, val typeParams: List<TypeIdentifier>? = null) {

    fun hydrate(data: Scope<*, *>, onTypeUnknown: (String) -> Unit): KGType? {
        val params = this.typeParams?.map {
            val t = it.hydrate(data, onTypeUnknown)
            if (t == null) onTypeUnknown(it.name)
            t!!
        } ?: listOf()
        val type = data.getType(this.name) ?: this.name.asKGType(params)

        return type
    }
}

// Convenience function, for null-chaining
fun KageParser.TypeIdentifierContext.toTypeIdentifier(): TypeIdentifier =
        if (this.tupleTypeIdentifier() != null) {
            val tupleItems = this.tupleTypeIdentifier().typeIdentifiers().typeIdentifier()
            val typeName = when (tupleItems.size) {
                2 -> "Pair"
                3 -> "Triple"
                4 -> "Tuple4"
                5 -> "Tuple5"
                6 -> "Tuple6"
                else -> throw UnsupportedOperationException("Tuples larger than 6 items not supported; you should consider creating a type instead")
            }
            TypeIdentifier(typeName, tupleItems.map { it.toTypeIdentifier() })
        } else {
            TypeIdentifier(
                    name = this.typeName.text,
                    typeParams = this.typeParams?.typeIdentifier()?.map { it.toTypeIdentifier() }
            )
        }
