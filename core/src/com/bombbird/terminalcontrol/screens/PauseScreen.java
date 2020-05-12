package com.bombbird.terminalcontrol.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.screens.gamescreen.GameScreen;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;
import com.bombbird.terminalcontrol.screens.settingsscreen.CategorySelectScreen;
import com.bombbird.terminalcontrol.utilities.saving.GameSaver;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class PauseScreen extends BasicScreen {
    private final GameScreen gameScreen;

    public PauseScreen(TerminalControl game, GameScreen gameScreen) {
        super(game, 5760, 3240);
        this.gameScreen = gameScreen;

        camera.position.set(2880, 1620, 0);

        loadButtons();
    }

    /** Loads the buttons for screen */
    private void loadButtons() {
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = Fonts.defaultFont30;
        textButtonStyle.up = TerminalControl.skin.getDrawable("Button_up");
        textButtonStyle.down = TerminalControl.skin.getDrawable("Button_down");

        TextButton resumeButton = new TextButton("Resume", textButtonStyle);
        resumeButton.setSize(1200, 300);
        resumeButton.setPosition((5760 - 1200) / 2f, 3240 - 1200);
        resumeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Un-pause the game
                gameScreen.setGameRunning(true);
                event.handle();
            }
        });
        stage.addActor(resumeButton);

        TextButton settingsButton = new TextButton("Settings", textButtonStyle);
        settingsButton.setSize(1200, 300);
        settingsButton.setPosition((5760 - 1200) / 2f, 3240 - 1600);
        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Change to settings state
                game.setScreen(new CategorySelectScreen(game, null, (RadarScreen) gameScreen));
            }
        });
        stage.addActor(settingsButton);

        TextButton quitButton = new TextButton("Quit", textButtonStyle);
        quitButton.setSize(1200, 300);
        quitButton.setPosition((5760 - 1200) / 2f, 3240 - 2000);
        quitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Go back to main menu screen
                TerminalControl.radarScreen.getMetar().setQuit(true);
                if (!TerminalControl.radarScreen.tutorial) GameSaver.saveGame(); //Save the game first
                TerminalControl.radarScreen = null;
                gameScreen.game.setScreen(new MainMenuScreen(gameScreen.game, null));
                gameScreen.dispose();
            }
        });
        stage.addActor(quitButton);
    }
}
