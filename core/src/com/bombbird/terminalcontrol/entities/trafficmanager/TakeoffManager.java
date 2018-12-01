package com.bombbird.terminalcontrol.entities.trafficmanager;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.RandomGenerator;
import com.bombbird.terminalcontrol.entities.Airport;
import com.bombbird.terminalcontrol.entities.Runway;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.MathTools;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class TakeoffManager {
    private Airport airport;

    private HashMap<String, String[]> nextAircraft;
    private HashMap<String, Aircraft> prevAircraft;
    private HashMap<String, Float> timers;

    private RadarScreen radarScreen;

    public TakeoffManager(Airport airport) {
        radarScreen = TerminalControl.radarScreen;

        this.airport = airport;
        nextAircraft = new HashMap<String, String[]>();
        prevAircraft = new HashMap<String, Aircraft>();
        timers = new HashMap<String, Float>();
        for (Runway runway: airport.getRunways().values()) {
            timers.put(runway.getName(), 180f);
            prevAircraft.put(runway.getName(), null);
        }
    }

    public TakeoffManager(Airport airport, JSONObject save) {
        radarScreen = TerminalControl.radarScreen;

        this.airport = airport;
        nextAircraft = new HashMap<String, String[]>();
        prevAircraft = new HashMap<String, Aircraft>();
        timers = new HashMap<String, Float>();
        for (Runway runway: airport.getRunways().values()) {
            JSONArray info = save.getJSONObject("nextAircraft").getJSONArray(runway.getName());
            if (info.length() == 2) {
                nextAircraft.put(runway.getName(), new String[]{info.getString(0), info.getString(1)});
            } else {
                nextAircraft.put(runway.getName(), null);
            }

            timers.put(runway.getName(), (float) save.getJSONObject("timers").getDouble(runway.getName()));
        }
    }

    /** Called after aircraft load during game load since aircrafts have not been loaded during the initial airport loading */
    public void updatePrevAcft(JSONObject save) {
        for (Runway runway : airport.getRunways().values()) {
            prevAircraft.put(runway.getName(), radarScreen.aircrafts.get(save.getJSONObject("prevAircraft").isNull(runway.getName()) ? null : save.getJSONObject("prevAircraft").getString(runway.getName())));
        }
    }

    public void update() {
        //Request takeoffs if takeoffs are less than 5 more than landings
        //Update the timers & next aircrafts to take off
        for (String rwy: timers.keySet()) {
            timers.put(rwy, timers.get(rwy) + Gdx.graphics.getDeltaTime());
            if (nextAircraft.get(rwy) == null) {
                String[] aircraftInfo = RandomGenerator.randomPlane(airport);
                nextAircraft.put(rwy, aircraftInfo);
            }
        }
        if (airport.getAirborne() - airport.getLandings() < 3) {
            if ("RCTP".equals(airport.getIcao())) {
                updateRCTP();
            } else if ("RCSS".equals(airport.getIcao())) {
                updateRCSS();
            }
        }
    }

    private void updateRCTP() {
        Runway runway = null;
        float dist = -1;
        for (Runway runway1: airport.getTakeoffRunways().values()) {
            float distance = runway1.getAircraftsOnAppr().size > 0 ? MathTools.pixelToNm(MathTools.distanceBetween(runway1.getAircraftsOnAppr().first().getX(), runway1.getAircraftsOnAppr().first().getY(), runway1.getX(), runway1.getY())) : 25;
            if (checkLanding(runway1) && checkLanding(runway1.getOppRwy()) && distance > dist) {
                if ("05L".equals(runway1.getName()) && checkPreceding("05L") && checkPreceding("05R") && checkPreceding("23R")) {
                    runway = runway1;
                    dist = distance;
                } else if ("05R".equals(runway1.getName()) && checkPreceding("05L") && checkPreceding("05R") && checkPreceding("23L")) {
                    runway = runway1;
                    dist = distance;
                } else if ("23L".equals(runway1.getName()) && checkPreceding("23L") && checkPreceding("23R") && checkPreceding("05R")) {
                    runway = runway1;
                    dist = distance;
                } else if ("23R".equals(runway1.getName()) && checkPreceding("23L") && checkPreceding("23R") && checkPreceding("05L")) {
                    runway = runway1;
                    dist = distance;
                }
            }
        }
        if (runway != null) {
            String callsign = nextAircraft.get(runway.getName())[0];
            radarScreen.newDeparture(callsign, nextAircraft.get(runway.getName())[1], airport, runway);
            prevAircraft.put(runway.getName(), radarScreen.aircrafts.get(callsign));
            nextAircraft.put(runway.getName(), null);
            timers.put(runway.getName(), 0f);
        }
    }

    private void updateRCSS() {
        Runway runway = null;
        float dist = -1;
        for (Runway runway1: airport.getTakeoffRunways().values()) {
            float distance = runway1.getAircraftsOnAppr().size > 0 ? MathTools.pixelToNm(MathTools.distanceBetween(runway1.getAircraftsOnAppr().first().getX(), runway1.getAircraftsOnAppr().first().getY(), runway1.getX(), runway1.getY())) : 25;
            if (checkPreceding("10") && checkPreceding("28") && checkLanding(runway1) && checkLanding(runway1.getOppRwy()) && distance > dist) {
                runway = runway1;
                dist = distance;
            }
        }
        if (runway != null) {
            String callsign = nextAircraft.get(runway.getName())[0];
            radarScreen.newDeparture(callsign, nextAircraft.get(runway.getName())[1], airport, runway);
            prevAircraft.put(runway.getName(), radarScreen.aircrafts.get(callsign));
            nextAircraft.put(runway.getName(), null);
            timers.put(runway.getName(), 0f);
        }
    }

    private boolean checkPreceding(String runway) {
        float additionalTime = 180 - 20 * (airport.getLandings() - airport.getAirborne()); //Additional time between departures when arrivals are not much higher than departures
        additionalTime = MathUtils.clamp(additionalTime, 0, 120);
        if (prevAircraft.get(runway) == null) {
            //If no aircraft has taken off before
            return true;
        } else if (nextAircraft.get(runway)[1].equals("M")) {
            if (prevAircraft.get(runway).getWakeCat() == 'H') {
                //Previous is heavy, minimum 120 sec
                return timers.get(runway) >= 120 + additionalTime;
            } else {
                //Previous is super, minimum 180 sec
                return timers.get(runway) >= 180 + additionalTime;
            }
        } else if (nextAircraft.get(runway)[1].equals("H") && prevAircraft.get(runway).getWakeCat() == 'J') {
            //Previous is super, minimum 120 sec
            return timers.get(runway) >= 120 + additionalTime;
        }
        return timers.get(runway) >= 90 + additionalTime;
    }

    private boolean checkLanding(Runway runway) {
        if (runway.getAircraftsOnAppr().size == 0) {
            //No aircraft on approach
            return true;
        } else {
            Aircraft aircraft = runway.getAircraftsOnAppr().first();
            return MathTools.pixelToNm(MathTools.distanceBetween(aircraft.getX(), aircraft.getY(), runway.getX(), runway.getY())) >= 5 && !aircraft.isOnGround() && runway.getOppRwy().getAircraftsOnAppr().size == 0;
        }
    }

    public HashMap<String, String[]> getNextAircraft() {
        return nextAircraft;
    }

    public HashMap<String, Aircraft> getPrevAircraft() {
        return prevAircraft;
    }

    public HashMap<String, Float> getTimers() {
        return timers;
    }
}