package nl.jolanrensen.kHomeAssistant.core

import com.soywiz.klock.DateTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import nl.jolanrensen.kHomeAssistant.RepeatedTask
import nl.jolanrensen.kHomeAssistant.helper.PriorityQueue
import nl.jolanrensen.kHomeAssistant.helper.priorityQueueOf

/**
 * Scheduler used by [KHomeAssistantInstance] to schedule and execute tasks at certain times.
 * @param kHomeAssistant the [KHomeAssistantInstance] instance
 */
class Scheduler(private val kHomeAssistant: KHomeAssistantInstance) {
    /**
     * Returns a new instance of the scheduler job.
     * The scheduler stops itself when there are no tasks left in
     * [scheduledRepeatedTasks]. If there is are tasks, it waits until it's time to execute the first (and is cancelable
     * doing this), then it executes the task and reschedules it.*/
    private fun getNewSchedulerJob() = kHomeAssistant.launch {
        while (scheduledRepeatedTasks.isNotEmpty()) {
            // Get next scheduled task in the future
            val next = scheduledRepeatedTasksLock.withLock {
                if (scheduledRepeatedTasks.isEmpty()) null
                else scheduledRepeatedTasks.next
            } ?: break // break if there are no tasks left

            // Suspend until it's time to execute the next task (can be canceled here)
            delay((next.scheduledNextExecution - DateTime.now()).millisecondsLong.also {
                kHomeAssistant.debugPrintln("Waiting for $it milliseconds until the next scheduled execution")
            })

            // check whether the next task isn't canceled in the meantime
            if (scheduledRepeatedTasksLock.withLock {
                    scheduledRepeatedTasks.isEmpty() || next != scheduledRepeatedTasks.next
                }) continue

            // remove it from the schedule and execute
            kHomeAssistant.launch { next.callback() }

            // set the last execution time
            next.lastExecutionScheduledExecutionTime = next.scheduledNextExecution

            // check for a reschedule, probably not needed for RepeatedIrregularTask
            next.update()
        }
        schedulerJob = null
    }

    /** The currently running scheduler job. */
    private var schedulerJob: Job? = null

    /** A [PriorityQueue] of [RepeatedTask]s that will be executed at their `scheduledNextExecution`.
     * Always use with [scheduledRepeatedTasksLock]. */
    private val scheduledRepeatedTasks: PriorityQueue<RepeatedTask> = priorityQueueOf()

    /** Returns whether the scheduler is empty. */
    val isEmpty
        get() = scheduledRepeatedTasks.isEmpty()

    /** Returns the amount of tasks the scheduler has. */
    val size
        get() = scheduledRepeatedTasks.size

    /** Mutex that needs to be used when working with [scheduledRepeatedTasks] */
    private val scheduledRepeatedTasksLock = Mutex()

    /** Cancel this task and schedule it again. */
    suspend fun reschedule(task: RepeatedTask) {
        cancel(task)

        // makes sure tasks cannot be scheduled for the same point in time already executed.
        if (task.scheduledNextExecution > task.lastExecutionScheduledExecutionTime)
            schedule(task)
    }

    /** Make this task be executed by the [schedulerJob]. */
    suspend fun schedule(task: RepeatedTask) {
        scheduledRepeatedTasksLock.withLock {
            if (scheduledRepeatedTasks.isEmpty() || task < scheduledRepeatedTasks.next) {
                schedulerJob?.cancel()
                scheduledRepeatedTasks += task
                schedulerJob = getNewSchedulerJob()
            } else {
                scheduledRepeatedTasks += task
            }
        }
    }

    /** Stop this task from being executed by the [schedulerJob]. */
    suspend fun cancel(task: RepeatedTask) {
        scheduledRepeatedTasksLock.withLock {
            scheduledRepeatedTasks -= task
        }
    }
}