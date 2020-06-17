package nl.jolanrensen.kHomeAssistant

import io.ktor.client.HttpClient

expect object WebsocketsHttpClient {
    val httpClient: HttpClient
}