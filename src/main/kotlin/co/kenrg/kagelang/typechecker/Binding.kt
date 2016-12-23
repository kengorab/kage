package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.tree.KGTree

data class Binding(val identifier: String, val expression: KGTree.KGExpression)