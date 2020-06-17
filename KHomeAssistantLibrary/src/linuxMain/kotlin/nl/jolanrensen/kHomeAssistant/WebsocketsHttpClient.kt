package nl.jolanrensen.kHomeAssistant

actual object WebsocketsHttpClient {
    actual val httpClient: io.ktor.client.HttpClient
        get() = HttpClient(Curl) {
            install(Websockets)
        }
}