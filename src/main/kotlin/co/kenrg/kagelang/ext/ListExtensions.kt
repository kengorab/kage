package co.kenrg.kagelang.ext

/**
 * Tests for pairwise equivalence between this list and the one passed (as a varargs),
 * i.e. for (a, b) and (c, d), if a == c and b == d or a == d and b == c.
 *
 * Implemented as an extension function for convenience.
 */
fun <T> List<T>.pairwiseEq(vararg others: T): Boolean =
        // For now, only handle lists of 2 items
        if (!(this.size == 2 && others.size == 2)) false
        else this[0] == others[0] && this[1] == others[1] || this[0] == others[1] && this[1] == others[0]
