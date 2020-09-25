package com.bombbird.terminalcontrol.ui.tabs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival;
import com.bombbird.terminalcontrol.entities.aircrafts.Departure;
import com.bombbird.terminalcontrol.entities.aircrafts.Emergency;
import com.bombbird.terminalcontrol.entities.aircrafts.NavState;
import com.bombbird.terminalcontrol.ui.Ui;

public class AltTab extends Tab {

    private boolean altModeChanged;
    private boolean altChanged;
    private final Array<String> alts;

    public AltTab(Ui ui) {
        super(ui);
        alts = new Array<>();
    }

    public void loadModes() {
        modeButtons.addButton(NavState.SID_STAR_RESTR, "Climb via SID/Descend via STAR");
        modeButtons.addButton(NavState.NO_RESTR, "Unrestricted");
    }

    public void updateModeButtons() {
        modeButtons.changeButtonText(NavState.SID_STAR_RESTR, selectedAircraft instanceof Arrival ? "Descend via STAR" : "Climb via SID");
        modeButtons.setButtonColour(false);
    }

    @Override
    public void updateElements() {
        if (selectedAircraft == null) return;
        notListening = true;
        modeButtons.updateButtonActivity(selectedAircraft.getNavState().getAltModes());
        if (visible) {
            valueBox.setVisible(true);
        }
        alts.clear();
        int lowestAlt;
        int highestAlt = -1;
        Array<Integer> allAlts;
        if (selectedAircraft instanceof Departure) {
            lowestAlt = selectedAircraft.getLowestAlt();
            highestAlt = TerminalControl.radarScreen.maxAlt;
            if (altMode == NavState.SID_STAR_RESTR && selectedAircraft.getRoute().getWptMinAlt(LatTab.clearedWpt) > highestAlt) {
                highestAlt = selectedAircraft.getRoute().getWptMinAlt(LatTab.clearedWpt);
            }
            allAlts = createAltArray(lowestAlt, highestAlt);
        } else if (selectedAircraft instanceof Arrival) {
            lowestAlt = TerminalControl.radarScreen.minAlt;
            if (latMode == NavState.SID_STAR && !clearedILS.equals(Ui.NOT_CLEARED_APCH)) {
                lowestAlt = selectedAircraft.getRoute().getWptMinAlt(selectedAircraft.getRoute().getWaypoints().size - 1);
                if (lowestAlt == -1) lowestAlt = TerminalControl.radarScreen.minAlt;
                highestAlt = lowestAlt;
            } else if (latMode == NavState.HOLD_AT) {
                int[] restr = selectedAircraft.getRoute().getHoldProcedure().getAltRestAtWpt(TerminalControl.radarScreen.waypoints.get(LatTab.holdWpt));
                lowestAlt = restr[0];
                highestAlt = restr[1];
            } else if (altMode == NavState.SID_STAR_RESTR && selectedAircraft.getAltitude() < TerminalControl.radarScreen.maxAlt) {
                //Set alt restrictions in box
                highestAlt = (int) selectedAircraft.getAltitude();
                highestAlt -= highestAlt % 1000;
                int starHighestAlt = selectedAircraft.getDirect() == null ? TerminalControl.radarScreen.maxAlt : selectedAircraft.getRoute().getWptMaxAlt(selectedAircraft.getDirect().getName());
                if (starHighestAlt > -1) highestAlt = starHighestAlt;
            }
            if (highestAlt == -1) {
                highestAlt = TerminalControl.radarScreen.maxAlt;
            }
            if (highestAlt < (int) selectedAircraft.getAltitude() && (int) selectedAircraft.getAltitude() <= TerminalControl.radarScreen.maxAlt) {
                highestAlt = ((int) selectedAircraft.getAltitude()) / 1000 * 1000;
            }
            if (selectedAircraft.getEmergency().isActive() && selectedAircraft.getEmergency().getType() == Emergency.Type.PRESSURE_LOSS) {
                highestAlt = 10000; //Cannot climb above 10000 feet due to pressure loss
            }
            if (highestAlt < lowestAlt) highestAlt = lowestAlt;
            if (selectedAircraft.isGsCap() || (selectedAircraft.getIls() != null && selectedAircraft.getIls().isNpa() && selectedAircraft.isLocCap())) {
                highestAlt = lowestAlt = selectedAircraft.getIls().getMissedApchProc().getClimbAlt();
            }
            allAlts = createAltArray(lowestAlt, highestAlt);
            String icao = selectedAircraft.getAirport().getIcao();
            if ("TCOO".equals(icao)) {
                checkAndAddIntermediate(allAlts, selectedAircraft.getAltitude(),3500);
            } else if (("TCHH".equals(icao) && selectedAircraft.getSidStar().getRunways().contains("25R", false)) || "TCHX".equals(icao)) {
                checkAndAddIntermediate(allAlts, selectedAircraft.getAltitude(),4300);
                checkAndAddIntermediate(allAlts, selectedAircraft.getAltitude(),4500);
            }
            allAlts.sort();
        } else {
            lowestAlt = 0;
            highestAlt = 10000;
            allAlts = createAltArray(lowestAlt, highestAlt);
            Gdx.app.log("Invalid aircraft type", "Aircraft not instance of departure or arrival");
        }
        clearedAlt = MathUtils.clamp(clearedAlt, allAlts.first(), allAlts.get(allAlts.size - 1));
        //Adds the possible altitudes between range to array
        for (int alt: allAlts) {
            alts.add(alt / 100 >= TerminalControl.radarScreen.transLvl ? "FL" + alt / 100 : String.valueOf(alt));
        }
        valueBox.setItems(alts);
        valueBox.setSelected(clearedAlt / 100 >= TerminalControl.radarScreen.transLvl ? "FL" + clearedAlt / 100 : Integer.toString(clearedAlt));
        notListening = false;
    }

