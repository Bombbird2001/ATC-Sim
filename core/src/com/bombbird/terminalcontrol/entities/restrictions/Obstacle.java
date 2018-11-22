package com.bombbird.terminalcontrol.entities.restrictions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.utilities.Fonts;

import java.util.ArrayList;

public class Obstacle extends Actor {
    private Polygon polygon;
    private int minAlt;
    private Label label;

    public Obstacle(String toParse) {
        parseInfo(toParse);
    }

    /** Parses the input string into relevant data */
    private void parseInfo(String toParse) {
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont12;
        labelStyle.fontColor = Color.GRAY;

        String[] obsInfo = toParse.split(", ");
        int index = 0;
        ArrayList<Float> vertices = new ArrayList<Float>();
        for (String s1: obsInfo) {
            switch (index) {
                case 0: minAlt = Integer.parseInt(s1); break;
                case 1: label = new Label(s1, labelStyle); break;
                case 2: label.setX(Integer.parseInt(s1)); break;
                case 3: label.setY(Integer.parseInt(s1)); break;
                default: vertices.add(Float.parseFloat(s1));
            }
            index++;
        }
        int i = 0;
        float[] verts = new float[vertices.size()];
        for (float f: vertices) {
            verts[i++] = f;
        }
        polygon = new Polygon(verts);
    }

    /** Draws the obstacle label to screen */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        label.draw(batch, 1);
    }

    /** Renders the polygon of obstacle to screen */
    public void renderShape() {
        TerminalControl.radarScreen.shapeRenderer.setColor(Color.GRAY);
        TerminalControl.radarScreen.shapeRenderer.polygon(polygon.getVertices());
    }

    /** Checks whether the input aircraft is inside the polygon area */
    public boolean isIn(Aircraft aircraft) {
        return polygon.contains(aircraft.getX(), aircraft.getY());
    }

    public int getMinAlt() {
        return minAlt;
    }
}
