package com.bombbird.terminalcontrol.entities;

import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.utilities.saving.FileLoader;
import com.bombbird.terminalcontrol.utilities.saving.GameSaver;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

public class UnlockManager {
    private static int planesLanded;
    public static final HashSet<String> unlocks = new HashSet<>();
    public static final LinkedHashMap<String, Integer> unlockList = new LinkedHashMap<>();
    public static final HashMap<String, String> unlockDescription = new HashMap<>();

    public static void loadUnlockList() {
        if (unlockList.size() == 0) {
            //TODO Add more achievements
            addUnlock("sweep1s", 100, "Unlock 1-second radar sweep");
            addUnlock("sweep4s", 200, "Unlock 4-second radar sweep");
            addUnlock("sweep8s", 300, "Unlock 8-second radar sweep");
            addUnlock("sweep0.5s", 400, "Unlock 0.5-second radar sweep");
            addUnlock("area30s", 500, "Unlock area penetration warning 30 seconds look-ahead");
            addUnlock("area1m", 600, "Unlock area penetration warning 1 minute look-ahead");
            addUnlock("area2m", 700, "Unlock area penetration warning 2 minutes look-ahead");
            addUnlock("collision30s", 800, "Unlock short term collision alert 30 seconds look-ahead");
            addUnlock("collision1m", 900, "Unlock short term collision alert 1 minute look-ahead");
            addUnlock("collision2m", 1000, "Unlock short term collision alert 2 minutes look-ahead");
        }
    }

    private static void addUnlock(String name, int planesNeeded, String description) {
        unlockList.put(name, planesNeeded);
        unlockDescription.put(name, description);
    }

    public static void loadStats() {
        loadUnlockList();
        JSONObject stats = FileLoader.loadStats();
        if (stats == null) {
            planesLanded = 0;
            //Load current saves and count stats
            JSONArray saves = FileLoader.loadSaves();
            for (int i = 0; i < saves.length(); i++) {
                JSONObject save = saves.getJSONObject(i);
                planesLanded += save.getInt("landings");
            }
        } else {
            //Load saved stats
            planesLanded = stats.getInt("planesLanded");
            JSONArray unlockArray = stats.getJSONArray("unlocks");
            for (int i = 0; i < unlockArray.length(); i++) {
                unlocks.add(unlockArray.getString(i));
            }
        }
        checkNewUnlocks();
        GameSaver.saveStats(planesLanded, unlocks);
    }

    public static boolean checkIncreaseLanded() {
        planesLanded++;
        boolean changed = checkNewUnlocks();
        GameSaver.saveStats(planesLanded, unlocks);

        return changed;
    }

    private static boolean checkNewUnlocks() {
        boolean newUnlock = false;
        for (String unlockName: unlockList.keySet()) {
            if (planesLanded >= unlockList.get(unlockName) && !unlocks.contains(unlockName)) {
                newUnlock = true;
                unlocks.add(unlockName);
            }
        }

        return newUnlock;
    }

    public static int getPlanesLanded() {
        return planesLanded;
    }

    public static Array<String> getSweepAvailable() {
        Array<String> sweeps = new Array<>();
        if (unlocks.contains("sweep0.5s")) sweeps.add("0.5s");
        if (unlocks.contains("sweep1s")) sweeps.add("1s");
        sweeps.add("2s");
        if (unlocks.contains("sweep4s")) sweeps.add("4s");
        if (unlocks.contains("sweep8s")) sweeps.add("8s");

        return sweeps;
    }
}
