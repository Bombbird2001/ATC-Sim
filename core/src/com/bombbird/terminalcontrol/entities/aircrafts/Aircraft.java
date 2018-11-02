package com.bombbird.terminalcontrol.entities.aircrafts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.entities.Airport;
import com.bombbird.terminalcontrol.entities.ILS;
import com.bombbird.terminalcontrol.entities.Runway;
import com.bombbird.terminalcontrol.entities.Waypoint;
import com.bombbird.terminalcontrol.entities.sidstar.SidStar;
import com.bombbird.terminalcontrol.screens.GameScreen;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.screens.ui.LatTab;
import com.bombbird.terminalcontrol.utilities.Fonts;
import com.bombbird.terminalcontrol.utilities.MathTools;

import static com.bombbird.terminalcontrol.screens.GameScreen.*;

public class Aircraft extends Actor {
    //Rendering parameters
    private Label label;
    private Label.LabelStyle labelStyle;
    public String[] labelText;
    private boolean selected;
    private static boolean loadedIcons = false;
    public static TextureAtlas iconAtlas = new TextureAtlas(Gdx.files.internal("game/aircrafts/aircraftIcons.atlas"));
    public static Skin skin = new Skin();
    private ImageButton icon;
    private Button labelButton;
    private Button clickSpot;
    private static final ImageButton.ImageButtonStyle buttonStyleCtrl = new ImageButton.ImageButtonStyle();
    private static final ImageButton.ImageButtonStyle buttonStyleDept = new ImageButton.ImageButtonStyle();
    private static final ImageButton.ImageButtonStyle buttonStyleUnctrl = new ImageButton.ImageButtonStyle();
    private static final ImageButton.ImageButtonStyle buttonStyleEnroute = new ImageButton.ImageButtonStyle();
    private boolean dragging;
    private Color color;

    //Aircraft information
    private Airport airport;
    private Runway runway;
    private boolean onGround;
    private boolean tkofLdg;

    //Aircraft characteristics
    private String callsign;
    private String icaoType;
    private char wakeCat;
    private int v2;
    private int typClimb;
    private int maxClimb;
    private int typDes;
    private int maxDes;
    private int apchSpd;
    private int controlState;
    private NavState navState;

    //Aircraft position
    private float x;
    private float y;
    private String latMode;
    private double heading;
    private double targetHeading;
    private int clearedHeading;
    private double angularVelocity;
    private double track;
    private int sidStarIndex;
    private Waypoint direct;
    private Waypoint afterWaypoint;
    private int afterWptHdg;
    private ILS ils;
    private boolean locCap;

    //Altitude
    private float prevAlt;
    private float altitude;
    private int clearedAltitude;
    private int targetAltitude;
    private float verticalSpeed;
    private boolean expedite;
    private String altMode;
    private int lowestAlt;
    private int highestAlt;
    private boolean gsCap;

    //Speed
    private float ias;
    private float tas;
    private float gs;
    private Vector2 deltaPosition;
    private int clearedIas;
    private int targetIas;
    private float deltaIas;
    private String spdMode;
    private int climbSpd;

    //Radar returns (for sweep delay)
    private float radarX;
    private float radarY;
    private double radarHdg;
    private double radarTrack;
    private float radarGs;
    private float radarAlt;
    private float radarVs;

