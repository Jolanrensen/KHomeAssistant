# KHomeAssistant (WIP)
Kotlin alternative to AppDaemon, an automation environment for Home Assistant.


Do you like Home Assistant and want to take automation to the next level? 
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

##Structure
###Calling services
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

###States
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

###Attributes
TODO

###Scheduler
TODO

##Getting started

TODO

###Functional and DSL style
TODO