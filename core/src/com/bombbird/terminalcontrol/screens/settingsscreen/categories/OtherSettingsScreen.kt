package com.bombbird.terminalcontrol.screens.settingsscreen.categories

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager.sweepAvailable
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTab
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTemplateScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.customsetting.WeatherScreen
import com.bombbird.terminalcontrol.utilities.files.GameSaver
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

class OtherSettingsScreen(game: TerminalControl, radarScreen: RadarScreen?, background: Image?) : SettingsTemplateScreen(game, radarScreen, background) {
    lateinit var weather: SelectBox<String>
    lateinit var sound: SelectBox<String>
    lateinit var sweep: SelectBox<String>
    lateinit var weatherLabel: Label
    lateinit var weatherSel: RadarScreen.Weather
    lateinit var soundLabel: Label
    var soundSel = 0
    lateinit var sweepLabel: Label
    var radarSweep = 0f

    //In game only
    private lateinit var speed: SelectBox<String>
    private lateinit var speedLabel: Label
    private var speedSel = 0

    init {
        infoString = "Set miscellaneous options below."
        loadUI(-1200, -200)
        setOptions()
    }

    /** Loads selectBox for display settings  */
    override fun loadBoxes() {
        weather = createStandardSelectBox()
        val weatherModes = Array<String>()
        weatherModes.add("Live weather", "Random weather", "Static weather")
        if (radarScreen != null) weatherModes.add("Set custom weather...")
        weather.items = weatherModes
        weather.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                if ("Set custom weather..." == weather.selected) {
                    //Go to weather change screen
                    game.screen = WeatherScreen(game)
                } else {
                    weatherSel = RadarScreen.Weather.valueOf(weather.selected.split(" ".toRegex()).toTypedArray()[0].toUpperCase(Locale.ROOT))
                }
            }
        })
        sound = createStandardSelectBox()
        val options = Array<String>(2)
        if (Gdx.app.type == Application.ApplicationType.Android) {
            options.add("Pilot voices + sound effects", "Sound effects only", "Off")
        } else if (Gdx.app.type == Application.ApplicationType.Desktop) {
            options.add("Sound effects", "Off")
        }
        sound.items = options
        sound.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                if ("Pilot voices + sound effects" == sound.selected) {
                    soundSel = 2
                } else if ("Sound effects" == sound.selected || "Sound effects only" == sound.selected) {
                    soundSel = 1
                } else if ("Off" == sound.selected) {
                    soundSel = 0
                }
            }
        })
        sweep = createStandardSelectBox()
        sweep.items = sweepAvailable
        sweep.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                radarSweep = sweep.selected.substring(0, sweep.selected.length - 1).toFloat()
            }
        })
        if (radarScreen != null) {
            speed = createStandardSelectBox()
            speed.setItems("1x", "2x", "4x")
            speed.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: Actor) {
                    speedSel = speed.selected[0].toInt() - 48
                }
            })
        }
    }

    /** Loads labels for display settings  */
    override fun loadLabel() {
        super.loadLabel()
        weatherLabel = Label("Weather: ", labelStyle)
        soundLabel = Label("Sounds: ", labelStyle)
        sweepLabel = Label("Radar sweep: ", labelStyle)
        if (radarScreen != null) {
            speedLabel = Label("Speed: ", labelStyle)
        }
    }

    /** Loads actors for display settings into tabs  */
    override fun loadTabs() {
        if (radarScreen != null && radarScreen.tutorial) {
            setOptions()
            //Only show speed tab in tutorial settings
            val tab1 = SettingsTab(this, 2)
            tab1.addActors(speed, speedLabel)
            settingsTabs.add(tab1)
            return
        }
        val tab1 = SettingsTab(this, 2)
        tab1.addActors(weather, weatherLabel)
        tab1.addActors(sound, soundLabel)
        tab1.addActors(sweep, sweepLabel)
        if (radarScreen != null) {
            tab1.addActors(speed, speedLabel)
        }
        settingsTabs.add(tab1)
    }

    /** Sets relevant options into select boxes  */
    override fun setOptions() {
        if (radarScreen != null) {
            speedSel = radarScreen.speed
            speed.selected = speedSel.toString() + "x"
            weatherSel = radarScreen.weatherSel
            soundSel = radarScreen.soundSel
            radarSweep = radarScreen.radarSweepDelay
            if (radarScreen.tutorial) return
        } else {
            radarSweep = TerminalControl.radarSweep
            weatherSel = TerminalControl.weatherSel
            soundSel = TerminalControl.soundSel
        }
        weather.selected = weatherSel.toString()[0].toString() + weatherSel.toString().substring(1).toLowerCase(Locale.ROOT) + " weather"
        var soundIndex = (if (Gdx.app.type == Application.ApplicationType.Android) 2 else 1) - soundSel
        if (soundIndex < 0) soundIndex = 0
        sound.selectedIndex = soundIndex
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.CEILING
        sweep.selected = df.format(radarSweep.toDouble()) + "s"
    }

    /** Confirms and applies the changes set  */
    override fun sendChanges() {
        if (radarScreen != null) {
            val changedToLive = weatherSel === RadarScreen.Weather.LIVE && radarScreen.weatherSel !== RadarScreen.Weather.LIVE
            radarScreen.weatherSel = weatherSel
            radarScreen.soundSel = soundSel
            radarScreen.speed = speedSel
            radarScreen.radarSweepDelay = radarSweep
            if (radarSweep < radarScreen.radarTime) radarScreen.radarTime = radarSweep
            if (changedToLive) radarScreen.metar.updateMetar(false) //If previous weather mode was not live, but now is changed to live, get live weather immediately
            Gdx.app.postRunnable { radarScreen.ui.updateInfoLabel() }
        } else {
            TerminalControl.weatherSel = weatherSel
            TerminalControl.soundSel = soundSel
            TerminalControl.radarSweep = radarSweep
            GameSaver.saveSettings()
        }
    }
}