    public Aircraft(String callsign, String icaoType, Airport airport) {
        if (!loadedIcons) {
            skin.addRegions(iconAtlas);
            buttonStyleCtrl.imageUp = skin.getDrawable("aircraftControlled");
            buttonStyleCtrl.imageDown = skin.getDrawable("aircraftControlled");
            buttonStyleDept.imageUp = skin.getDrawable("aircraftDeparture");
            buttonStyleDept.imageDown = skin.getDrawable("aircraftDeparture");
            buttonStyleUnctrl.imageUp = skin.getDrawable("aircraftNotControlled");
            buttonStyleUnctrl.imageDown = skin.getDrawable("aircraftNotControlled");
            buttonStyleEnroute.imageUp = skin.getDrawable("aircraftEnroute");
            buttonStyleEnroute.imageDown = skin.getDrawable("aircraftEnroute");
            loadedIcons = true;
        }
        this.callsign = callsign;
        stage.addActor(this);
        this.icaoType = icaoType;
        int[] perfData = AircraftType.getAircraftInfo(icaoType);
        if (perfData == null) {
            //If aircraft type not found in file
            Gdx.app.log("Aircraft not found", icaoType + " not found in game/aircrafts/aircrafts.air");
        }
        if (perfData[0] == 0) {
            wakeCat = 'M';
        } else if (perfData[0] == 1) {
            wakeCat = 'H';
        } else if (perfData[0] == 2) {
            wakeCat = 'J';
        } else {
            Gdx.app.log("Invalid wake category", "Invalid wake turbulence category set for " + callsign + ", " + icaoType + "!");
        }
        float loadFactor = MathUtils.random(-1 , 1) / 20f;
        v2 = (int)(perfData[1] * (1 + loadFactor));
        typClimb = (int)(perfData[2] * (1 - loadFactor));
        maxClimb = typClimb + 1000;
        typDes = (int)(perfData[3] * (1 - loadFactor));
        maxDes = typDes + 1000;
        apchSpd = (int)(perfData[4] * (1 + loadFactor));
        this.airport = airport;
        latMode = "vector";
        heading = 0;
        targetHeading = 0;
        clearedHeading = (int)(heading);
        track = 0;
        sidStarIndex = 0;
        afterWptHdg = 360;
        altitude = 10000;
        clearedAltitude = 10000;
        targetAltitude = 10000;
        verticalSpeed = 0;
        expedite = false;
        altMode = "open";
        ias = 250;
        tas = MathTools.iasToTas(ias, altitude);
        gs = tas;
        deltaPosition = new Vector2();
        clearedIas = 250;
        targetIas = 250;
        deltaIas = 0;
        spdMode = "sidstar"; //TODO Implement SIDSTAR speed restrictions
        tkofLdg = false;
        gsCap = false;
        locCap = false;

        selected = false;
        dragging = false;
    }

    public void initRadarPos() {
        radarX = x;
        radarY = y;
        radarHdg = heading;
        radarTrack = track;
        radarGs = gs;
        radarAlt = altitude;
        radarVs = verticalSpeed;
    }

