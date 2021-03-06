package com.bombbird.terminalcontrol.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.entities.runways.RunwayConfig
import com.bombbird.terminalcontrol.utilities.Fonts

class RunwayChanger {
    private val radarScreen = TerminalControl.radarScreen!!
    private val background: Image = Image(TerminalControl.skin.getDrawable("ListBackground"))
    private val airportLabel: Label
    private val changeButton: TextButton
    private val confirmButton: TextButton
    private val scrollPane: ScrollPane
    private val newRunwaysLabel: Label
    private val possibleConfigs = Array<RunwayConfig>()
    private var runwayConfig: RunwayConfig? = null
    private var airport: Airport? = null
    var isVisible: Boolean
        private set

    init {
        background.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                super.clicked(event, x, y)
                event.handle() //Prevents hiding of runway changer when background is tapped
            }
        })
        TerminalControl.radarScreen?.ui?.addActor(background, 0.1f, 0.8f, 3240 * 0.05f, 3240 * 0.35f)

        val labelStyle = LabelStyle()
        labelStyle.fontColor = Color.WHITE
        labelStyle.font = Fonts.defaultFont20
        airportLabel = Label("", labelStyle)
        radarScreen.ui.addActor(airportLabel, 0.45f, -1f, 3240 * 0.38f, airportLabel.height)

        val scrollTable = Table()
        scrollPane = ScrollPane(scrollTable)
        scrollPane.style.background = TerminalControl.skin.getDrawable("ListBackground")
        scrollPane.isVisible = false
        radarScreen.ui.addActor(scrollPane, 0.11f, 0.78f, 3240 * 0.055f, 3240 * 0.175f)

        val labelStyle1 = LabelStyle()
        labelStyle1.fontColor = Color.BLACK
        labelStyle1.font = Fonts.defaultFont20
        newRunwaysLabel = Label("Loading...", labelStyle1)
        newRunwaysLabel.wrap = true
        scrollTable.add(newRunwaysLabel).width(0.76f * radarScreen.ui.paneWidth).height(300f).pad(10f, 0.01f * radarScreen.ui.paneWidth, 10f, 0.11f * radarScreen.ui.paneWidth)

        val textButtonStyle = TextButtonStyle()
        textButtonStyle.font = Fonts.defaultFont20
        textButtonStyle.fontColor = Color.WHITE
        textButtonStyle.up = TerminalControl.skin.getDrawable("Button_up")
        textButtonStyle.down = TerminalControl.skin.getDrawable("Button_down")
        confirmButton = TextButton("Confirm runway change", textButtonStyle)
        confirmButton.align(Align.center)
        confirmButton.label.wrap = true
        confirmButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                if (runwayConfig != null) {
                    airport?.isPendingRwyChange = true
                    airport?.rwyChangeTimer = -1f
                    updateRunways()
                    hideAll()
                    radarScreen.utilityBox.setVisible(true)
                    radarScreen.ui.updateMetar()
                }
                event.handle()
            }

        })
        radarScreen.ui.addActor(confirmButton, 0.525f, 0.325f, 3240 * 0.25f, 3240 * 0.11f)

        changeButton = TextButton("Change runway configuration", textButtonStyle)
        changeButton.align(Align.center)
        changeButton.label.wrap = true
        changeButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                if (airport?.isPendingRwyChange == false && possibleConfigs.size > 0) {
                    runwayConfig = if (runwayConfig == null) possibleConfigs.first() else {
                        if (possibleConfigs.size > 1) possibleConfigs.add(possibleConfigs.removeIndex(0))
                        possibleConfigs.first()
                    }
                }
                confirmButton.isVisible = runwayConfig != null
                scrollPane.isVisible = true
                updateRunwayLabel()
                event.handle()
            }
        })
        radarScreen.ui.addActor(changeButton, 0.15f, 0.325f, 3240 * 0.25f, 3240 * 0.11f)

        var inputListener: InputListener? = null
        for (eventListener in scrollPane.listeners) {
            if (eventListener is InputListener) {
                inputListener = eventListener
            }
        }
        if (inputListener != null) scrollPane.removeListener(inputListener)

        isVisible = false
        hideAll()
    }

    fun update() {
        if (airport == null) return
        airport?.rwyChangeTimer?.let {
            if (it < 0) hideAll()
            val min = it.toInt() / 60
            val sec = it.toInt() - min * 60
            val secText = if (sec < 10) "0$sec" else sec.toString()
            changeButton.setText(if (airport?.isPendingRwyChange == true) "Time left: $min:$secText" else "Change runway configuration")
        }
    }

    fun setMainVisible(visible: Boolean) {
        isVisible = visible
        background.isVisible = visible
        airportLabel.isVisible = visible
        changeButton.isVisible = visible
    }

    fun hideAll() {
        isVisible = false
        background.isVisible = false
        airportLabel.isVisible = false
        changeButton.isVisible = false
        confirmButton.isVisible = false
        scrollPane.isVisible = false
        possibleConfigs.clear()
    }

    fun setAirport(icao: String) {
        runwayConfig = null
        possibleConfigs.clear()
        confirmButton.isVisible = false
        scrollPane.isVisible = false
        airportLabel.setText(icao)
        airport = radarScreen.airports[icao]
        airport?.metar?.let {
            val windHdg = it.optInt("windDirection", 0)
            var windSpd = it.getInt("windSpeed")
            if (windHdg == 0) windSpd = 0
            airport?.runwayManager?.getSuitableConfigs(windHdg, windSpd)?.let { configs ->
                for (config in configs) {
                    if (config.landingRunwayMap.keys == airport?.landingRunways?.keys && config.takeoffRunwayMap.keys == airport?.takeoffRunways?.keys) continue
                    possibleConfigs.add(config)
                }
            }
            //println(possibleConfigs)
        }

        if (airport?.isPendingRwyChange == true) {
            runwayConfig = airport?.runwayManager?.latestRunwayConfig
            updateRunwayLabel()
            confirmButton.isVisible = true
            scrollPane.isVisible = true
        }
    }

    private fun updateRunwayLabel() {
        if (runwayConfig == null && possibleConfigs.isEmpty) {
            newRunwaysLabel.setText("Runway change not permitted due to winds")
            return
        }
        val stringBuilder = StringBuilder()
        val rwySet = HashSet<String>()
        runwayConfig?.landingRunwayMap?.keys?.let {
            rwySet.addAll(it)
        }
        runwayConfig?.takeoffRunwayMap?.keys?.let {
            rwySet.addAll(it)
        }
        for (rwy in rwySet) {
            if (airport?.runways?.get(rwy)?.isEmergencyClosed == false && airport?.runways?.get(rwy)?.oppRwy?.isEmergencyClosed == false) {
                val tkof = runwayConfig?.takeoffRunwayMap?.containsKey(rwy)
                val ldg = runwayConfig?.landingRunwayMap?.containsKey(rwy)
                val tmp: String = if (tkof == true && ldg == true) {
                    "takeoffs and landings."
                } else if (tkof == true) {
                    "takeoffs."
                } else if (ldg == true) {
                    "landings."
                } else {
                    "nothing lol."
                }
                stringBuilder.append("Runway ")
                stringBuilder.append(rwy)
                stringBuilder.append(" will be active for ")
                stringBuilder.append(tmp)
                stringBuilder.append("\n")
            }
        }
        if (stringBuilder.isEmpty()) {
            newRunwaysLabel.setText("All runways are/will be closed")
            return
        }
        newRunwaysLabel.setText(stringBuilder.toString())
    }

    //Change runways
    private fun updateRunways() {
        airport?.rwyChangeTimer = -1f
        runwayConfig?.let {
            airport?.runwayManager?.latestRunwayConfig = it
        }
        runwayConfig?.applyConfig()
        airport?.updateZoneStatus()
        airport?.resetRwyChangeTimer()
    }

    fun containsLandingRunway(icao: String, rwy: String): Boolean {
        return icao == airport?.icao && runwayConfig?.landingRunwayMap?.containsKey(rwy) == true && confirmButton.isVisible
    }
}