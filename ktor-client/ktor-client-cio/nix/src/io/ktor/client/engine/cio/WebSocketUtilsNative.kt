/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.ktor.client.engine.cio

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.datetime.*
import kotlin.coroutines.*

internal actual fun startWebSocketSession(
    status: HttpStatusCode,
    requestTime: Instant,
    headers: Headers,
    version: HttpProtocolVersion,
    callContext: CoroutineContext,
    input: ByteReadChannel,
    output: ByteWriteChannel,
    responseTime: Instant
): HttpResponseData {
    error("WebSockets is not supported in native CIO engine.")
}
