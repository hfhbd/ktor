/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package io.ktor.server.sessions

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.util.date.*
import kotlin.time.*
import kotlin.time.Duration.Companion.days

public val DEFAULT_SESSION_MAX_AGE: Duration = 7.days

/**
 * SessionTransport that adds a Set-Cookie header and reads Cookie header
 * for the specified cookie [name], and a specific cookie [configuration] after
 * applying/un-applying the specified transforms defined by [transformers].
 *
 * @property name is a cookie name
 * @property configuration is a cookie configuration
 * @property transformers is a list of session transformers
 */
public class SessionTransportCookie(
    public val name: String,
    public val configuration: CookieConfiguration,
    public val transformers: List<SessionTransportTransformer>
) : SessionTransport {

    override fun receive(call: ApplicationCall): String? {
        return transformers.transformRead(call.request.cookies[name, configuration.encoding])
    }

    override fun send(call: ApplicationCall, value: String) {
        val now = call.application.environment.clock.now()
        val maxAge = configuration.maxAge
        val expires = now + maxAge

        val cookie = Cookie(
            name,
            transformers.transformWrite(value),
            configuration.encoding,
            maxAge,
            expires,
            configuration.domain,
            configuration.path,
            configuration.secure,
            configuration.httpOnly,
            configuration.extensions
        )

        call.response.cookies.append(cookie)
    }

    override fun clear(call: ApplicationCall) {
        call.response.cookies.appendExpired(name, configuration.domain, configuration.path)
    }

    override fun toString(): String {
        return "SessionTransportCookie: $name"
    }
}

/**
 * Cookie configuration being used to send sessions
 */
public class CookieConfiguration {
    /**
     * Cookie time to live duration or 0 for session cookies.
     * Session cookies are client-driven. For example, a web browser usually removes session
     * cookies at browser or window close unless the session is restored.
     */
    public var maxAge: Duration = DEFAULT_SESSION_MAX_AGE
        set(newMaxAge) {
            require(!newMaxAge.isNegative()) { "maxAgeInSeconds shouldn't be negative: $newMaxAge" }
            field = newMaxAge
        }

    /**
     * Cookie encoding
     */
    public var encoding: CookieEncoding = CookieEncoding.URI_ENCODING

    /**
     * Cookie domain
     */
    public var domain: String? = null

    /**
     * Cookie path
     */
    public var path: String? = "/"

    /**
     * Send cookies only over secure connection
     */
    public var secure: Boolean = false

    /**
     * This cookie is only for transferring over HTTP(s) and shouldn't be accessible via JavaScript
     */
    public var httpOnly: Boolean = true

    /**
     * Any additional extra cookie parameters
     */
    public val extensions: MutableMap<String, String?> = mutableMapOf()
}
