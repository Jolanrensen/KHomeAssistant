package nl.jolanrensen.kHomeAssistant

import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.client.features.websocket.ws
import io.ktor.client.features.websocket.wss
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.send
import nl.jolanrensen.kHomeAssistant.WebsocketsHttpClient.httpClient

/**
 * KHomeAssistant instance
 *
 * run run() to make the instance run
 */
class KHomeAssistant(
        val host: String,
        val port: Int = 8123,
        val accessToken: String,
        val automations: List<Automation>,
        val secure: Boolean = false
) {


//    val stateListeners

    suspend fun run() {
        connect()
        initializeAutomations()

    }


    private suspend fun connect() {
        val block: suspend DefaultClientWebSocketSession.() -> Unit = {
            session = this

            var response = AuthResponse.fromJson(
                    (incoming.receive() as Frame.Text).readText()
            )

            if (response.isAuthRequired) {
                send(AuthMessage(access_token = accessToken).toJson())
                response = AuthResponse.fromJson(
                        (incoming.receive() as Frame.Text).readText()
                )
            }
            if (!response.isAuthOk) {
                throw IllegalArgumentException(response.message)
            }

            println("successfully logged in")

            // TODO continue here
        }

        if (secure) httpClient.wss(
                host = host,
                port = port,
                path = "/api/websocket",
                block = block
        ) else httpClient.ws(
                host = host,
                port = port,
                path = "/api/websocket",
                block = block
        )
    }

    private fun initializeAutomations() {
        session
        for (it in automations) {
            try {
                it.kHomeAssistant = this
                it.initialize()
                println("Successfully initialized automation ${it::class.simpleName}")
            } catch (e: Exception) {
                println("FAILED to initialize automation ${it::class.simpleName}: $e")
            }
        }
    }


}