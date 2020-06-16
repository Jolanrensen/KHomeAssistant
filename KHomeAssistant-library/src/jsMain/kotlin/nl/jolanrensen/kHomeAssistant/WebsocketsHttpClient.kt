package nl.jolanrensen.kHomeAssistant

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.features.websocket.WebSockets

import io.ktor.util.KtorExperimentalAPI

actual object WebsocketsHttpClient {
    @KtorExperimentalAPI
    actual val httpClient: HttpClient
        get() =  HttpClient(Js) {
            install(WebSockets)
        }

}