    public void loadLabel() {
        icon = new ImageButton(buttonStyleUnctrl);
        icon.setSize(20, 20);
        icon.getImageCell().size(20, 20);
        stage.addActor(icon);

        labelText = new String[11];
        labelText[9] = airport.getIcao();
        labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont6;
        labelStyle.fontColor = Color.WHITE;
        label = new Label("Loading...", labelStyle);
        label.setPosition(x - label.getWidth() / 2, y + 25);

        labelButton = new Button(skin.getDrawable("labelBackgroundSmall"), skin.getDrawable("labelBackgroundSmall"));
        labelButton.setSize(label.getWidth() + 10, label.getHeight());

        clickSpot = new Button(new Button.ButtonStyle());
        clickSpot.setSize(labelButton.getWidth(), labelButton.getHeight());
        clickSpot.setName(callsign);
        clickSpot.addListener(new DragListener() {
            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                label.moveBy(x - labelButton.getWidth() / 2, y - labelButton.getHeight() / 2);
                dragging = true;
                event.handle();
            }
        });
        clickSpot.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!dragging) {
                    RadarScreen.setSelectedAircraft(RadarScreen.aircrafts.get(actor.getName()));
                } else {
                    dragging = false;
                }
            }
        });

        GameScreen.stage.addActor(labelButton);
        GameScreen.stage.addActor(label);
        GameScreen.stage.addActor(clickSpot);
    }

    public void renderShape() {
        if (direct != null && selected) {
            drawSidStar();
        }
        moderateLabel();
        shapeRenderer.setColor(Color.WHITE);
        GameScreen.shapeRenderer.line(label.getX() + label.getWidth() / 2, label.getY() + label.getHeight() / 2, radarX, radarY);
        if (controlState == 1 || controlState == 2) {
            shapeRenderer.setColor(color);
            GameScreen.shapeRenderer.line(radarX, radarY, radarX + radarGs * MathUtils.cosDeg((float)(90 - radarTrack)), radarY + radarGs * MathUtils.sinDeg((float)(90 - radarTrack)));
        }
    }

    public double update() {
        tas = MathTools.iasToTas(ias, altitude);
        updateIas();
        if (tkofLdg) {
            updateTkofLdg();
        }
        if (direct != null) {
            direct.setSelected(true);
        }
        if (!onGround) {
            double[] info = updateTargetHeading();
            targetHeading = info[0];
            updateHeading(targetHeading);
            updatePosition(info[1]);
            updateAltitude();
            return targetHeading;
        } else {
            gs = tas - airport.getWinds()[1] * MathUtils.cosDeg(airport.getWinds()[0] - runway.getHeading());
            updatePosition(0);
            return 0;
        }
    }

    public void updateTkofLdg() {
        //Overriden method for arrival/departure
    }

    private void updateIas() {
        float targetdeltaIas = (getTargetIas() - getIas()) / 5;
        if (targetdeltaIas > deltaIas + 0.05) {
            deltaIas += 0.2f * Gdx.graphics.getDeltaTime();
        } else if (targetdeltaIas < deltaIas - 0.05) {
            deltaIas -= 0.2f * Gdx.graphics.getDeltaTime();
        } else {
            deltaIas = targetdeltaIas;
        }
        float max = 1.5f;
        float min = -2.25f;
        if (tkofLdg) {
            max = 3;
            if (gs >= 60) {
                min = -4.5f;
            } else {
                min = -1.5f;
            }
        }
        if (deltaIas > max) {
            deltaIas = max;
        } else if (deltaIas< min) {
            deltaIas = min;
        }
        ias = ias + deltaIas * Gdx.graphics.getDeltaTime();
        if (Math.abs(targetIas - ias) < 1) {
            ias = targetIas;
        }
    }

    public void updateAltitude() {
        float targetVertSpd = (targetAltitude - altitude) / 0.1f;
        if (targetVertSpd > verticalSpeed + 100) {
            verticalSpeed = verticalSpeed + 500 * Gdx.graphics.getDeltaTime();
        } else if (targetVertSpd < verticalSpeed - 100) {
            verticalSpeed = verticalSpeed - 500 * Gdx.graphics.getDeltaTime();
        }
        if (!expedite && verticalSpeed > typClimb) {
            verticalSpeed = typClimb;
        } else if (!expedite && verticalSpeed < -typDes) {
            verticalSpeed = -typDes;
        } else if (expedite && verticalSpeed > maxClimb) {
            verticalSpeed = maxClimb;
        } else if (expedite && verticalSpeed < -maxDes) {
            verticalSpeed = -maxDes;
        }
        altitude = altitude + verticalSpeed / 60 * Gdx.graphics.getDeltaTime();
        if (Math.abs(targetAltitude - altitude) < 50) {
            altitude = targetAltitude;
            verticalSpeed = 0;
            expedite = false;
        }
        if (prevAlt < altitude && (int)(prevAlt / 1000) <= (int)(altitude / 1000)) {
            updateAltRestrictions();
        }
        prevAlt = altitude;
    }

    public double[] updateTargetHeading() {
        getDeltaPosition().setZero();
        double targetHeading = 0;
        double angleDiff = 0;

        //Get wind data
        int[] winds;
        if (altitude - airport.getElevation() <= 4000) {
            winds = airport.getWinds();
        } else {
            winds = RadarScreen.airports.get(RadarScreen.mainName).getWinds();
        }
        float windHdg = winds[0] + 180;
        int windSpd = winds[1];

        if (latMode.equals("vector") && (ils == null || !ils.isInsideILS(x, y))) {
            targetHeading = clearedHeading;
            double angle = 180 - windHdg + heading;
            gs = (float) Math.sqrt(Math.pow(tas, 2) + Math.pow(windSpd, 2) - 2 * tas * windSpd * MathUtils.cosDeg((float)angle));
            locCap = false;
            angleDiff = Math.asin(windSpd * MathUtils.sinDeg((float)angle) / gs) * MathUtils.radiansToDegrees;
        } else if (latMode.equals("sidstar") || (latMode.equals("vector") && locCap)) {
            float deltaX;
            float deltaY;
            if (latMode.equals("sidstar")) {
                //Calculates x, y between waypoint, and plane
                deltaX = direct.getPosX() - x;
                deltaY = direct.getPosY() - y;
                if (locCap) {
                    locCap = false;
                }
            } else {
                //Calculates x, y between point 0.75nm ahead of plane, and plane
                if (!getIls().getRwy().equals(runway)) {
                    runway = getIls().getRwy();
                }
                Vector2 position = this.getIls().getPointAhead(this);
                deltaX = position.x - x;
                deltaY = position.y - y;
            }

            //Find target track angle
            if (deltaX >= 0) {
                targetHeading = 90 - (Math.atan(deltaY / deltaX) * MathUtils.radiansToDegrees);
            } else {
                targetHeading = 270 - (Math.atan(deltaY / deltaX) * MathUtils.radiansToDegrees);
            }

            //Calculate required aircraft heading to account for winds
            //Using sine rule to determine angle between aircraft velocity and actual velocity
            double angle = windHdg - targetHeading;
            angleDiff = Math.asin(windSpd * MathUtils.sinDeg((float)angle) / tas) * MathUtils.radiansToDegrees;
            targetHeading -= angleDiff;

            //Aaaand now the cosine rule to determine ground speed
            gs = (float) Math.sqrt(Math.pow(tas, 2) + Math.pow(windSpd, 2) - 2 * tas * windSpd * MathUtils.cosDeg((float)(180 - angle - angleDiff)));

            //Add magnetic deviation to give magnetic heading
            targetHeading += RadarScreen.magHdgDev;

            //If within __px of waypoint, target next waypoint
            //Distance determined by angle that needs to be turned
            double distance = MathTools.distanceBetween(x, y, direct.getPosX(), direct.getPosY());
            double requiredDistance = Math.abs(findDeltaHeading(findNextTargetHdg())) + 10;
            if (distance <= requiredDistance) {
                updateDirect();
            }
        }

        if (targetHeading > 360) {
            targetHeading -= 360;
        } else if (targetHeading <= 0) {
            targetHeading += 360;
        }

        return new double[] {targetHeading, angleDiff};
    }

    public double findNextTargetHdg() {
        if (navState.getDispLatMode().first().equals("After waypoint, fly heading")) {
            return afterWptHdg;
        }
        Waypoint nextWpt = getSidStar().getWaypoint(getSidStarIndex() + 1);
        if (nextWpt == null) {
            return -1;
        } else {
            if (direct.equals(afterWaypoint)) {
                return afterWptHdg;
            }
            float deltaX = nextWpt.getPosX() - getDirect().getPosX();
            float deltaY = nextWpt.getPosY() - getDirect().getPosY();
            double nextTarget;
            if (deltaX >= 0) {
                nextTarget = 90 - (Math.atan(deltaY / deltaX) * MathUtils.radiansToDegrees);
            } else {
                nextTarget = 270 - (Math.atan(deltaY / deltaX) * MathUtils.radiansToDegrees);
            }
            return nextTarget;
        }
    }

    private void updatePosition(double angleDiff) {
        //Angle diff is angle correction due to winds
        track = heading - RadarScreen.magHdgDev + angleDiff;
        deltaPosition.x = Gdx.graphics.getDeltaTime() * MathTools.nmToPixel(gs) / 3600 * MathUtils.cosDeg((float)(90 - track));
        deltaPosition.y = Gdx.graphics.getDeltaTime() * MathTools.nmToPixel(gs) / 3600 * MathUtils.sinDeg((float)(90 - track));
        x += deltaPosition.x;
        y += deltaPosition.y;
        if (!locCap && getIls() != null && getIls().isInsideILS(x, y)) {
            locCap = true;
        }
        if (x < 1260 || x > 4500 || y < 0 || y > 3240) {
            removeAircraft();
        }
    }

    private double findDeltaHeading(double targetHeading) {
        double deltaHeading = targetHeading - heading;
        int forceDirection = 0;
        if (navState.getDispLatMode().first().equals("Turn left heading")) {
            forceDirection = 1;
        } else if (navState.getDispLatMode().first().equals("Turn right heading")) {
            forceDirection = 2;
        }
        switch (forceDirection) {
            case 0: //Not specified: pick quickest direction
                if (deltaHeading > 180) {
                    deltaHeading -= 360; //Turn left: deltaHeading is -ve
                } else if (deltaHeading <= -180) {
                    deltaHeading += 360; //Turn right: deltaHeading is +ve
                }
                break;
            case 1: //Must turn left
                if (deltaHeading > 0) {
                    deltaHeading -= 360;
                }
                break;
            case 2: //Must turn right
                if (deltaHeading < 0) {
                    deltaHeading += 360;
                }
                break;
            default:
                Gdx.app.log("Direction error", "Invalid turn direction specified!");
        }
        return deltaHeading;
    }

    private void updateHeading(double targetHeading) {
        double deltaHeading = findDeltaHeading(targetHeading);
        //Note: angular velocities unit is change in heading per second
        double targetAngularVelocity = 0;
        if (deltaHeading > 0) {
            //Aircraft needs to turn right
            targetAngularVelocity = 2.5;
        } else if (deltaHeading < 0) {
            //Aircraft needs to turn left
            targetAngularVelocity = -2.5;
        }
        if (Math.abs(deltaHeading) <= 10) {
            targetAngularVelocity = deltaHeading / 3;
        }
        //Update angular velocity towards target angular velocity
        if (targetAngularVelocity > angularVelocity + 0.1f) {
            //If need to turn right, start turning right
            angularVelocity += 0.5f * Gdx.graphics.getDeltaTime();
        } else if (targetAngularVelocity < angularVelocity - 0.1f) {
            //If need to turn left, start turning left
            angularVelocity -= 0.5f * Gdx.graphics.getDeltaTime();
        } else {
            //If within +-0.1 of target, set equal to target
            angularVelocity = targetAngularVelocity;
        }

        //Add angular velocity to heading
        heading = heading + angularVelocity * Gdx.graphics.getDeltaTime();
        if (heading > 360) {
            heading -= 360;
        } else if (heading <= 0) {
            heading += 360;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        update();
        updateLabel();
        icon.setPosition(radarX - 10, radarY - 10);
        icon.setColor(Color.BLACK); //Icon doesn't draw without this for some reason
        icon.draw(batch, 1);
    }

    public void drawSidStar() {
        GameScreen.shapeRenderer.setColor(Color.WHITE);
        GameScreen.shapeRenderer.line(radarX, radarY, direct.getPosX(), direct.getPosY());
    }

    public void updateDirect() {
        direct.setSelected(false);
        sidStarIndex++;
        if (direct.equals(afterWaypoint) && navState.getDispLatMode().first().equals("After waypoint, fly heading")) {
            clearedHeading = afterWptHdg;
            navState.getLatModes().removeValue("After waypoint, fly heading", false);
            navState.getLatModes().removeValue("Hold at", false);
            if (navState.getDispAltMode().first().contains("STAR")) {
                navState.getDispAltMode().removeFirst();
                navState.getDispAltMode().addFirst("Climb/descend to");
            }
            if (navState.getDispSpdMode().first().contains("STAR")) {
                navState.getDispSpdMode().removeFirst();
                navState.getDispSpdMode().addFirst("No speed restrictions");
            }
            navState.getClearedHdg().removeLast();
            navState.getClearedHdg().addLast(afterWptHdg);
            updateVectorMode();
        } else {
            direct = getSidStar().getWaypoint(sidStarIndex);
            navState.getClearedDirect().removeFirst();
            navState.getClearedDirect().addFirst(direct);
            if (direct != null) {
                direct.setSelected(true);
            }
        }
        updateAltRestrictions();
        updateTargetAltitude();
        if (selected && (controlState == 1 || controlState == 2)) {
            updateUISelections();
            ui.updateState();
        }
    }

    public void updateVectorMode() {
        //Switch aircraft latmode to vector mode
        latMode = "vector";
        navState.getDispLatMode().removeFirst();
        navState.getDispLatMode().addFirst("Fly heading");
    }

    public void removeSidStarMode() {
        if (!navState.getDispLatMode().removeValue(getSidStar().getName() + " arrival", false)) {
            navState.getDispLatMode().removeValue(getSidStar().getName() + " departure", false);
        } else {
            navState.getDispLatMode().removeValue("After waypoint, fly heading", false);
            navState.getDispLatMode().removeValue("Hold at", false);
        }
        if (selected && (controlState == 1 || controlState == 2)) {
            ui.updateState();
        }
    }

    public void setControlState(int controlState) {
        this.controlState = controlState;
        if (controlState == -1) { //En route aircraft - gray
            icon.setStyle(buttonStyleEnroute);
        } else if (controlState == 0) { //Uncontrolled aircraft - yellow
            icon.setStyle(buttonStyleUnctrl);
        } else if (controlState == 1) { //Controlled arrival - blue
            icon.setStyle(buttonStyleCtrl);
        } else if (controlState == 2) { //Controlled departure - green
            icon.setStyle(buttonStyleDept);
        } else {
            Gdx.app.log("Aircraft control state error", "Invalid control state " + controlState + " set!");
        }
        if (selected) {
            if (controlState == -1 || controlState == 0) {
                RadarScreen.ui.setNormalPane(true);
                RadarScreen.ui.setSelectedPane(null);
            } else {
                RadarScreen.ui.setNormalPane(false);
                RadarScreen.ui.setSelectedPane(this);
            }
        }
    }

    private void moderateLabel() {
        if (label.getX() < 936) {
            label.setX(936);
        } else if (label.getX() + label.getWidth() > 4824) {
            label.setX(4824 - label.getWidth());
        }
        if (label.getY() < 0) {
            label.setY(0);
        } else if (label.getY() + label.getHeight() > 3240) {
            label.setY(3240 - label.getHeight());
        }
    }

    public void updateLabel() {
        String vertSpd;
        if (radarVs < -100 || gsCap) {
            vertSpd = " DOWN ";
        } else if (radarVs > 100) {
            vertSpd = " UP ";
        } else {
            vertSpd = " = ";
        }
        labelText[0] = callsign;
        labelText[1] = icaoType + "/" + wakeCat;
        labelText[2] = Integer.toString((int)(radarAlt / 100));
        labelText[3] = gsCap ? "GS" : Integer.toString(targetAltitude / 100);
        labelText[10] = Integer.toString(getNavState().getClearedAlt().first() / 100);
        if ((int) radarHdg == 0) {
            radarHdg += 360;
        }
        labelText[4] = Integer.toString(MathUtils.round((float) radarHdg));
        if (latMode.equals("vector")) {
            if (locCap) {
                labelText[5] = "LOC";
            } else {
                labelText[5] = Integer.toString(clearedHeading);
            }
        } else if (latMode.equals("sidstar")) {
            if (direct.equals(afterWaypoint) && navState.getDispLatMode().last().equals("After waypoint, fly heading")) {
                labelText[5] = direct.getName() + Integer.toString(afterWptHdg);
            } else {
                labelText[5] = direct.getName();
            }
        }
        labelText[6] = Integer.toString((int) radarGs);
        labelText[7] = Integer.toString(clearedIas);
        if (ils != null) {
            labelText[8] = ils.getName();
        } else {
            labelText[8] = getSidStar().getName();
        }
        String exped = expedite ? " =>> " : " => ";
        String updatedText;
        if (getControlState() == 1 || getControlState() == 2) {
            updatedText = labelText[0] + " " + labelText[1] + "\n" + labelText[2] + vertSpd + labelText[3] + exped + labelText[10] + "\n" + labelText[4] + " " + labelText[5] + " " + labelText[8] + "\n" + labelText[6] + " " + labelText[7] + " " + labelText[9];
        } else {
            updatedText = labelText[0] + "\n" + labelText[2] + " " + labelText[4] + "\n" + labelText[6];
        }
        label.setText(updatedText);
        label.pack();
        labelButton.setSize(label.getWidth() + 10, label.getHeight());
        labelButton.setPosition(label.getX() - 5, label.getY());
        clickSpot.setSize(labelButton.getWidth(), labelButton.getHeight());
        clickSpot.setPosition(labelButton.getX(), labelButton.getY());
    }

    public void updateSelectedWaypoints(Aircraft aircraft) {
        Array<Waypoint> remainingWpts = getRemainingWaypoints();
        if (aircraft != null) {
            for (Waypoint waypoint: remainingWpts) {
                waypoint.setSelected(false);
            }
            for (Waypoint waypoint: aircraft.getRemainingWaypoints()) {
                waypoint.setSelected(true);
            }
        } else {
            for (Waypoint waypoint: remainingWpts) {
                waypoint.setSelected(false);
            }
        }
        if (direct != null) {
            direct.setSelected(true);
        }
    }

    private void updateUISelections() {
        ui.latTab.getSettingsBox().setSelected(navState.getDispLatMode().last());
        LatTab.clearedHdg = clearedHeading;
        if (direct != null) {
            ui.latTab.getValueBox().setSelected(direct.getName());
        }

        if (this instanceof Departure && Integer.parseInt(ui.altTab.getValueBox().getSelected()) < lowestAlt) {
            ui.altTab.getValueBox().setSelected(Integer.toString(lowestAlt));
        }

        ui.spdTab.getValueBox().setSelected(Integer.toString(clearedIas));
    }

    public void updateRadarInfo() {
        label.moveBy(x - radarX, y - radarY);
        radarX = x;
        radarY = y;
        radarHdg = heading;
        radarTrack = track;
        radarAlt = altitude;
        radarGs = gs;
        radarVs = verticalSpeed;
    }

    public Array<Waypoint> getRemainingWaypoints() {
        return getSidStar().getRemainingWaypoints(sidStarIndex);
    }

    public SidStar getSidStar() {
        return null;
    }

    public int getSidStarIndex() {
        return sidStarIndex;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setTargetIas(int ias) {
        targetIas = ias;
    }

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public String[] getLabelText() {
        return labelText;
    }

    public void setLabelText(String[] labelText) {
        this.labelText = labelText;
    }

    public boolean isSelected() {
        return selected;
    }

    public ImageButton getIcon() {
        return icon;
    }

    public void setIcon(ImageButton icon) {
        this.icon = icon;
    }

    public Airport getAirport() {
        return airport;
    }

    public void setAirport(Airport airport) {
        this.airport = airport;
    }

    public Runway getRunway() {
        return runway;
    }

    public void setRunway(Runway runway) {
        this.runway = runway;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public boolean isTkofLdg() {
        return tkofLdg;
    }

    public void setTkofLdg(boolean tkofLdg) {
        this.tkofLdg = tkofLdg;
    }

    public String getCallsign() {
        return callsign;
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    public String getIcaoType() {
        return icaoType;
    }

    public void setIcaoType(String icaoType) {
        this.icaoType = icaoType;
    }

    public char getWakeCat() {
        return wakeCat;
    }

    public void setWakeCat(char wakeCat) {
        this.wakeCat = wakeCat;
    }

    public int getV2() {
        return v2;
    }

    public void setV2(int v2) {
        this.v2 = v2;
    }

    public int getTypClimb() {
        return typClimb;
    }

    public void setTypClimb(int typClimb) {
        this.typClimb = typClimb;
    }

    public int getTypDes() {
        return typDes;
    }

    public void setTypDes(int typDes) {
        this.typDes = typDes;
    }

    public int getMaxDes() {
        return maxDes;
    }

    public void setMaxDes(int maxDes) {
        this.maxDes = maxDes;
    }

    public int getApchSpd() {
        return apchSpd;
    }

    public void setApchSpd(int apchSpd) {
        this.apchSpd = apchSpd;
    }

    public int getControlState() {
        return controlState;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public void setX(float x) {
        this.x = x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public void setY(float y) {
        this.y = y;
    }

    public String getLatMode() {
        return latMode;
    }

    public void setLatMode(String latMode) {
        this.latMode = latMode;
    }

    public double getHeading() {
        return heading;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    public double getTargetHeading() {
        return targetHeading;
    }

    public void setTargetHeading(double targetHeading) {
        this.targetHeading = targetHeading;
    }

    public int getClearedHeading() {
        return clearedHeading;
    }

    public void setClearedHeading(int clearedHeading) {
        this.clearedHeading = clearedHeading;
    }

    public double getAngularVelocity() {
        return angularVelocity;
    }

    public void setAngularVelocity(double angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    public double getTrack() {
        return track;
    }

    public void setTrack(double track) {
        this.track = track;
    }

    public Waypoint getDirect() {
        return direct;
    }

    public void setDirect(Waypoint direct) {
        this.direct = direct;
    }

    public float getAltitude() {
        return altitude;
    }

    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }

    public int getClearedAltitude() {
        return clearedAltitude;
    }

    public void setClearedAltitude(int clearedAltitude) {
        this.clearedAltitude = clearedAltitude;
        updateTargetAltitude();
    }

    public int getTargetAltitude() {
        return targetAltitude;
    }

    public void setTargetAltitude(int targetAltitude) {
        this.targetAltitude = targetAltitude;
    }

    public void updateTargetAltitude() {
        //When called, gets current cleared altitude, alt nav mode and updates the target altitude of aircraft
        if (navState.getDispAltMode().first().contains("/")) {
            //No alt restrictions
            targetAltitude = clearedAltitude;
        } else {
            //Restrictions
            if (clearedAltitude > highestAlt) {
                targetAltitude = highestAlt;
            } else if (clearedAltitude < lowestAlt) {
                if (this instanceof Departure) {
                    clearedAltitude = lowestAlt;
                }
                targetAltitude = lowestAlt;
            } else {
                targetAltitude = clearedAltitude;
            }
        }
    }

    public void removeAircraft() {
        label.remove();
        icon.remove();
        labelButton.remove();
        clickSpot.remove();
        remove();
        RadarScreen.aircrafts.remove(callsign);
    }

    public void updateAltRestrictions() {
        //Overriden method that sets the altitude restrictions of the aircraft
    }

    public float getVerticalSpeed() {
        return verticalSpeed;
    }

    public void setVerticalSpeed(float verticalSpeed) {
        this.verticalSpeed = verticalSpeed;
    }

    public boolean isExpedite() {
        return expedite;
    }

    public void setExpedite(boolean expedite) {
        this.expedite = expedite;
    }

    public String getAltMode() {
        return altMode;
    }

    public void setAltMode(String altMode) {
        this.altMode = altMode;
    }

    public float getIas() {
        return ias;
    }

    public void setIas(float ias) {
        this.ias = ias;
    }

    public float getTas() {
        return tas;
    }

    public void setTas(float tas) {
        this.tas = tas;
    }

    public float getGs() {
        return gs;
    }

    public void setGs(float gs) {
        this.gs = gs;
    }

    public Vector2 getDeltaPosition() {
        return deltaPosition;
    }

    public void setDeltaPosition(Vector2 deltaPosition) {
        this.deltaPosition = deltaPosition;
    }

    public int getClearedIas() {
        return clearedIas;
    }

    public void setClearedIas(int clearedIas) {
        this.clearedIas = clearedIas;
    }

    public int getTargetIas() {
        return targetIas;
    }

    public float getDeltaIas() {
        return deltaIas;
    }

    public void setDeltaIas(float deltaIas) {
        this.deltaIas = deltaIas;
    }

    public String getSpdMode() {
        return spdMode;
    }

    public void setSpdMode(String spdMode) {
        this.spdMode = spdMode;
    }

    public NavState getNavState() {
        return navState;
    }

    public void setNavState(NavState navState) {
        this.navState = navState;
    }

    public int getMaxClimb() {
        return maxClimb;
    }

    public void setMaxClimb(int maxClimb) {
        this.maxClimb = maxClimb;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    public void setSidStarIndex(int sidStarIndex) {
        this.sidStarIndex = sidStarIndex;
    }

    public Waypoint getAfterWaypoint() {
        return afterWaypoint;
    }

    public void setAfterWaypoint(Waypoint afterWaypoint) {
        this.afterWaypoint = afterWaypoint;
    }

    public int getAfterWptHdg() {
        return afterWptHdg;
    }

    public void setAfterWptHdg(int afterWptHdg) {
        this.afterWptHdg = afterWptHdg;
    }

    public int getLowestAlt() {
        return lowestAlt;
    }

    public void setLowestAlt(int lowestAlt) {
        this.lowestAlt = lowestAlt;
    }

    public int getHighestAlt() {
        return highestAlt;
    }

    public void setHighestAlt(int highestAlt) {
        this.highestAlt = highestAlt;
    }

    public int getClimbSpd() {
        return climbSpd;
    }

    public void setClimbSpd(int climbSpd) {
        this.climbSpd = climbSpd;
    }

    public int getMaxWptSpd(String wpt) {
        return getSidStar().getWptMaxSpd(wpt);
    }

    public ILS getIls() {
        return ils;
    }

    public void setIls(ILS ils) {
        this.ils = ils;
    }

    public boolean isGsCap() {
        return gsCap;
    }

    public void setGsCap(boolean gsCap) {
        this.gsCap = gsCap;
    }
}