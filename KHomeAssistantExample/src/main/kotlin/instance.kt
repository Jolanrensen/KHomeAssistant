import nl.jolanrensen.kHomeAssistant.core.KHomeAssistantInstance

val kHomeAssistant = KHomeAssistantInstance(
    host = "home.jolanrensen.nl",
    port = 8123,
    secure = true,
    debug = false,
    accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI4OTcwNGExYmYwYjQ0NTM0YjQ1NTBiZjY4YmVjNjViYiIsImlhdCI6MTU5NDMwNzU1NCwiZXhwIjoxOTA5NjY3NTU0fQ.GvXzQcNV54pTSuUXVnKYCw-BggB_dCn2lH71wJrH8e8"
)