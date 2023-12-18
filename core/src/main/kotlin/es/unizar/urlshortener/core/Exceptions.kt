package es.unizar.urlshortener.core

class InvalidUrlException(url: String) : Exception("[$url] does not follow a supported schema")

class RedirectionNotFound(key: String) : Exception("[$key] is not known")

class UrlReachabilityException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class ForbiddenRedirection(key: String) : Exception("[$key] is not reachable")

class PendingRedirection(retryAfterSeconds: Int) : Exception("Retry after $retryAfterSeconds seconds")


