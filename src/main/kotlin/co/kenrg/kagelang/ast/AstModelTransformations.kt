package co.kenrg.kagelang.ast

import java.util.*
import kotlin.reflect.KParameter
import kotlin.reflect.memberProperties
import kotlin.reflect.primaryConstructor

fun Node.process(operation: (Node) -> Unit) {
    operation(this)
    this.javaClass.kotlin.memberProperties.forEach { p ->
        val v = p.get(this)
        when (v) {
            is Node -> v.process(operation)
            is Collection<*> -> v.forEach { if (it is Node) it.process(operation) }
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : Node> Node.processNodeType(clazz: Class<T>, fn: (T) -> Unit) {
    this.process { if (clazz.isInstance(it)) fn(it as T) }
}

fun Node.transform(fn: (Node) -> Node): Node {
    fn(this)
    val changes = HashMap<String, Any>()
    this.javaClass.kotlin.memberProperties.forEach { prop ->
        val value = prop.get(this)
        when (value) {
            is Node -> {
                val newValue = value.transform(fn)
                if (value != newValue) changes[prop.name] = newValue
            }
            is Collection<*> -> {
                val newValue = value.map { (it as? Node)?.transform(fn) ?: it }
                if (value != newValue) changes[prop.name] = newValue
            }
        }
    }

    var instanceToTransform = this
    if (!changes.isEmpty()) {
        val constructor = this.javaClass.kotlin.primaryConstructor!!
        val defaultParams = this.javaClass.kotlin.memberProperties.map { it.name to it.get(this) }.toMap()

        val params = HashMap<KParameter, Any?>()
        constructor.parameters.forEach { param ->
            if (changes.containsKey(param.name)) {
                params[param] = changes[param.name]
            } else {
                params[param] = defaultParams[param.name]
            }
        }
        instanceToTransform = constructor.callBy(params)
    }

    return fn(instanceToTransform)
}