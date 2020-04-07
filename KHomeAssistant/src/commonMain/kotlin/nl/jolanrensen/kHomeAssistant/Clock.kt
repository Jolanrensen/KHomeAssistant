package nl.jolanrensen.kHomeAssistant

expect object Clock {
    fun fixedRateTimer(rate: Long, action: () -> Unit)
}