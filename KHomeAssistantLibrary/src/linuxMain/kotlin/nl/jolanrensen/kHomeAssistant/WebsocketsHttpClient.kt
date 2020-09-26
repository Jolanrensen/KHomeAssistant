package nl.jolanrensen.kHomeAssistant

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.features.websocket.WebSockets
import io.ktor.network.tls.CIOCipherSuites
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
actual object WebsocketsHttpClient {
    actual val httpClient: HttpClient
        get() = HttpClient(CIO) {
            install(WebSockets)
            engine {
                /**
                 * Maximum number of socket connections.
                 */
                maxConnectionsCount = 1000

                /**
                 * Endpoint specific settings.
                 */
                endpoint {
                    /**
                     * Maximum number of requests for a specific endpoint route.
                     */
                    maxConnectionsPerRoute = 100

                    /**
                     * Max size of scheduled requests per connection(pipeline queue size).
                     */
                    pipelineMaxSize = 20

                    /**
                     * Max number of milliseconds to keep iddle connection alive.
                     */
                    keepAliveTime = 5000

                    /**
                     * Number of milliseconds to wait trying to connect to the server.
                     */
                    connectTimeout = 5000

                    /**
                     * Maximum number of attempts for retrying a connection.
                     */
                    connectRetryAttempts = 5
                }

                /**
                 * Https specific settings.
                 */
                https {
                    /**
                     * Custom server name for TLS server name extension.
                     * See also: https://en.wikipedia.org/wiki/Server_Name_Indication
                     */
                    serverName = "api.ktor.io"

                    /**
                     * List of allowed [CipherSuite]s.
                     */
//                    cipherSuites = CIOCipherSuites.SupportedSuites

                    /**
                     * Custom [X509TrustManager] to verify server authority.
                     *
                     * Use system by default.
                     */
//                    trustManager = myCustomTrustManager

                    /**
                     * [SecureRandom] to use in encryption.
                     */
//                    random = mySecureRandom
                }
            }
        }

}