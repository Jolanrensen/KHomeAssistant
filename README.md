# KHomeAssistant (WIP)

Kotlin alternative to AppDaemon, an automation environment and library for Home Assistant.


Do you like Home Assistant and want to take automation to the next level?
Do you want to create an Android application that needs to communicate with a Home Assistant instance?
Have you tried AppDaemon but you found the typeless programming in Python to be a giant trial-and-error situation?
Well, then KHomeAssistant is the library / add-on for you!

Using an IDE like Intellij and the great Kotlin language you can easily verify whether your code will run and behave before you use it with Home Assistant.
Plus, if you are familiar with Java or other object based programming languages, you'll quickly feel at home with Kotlin.
The autocomplete is your friend and the type completion is godsent, so there's much to enjoy.

## Simple Example

Let's get a quick comparison between an AppDaemon automation and the KHomeAssistant variant, as well as an abbreviated version.
```python
class OutsideLights(hass.Hass):
    
    off_scene = "scene.outside_lights_off"
    on_scene = "scene.outside_lights_on"

    def initialize(self):
        self.run_at_sunrise(self.on_sunrise, offset=900)
        self.run_at_sunset(self.on_sunset, offset=-900)

    def on_sunrise(self, kwargs):
        self.log("OutsideLights: Sunrise Triggered")
        self.turn_on(self.off_scene)

    def on_sunset(self, kwargs):
        self.log("OutsideLights: Sunset Triggered")
        self.turn_on(self.on_scene)

```
And now the same automation but converted directly to Kotlin:
```kotlin
class OutsideLights(kHass: KHomeAssistant) : Automation(kHass) {

    private val offScene: Scene.Entity = Scene.Entity("outside_lights_off")
    private val onScene: Scene.Entity = Scene.Entity("outside_lights_on")

    override suspend fun initialize() {
        runEveryDayAtSunrise(offset = 15.minutes, callback = ::onSunrise)
        runEveryDayAtSunset(offset = -15.minutes, callback = ::onSunset)
    }

    private suspend fun onSunrise() {
        println("OutsideLights: Sunrise Triggered")
        offScene.turnOn()
    }

    private suspend fun onSunset() {
        println("OutsideLights: Sunset Triggered")
        onScene.turnOn()
    }
}
```
This looks very similar right? As a bonus `offScene` is now of the type `Scene.Entity` meaning we can now easily see that `onScene.entities` will give us a list of entities that are affected by this scene, thanks to the autocomplete of the IDE.
We can also private our functions and attributes to keep the project clean as well as use typed TimeSpans thanks to the klock library.

Now let's Kotlin-ify, because no one wants to type stuff that isn't necessary.
```kotlin
class OutsideLights(kHass: KHomeAssistant) : Automation(kHass) {

    private val offScene: Scene.Entity = Scene["outside_lights_off"]
    private val onScene: Scene.Entity = Scene["outside_lights_on"]

    override suspend fun initialize() {
        runEveryDayAtSunrise(offset = 15.minutes) {
            println("OutsideLights: Sunrise Triggered")
            offScene.turnOn()
        }
        runEveryDayAtSunset(offset = -15.minutes) {
            println("OutsideLights: Sunset Triggered")
            onScene.turnOn()
        }
    }
}
```
As you can see, callbacks can be inlined, making it much clearer what the automation does when. 
Using helper functions in the library `.Entity()` can be replaced with `[]` which is less typing and still quite clear. 
Also note that type annotations like `: Scene.Entity` can be omitted as the IDE will hint the type anyways.

## Structure

### Calling services

While calling services directly from an automation is still possible, 
```kotlin
callService(serviceDomain = "light", serviceName = "turn_on", entityID = "light.bedroom_lamp")
``` 
this is far from ideal to work with.
Hence, the library 'objectifies' domains and entities so that service executions can be done like calling a function on an object.
This converts the service call from above to 
```kotlin
Light.Entity("bedroom_lamp").turnOn()

\\ or
Light["bedroom_lamp"].turnOn()
```
Services that do not take an entity ID as input can be called from the domain itself.
For example:
```kotlin
callService(serviceDomain = "homeassistant", serviceName = "restart")
```
becomes simply
```kotlin
HomeAssistant.restart()
```

Now you can see that because domains and entities are now typed, there can be a lack of support in the library.
While it is encouraged to help expand the library, in the meantime you can use something like this:
```kotlin
Domain("some_unsupported_domain")["some_entity"]
    .callService(serviceName = "some_service", data = json { "some_value" to 10 })
```

### States

States are an important part of Home Assistant entities and are usually represented as a String.
However, sometimes these states are actually Floats like for an "input_number" or the state can only
be one of several types, like "on" or "off" for a "light" entity. Hence, states in KHomeAssistant are typed
and dependent on the type of the entity. For example:
```kotlin
val lightState: OnOff = Light["some_light"].state

when (lightState) {
    ON -> { /* do something */ }
    OFF -> { /* do something else */ }
    UNKNOWN, UNAVAILABLE -> { /* notify or something */ }
}
```
As you can see the state of a `Light` is represented as the enum class `OnOff` which can only exist in a certain amount of states.

