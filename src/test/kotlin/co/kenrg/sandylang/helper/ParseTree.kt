package co.kenrg.sandylang.helper

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.TerminalNode
import java.util.*

abstract class ParseTreeElement {
    abstract fun multiLineString(indentation: String = ""): String
}

class ParseTreeLeaf(val text: String) : ParseTreeElement() {
    override fun multiLineString(indentation: String): String {
        return "$indentation$this\n"
    }

    override fun toString(): String {
        return "Leaf[$text]"
    }
}

class ParseTreeNode(val nodeName: String) : ParseTreeElement() {
    val children = LinkedList<ParseTreeElement>()

    fun addChild(child: ParseTreeElement): ParseTreeNode {
        children.add(child)
        return this
    }

    override fun toString(): String {
        return "Node(name='$nodeName') $children"
    }

    override fun multiLineString(indentation: String): String {
        val sb = StringBuilder()
        sb.append("$indentation$nodeName\n")
        children.forEach { child -> sb.append(child.multiLineString(indentation + "  ")) }
        return sb.toString()
    }
}

fun toParseTree(node: ParserRuleContext): ParseTreeNode {
    val res = ParseTreeNode(node.javaClass.simpleName.removeSuffix("Context"))
    node.children.forEach { c ->
        when (c) {
            is ParserRuleContext -> res.addChild(toParseTree(c))
            is TerminalNode -> res.addChild(ParseTreeLeaf(c.text))
        }
    }
    return res
}