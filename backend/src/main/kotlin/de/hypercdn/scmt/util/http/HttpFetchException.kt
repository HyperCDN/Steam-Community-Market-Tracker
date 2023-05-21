package de.hypercdn.scmt.util.http

class HttpFetchException(
    var code: Int,
    msg: String? = "No Message Provided"
) : RuntimeException("${code}: $msg")