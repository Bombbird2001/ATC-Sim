package com.bombbird.terminalcontrol.entities.approaches;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;
import com.bombbird.terminalcontrol.entities.Airport;
import com.bombbird.terminalcontrol.entities.Runway;
import com.bombbird.terminalcontrol.screens.GameScreen;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.MathTools;

public class LDA extends ILS {
    private Queue<int[]> nonPrecAlts;
    private Vector2 gsRing;
    private float lineUpDist;

    public LDA(Airport airport, String toParse) {
        super(airport, toParse);
    }

    /** Overrides method in ILS to also load the non precision approach altitudes */
    @Override
    public void parseInfo(String toParse) {
        super.parseInfo(toParse);

        nonPrecAlts = new Queue<int[]>();

        String[] info = toParse.split(",");
        lineUpDist = Float.parseFloat(info[8]);

        for (String s3: info[9].split("-")) {
            int[] altDist = new int[2];
            int index1 = 0;
            for (String s2 : s3.split(">")) {
                altDist[index1] = Integer.parseInt(s2);
                index1++;
            }
            nonPrecAlts.addLast(altDist);
        }
    }

    /** Calculates position of FAF on LOC course */
    private void calculateFAFRing() {
        gsRing = new Vector2(getX() + MathTools.nmToPixel(nonPrecAlts.last()[1]) * MathUtils.cosDeg(270 - getHeading() + RadarScreen.magHdgDev), getY() + MathTools.nmToPixel(nonPrecAlts.last()[1]) * MathUtils.sinDeg(270 - getHeading() + RadarScreen.magHdgDev));
    }

    /** Overrides method in ILS to ignore it */
    @Override
    public void calculateGsRings() {
    }

    /** Overrides method in ILS to draw FAF point on LOC course */

    @Override
    public void drawGsCircles() {
        GameScreen.shapeRenderer.circle(gsRing.x, gsRing.y, 8);
    }


    public Queue<int[]> getNonPrecAlts() {
        return nonPrecAlts;
    }

    public float getLineUpDist() {
        return lineUpDist;
    }
}
