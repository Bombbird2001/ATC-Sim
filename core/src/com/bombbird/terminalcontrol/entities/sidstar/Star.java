package com.bombbird.terminalcontrol.entities.sidstar;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.entities.procedures.HoldProcedure;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class Star extends SidStar {
    private Array<Array<String>> inbound;
    private HashMap<String, Array<Waypoint>> rwyWpts;
    private HashMap<String, Array<int[]>> rwyRestrictions;
    private HashMap<String, Array<Boolean>> rwyFlyOver;

    public Star(Airport airport, JSONObject jo) {
        super(airport, jo);
    }

    @Override
    public void parseInfo(JSONObject jo) {
        super.parseInfo(jo);

        inbound = new Array<Array<String>>();
        rwyWpts = new HashMap<String, Array<Waypoint>>();
        rwyRestrictions = new HashMap<String, Array<int[]>>();
        rwyFlyOver = new HashMap<String, Array<Boolean>>();

        JSONObject rwys = jo.getJSONObject("rwys");
        for (String rwy: rwys.keySet()) {
            getRunways().add(rwy);
            Array<Waypoint> wpts = new Array<Waypoint>();
            Array<int[]> restrictions = new Array<int[]>();
            Array<Boolean> flyOver = new Array<Boolean>();

            JSONArray rwyObject = rwys.getJSONArray(rwy);
            for (int i = 0; i < rwyObject.length(); i++) {
                String[] data = rwyObject.getString(i).split(" ");
                wpts.add(TerminalControl.radarScreen.waypoints.get(data[0]));
                restrictions.add(new int[] {Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3])});
                flyOver.add(data.length > 4 && data[4].equals("FO"));
            }
            rwyWpts.put(rwy, wpts);
            rwyRestrictions.put(rwy, restrictions);
            rwyFlyOver.put(rwy, flyOver);
        }

        JSONArray inbounds = jo.getJSONArray("inbound");
        for (int i = 0; i < inbounds.length(); i++) {
            JSONArray trans = inbounds.getJSONArray(i);
            Array<String> transData = new Array<String>();
            for (int j = 0; j < trans.length(); j++) {
                transData.add(trans.getString(j));
            }
            inbound.add(transData);
        }
    }

    public Array<String> getRandomInbound() {
        return inbound.get(MathUtils.random(inbound.size - 1));
    }

    public HoldProcedure getHoldProcedure() {
        return getAirport().getHoldProcedures().get(getName());
    }

    public Array<Waypoint> getRwyWpts(String runway) {
        return rwyWpts.get(runway);
    }

    public Array<int[]> getRwyRestrictions(String runway) {
        return rwyRestrictions.get(runway);
    }

    public Array<Boolean> getRwyFlyOver(String runway) {
        return rwyFlyOver.get(runway);
    }
}
