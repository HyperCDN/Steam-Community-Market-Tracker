package de.hypercdn.scmt.util.data

fun Boolean.Companion.ofInt(int: Int?): Boolean {
    return int?.coerceAtLeast(0) != 0
}