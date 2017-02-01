package co.kenrg.kagelang.model

import co.kenrg.kagelang.KageParser
import co.kenrg.kagelang.tree.types.KGType
import co.kenrg.kagelang.tree.types.asKGType

data class TypeIdentifier(val name: String, val typeParams: List<TypeIdentifier>? = null) {

    fun hydrate(data: Scope<*, *>, onTypeUnknown: (String) -> Unit): KGType? {
        val type = data.getType(this.name) ?: this.name.asKGType()
        val params = this.typeParams?.map {
            val t = it.hydrate(data, onTypeUnknown)
            if (t == null) onTypeUnknown(it.name)
            t!!
        }

        return type?.copy(typeParams = params)
    }
}

// Convenience function, for null-chaining
fun KageParser.TypeIdentifierContext.toTypeIdentifier(): TypeIdentifier =
        TypeIdentifier(
                name = this.typeName.text,
                typeParams = this.typeParams?.typeIdentifier()?.map { it.toTypeIdentifier() }
        )