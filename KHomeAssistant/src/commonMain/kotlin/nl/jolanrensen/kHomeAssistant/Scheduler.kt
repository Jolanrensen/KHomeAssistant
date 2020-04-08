package nl.jolanrensen.kHomeAssistant

import com.soywiz.klock.*

object Scheduler {

    fun KHomeAssistantContext.runEveryDayAt(time: Time, callback: suspend () -> Unit) =
        runEvery(1.days, DateTime(DateTime.EPOCH.date, time).localUnadjusted, callback)


//    fun KHomeAssistantContext.runEveryDay(alignWith: DateTimeTz, callback: suspend () -> Unit) =
//        runEvery(1.days, alignWith, callback)

    fun KHomeAssistantContext.runEveryDay(alignWith: DateTimeTz = DateTime.EPOCH.localUnadjusted, callback: suspend () -> Unit) =
        runEvery(1.days, alignWith, callback)

//    fun KHomeAssistantContext.runEveryHour(alignWith: DateTimeTz = DateTime.EPOCH.local, callback: suspend () -> Unit) =
//        runEvery(1.hours, alignWith, callback)

    fun KHomeAssistantContext.runEveryHour(alignWith: DateTimeTz = DateTime.EPOCH.localUnadjusted, callback: suspend () -> Unit) =
        runEvery(1.hours, alignWith, callback)
    
//    fun KHomeAssistantContext.runEveryMinute(alignWith: DateTimeTz, callback: suspend () -> Unit) =
//        runEvery(1.minutes, alignWith, callback)
    
    fun KHomeAssistantContext.runEveryMinute(alignWith: DateTimeTz = DateTime.EPOCH.localUnadjusted, callback: suspend () -> Unit) =
        runEvery(1.minutes, alignWith, callback)
    
//    fun KHomeAssistantContext.runEvery(runEvery: TimeSpan, alignWith: DateTimeTz, callback: suspend () -> Unit) =
//        runEvery(runEvery, alignWith.utc, callback)

    fun KHomeAssistantContext.runEvery(runEvery: TimeSpan, alignWith: DateTimeTz = DateTime.EPOCH.localUnadjusted, callback: suspend () -> Unit) {
        kHomeAssistant()!!.scheduledRepeatedTasks += RepeatedTask(runEvery, alignWith.utc, callback)
    }


}

data class RepeatedTask(
    val runEvery: TimeSpan,
    var alignWith: DateTime, // can be adjusted to a alignWith closest to now, yet in the past
    val callback: suspend () -> Unit
)