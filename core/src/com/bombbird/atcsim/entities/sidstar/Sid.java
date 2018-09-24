package com.bombbird.atcsim.entities.sidstar;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.bombbird.atcsim.entities.Waypoint;

public class Sid extends SidStar {
    private int[] initClimb;
    private Array<Integer> outboundHdg;

    public Sid(String name, Array<String> runways, int[] initClimb, Array<Integer> outboundHdg, Array<Waypoint> waypoints, Array<int[]> restrictions) {
        super(name, runways, waypoints, restrictions);
        this.initClimb = initClimb;
        this.outboundHdg = outboundHdg;
    }

    public int getOutboundHdg() {
        return outboundHdg.get(MathUtils.random(outboundHdg.size - 1));
    }

    public int[] getInitClimb() {
        return initClimb;
    }
}
