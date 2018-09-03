package com.bombbird.atcsim.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bombbird.atcsim.AtcSim;
import com.bombbird.atcsim.entities.Airport;
import com.bombbird.atcsim.entities.RangeCircle;
import com.bombbird.atcsim.entities.restrictions.Obstacle;
import com.bombbird.atcsim.entities.restrictions.RestrictedArea;

public class GameScreen implements Screen, GestureDetector.GestureListener, InputProcessor {
    //Init game (set in constructor)
    private final AtcSim game;
    public static Stage stage;

    //Set input processors
    InputMultiplexer inputMultiplexer = new InputMultiplexer();
    InputProcessor inputProcessor1 = this;
    InputProcessor inputProcessor2;
    private int lastX;
    private int lastY;

    //Create new camera
    OrthographicCamera camera;
    Viewport viewport;

    //Create texture stuff
    Skin skin;
    public static ShapeRenderer shapeRenderer;

    //Create range circles
    private RangeCircle[] rangeCircles;

    //Create obstacle resources
    FileHandle obstacles;
    Array<Obstacle> obsArray;
    FileHandle restrictions;
    Array<RestrictedArea> restArray;

    //Create airports
    Array<Airport> airports;

    GameScreen(final AtcSim game) {
        this.game = game;
        shapeRenderer = new ShapeRenderer();

        //Initiate range circles
        rangeCircles = new RangeCircle[3];

        //Initiate airports
        airports = new Array<Airport>();
    }

    void loadRange() {
        //Load radar screen range circles
        rangeCircles[0] = new RangeCircle(10, -67, shapeRenderer);
        rangeCircles[1] = new RangeCircle(30, -235, shapeRenderer);
        rangeCircles[2] = new RangeCircle(50, -397, shapeRenderer);
        for (RangeCircle circle: rangeCircles) {
            stage.addActor(circle);
        }
    }

    void loadScroll() {
        //Add scroll listener
        stage.addListener(new InputListener() {
            @Override
            public boolean scrolled(InputEvent event, float x, float y, int amount) {
                camera.zoom += amount * 0.042f;
                return false;
            }
        });
    }

    private void handleInput(float dt) {
        float ZOOMCONSTANT = 0.5f;
        float SCROLLCONSTANT = 150.0f;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            //Zoom in
            camera.zoom += ZOOMCONSTANT * dt;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            //Zoom out
            camera.zoom -= ZOOMCONSTANT * dt;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            //Move left
            camera.translate(-SCROLLCONSTANT * dt, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            //Move right
            camera.translate(SCROLLCONSTANT * dt, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            //Move down
            camera.translate(0, -SCROLLCONSTANT * dt, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            //Move up
            camera.translate(0, SCROLLCONSTANT * dt, 0);
        }

        //Make sure user doesn't zoom in too much or zoom out of bounds
        if (camera.zoom < 0.2f) {
            camera.zoom = 0.2f;
        } else if (camera.zoom > 1.0f) {
            camera.zoom = 1.0f;
        }

        //Setting new boundaries for camera position after zooming
        float effectiveViewportWidth = camera.viewportWidth * camera.zoom;
        float xDeviation = -(effectiveViewportWidth - camera.viewportWidth) / 2f;
        float effectiveViewportHeight = camera.viewportHeight * camera.zoom;
        float yDeviation = -(effectiveViewportHeight - camera.viewportHeight) / 2f;
        float leftLimit = effectiveViewportWidth / 2f;
        float rightLimit = leftLimit + 2 * xDeviation;
        float downLimit = effectiveViewportHeight / 2f;
        float upLimit = downLimit + 2 * yDeviation;

        //Prevent camera from going out of boundary
        if (camera.position.x < leftLimit) {
            camera.position.x = leftLimit;
        } else if (camera.position.x > rightLimit) {
            camera.position.x = rightLimit;
        }
        if (camera.position.y < downLimit) {
            camera.position.y = downLimit;
        } else if (camera.position.y > upLimit) {
            camera.position.y = upLimit;
        }
    }

    @Override
    public void show() {

    }

    void renderShape() {

    }

    @Override
    public void render(float delta) {
        //Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //Test for input, update camera
        handleInput(delta);
        camera.update();

        //Set rendering for stage camera
        game.batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        //Update stage
        stage.act(delta);

        //Render each of the range circles, obstacles using shaperenderer
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (RangeCircle rangeCircle: rangeCircles) {
            rangeCircle.renderShape();
        }
        renderShape();
        shapeRenderer.end();
        /*
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        //TODO: Add rendering for runways
        shapeRenderer.end();
        */

        //Draw to the spritebatch
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

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        stage.clear();
        stage.dispose();
        skin.dispose();
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {

    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        lastX = screenX;
        lastY = screenY;
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        int deltaX = (int)((screenX - lastX) * camera.zoom);
        int deltaY = (int)((screenY - lastY) * camera.zoom);
        camera.translate(-deltaX, deltaY);
        lastX = screenX;
        lastY = screenY;
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
