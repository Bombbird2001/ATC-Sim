package com.bombbird.terminalcontrol.entities.trafficmanager;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.AircraftType;
import com.bombbird.terminalcontrol.entities.waketurbulence.SeparationMatrix;
import com.bombbird.terminalcontrol.utilities.math.RandomGenerator;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.runways.Runway;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;
import com.bombbird.terminalcontrol.utilities.math.MathTools;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class TakeoffManager {
    private final Airport airport;

    private final HashMap<String, String[]> nextAircraft;
    private final HashMap<String, Aircraft> prevAircraft;
    private final HashMap<String, Float> timers;

    private final RadarScreen radarScreen;

    public TakeoffManager(Airport airport) {
        radarScreen = TerminalControl.radarScreen;

        this.airport = airport;
        nextAircraft = new HashMap<>();
        prevAircraft = new HashMap<>();
        timers = new HashMap<>();
        for (Runway runway: airport.getRunways().values()) {
            timers.put(runway.getName(), 180f);
            prevAircraft.put(runway.getName(), null);
        }
    }

    public TakeoffManager(Airport airport, JSONObject save) {
        radarScreen = TerminalControl.radarScreen;

        this.airport = airport;
        nextAircraft = new HashMap<>();
        prevAircraft = new HashMap<>();
        timers = new HashMap<>();
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

    /** Called after aircraft load during game load since aircraft have not been loaded during the initial airport loading */
    public void updatePrevAcft(JSONObject save) {
        for (Runway runway : airport.getRunways().values()) {
            prevAircraft.put(runway.getName(), save.getJSONObject("prevAircraft").isNull(runway.getName()) ? null : radarScreen.aircrafts.get(save.getJSONObject("prevAircraft").getString(runway.getName())));
        }
    }

    /** Update loop */
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
        if (airport.getAirborne() - airport.getLandings() < 5) {
            if ("TCTP".equals(airport.getIcao())) {
                updateTCTP();
            } else if ("TCSS".equals(airport.getIcao())) {
                updateTCSS();
            } else if ("TCWS".equals(airport.getIcao())) {
                updateTCWS();
            } else if ("TCTT".equals(airport.getIcao())) {
                updateTCTT();
            } else if ("TCAA".equals(airport.getIcao())) {
                updateTCAA();
            } else if ("TCBB".equals(airport.getIcao())) {
                updateTCBB();
            } else if ("TCOO".equals(airport.getIcao())) {
                updateTCOO();
            } else if ("TCBE".equals(airport.getIcao())) {
                updateTCBE();
            } else if ("TCHH".equals(airport.getIcao())) {
                updateTCHH();
            } else if ("TCMC".equals(airport.getIcao())) {
                updateTCMC();
            } else if ("TCBD".equals(airport.getIcao())) {
                updateTCBD();
            } else if ("TCBS".equals(airport.getIcao())) {
                updateTCBS();
            } else if ("TCMD".equals(airport.getIcao())) {
                updateTCMD();
            } else if ("TCPG".equals(airport.getIcao())) {
                updateTCPG();
            } else if ("TCPO".equals(airport.getIcao())) {
                updateTCPO();
            } else if ("TCHX".equals(airport.getIcao())) {
                updateTCHX();
            } else {
                Gdx.app.log("Takeoff manager", "Takeoff settings for " + airport.getIcao() + " are unavailable.");
            }
        }
    }

    /** Checks takeoff status for Taiwan Taoyuan */
    private void updateTCTP() {
        Runway runway = null;
        float dist = -1;
        for (Runway runway1: airport.getTakeoffRunways().values()) {
            float distance = runway1.getAircraftsOnAppr().size > 0 ? MathTools.pixelToNm(MathTools.distanceBetween(runway1.getAircraftsOnAppr().first().getX(), runway1.getAircraftsOnAppr().first().getY(), runway1.getX(), runway1.getY())) : 25;
            if (!runway1.isEmergencyClosed() && checkPreceding(runway1.getName()) && checkLanding(runway1) && checkOppLanding(runway1) && checkPreceding(runway1.getOppRwy().getName()) && (distance > dist || distance > 24.9)) {
                if ("05L".equals(runway1.getName()) && checkPreceding("05R") && checkOppLanding(airport.getRunways().get("05R"))) {
                    runway = runway1;
                    dist = distance;
                } else if ("05R".equals(runway1.getName()) && checkPreceding("05L") && checkOppLanding(airport.getRunways().get("05L"))) {
                    runway = runway1;
                    if (distance > 24.9) break;
                    dist = distance;
                } else if ("23L".equals(runway1.getName()) && checkPreceding("23R") && checkOppLanding(airport.getRunways().get("23R"))) {
                    runway = runway1;
                    if (distance > 24.9) break;
                    dist = distance;
                } else if ("23R".equals(runway1.getName()) && checkPreceding("23L") && checkOppLanding(airport.getRunways().get("23L"))) {
                    runway = runway1;
                    dist = distance;
                }
            }
        }
        updateRunway(runway);
    }

    /** Checks takeoff status for Taipei Songshan */
    private void updateTCSS() {
        Runway runway = null;
        float dist = -1;
        for (Runway runway1: airport.getTakeoffRunways().values()) {
            float distance = runway1.getAircraftsOnAppr().size > 0 ? MathTools.pixelToNm(MathTools.distanceBetween(runway1.getAircraftsOnAppr().first().getX(), runway1.getAircraftsOnAppr().first().getY(), runway1.getX(), runway1.getY())) : 25;
            if (!runway1.isEmergencyClosed() && checkPreceding("10") && checkPreceding("28") && checkLanding(runway1) && checkOppLanding(runway1) && distance > dist) {
                runway = runway1;
                dist = distance;
            }
        }
        updateRunway(runway);
    }

    /** Checks takeoff status for Singapore Changi */
    private void updateTCWS() {
        Runway runway = null;
        float dist = -1;
        for (Runway runway1: airport.getTakeoffRunways().values()) {
            float distance = runway1.getAircraftsOnAppr().size > 0 ? MathTools.pixelToNm(MathTools.distanceBetween(runway1.getAircraftsOnAppr().first().getX(), runway1.getAircraftsOnAppr().first().getY(), runway1.getX(), runway1.getY())) : 25;
            if (!runway1.isEmergencyClosed() && checkPreceding(runway1.getName()) && checkLanding(runway1) && checkOppLanding(runway1) && checkPreceding(runway1.getOppRwy().getName()) && (distance > dist || distance > 24.9)) {
                if ("02L".equals(runway1.getName()) && checkPreceding("02C") && checkOppLanding(airport.getRunways().get("02C"))) {
                    runway = runway1;
                    dist = distance;
                } else if ("02C".equals(runway1.getName()) && checkPreceding("02L") && checkOppLanding(airport.getRunways().get("02L"))) {
                    runway = runway1;
                    if (distance > 24.9) break;
                    dist = distance;
                } else if ("20C".equals(runway1.getName()) && checkPreceding("20R") && checkOppLanding(airport.getRunways().get("20R"))) {
                    runway = runway1;
                    if (distance > 24.9) break;
                    dist = distance;
                } else if ("20R".equals(runway1.getName()) && checkPreceding("20C") && checkOppLanding(airport.getRunways().get("20C"))) {
                    runway = runway1;
                    dist = distance;
                }
            }
        }
        updateRunway(runway);
    }

    /** Checks takeoff status for Tokyo Haneda */
    private void updateTCTT() {
        Runway runway = null;
        float dist = -1;
        for (Runway runway1: airport.getTakeoffRunways().values()) {
            float distance = runway1.getAircraftsOnAppr().size > 0 ? MathTools.pixelToNm(MathTools.distanceBetween(runway1.getAircraftsOnAppr().first().getX(), runway1.getAircraftsOnAppr().first().getY(), runway1.getX(), runway1.getY())) : 25;
            if (!runway1.isEmergencyClosed() && checkPreceding(runway1.getName()) && checkLanding(runway1) && checkOppLanding(runway1) && checkPreceding(runway1.getOppRwy().getName()) && (distance > dist || distance > 24.9)) {
                if (airport.allowSimultDep() && timers.get("05") >= 60 && "34R".equals(runway1.getName()) && checkOppLanding(airport.getRunways().get("04")) && checkOppLanding(airport.getRunways().get("05"))) {
                    //Additional check for runway 05 departure - 60 seconds apart
                    //Don't use 34R if departure volume is low
                    runway = runway1;
                    dist = distance;
                } else if ((!airport.allowSimultDep() || timers.get("34R") >= 60) && "05".equals(runway1.getName()) && checkOppLanding(airport.getRunways().get("04")) && checkPreceding("16L") && checkPreceding("16R")) {
                    //Additional check for runway 34R departure - 60 seconds apart
                    //Additional check if aircraft landing on 34R has touched down; is no longer in conflict with 05
                    boolean tkof = false;
                    Runway r34r = airport.getRunways().get("34R");
                    if (r34r.getAircraftsOnAppr().size == 0) {
                        tkof = true;
                    } else {
                        int index = 0;
                        while (index < r34r.getAircraftsOnAppr().size) {
                            Aircraft aircraft = r34r.getAircraftsOnAppr().get(index);
                            if (aircraft.isOnGround()) {
                                tkof = true;
                            } else {
                                tkof = !(MathTools.pixelToNm(MathTools.distanceBetween(aircraft.getX(), aircraft.getY(), r34r.getX(), r34r.getY())) < 6);
                                break;
                            }
                            index++;
                        }
                    }
                    if (tkof) {
                        runway = runway1;
                        if (distance > 24.9) break;
                        dist = distance;
                    }
                } else if ("16L".equals(runway1.getName()) && checkLandingTCTT23() && checkOppLanding(airport.getRunways().get("16R")) && checkPreceding("05")) {
                    runway = runway1;
                    if (distance > 24.9) break;
                    dist = distance;
                } else if ((airport.getTakeoffRunways().containsKey("22") || airport.allowSimultDep()) && "16R".equals(runway1.getName()) && checkLandingTCTT23() && checkOppLanding(airport.getRunways().get("16L")) && checkPreceding("05")) {
                    //Use 16R only if departure volume is low, or if other departure runway in use is 22
                    runway = runway1;
                    if (airport.getTakeoffRunways().containsKey("22") && distance > 24.9) break;
                    dist = distance;
                } else if (airport.allowSimultDep() && "22".equals(runway1.getName()) && checkOppLanding(airport.getRunways().get("34L"))) {
                    //Use only 16R if departure volume is low
                    runway = runway1;
                    dist = distance;
                }
            }
        }
        updateRunway(runway);
    }

    /** Checks takeoff status for Tokyo Narita */
    private void updateTCAA() {
        Runway runway = null;
        float dist = -1;
        for (Runway runway1: airport.getTakeoffRunways().values()) {
            float distance = runway1.getAircraftsOnAppr().size > 0 ? MathTools.pixelToNm(MathTools.distanceBetween(runway1.getAircraftsOnAppr().first().getX(), runway1.getAircraftsOnAppr().first().getY(), runway1.getX(), runway1.getY())) : 25;
            if (!runway1.isEmergencyClosed() && checkPreceding(runway1.getName()) && checkLanding(runway1) && checkOppLanding(runway1) && checkPreceding(runway1.getOppRwy().getName()) && (distance > dist || distance > 24.9)) {
                if ("16L".equals(runway1.getName()) && checkOppLanding(airport.getRunways().get("16R")) && (checkPreceding("16R") || airport.allowSimultDep())) {
                    //Prefer 16R if departure volume is low
                    runway = runway1;
                    dist = distance;
                } else if ("16R".equals(runway1.getName()) && checkOppLanding(airport.getRunways().get("16L")) && (checkPreceding("16L") || airport.allowSimultDep())) {
                    runway = runway1;
                    if (distance > 24.9) break;
                    dist = distance;
                } else if ("34L".equals(runway1.getName()) && checkOppLanding(airport.getRunways().get("34R")) && (checkPreceding("34R") || airport.allowSimultDep())) {
                    runway = runway1;
                    if (distance > 24.9) break;
                    dist = distance;
                } else if ("34R".equals(runway1.getName()) && checkOppLanding(airport.getRunways().get("34L")) && (checkPreceding("34L") || airport.allowSimultDep())) {
                    //Prefer 34L if departure volume is low
                    runway = runway1;
                    dist = distance;
                }
            }
        }
        updateRunway(runway);
    }

    /** Checks takeoff status for Osaka Kansai */
    private void updateTCBB() {
        Runway runway = null;
        float dist = -1;
        for (Runway runway1: airport.getTakeoffRunways().values()) {
            float distance = runway1.getAircraftsOnAppr().size > 0 ? MathTools.pixelToNm(MathTools.distanceBetween(runway1.getAircraftsOnAppr().first().getX(), runway1.getAircraftsOnAppr().first().getY(), runway1.getX(), runway1.getY())) : 25;
            if (!runway1.isEmergencyClosed() && checkPreceding(runway1.getName()) && checkLanding(runway1) && checkOppLanding(runway1) && checkPreceding(runway1.getOppRwy().getName()) && (distance > dist || distance > 24.9)) {
                if ("06L".equals(runway1.getName()) && checkPreceding("06R") && checkOppLanding(airport.getRunways().get("06R"))) {
                    runway = runway1;
                    dist = distance;
                } else if ("06R".equals(runway1.getName()) && checkPreceding("06L") && checkOppLanding(airport.getRunways().get("06L"))) {
                    runway = runway1;
                    if (distance > 24.9) break;
                    dist = distance;
                } else if ("24L".equals(runway1.getName()) && checkPreceding("24R") && checkOppLanding(airport.getRunways().get("24R"))) {
                    runway = runway1;
                    if (distance > 24.9) break;
                    dist = distance;
                } else if ("24R".equals(runway1.getName()) && checkPreceding("24L") && checkOppLanding(airport.getRunways().get("24L"))) {
                    runway = runway1;
                    dist = distance;
                }
            }
        }
        updateRunway(runway);
    }

    /** Checks takeoff status for Osaka Itami */
    private void updateTCOO() {
        Runway runway = airport.getRunways().get("32L");
        if (!runway.isEmergencyClosed() && runway.isTakeoff() && checkPreceding("32L") && checkLanding(runway)) {
            updateRunway(runway);
        }
    }

    /** Checks takeoff status for Kobe */
    private void updateTCBE() {
        Runway runway = airport.getRunways().get("09");
        if (!runway.isEmergencyClosed() && runway.isTakeoff() && checkPreceding("09") && checkLanding(runway)) {
            updateRunway(runway);
        }
    }

    /** Checks takeoff status for Hong Kong */
    private void updateTCHH() {
        Runway runway = null;
        float dist = -1;
        for (Runway runway1: airport.getTakeoffRunways().values()) {
            float distance = runway1.getAircraftsOnAppr().size > 0 ? MathTools.pixelToNm(MathTools.distanceBetween(runway1.getAircraftsOnAppr().first().getX(), runway1.getAircraftsOnAppr().first().getY(), runway1.getX(), runway1.getY())) : 25;
            if (!runway1.isEmergencyClosed() && checkPreceding(runway1.getName()) && checkLanding(runway1) && checkOppLanding(runway1) && checkPreceding(runway1.getOppRwy().getName()) && (distance > dist || distance > 24.9)) {
                if ("07L".equals(runway1.getName()) && checkPreceding("07R") && checkOppLanding(airport.getRunways().get("07R"))) {
                    runway = runway1;
                    dist = distance;
                } else if ("07R".equals(runway1.getName()) && checkPreceding("07L") && checkOppLanding(airport.getRunways().get("07L"))) {
                    runway = runway1;
                    if (distance > 24.9) break;
                    dist = distance;
                } else if ("25L".equals(runway1.getName()) && checkPreceding("25R") && checkOppLanding(airport.getRunways().get("25R"))) {
                    runway = runway1;
                    if (distance > 24.9) break;
                    dist = distance;
                } else if ("25R".equals(runway1.getName()) && checkPreceding("25L") && checkOppLanding(airport.getRunways().get("25L"))) {
                    runway = runway1;
                    dist = distance;
                }
            }
        }
        updateRunway(runway);
    }

    /** Checks takeoff status for Macau */
    private void updateTCMC() {
        Runway runway = null;
        float dist = -1;
        for (Runway runway1: airport.getTakeoffRunways().values()) {
            float distance = runway1.getAircraftsOnAppr().size > 0 ? MathTools.pixelToNm(MathTools.distanceBetween(runway1.getAircraftsOnAppr().first().getX(), runway1.getAircraftsOnAppr().first().getY(), runway1.getX(), runway1.getY())) : 25;
            if (!runway1.isEmergencyClosed() && checkPreceding("16") && checkPreceding("34") && checkLanding(runway1) && checkOppLanding(runway1) && distance > dist) {
                runway = runway1;
                dist = distance;
            }
        }
        updateRunway(runway);
    }

    /** Checks takeoff status for Bangkok Don Mueang */
    private void updateTCBD() {
        Runway runway = null;
        float dist = -1;
        for (Runway runway1: airport.getTakeoffRunways().values()) {
            float distance = runway1.getAircraftsOnAppr().size > 0 ? MathTools.pixelToNm(MathTools.distanceBetween(runway1.getAircraftsOnAppr().first().getX(), runway1.getAircraftsOnAppr().first().getY(), runway1.getX(), runway1.getY())) : 25;
            if (!runway1.isEmergencyClosed() && checkPreceding(runway1.getName()) && checkLanding(runway1) && checkOppLanding(runway1) && checkPreceding(runway1.getOppRwy().getName()) && distance > dist) {
                if ("03R".equals(runway1.getName()) && checkOppLanding(airport.getRunways().get("03L"))) {
                    runway = runway1;
                    dist = distance;
                } else if ("21L".equals(runway1.getName()) && checkOppLanding(airport.getRunways().get("21R"))) {
                    runway = runway1;
                    dist = distance;
                }
            }
        }
        updateRunway(runway);
    }

    /** Checks takeoff status for Bangkok Suvarnabhumi */
    private void updateTCBS() {
        Runway runway = null;
        float dist = -1;
        for (Runway runway1: airport.getTakeoffRunways().values()) {
            float distance = runway1.getAircraftsOnAppr().size > 0 ? MathTools.pixelToNm(MathTools.distanceBetween(runway1.getAircraftsOnAppr().first().getX(), runway1.getAircraftsOnAppr().first().getY(), runway1.getX(), runway1.getY())) : 25;
            if (!runway1.isEmergencyClosed() && checkPreceding(runway1.getName()) && checkLanding(runway1) && checkOppLanding(runway1) && checkPreceding(runway1.getOppRwy().getName()) && (distance > dist || distance > 24.9)) {
                if ("01L".equals(runway1.getName()) && checkPreceding("01R") && checkOppLanding(airport.getRunways().get("01R"))) {
                    runway = runway1;
                    if (distance > 24.9) break;
                    dist = distance;
                } else if ("01R".equals(runway1.getName()) && checkPreceding("01L") && checkOppLanding(airport.getRunways().get("01L"))) {
                    runway = runway1;
                    dist = distance;
                } else if ("19L".equals(runway1.getName()) && checkPreceding("19R") && checkOppLanding(airport.getRunways().get("19R"))) {
                    runway = runway1;
                    dist = distance;
                } else if ("19R".equals(runway1.getName()) && checkPreceding("19L") && checkOppLanding(airport.getRunways().get("19L"))) {
                    runway = runway1;
                    if (distance > 24.9) break;
                    dist = distance;
                }
            }
        }
        updateRunway(runway);
    }

    /** Checks takeoff status for Madrid Barajas */
    private void updateTCMD() {
        Runway runway = null;
        float dist = -1;
        for (Runway runway1: airport.getTakeoffRunways().values()) {
            float distance = runway1.getAircraftsOnAppr().size > 0 ? MathTools.pixelToNm(MathTools.distanceBetween(runway1.getAircraftsOnAppr().first().getX(), runway1.getAircraftsOnAppr().first().getY(), runway1.getX(), runway1.getY())) : 25;
            if (!runway1.isEmergencyClosed() && checkPreceding(runway1.getName()) && checkLanding(runway1) && checkOppLanding(runway1) && checkPreceding(runway1.getOppRwy().getName()) && (distance > dist || distance > 24.9)) {
                if ("36L".equals(runway1.getName()) && checkOppLanding(airport.getRunways().get("36R"))) {
                    runway = runway1;
                    dist = distance;
                } else if (airport.allowSimultDep() && "36R".equals(runway1.getName()) && checkOppLanding(airport.getRunways().get("36L"))) {
                    //Use only 36L if departure volume is low
                    runway = runway1;
                    dist = distance;
                } else if ("14L".equals(runway1.getName()) && checkOppLanding(airport.getRunways().get("14R"))) {
                    runway = runway1;
                    dist = distance;
                } else if (airport.allowSimultDep() && "14R".equals(runway1.getName()) && checkOppLanding(airport.getRunways().get("14L"))) {
                    //Use only 14L if departure volume is low
                    runway = runway1;
                    dist = distance;
                }
            }
        }
        updateRunway(runway);
    }

    /** Checks takeoff status for Paris Charles de Gaulle */
    private void updateTCPG() {
        Runway runway = null;
        float dist = -1;
        for (Runway runway1: airport.getTakeoffRunways().values()) {
            float distance = runway1.getAircraftsOnAppr().size > 0 ? MathTools.pixelToNm(MathTools.distanceBetween(runway1.getAircraftsOnAppr().first().getX(), runway1.getAircraftsOnAppr().first().getY(), runway1.getX(), runway1.getY())) : 25;
            if (!runway1.isEmergencyClosed() && checkPreceding(runway1.getName()) && checkLanding(runway1) && checkOppLanding(runway1) && checkPreceding(runway1.getOppRwy().getName()) && (distance > dist || distance > 24.9)) {
                if ("26R".equals(runway1.getName()) && checkOppLanding(airport.getRunways().get("26L"))) {
                    runway = runway1;
                    dist = distance;
                } else if (airport.allowSimultDep() && "27L".equals(runway1.getName()) && checkOppLanding(airport.getRunways().get("27R"))) {
                    //Use only 26R if departure volume is low
                    runway = runway1;
                    dist = distance;
                } else if ("08L".equals(runway1.getName()) && checkOppLanding(airport.getRunways().get("08R"))) {
                    runway = runway1;
                    dist = distance;
                } else if (airport.allowSimultDep() && "09R".equals(runway1.getName()) && checkOppLanding(airport.getRunways().get("09L"))) {
                    //Use only 08L if departure volume is low
                    runway = runway1;
                    dist = distance;
                }
            }
        }
        updateRunway(runway);
    }

    /** Checks takeoff status for Paris Orly */
    private void updateTCPO() {
        Runway runway = null;
        float dist = -1;
        for (Runway runway1: airport.getTakeoffRunways().values()) {
            float distance = runway1.getAircraftsOnAppr().size > 0 ? MathTools.pixelToNm(MathTools.distanceBetween(runway1.getAircraftsOnAppr().first().getX(), runway1.getAircraftsOnAppr().first().getY(), runway1.getX(), runway1.getY())) : 25;
            if (!runway1.isEmergencyClosed() && checkPreceding(runway1.getName()) && checkLanding(runway1) && checkOppLanding(runway1) && checkPreceding(runway1.getOppRwy().getName()) && (distance > dist || distance > 24.9)) {
                if ("07".equals(runway1.getName()) && checkOppLanding(airport.getRunways().get("06"))) {
                    runway = runway1;
                    dist = distance;
                } else if ("24".equals(runway1.getName()) && checkOppLanding(airport.getRunways().get("25"))) {
                    runway = runway1;
                    dist = distance;
                }
            }
        }
        updateRunway(runway);
    }

    /** Checks takeoff status for Kai Tak (old Hong Kong airport) */
    private void updateTCHX() {
        Runway runway = null;
        float dist = -1;
        for (Runway runway1: airport.getTakeoffRunways().values()) {
            float distance = runway1.getAircraftsOnAppr().size > 0 ? MathTools.pixelToNm(MathTools.distanceBetween(runway1.getAircraftsOnAppr().first().getX(), runway1.getAircraftsOnAppr().first().getY(), runway1.getX(), runway1.getY())) : 25;
            if (!runway1.isEmergencyClosed() && checkPreceding("13") && checkPreceding("31") && checkLanding(runway1) && checkOppLanding(runway1) && distance > dist) {
                runway = runway1;
                dist = distance;
            }
        }
        updateRunway(runway);
    }

    /** Checks whether airport has available runways for takeoff, updates hashMap and timer if available */
    private void updateRunway(Runway runway) {
        if (runway != null && radarScreen.getTfcMode() == RadarScreen.TfcMode.NORMAL) {
            String callsign = nextAircraft.get(runway.getName())[0];
            radarScreen.newDeparture(callsign, nextAircraft.get(runway.getName())[1], airport, runway);
            prevAircraft.put(runway.getName(), radarScreen.aircrafts.get(callsign));
            nextAircraft.put(runway.getName(), null);
            timers.put(runway.getName(), 0f);
        }
    }

    /** Check the previous departure aircraft */
    private boolean checkPreceding(String runway) {
        float additionalTime = 100 - 15 * (airport.getLandings() - airport.getAirborne()); //Additional time between departures when arrivals are not much higher than departures
        additionalTime = MathUtils.clamp(additionalTime, 0, 150);
        return prevAircraft.get(runway) == null || timers.get(runway) > SeparationMatrix.getTakeoffSepTime(prevAircraft.get(runway).getRecat(), AircraftType.getRecat(nextAircraft.get(runway)[1])) + additionalTime;
    }

    /** Check for any landing aircrafts */
    private boolean checkLanding(Runway runway) {
        if (runway.getAircraftsOnAppr().size == 0) {
            //No aircraft on approach
            return true;
        } else {
            Aircraft aircraft = runway.getAircraftsOnAppr().first();
            return MathTools.pixelToNm(MathTools.distanceBetween(aircraft.getX(), aircraft.getY(), runway.getX(), runway.getY())) >= 5 && !aircraft.isOnGround() && runway.getOppRwy().getAircraftsOnAppr().size == 0;
        }
    }

    /** Checks specific case for TCTT's runway 23, same as above function but will also return true if aircraft has landed */
    private boolean checkLandingTCTT23() {
        if (!"TCTT".equals(airport.getIcao())) return false;
        Runway runway = airport.getRunways().get("23");
        if (runway == null) return false;
        if (runway.getAircraftsOnAppr().size == 0) {
            //No aircraft on approach
            return true;
        } else {
            Aircraft aircraft = runway.getAircraftsOnAppr().first();
            Aircraft aircraft1 = null;
            if (runway.getAircraftsOnAppr().size > 1) aircraft1 = runway.getAircraftsOnAppr().get(1);
            if (runway.getOppRwy().getAircraftsOnAppr().size == 0) {
                //No planes landing opposite
                if (MathTools.pixelToNm(MathTools.distanceBetween(aircraft.getX(), aircraft.getY(), runway.getX(), runway.getY())) >= 5 && !aircraft.isOnGround()) {
                    //If latest aircraft is more than 5 miles away and not landed yet
                    return true;
                } else {
                    //If first aircraft has touched down, 2nd aircraft is non-existent OR is more than 5 miles away
                    return aircraft.isOnGround() && (aircraft1 == null || !aircraft1.isOnGround() && MathTools.pixelToNm(MathTools.distanceBetween(aircraft1.getX(), aircraft1.getY(), runway.getX(), runway.getY())) >= 5);
                }
            }
            return false;
        }
    }

    /** Check for any aircrafts landing on opposite runway */
    private boolean checkOppLanding(Runway runway) {
        return runway.getOppRwy().getAircraftsOnAppr().size == 0;
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
