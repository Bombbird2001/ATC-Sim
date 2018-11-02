package com.bombbird.terminalcontrol.entities.restrictions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.bombbird.terminalcontrol.screens.GameScreen;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class Obstacle extends Actor {
    private Polygon polygon;
    private int minAlt;
    private Label label;

    public Obstacle(float[] vertices, int minAlt, String text, int textX, int textY) {
        this.minAlt = minAlt;
        polygon = new Polygon(vertices);
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont12;
        labelStyle.fontColor = Color.GRAY;
        label = new Label(text, labelStyle);
        label.setPosition(textX, textY);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        label.draw(batch, 1);
    }

    public void renderShape() {
        GameScreen.shapeRenderer.setColor(Color.GRAY);
        GameScreen.shapeRenderer.polygon(polygon.getVertices());
    }

    //TODO: Test for conflict with aircraft
}