In some cases states are also writable, which is dependent on the domain's implementation. 
For instance, `Light` performs a `turnOn()` when the `state` is set to `ON` and a `turnOff()` when it's set to `OFF`.
The IDE will tell you if it's possible to set the state like that or not.

### Attributes

Most entities in Home Assistant have their own attributes. This can range from the current brightness of a light or the volume level of the media player.
Unlike AppDaemon, attributes are available directly as properties of an entity. This means you can get the volume of your stereo like:
```kotlin
val volume: Float = MediaPlayer["stereo"].volume_level
```
(If an entity does not have the attribute an exception will be thrown unless a default is set in the `attrsDelegate` in the entity class. 
This means that if you're not sure your entity actually has the property you want, you must surround it with `try { ... } catch (e: Exception) { ... }`).

Aside from this, KHomeAssistant also allows attributes to be writable. This means that
```kotlin
MediaPlayer["stereo"].volume_level = 0.3f
```
will under the hood call
```kotlin
MediaPlayer["stereo"].volumeSet(0.3f)
```
This allows for beautiful notations like:
```kotlin
MediaPlayer["stereo"].volume_level += 0.05f
```

Attributes in KHomeAssistant are cached and updated when state changes occur, meaning they should always be up to
date. If the cache has not been updated in a while it will fully refresh it.

If you want to take a look at the current state / attributes of an entity, simply print it.
The `toString` method of an entity is very powerful.


### Listeners

An important part of automations is being able to react to state- or attribute changes. 
Most entities include their own helper functions to provide listeners which improves readability
and understandability. For instance, there's:
```kotlin
Light["bedroom_lamp"].onTurnedOn {
    // do something
}
```
or
```kotlin
GarageDoorBinarySensor["garage_door"].onOpened {
    // do something
}
```

However, not all possible attributes and states can be covered with helper functions, so
KHomeAssistant provides a few extension functions on all entities.
For example, for state changes there is:
```kotlin
MediaPlayer["stereo"].onStateChangedTo(PAUSED) {
    // do something
}
```
but also a more general
```kotlin
myEntity.onStateChanged {
    // do something
}
```
and even
```kotlin
myEntity.onStateChanged({ oldValue, newValue ->
    // do something with old or new state value
})
```

Listening for attribute changes works in a similar manner. You can listen for any attribute change
using
```kotlin
myEntity.onAttributesChanged {
    // do something
}
```
or you can specify which attribute to listen for. This can be any property or attribute of the
entity of your choice. For instance
```kotlin
val stereo = MediaPlayer["stereo"]
stereo.onAttributeChangedNotTo(stereo::source, "TV") {
    // turn off the tv or something
}

// or in DSL style (for more info see below)
MediaPlayer["stereo"] {
    onAttributeChangedNotTo(::source, "TV") {
        // turn off the tv or something
    }
}
```

For all callbacks, `this` corresponds to the entity. This means you can simply type
```kotlin
Switch["bedroom_switch"].onTurnedOn {
    // toggle the lights or something
  
    // turns off the switch
    turnOff()
}
```

### Scheduler

Scheduling when to run something is another essential part for automation. While you can freely
use `delay(5.seconds)` in your code (as the thread will then simply suspend for 5 seconds), if
you want to schedule something for each day, this is undoable.
KHomeAssistant uses an in-house built scheduler to make this easy.

For regular time intervals, there are functions available like
```kotlin
runEveryMinute {
    // this gets run at the start of each minute
}
```
and 
```kotlin
runEveryHour(offset = 30.minutes + 15.seconds) {
    // this gets run every hour at the 30 minute mark plus 15 seconds
}
```
You can even go completely custom
```kotlin
runEvery(
    timeSpan = 1.9.hours + 23.minutes - 4.8.seconds + 1.milliseconds,
    alignWith = DateTime.nowLocal()
) {
    // do something
}
```

There are also irregular intervals such as running something at sunset.
Offsets are also available.
```kotlin
runEveryDayAtSunset(offset = -15.minutes) {
    // this gets run every day 15 minutes before sunset
}
```
As the time of the sunset changes every day, the next execution time gets updated automatically
using an attribute listener.
You can also create a scheduled execution for a changing execution time yourself (for instance
using a `datetime_input`). To understand how that would work, let's look again at how `runEveryDayAtSunset` works.
```kotlin
runAt(
    getNextLocalExecutionTime = { sun.nextRising },  // define how to get the execution time value
    whenToUpdate = { update ->  // define when to update the execution time value
        sun.onAttributeChanged(sun::nextRising) { update() }  // namely when the nextRising attribute changes
    }
) {
    // do something
}
```

Finally, you can define one-off schedulings.
There is `runIn()` where you can schedule something to run in a certain amount of time from now.
For example:
```kotlin
runIn(5.minutes) {
    // do something
}
```
There's also `runAt()` where you can define when to run something at a certain point in (local) time.
For example:
```kotlin
runAt(
    DateTime(
        year = Year(2020), 
        month = Month.September, 
        day = 22, 
        hour = 13, 
        minute = 30
    ).localUnadjusted
) {
    // do something
}
```
All schedules return a `Task` instance, which can be `cancel()`'ed at any time.

## Getting started

I'm still working on getting KHomeAssistant to work as an Add-On for Home Assistant, however, in the
meantime, you can already test KHomeAssistant from your own PC, as long as you can connect to your 
Home Assistant instance over the network / internet. Recommended tools are Intellij by Jetbrains and some Kotlin knowledge of course.

The library is not yet published, so for now, you can keep the library (KHomeAssistantLibrary) and your instance consisting
of automation (KHomeAssistantExample) in the same project. Eventually, the library will be published.

Firstly you'll need a Long Lived Access Token.
This, you can create by going to your profile on the web interface, scrolling down and creating a new one.

All communication between the program and the Home Assistant instance goes via `KHomeAssistantInstance`.
To get started, create an instance like
```kotlin
val kHomeAssistant = KHomeAssistantInstance(
    host = "THE IP OR HOSTNAME OF YOUR INSTANCE",
    port = 8123,  // for instance
    secure = true,  // true if you're using https instead of http
    debug = false,  // prints more messages if true
    timeout = 2.seconds,  // Timeout for confirmation for updating states and attributes.
    accessToken = "THE ACCESS TOKEN"
)
```
Then, using this instance, you can call `.run` on it from a Coroutine context with instances of the automations
you want to run. This can be done from the `main()` method if you like. For instance:
```kotlin
fun main() {
    runBlocking {
        kHomeAssistant.run(OutsideLights(kHomeAssistant)/*, maybe others */)
    }
}
```
And that's all! 

### Functional, DSL style and more fun styles

The beauty of Kotlin is that you can use it however you like and if you want, you
can combine the strengths of object oriented programming and functional programming.

To start off, all automations can also be created using a function call instead of having to create a class.
```kotlin
val automation: Automation = automation(kHomeAssistant, "Automation Name") {
    // this can be seen as the initialize method
}
```
Automations can even be defined directly in the `run` method if you would like to do so.
```kotlin
kHomeAssistant.run(
    automation("one") {
        // one automation
    },
    automation("other") {
       // other automation
    }
)
```

Next, all entities are invokable, DSL-style, which can be compared to calling `.apply { }` on it.
This makes for very readable and clear syntax and less typing.
No one is a fan of syntax like
```kotlin
bedroom_lamp.color = Colors.RED
bedroom_lamp.brightness_pct = 100f

bedroom_lamp.onAttributeChanged(bedroom_lamp::effect) {
    // do something
}
```
instead, you can use
```kotlin
bedroom_lamp {
    color = Colors.RED
    brightness_pct = 100f   
   
    onAttributeChanged(::effect) {
        // do something
    }      
}
```
which does exactly the same thing, however it is a lot clearer with less typing (so less room for error).

If you want to target multiple lights at once, simply put them in a list or array and go for it:
```kotlin
val lights = listOf(bedroom_lamp, other_lamp)
lights {
    color = Colors.RED
    brightness_pct = 100f

    onAttributeChanged(::effect) {
        // do something
    }
}
```
This saves you from even having to write a for loop.

If all you ever want is to address multiple entities at once and don't want to address them individually, you can immediately
create a list of entities using:
```kotlin
val lights: List<Light.Entity> = Light.Entities("light1", "light2", "light3")

// or
val lights: List<Light.Entity> = Light["light1", "light2", "light3"]
```
This also means that 
```kotlin
val light1 = Light["light1"]
val light2 = Light["light2"]
val light3 = Light["light3"]
```
can (only inside a function) be shortened to:
```kotlin
val (light1, light2, light3) = Light["light1", "light2", "light3"]
```

Another fun notation is the delegate notation. You can initialize an entity like this:
```kotlin
val bedroom_lamp by Light
```
For this to work, the name of the variable needs to exactly match the name of the entity in Home Assistant.

## Implemented Domains

 - switch
 - sun
 - scene
 - notify
 - mqtt
 - media_player
 - light
 - home_assistant
 - hassio
 - group
 - input
    - input_boolean
    - input_datetime
    - input_number
    - input_select
    - input_text
 - binary_sensor (split per device_class)
    - generic
    - battery
    - connectivity
    - ...
    - all of them are present
 - sensor (split per device_class)
    - generic
    - battery
    - TODO
 - Tell me what TODO next!
 - You can also implement a domain yourself following the `ExampleDomain` in the KHomeAssistantExample subproject.
 
# Disclaimer

This library is VERY MUCH a work in progress. If you're feeling adventurous you can try it out or help
by creating implementing more domains! Any tips or contributions are welcome as well.

Testing is a large part that still needs to be done.

While the library uses Kotlin Multiplatform. Currently only Kotlin/JVM works. There are still some reflection pieces
missing in Kotlin/JS which I hope will be added soon. Kotlin/Native, same story.