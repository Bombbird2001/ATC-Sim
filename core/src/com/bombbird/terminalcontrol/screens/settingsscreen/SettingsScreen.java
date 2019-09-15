package com.bombbird.terminalcontrol.screens.settingsscreen;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class SettingsScreen implements Screen {
    public final TerminalControl game;

    public Stage stage;
    public OrthographicCamera camera;
    public Viewport viewport;

    public SelectBox.SelectBoxStyle selectBoxStyle;
    public SelectBox<String> trajectoryLine;
    public SelectBox<String> weather;
    public SelectBox<String> sound;

    public TextButton confirmButton;
    public TextButton cancelButton;

    public Label trajectoryLabel;
    public int trajectorySel;

    public Label weatherLabel;
    public boolean weatherSel;

    public Label soundLabel;
    public int soundSel;

    public SettingsScreen(final TerminalControl game) {
        this.game = game;

        //Set camera params
        camera = new OrthographicCamera();
        camera.setToOrtho(false,5760, 3240);
        viewport = new FitViewport(TerminalControl.WIDTH, TerminalControl.HEIGHT, camera);
        viewport.apply();

        //Set stage params
        stage = new Stage(new FitViewport(5760, 3240));
        stage.getViewport().update(TerminalControl.WIDTH, TerminalControl.HEIGHT, true);
        Gdx.input.setInputProcessor(stage);
    }

    public void loadUI(int xOffset, int yOffset) {
        loadStyles();

        loadBoxes(xOffset, yOffset);

        loadButton();

        loadLabel();
    }

    /** Loads the styles for the selectBox */
    public void loadStyles() {
        ScrollPane.ScrollPaneStyle paneStyle = new ScrollPane.ScrollPaneStyle();
        paneStyle.background = TerminalControl.skin.getDrawable("ListBackground");

        List.ListStyle listStyle = new List.ListStyle();
        listStyle.font = Fonts.defaultFont20;
        listStyle.fontColorSelected = Color.WHITE;
        listStyle.fontColorUnselected = Color.BLACK;
        Drawable button_down = TerminalControl.skin.getDrawable("Button_down");
        button_down.setTopHeight(75);
        button_down.setBottomHeight(75);
        listStyle.selection = button_down;

        selectBoxStyle = new SelectBox.SelectBoxStyle();
        selectBoxStyle.font = Fonts.defaultFont20;
        selectBoxStyle.fontColor = Color.WHITE;
        selectBoxStyle.listStyle = listStyle;
        selectBoxStyle.scrollStyle = paneStyle;
        selectBoxStyle.background = TerminalControl.skin.getDrawable("Button_up");
    }

    /** Loads selectBox for settings */
    public void loadBoxes(int xOffset, int yOffset) {
        trajectoryLine = new SelectBox<String>(selectBoxStyle);
        Array<String> options = new Array<String>(3);
        options.add("60 sec", "90 sec", "120 sec", "150 sec");
        trajectoryLine.setItems(options);
        trajectoryLine.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                trajectorySel = Integer.parseInt(trajectoryLine.getSelected().split(" ")[0]);
            }
        });
        trajectoryLine.setSize(1200, 300);
        trajectoryLine.setPosition(5760 / 2f - 400 + xOffset, 3240 * 0.8f + yOffset);
        trajectoryLine.setAlignment(Align.center);
        trajectoryLine.getList().setAlignment(Align.center);
        stage.addActor(trajectoryLine);

        weather = new SelectBox<String>(selectBoxStyle);
        Array<String> options1 = new Array<String>(2);
        options1.add("Live weather", "Random weather"); //TODO Add custom weather in future
        weather.setItems(options1);
        weather.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                weatherSel = "Live weather".equals(weather.getSelected());
            }
        });
        weather.setSize(1200, 300);
        weather.setPosition(5760 / 2f - 400 + xOffset, 3240 * 0.6f + yOffset);
        weather.setAlignment(Align.center);
        weather.getList().setAlignment(Align.center);
        stage.addActor(weather);

        sound = new SelectBox<String>(selectBoxStyle);
        Array<String> options2 = new Array<String>(2);
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            options2.add("Pilot voices + sound effects", "Sound effects only", "Off");
        } else if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            options2.add("Sound effects", "Off");
        }
        sound.setItems(options2);
        sound.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if ("Pilot voices + sound effects".equals(sound.getSelected())) {
                    soundSel = 2;
                } else if ("Sound effects".equals(sound.getSelected()) || "Sound effects only".equals(sound.getSelected())) {
                    soundSel = 1;
                } else if ("Off".equals(sound.getSelected())) {
                    soundSel = 0;
                }
            }
        });
        sound.setSize(1200, 300);
        sound.setPosition(5760 / 2f - 400 + xOffset, 3240 * 0.4f + yOffset);
        sound.setAlignment(Align.center);
        sound.getList().setAlignment(Align.center);
        stage.addActor(sound);
    }

    /** Loads buttons */
    public void loadButton() {
        //No default, depends on type of settings screen
    }

    /** Loads labels */
    public void loadLabel() {
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont20;
        labelStyle.fontColor = Color.WHITE;

        trajectoryLabel = new Label("Trajectory line timing: ", labelStyle);
        trajectoryLabel.setPosition(trajectoryLine.getX() - 100 - trajectoryLabel.getWidth(), trajectoryLine.getY() + trajectoryLine.getHeight() / 2 - trajectoryLabel.getHeight() / 2);
        stage.addActor(trajectoryLabel);

        weatherLabel = new Label("Weather: ", labelStyle);
        weatherLabel.setPosition(weather.getX() - 100 - weatherLabel.getWidth(), weather.getY() + weather.getHeight() / 2 - weatherLabel.getHeight() / 2);
        stage.addActor(weatherLabel);

        soundLabel = new Label("Sounds: ", labelStyle);
        soundLabel.setPosition(sound.getX() - 100 - soundLabel.getWidth(), sound.getY() + sound.getHeight() / 2 - soundLabel.getHeight() / 2);
        stage.addActor(soundLabel);
    }

    /** Sets relevant options into select boxes */
    public void setOptions() {
        trajectoryLine.setSelected(trajectorySel + " sec");
        weather.setSelectedIndex(weatherSel ? 0 : 1);
        int soundIndex = (Gdx.app.getType() == Application.ApplicationType.Android ? 2 : 1) - soundSel;
        if (soundIndex < 0) soundIndex = 0;
        sound.setSelectedIndex(soundIndex);
    }

    /** Confirms and applies the changes set */
    public void sendChanges() {
        //No default implementation
    }

    @Override
    public void show() {
        if (Fonts.defaultFont6 == null) {
            //Regenerate fonts that were disposed
            Fonts.generateAllFonts();
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        game.batch.setProjectionMatrix(camera.combined);
        stage.act(delta);
        game.batch.begin();
        boolean success = false;
        while (!success) {
            try {
                stage.draw();
                game.batch.end();
                success = true;
            } catch (IndexOutOfBoundsException e) {
                Gdx.app.log("SettingsScreen", "stage.draw() render error");
                stage.getBatch().end();
                e.printStackTrace();
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        stage.getViewport().update(width, height, true);
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
    }

    @Override
    public void pause() {
        //No default implementation
    }

    @Override
    public void resume() {
        //No default implementation
    }

    @Override
    public void hide() {
        //No default implementation
    }

    @Override
    public void dispose() {

    }
}