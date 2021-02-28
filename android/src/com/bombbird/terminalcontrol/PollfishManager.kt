package com.bombbird.terminalcontrol

import android.app.Activity
import android.content.Context
import com.badlogic.gdx.Game
import com.bombbird.terminalcontrol.screens.BasicScreen
import com.bombbird.terminalcontrol.screens.selectgamescreen.LoadGameScreen
import com.bombbird.terminalcontrol.screens.selectgamescreen.NewGameScreen
import com.bombbird.terminalcontrol.screens.selectgamescreen.SelectGameScreen
import com.bombbird.terminalcontrol.ui.dialogs.CustomDialog
import com.bombbird.terminalcontrol.utilities.SurveyAdsManager
import com.bombbird.terminalcontrol.utilities.Values
import com.pollfish.main.PollFish

class PollfishManager(private val activity: Activity, private val game: Game) {
    private var currentAirport = ""

    fun initPollfish() {
        val paramsBuilder = PollFish.ParamsBuilder(Values.POLLFISH_KEY).releaseMode(true).rewardMode(true)
            .pollfishCompletedSurveyListener {
                val newExpiry = SurveyAdsManager.getExpiryDateTime(6)
                val pref = activity.getPreferences(Context.MODE_PRIVATE)
                with (pref.edit()) {
                    for (arpt in SurveyAdsManager.unlockableAirports) {
                        putString(arpt, newExpiry)
                    }
                    apply()
                }
                SurveyAdsManager.loadData()
                game.screen?.let {
                    if (it is LoadGameScreen || it is NewGameScreen) {
                        CustomDialog("Survey", "Thank you for completing the survey -\nall airports are now unlocked for 6 hours from now", "", "Ok!").show((it as BasicScreen).stage)
                    }
                }
            }
            .pollfishUserNotEligibleListener {
                game.screen?.let {
                    if (it is LoadGameScreen || it is NewGameScreen) {
                        object : CustomDialog("Survey", "Sorry, you were not eligible for the survey\nWatch an ad to unlock $currentAirport for 1 hour?", "No", "Ok!") {
                            override fun result(resObj: Any?) {
                                if (resObj == DIALOG_POSITIVE) (it as? SelectGameScreen)?.showAdOrDialogOnFail(currentAirport)
                            }
                        }.show((it as BasicScreen).stage)
                    }
                }
            }
            .build()
        PollFish.initWith(activity, paramsBuilder)
    }

    fun showSurvey(airport: String) {
        currentAirport = airport
        if (PollFish.isPollfishPresent()) PollFish.show()
    }
}