    public Array<Integer> createAltArray(int lowestAlt, int highestAlt) {
        Array<Integer> newAltArray = new Array<>();
        int start = lowestAlt;
        if (lowestAlt % 1000 != 0) {
            newAltArray.add(lowestAlt);
            start = ((lowestAlt / 1000) + 1) * 1000;
        }
        for (int i = start; i < highestAlt; i += 1000) {
            newAltArray.add(i);
        }
        newAltArray.add(highestAlt);
        return newAltArray;
    }

    public void checkAndAddIntermediate(Array<Integer> allAlts, float currentAlt, int altToAdd) {
        if (currentAlt < altToAdd - 20) return; //Current aircraft altitude must be at lowest 20 feet lower than altitude to add
        allAlts.add(altToAdd);
    }

    @Override
    public void compareWithAC() {
        altModeChanged = altMode != selectedAircraft.getNavState().getDispAltMode().last();
        altChanged = clearedAlt != selectedAircraft.getNavState().getClearedAlt().last();

        tabChanged = altModeChanged || altChanged;
    }

    @Override
    public void updateElementColours() {
        notListening = true;
        modeButtons.setButtonColour(altModeChanged);

        //Alt box colour
        if (altChanged) {
            valueBox.getStyle().fontColor = Color.YELLOW;
        } else {
            valueBox.getStyle().fontColor = Color.WHITE;
        }

        super.updateElementColours();
        notListening = false;
    }

    @Override
    public void updateMode() {
        if (selectedAircraft == null) return;
        if (selectedAircraft.getNavState() == null) return;
        selectedAircraft.getNavState().sendAlt(altMode, clearedAlt);
    }

    @Override
    public void getACState() {
        altMode = selectedAircraft.getNavState().getDispAltMode().last();
        modeButtons.setMode(altMode);
        altModeChanged = false;
        clearedAlt = selectedAircraft.getNavState().getClearedAlt().last();
        altChanged = false;
    }

    @Override
    public void getChoices() {
        altMode = modeButtons.getMode();
        clearedAlt = valueBox.getSelected().contains("FL") ? Integer.parseInt(valueBox.getSelected().substring(2)) * 100 : Integer.parseInt(valueBox.getSelected());
    }

    public boolean isAltChanged() {
        return altChanged;
    }
}
