package com.bombbird.atcsim.entities.aircrafts;

import com.bombbird.atcsim.utilities.FileLoader;

import java.util.HashMap;

public abstract class AircraftType {
    private static HashMap<String, int[]> aircraftTypes = FileLoader.loadAircraftData();

    public static int[] getAircraftInfo(String icao) {
        return aircraftTypes.get(icao);
    }
}