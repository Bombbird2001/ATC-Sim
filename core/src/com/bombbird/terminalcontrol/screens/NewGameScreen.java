package com.bombbird.terminalcontrol.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class NewGameScreen implements Screen {
    //Init game (set in constructor)
    private final TerminalControl game;
    private Stage stage;
    private Table scrollTable;

    //Create new camera
    private OrthographicCamera camera;
    private Viewport viewport;

    NewGameScreen(final TerminalControl game) {
        this.game = game;

        //Set camera params
        camera = new OrthographicCamera();
        camera.setToOrtho(false,2880, 1620);
        viewport = new FitViewport(TerminalControl.WIDTH, TerminalControl.HEIGHT, camera);
        viewport.apply();

        //Set stage params
        stage = new Stage(new FitViewport(2880, 1620));
        stage.getViewport().update(TerminalControl.WIDTH, TerminalControl.HEIGHT, true);
        Gdx.input.setInputProcessor(stage);

        //Set table params (for scrollpane)
        scrollTable = new Table();
    }

    private void loadUI() {
        int buttonWidth = 1000;
        int buttonHeight = 200;

        //Reset stage
        stage.clear();

        //Set label params
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont20;
        labelStyle.fontColor = Color.WHITE;
        Label headerLabel = new Label("Choose airport:", labelStyle);
        headerLabel.setWidth(buttonWidth);
        headerLabel.setHeight(buttonHeight);
        headerLabel.setPosition(2880 / 2.0f - buttonWidth / 2.0f, 1620 * 0.85f);
        headerLabel.setAlignment(Align.center);
        stage.addActor(headerLabel);

        //Set button textures
        //Using main menu textures for now, will change later
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = Fonts.defaultFont12;
        buttonStyle.up = TerminalControl.skin.getDrawable("Button_up");
        buttonStyle.down = TerminalControl.skin.getDrawable("Button_down");

        //Load airports
        String[] airports = {"RCTP\nTaiwan Taoyuan International Airport", "WSSS\nSingapore Changi Airport", "VHHH\nHong Kong International Airport", "RJAA\nNarita International Airport", "WMKK\nKuala Lumpur International Airport", "WIII\nSoekarno-Hatta International Airport", "ZSPD\nShanghai Pudong International Airport", "VTBS\nBangkok Suvarnabhumi Airport", "VVTS\nTan Son Nhat International Airport"};
        for (String airport: airports) {
            final TextButton airportButton = new TextButton(airport, buttonStyle);
            airportButton.setName(airport.substring(0, 4));
            airportButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    String name = actor.getName();
                    game.setScreen(new RadarScreen(game, name));
                }
            });
            scrollTable.add(airportButton).width(buttonWidth * 1.2f).height(buttonHeight);
            scrollTable.row();
        }
        ScrollPane scrollPane = new ScrollPane(scrollTable);
        scrollPane.setupFadeScrollBars(1, 1.5f);
        scrollPane.setX(2880 / 2f - buttonWidth * 0.6f);
        scrollPane.setY(1620 * 0.2f);
        scrollPane.setWidth(buttonWidth * 1.2f);
        scrollPane.setHeight(1620 * 0.6f);

        stage.addActor(scrollPane);

        //Set back button params
        TextButton backButton = new TextButton("<- Back", buttonStyle);
        backButton.setWidth(buttonWidth);
        backButton.setHeight(buttonHeight);
        backButton.setPosition(2880 / 2.0f - buttonWidth / 2.0f, 1620 * 0.05f);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Go back to main menu
                game.setScreen(new MainMenuScreen(game));
            }
        });

        stage.addActor(backButton);
    }

    @Override
    public void show() {
        loadUI();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0.3f, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        game.batch.setProjectionMatrix(camera.combined);
        stage.act(delta);
        game.batch.begin();
        stage.draw();
        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        stage.getViewport().update(width, height, true);
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
    }

    @Override
    public void pause() {
        //Implements pause method of screen
    }

    @Override
    public void resume() {
        //Implements resume method of screen
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        stage.clear();
        stage.dispose();
    }
}