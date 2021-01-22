/**
 * Copyright (c) 2006-2021 Julien Gouesse
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package engine.statemachine;

import java.net.URL;
import java.nio.FloatBuffer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.CollisionTree;
import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.extension.model.util.KeyframeController;
import com.ardor3d.extension.model.util.KeyframeController.PointInTime;
import com.ardor3d.framework.Canvas;
import com.ardor3d.framework.CanvasRenderer;
import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.framework.jogl.JoglNewtWindow;
import com.ardor3d.image.Texture;
import com.ardor3d.input.GrabbedState;
import com.ardor3d.input.MouseManager;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.intersection.BoundingCollisionResults;
import com.ardor3d.intersection.BoundingPickResults;
import com.ardor3d.intersection.CollisionResults;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Quaternion;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.ContextManager;
import com.ardor3d.renderer.RenderContext;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.WireframeState;
import com.ardor3d.scenegraph.FloatBufferData;
import com.ardor3d.scenegraph.IndexBufferData;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.MeshData;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.ComplexSpatialController.RepeatType;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.extension.CameraNode;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.scenegraph.visitor.Visitor;
import com.ardor3d.ui.text.BasicText;
import com.ardor3d.util.GameTaskQueue;
import com.ardor3d.util.GameTaskQueueManager;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

import engine.data.Enemy;
import engine.data.EnemyData;
import engine.data.EnemyFactory;
import engine.data.Level;
import engine.data.LevelFactory;
import engine.data.Objective;
import engine.data.ObjectiveStatus;
import engine.data.PlayerData;
import engine.data.ProfileData;
import engine.data.ProjectileController;
import engine.data.ProjectileData;
import engine.data.SkyboxFactory;
import engine.data.common.AmmunitionBox;
import engine.data.common.AmmunitionBoxFactory;
import engine.data.common.Medikit;
import engine.data.common.MedikitFactory;
import engine.data.common.Teleporter;
import engine.data.common.TeleporterFactory;
import engine.data.common.userdata.CollectibleUserData;
import engine.data.common.userdata.TeleporterUserData;
import engine.input.Action;
import engine.input.ActionMap;
import engine.input.ExtendedFirstPersonControl;
import engine.input.MouseAndKeyboardSettings;
import engine.misc.ApplicativeTimer;
import engine.misc.MD2FrameSet;
import engine.misc.NodeHelper;
import engine.sound.SoundManager;
import engine.taskmanagement.TaskManager;
import engine.weaponry.Ammunition;
import engine.weaponry.AmmunitionFactory;
import engine.weaponry.Weapon;
import engine.weaponry.WeaponFactory;

/**
 * State used during the game, the party.
 * 
 * @author Julien Gouesse
 *
 */
public final class GameState extends ScenegraphStateWithCustomCameraParameters {

    /** Our native window, not the OpenGL surface itself */
    private final NativeCanvas canvas;
    /** node of the player */
    private final CameraNode playerNode;
    /** data of the player */
    private final PlayerData playerData;
    /** player object that relies on a state machine */
    private final LogicalPlayer playerWithStateMachine;
    /** player's game statistics */
    private GameStatistics gameStats;
    /** list containing all objects that can be picked up */
    private final ArrayList<Node> collectibleObjectsList;
    /** list of teleporters */
    private final ArrayList<Node> teleportersList;
    /** text label showing the ammunition */
    private final BasicText ammoTextLabel;
    /** text label showing the frame rate */
    private final BasicText fpsTextLabel;
    /** text label showing the health */
    private final BasicText healthTextLabel;
    /** text label of the head-up display */
    private final BasicText headUpDisplayLabel;
    /** text label of the objectives display */
    private final BasicText objectivesDisplayLabel;
    /** instance that creates all teleporters */
    private final TeleporterFactory teleporterFactory;
    /** instance that creates all ammunitions */
    private final AmmunitionFactory ammunitionFactory;
    /** instance that creates all ammunition boxes */
    private final AmmunitionBoxFactory ammunitionBoxFactory;
    /** instance that creates all sky boxes */
    private final SkyboxFactory skyboxFactory;
    /** instance that creates all typical medical kits */
    private final MedikitFactory medikitFactory;
    /** instance that creates all enemies */
    private final EnemyFactory enemyFactory;
    /** instance that creates all weapons */
    private final WeaponFactory weaponFactory;
    /** instance that creates all levels */
    private final LevelFactory levelFactory;
    /** timer that can be paused and used to measure the elapsed time */
    private final ApplicativeTimer timer;

    private final TaskManager taskManager;

    private final Map<Node, ProjectileData> projectilesMap;

    private final Random random;

    private final HashMap<Mesh, EnemyData> enemiesDataMap;

    private final HashMap<EnemyData, Long> enemiesLatestDetection;

    private Long latestPlayerDeath;

    private final ProjectileDataOpponentsComparator projectileDataOpponentsComparator;

    private ExtendedFirstPersonControl fpsc;

    private final PhysicalLayer physicalLayer;

    private final MouseManager mouseManager;

    private final TransitionTriggerAction<ScenegraphState, String> toPauseMenuTriggerAction;

    private final TransitionTriggerAction<ScenegraphState, String> toPauseMenuTriggerActionForExitConfirm;

    private final TransitionTriggerAction<ScenegraphState, String> toGameOverTriggerAction;

    private final TriggerAction toggleScreenModeAction;

    private final ActionMap defaultActionMap;

    private final ActionMap customActionMap;

    private final MouseAndKeyboardSettings defaultMouseAndKeyboardSettings;

    private final MouseAndKeyboardSettings customMouseAndKeyboardSettings;

    private static final String gameoverSoundSamplePath = "/sounds/gameover.ogg";

    private static String gameoverSoundSampleIdentifier = null;

    private static final String victory0SoundSamplePath = "/sounds/mus3beyond.ogg";

    private static String victory0SoundSampleIdentifier = null;

    private static final String victory1SoundSamplePath = "/sounds/applause.ogg";

    private static String victory1SoundSampleIdentifier = null;

    private static final String[] painSoundSamplePaths = new String[] { "/sounds/pain1.ogg", "/sounds/pain2.ogg",
            "/sounds/pain3.ogg", "/sounds/pain4.ogg", "/sounds/pain5.ogg", "/sounds/pain6.ogg" };

    private static final String[] painSoundSampleIdentifiers = new String[painSoundSamplePaths.length];

    private static final String enemyShotgunShotSamplePath = "/sounds/shotgun_shot.ogg";

    private static String enemyShotgunShotSampleIdentifier = null;

    private WireframeState wireframeState;

    private Vector3 previousPosition = new Vector3();

    /** data of the profile */
    private final ProfileData profileData;

    private HashMap<Objective, ObjectiveStatus> previousObjectivesStatusesMap;

    private Level level;

    /**
     * Camera node that draws its content at last just after clearing the depth
     * buffer in order to prevent the weapons from being clipped into 3D objects
     * FIXME detect whether the weapon is close to another 3D object and update
     * its position
     * 
     * @author Julien Gouesse
     *
     */
    public static final class PlayerCameraNode extends CameraNode {

        public PlayerCameraNode(final String name, final Camera camera) {
            super(name, camera);
        }

        @Override
        public void draw(final Renderer renderer) {
            /**
             * Ardor3D doesn't put nodes without render delegate into render
             * queues by default, it must be done here
             */
            final boolean queued;
            if (!renderer.isProcessingQueue())
                queued = renderer.checkAndAdd(this);
            else
                queued = false;
            if (!queued) {// clears the depth buffer
                renderer.clearBuffers(Renderer.BUFFER_DEPTH);
                // renders
                super.draw(renderer);
            }
        }
    }

    /**
     * Constructor
     * 
     * @param canvas
     * @param physicalLayer
     * @param toPauseMenuTriggerAction
     * @param toPauseMenuTriggerActionForExitConfirm
     * @param toGameOverTriggerAction
     * @param toggleScreenModeAction
     * @param soundManager
     * @param taskManager
     * @param mouseManager
     * @param defaultActionMap
     * @param customActionMap
     * @param defaultMouseAndKeyboardSettings
     * @param customMouseAndKeyboardSettings
     * @param profileData
     *            data of the profile
     */
    public GameState(final NativeCanvas canvas, final PhysicalLayer physicalLayer,
            final TransitionTriggerAction<ScenegraphState, String> toPauseMenuTriggerAction,
            final TransitionTriggerAction<ScenegraphState, String> toPauseMenuTriggerActionForExitConfirm,
            final TransitionTriggerAction<ScenegraphState, String> toGameOverTriggerAction,
            final TriggerAction toggleScreenModeAction, final SoundManager soundManager, final TaskManager taskManager,
            final MouseManager mouseManager, final ActionMap defaultActionMap, final ActionMap customActionMap,
            final MouseAndKeyboardSettings defaultMouseAndKeyboardSettings,
            final MouseAndKeyboardSettings customMouseAndKeyboardSettings, final ProfileData profileData) {
        super(soundManager, new LogicalLayer(), new Node(), canvas.getCanvasRenderer().getCamera());
        this.mouseManager = mouseManager;
        this.physicalLayer = physicalLayer;
        this.toPauseMenuTriggerAction = toPauseMenuTriggerAction;
        this.toPauseMenuTriggerActionForExitConfirm = toPauseMenuTriggerActionForExitConfirm;
        this.toGameOverTriggerAction = toGameOverTriggerAction;
        this.toggleScreenModeAction = toggleScreenModeAction;
        this.defaultActionMap = defaultActionMap;
        this.customActionMap = customActionMap;
        this.defaultMouseAndKeyboardSettings = defaultMouseAndKeyboardSettings;
        this.customMouseAndKeyboardSettings = customMouseAndKeyboardSettings;
        this.profileData = profileData;
        random = new Random();
        projectileDataOpponentsComparator = new ProjectileDataOpponentsComparator();
        enemiesDataMap = new HashMap<>();
        enemiesLatestDetection = new HashMap<>();
        this.taskManager = taskManager;
        timer = new ApplicativeTimer();
        collectibleObjectsList = new ArrayList<>();
        projectilesMap = new HashMap<>();
        teleportersList = new ArrayList<>();
        // initializes the factories, the build-in ammo and the build-in weapons
        teleporterFactory = initializeTeleporterFactory();
        medikitFactory = initializeMedikitFactory();
        skyboxFactory = initializeSkyboxFactory();
        ammunitionFactory = initializeAmmunitionFactory();
        ammunitionBoxFactory = initializeAmmunitionBoxFactory();
        enemyFactory = initializeEnemyFactory();
        weaponFactory = initializeWeaponFactory();
        levelFactory = initializeLevelFactory();
        this.canvas = canvas;
        final Camera cam = canvas.getCanvasRenderer().getCamera();
        // creates a node that follows the camera
        playerNode = new PlayerCameraNode("player", cam);
        playerNode.getSceneHints().setRenderBucketType(RenderBucketType.PostBucket);
        // attaches a node for the crosshair to the player node
        final Node crosshairNode = createCrosshairNode();
        playerNode.attachChild(crosshairNode);
        // sets the limits of the ammunition containers used by the player
        final Map<Ammunition, Integer> ammunitionMaxCountMap = new HashMap<>();
        final int ammoCount = ammunitionFactory.getSize();
        for (int ammoIndex = 0; ammoIndex < ammoCount; ammoIndex++) {
            final Ammunition ammo = ammunitionFactory.get(ammoIndex);
            // FIXME put more reasonable values into this map, a player
            // shouldn't be able to have 1000 rockets
            ammunitionMaxCountMap.put(ammo, Integer.valueOf(1000));
        }
        // builds the player data
        playerData = new PlayerData(playerNode, ammunitionFactory, weaponFactory, ammunitionMaxCountMap, true) {

            private final FloatBuffer projectileVertexBuffer;

            {
                projectileVertexBuffer = BufferUtils.createFloatBuffer(6);
                projectileVertexBuffer.put(-0.1f).put(-0.1f).put(-0.1f).put(0.1f).put(0.1f).put(0.1f).rewind();
            }

            @Override
            public Map.Entry<Integer, Integer> attack() {
                final Map.Entry<Integer, Integer> consumedAmmunitionOrKnockCounts = super.attack();
                final String identifier = getCurrentWeaponBlowOrShotSoundSampleIdentifier();
                // primary hand
                for (int index = 0; index < consumedAmmunitionOrKnockCounts.getKey().intValue(); index++) {
                    if (isCurrentWeaponAmmunitionCountDisplayable()) {// creates
                                                                      // a new
                                                                      // projectile
                                                                      // launched
                                                                      // by the
                                                                      // primary
                                                                      // hand
                        createProjectile(cameraNode.getChild(0));
                    }
                    if (identifier != null)
                        soundManager.play(false, false, identifier);
                }
                // secondary hand
                for (int index = 0; index < consumedAmmunitionOrKnockCounts.getValue().intValue(); index++) {
                    if (isCurrentWeaponAmmunitionCountDisplayable()) {// creates
                                                                      // a new
                                                                      // projectile
                                                                      // launched
                                                                      // by the
                                                                      // secondary
                                                                      // hand
                        createProjectile(cameraNode.getChild(0));
                    }
                    if (identifier != null)
                        soundManager.play(false, false, identifier);
                }
                return (consumedAmmunitionOrKnockCounts);
            }

            private final void createProjectile(final Spatial weaponSpatial) {
                // uses the world bound of the primary weapon to compute the
                // initial position of the shot
                final Vector3 initialLocation = new Vector3(weaponSpatial.getWorldBound().getCenter());
                final String originator = playerNode.getName();
                final double initialSpeed = 350.0 / 1000000000.0;
                final double initialAcceleration = 0;
                final Vector3 initialDirection = weaponSpatial.getWorldTransform().getMatrix().getColumn(2, null);
                final long initialTimeInNanos = timer.getElapsedTimeInNanoseconds();
                final ProjectileData projectileData = new ProjectileData(originator, initialLocation, initialSpeed,
                        initialAcceleration, initialDirection, initialTimeInNanos);
                final Node projectileNode = new Node(projectileData.toString());
                NodeHelper.setModelBound(projectileNode, BoundingBox.class);
                projectileNode.setTransform(weaponSpatial.getWorldTransform());
                projectileNode.setTranslation(initialLocation);
                // TODO use the correct node
                // final WeaponUserData
                // weaponUserData=(WeaponUserData)((Node)weaponSpatial).getUserData();
                // final Weapon weapon=weaponUserData.getWeapon();
                Mesh projectileMesh = createProjectileMesh(null, projectileData);
                projectileNode.attachChild(projectileMesh);
                projectileNode.addController(new ProjectileController(timer, projectileData));
                // stores it for a further use
                projectilesMap.put(projectileNode, projectileData);
                getRoot().attachChild(projectileNode);
            }

            private Mesh createProjectileMesh(final Weapon weapon, final ProjectileData projectileData) {
                // TODO support several kinds of projectile with different sizes
                Mesh projectileMesh = new Mesh("Mesh@" + projectileData.toString());
                MeshData projectileMeshData = new MeshData();
                projectileMeshData.setVertexBuffer(projectileVertexBuffer);
                projectileMesh.setMeshData(projectileMeshData);
                return (projectileMesh);
            }

            @Override
            public int reload() {
                final int reloadedAmmoCount = super.reload();
                if (reloadedAmmoCount > 0) {
                    final String identifier = getCurrentWeaponReloadSoundSampleIdentifier();
                    if (identifier != null)
                        soundManager.play(false, false, identifier);
                }
                return (reloadedAmmoCount);
            }
        };
        latestPlayerDeath = null;
        playerWithStateMachine = new LogicalPlayer(playerData);
        // initializes all text displays
        ammoTextLabel = initializeAmmunitionTextLabel();
        fpsTextLabel = initializeFpsTextLabel();
        healthTextLabel = initializeHealthTextLabel();
        headUpDisplayLabel = initializeHeadUpDisplayLabel();
        objectivesDisplayLabel = initializeObjectivesDisplayLabel();
        initializeCollisionSystem(cam);
        wireframeState = new WireframeState();
        wireframeState.setEnabled(false);
        getRoot().setRenderState(wireframeState);
    }

    @SuppressWarnings("cast")
    private final Node createCrosshairNode() {
        final Node crosshairNode = new Node("crosshair");
        final Mesh crosshairMesh = new Mesh();
        final MeshData crosshairMeshData = new MeshData();
        final Camera cam = playerNode.getCamera();
        final FloatBuffer crosshairVertexBuffer = BufferUtils.createFloatBuffer(36);
        final float halfSmallWidth = 0.0004f * 1920.0f / ((float) cam.getWidth());
        final float halfSmallHeight = 0.0004f * 1080.0f / ((float) cam.getHeight());
        final float halfBigWidth = 0.005f * 1920.0f / ((float) cam.getWidth());
        final float halfBigHeight = 0.005f * 1080.0f / ((float) cam.getHeight());
        final float z = 1.0f;
        crosshairVertexBuffer.put(-halfSmallWidth).put(-halfBigHeight).put(z);
        crosshairVertexBuffer.put(-halfSmallWidth).put(+halfBigHeight).put(z);
        crosshairVertexBuffer.put(+halfSmallWidth).put(+halfBigHeight).put(z);
        crosshairVertexBuffer.put(+halfSmallWidth).put(+halfBigHeight).put(z);
        crosshairVertexBuffer.put(+halfSmallWidth).put(-halfBigHeight).put(z);
        crosshairVertexBuffer.put(-halfSmallWidth).put(-halfBigHeight).put(z);
        crosshairVertexBuffer.put(-halfBigWidth).put(-halfSmallHeight).put(z);
        crosshairVertexBuffer.put(-halfBigWidth).put(+halfSmallHeight).put(z);
        crosshairVertexBuffer.put(+halfBigWidth).put(+halfSmallHeight).put(z);
        crosshairVertexBuffer.put(+halfBigWidth).put(+halfSmallHeight).put(z);
        crosshairVertexBuffer.put(+halfBigWidth).put(-halfSmallHeight).put(z);
        crosshairVertexBuffer.put(-halfBigWidth).put(-halfSmallHeight).put(z);
        crosshairVertexBuffer.rewind();
        crosshairMeshData.setVertexBuffer(crosshairVertexBuffer);
        final FloatBuffer crosshairColorBuffer = BufferUtils.createFloatBuffer(48);
        for (int vertexIndex = 0; vertexIndex < 12; vertexIndex++)
            crosshairColorBuffer.put(1.0f).put(0.0f).put(0.0f).put(1.0f);
        crosshairColorBuffer.rewind();
        crosshairMeshData.setColorBuffer(crosshairColorBuffer);
        crosshairMesh.setMeshData(crosshairMeshData);
        crosshairNode.attachChild(crosshairMesh);
        return (crosshairNode);
    }

    @SuppressWarnings("cast")
    private final void updateCrosshairNode() {
        final Node crosshairNode = (Node) playerNode.getChild("crosshair");
        if (crosshairNode != null) {
            final Mesh crosshairMesh = (Mesh) crosshairNode.getChild(0);
            final MeshData crosshairMeshData = crosshairMesh.getMeshData();
            final FloatBuffer crosshairVertexBuffer = crosshairMeshData.getVertexBuffer();
            final Camera cam = playerNode.getCamera();
            final float halfSmallWidth = 0.0004f * 1920.0f / ((float) cam.getWidth());
            final float halfSmallHeight = 0.0004f * 1080.0f / ((float) cam.getHeight());
            final float halfBigWidth = 0.005f * 1920.0f / ((float) cam.getWidth());
            final float halfBigHeight = 0.005f * 1080.0f / ((float) cam.getHeight());
            final float z = 1.0f;
            crosshairVertexBuffer.rewind();
            crosshairVertexBuffer.put(-halfSmallWidth).put(-halfBigHeight).put(z);
            crosshairVertexBuffer.put(-halfSmallWidth).put(+halfBigHeight).put(z);
            crosshairVertexBuffer.put(+halfSmallWidth).put(+halfBigHeight).put(z);
            crosshairVertexBuffer.put(+halfSmallWidth).put(+halfBigHeight).put(z);
            crosshairVertexBuffer.put(+halfSmallWidth).put(-halfBigHeight).put(z);
            crosshairVertexBuffer.put(-halfSmallWidth).put(-halfBigHeight).put(z);
            crosshairVertexBuffer.put(-halfBigWidth).put(-halfSmallHeight).put(z);
            crosshairVertexBuffer.put(-halfBigWidth).put(+halfSmallHeight).put(z);
            crosshairVertexBuffer.put(+halfBigWidth).put(+halfSmallHeight).put(z);
            crosshairVertexBuffer.put(+halfBigWidth).put(+halfSmallHeight).put(z);
            crosshairVertexBuffer.put(+halfBigWidth).put(-halfSmallHeight).put(z);
            crosshairVertexBuffer.put(-halfBigWidth).put(-halfSmallHeight).put(z);
            crosshairVertexBuffer.rewind();
            final FloatBuffer crosshairColorBuffer = crosshairMeshData.getColorBuffer();
            crosshairColorBuffer.rewind();
            for (int vertexIndex = 0; vertexIndex < 12; vertexIndex++)
                crosshairColorBuffer.put(1.0f).put(0.0f).put(0.0f).put(1.0f);
            crosshairColorBuffer.rewind();
            crosshairMeshData.getVertexCoords().setNeedsRefresh(true);
            crosshairMeshData.getColorCoords().setNeedsRefresh(true);
        }
    }

    private final void initializeCollisionSystem(final Camera cam) {
        // configures the collision system
        CollisionTreeManager.getInstance().setTreeType(CollisionTree.Type.AABB);
        // adds a mesh with an invisible mesh data
        final Mesh playerMesh = new Mesh("player");
        final MeshData playerMeshData = new MeshData();
        final FloatBuffer playerVertexBuffer = BufferUtils.createFloatBuffer(6);
        playerVertexBuffer.put(-0.5f).put(-0.9f).put(-0.5f).put(0.5f).put(0.9f).put(0.5f).rewind();
        playerMeshData.setVertexBuffer(playerVertexBuffer);
        playerMesh.setMeshData(playerMeshData);
        playerNode.attachChild(playerMesh);
        // adds a bounding box to the camera node
        NodeHelper.setModelBound(playerNode, BoundingBox.class);
        playerNode.addController(new SpatialController<Spatial>() {

            private final FloatBuffer projectileVertexBuffer;

            {
                projectileVertexBuffer = BufferUtils.createFloatBuffer(6);
                projectileVertexBuffer.put(-0.1f).put(-0.1f).put(-0.1f).put(0.1f).put(0.1f).put(0.1f).rewind();
            }

            private final CollisionResults collisionResults = new BoundingCollisionResults();

            private boolean wasBeingTeleported = false;

            // private long previouslyMeasuredElapsedTime=-1;

            @SuppressWarnings({ "unchecked", "cast" })
            @Override
            public void update(double timeSinceLastCall, Spatial caller) {
                // updates the timer
                timer.update();
                final long absoluteElapsedTimeInNanoseconds = timer.getElapsedTimeInNanoseconds();
                /*
                 * final long elapsedTimeSinceLatestCallInNanos=
                 * previouslyMeasuredElapsedTime==-1?0:
                 * absoluteElapsedTimeInNanoseconds-
                 * previouslyMeasuredElapsedTime; previouslyMeasuredElapsedTime=
                 * absoluteElapsedTimeInNanoseconds;
                 */
                // retrieves the data from the camera
                final Camera cam = ContextManager.getCurrentContext().getCurrentCamera();
                // temporary avoids to move on Y
                cam.setLocation(cam.getLocation().getX(), 0.5, cam.getLocation().getZ());
                // FIXME remove this temporary system
                double playerStartX = previousPosition.getX();
                double playerStartZ = previousPosition.getZ();
                double playerEndX = cam.getLocation().getX();
                double playerEndZ = cam.getLocation().getZ();
                double playerX, playerZ;
                double distance = previousPosition.distance(cam.getLocation());
                final int stepCount = (int) Math.ceil(distance / 0.2);
                double stepX = stepCount == 0 ? 0 : (playerEndX - playerStartX) / stepCount;
                double stepZ = stepCount == 0 ? 0 : (playerEndZ - playerStartZ) / stepCount;
                boolean collisionFound = false;
                double correctX = playerStartX, correctZ = playerStartZ;
                int tmpX, tmpZ;
                /*
                 * final List<BoundingBox>
                 * boundingBoxList=level.getCollisionVolumes();
                 * if(boundingBoxList!=null&&!boundingBoxList.isEmpty()) {final
                 * BoundingBox
                 * playerBoundingBox=(BoundingBox)playerMesh.getModelBound();
                 * //TODO for(final BoundingBox boundingBox:boundingBoxList)
                 * if(playerBoundingBox.intersects(boundingBox)) {
                 * 
                 * } } else {//no collision, nothing to prevent the player from
                 * moving correctX=playerEndX; correctZ=playerEndZ; }
                 */
                // temporary hack to disable collisions in some levels
                final boolean[][] collisionMap = level.getCollisionMap();
                for (int i = 1; i <= stepCount && !collisionFound; i++) {
                    playerX = playerStartX + (stepX * i);
                    playerZ = playerStartZ + (stepZ * i);
                    for (int z = 0; z < 2 && !collisionFound; z++)
                        for (int x = 0; x < 2 && !collisionFound; x++) {
                            tmpX = (int) (playerX - 0.2 + (x * 0.4));
                            tmpZ = (int) (playerZ - 0.2 + (z * 0.4));
                            if (collisionMap != null && 0 <= tmpX && tmpX < collisionMap.length && 0 <= tmpZ
                                    && tmpZ < collisionMap[tmpX].length)
                                collisionFound = collisionMap[tmpX][tmpZ];
                            else
                                collisionFound = false;
                        }
                    if (!collisionFound) {
                        correctX = playerX;
                        correctZ = playerZ;
                    } else if (stepX != 0 && stepZ != 0) {
                        collisionFound = false;
                        playerZ = playerStartZ + (stepZ * (i - 1));
                        for (int z = 0; z < 2 && !collisionFound; z++)
                            for (int x = 0; x < 2 && !collisionFound; x++) {
                                tmpX = (int) (playerX - 0.2 + (x * 0.4));
                                tmpZ = (int) (playerZ - 0.2 + (z * 0.4));
                                if (collisionMap != null && 0 <= tmpX && tmpX < collisionMap.length && 0 <= tmpZ
                                        && tmpZ < collisionMap[tmpX].length)
                                    collisionFound = collisionMap[tmpX][tmpZ];
                                else
                                    collisionFound = false;
                            }
                        if (!collisionFound) {
                            correctX = playerX;
                            correctZ = playerZ;
                        } else {
                            collisionFound = false;
                            playerX = playerStartX + (stepX * (i - 1));
                            playerZ = playerStartZ + (stepZ * i);
                            for (int z = 0; z < 2 && !collisionFound; z++)
                                for (int x = 0; x < 2 && !collisionFound; x++) {
                                    tmpX = (int) (playerX - 0.2 + (x * 0.4));
                                    tmpZ = (int) (playerZ - 0.2 + (z * 0.4));
                                    if (collisionMap != null && 0 <= tmpX && tmpX < collisionMap.length && 0 <= tmpZ
                                            && tmpZ < collisionMap[tmpX].length)
                                        collisionFound = collisionMap[tmpX][tmpZ];
                                    else
                                        collisionFound = false;
                                }
                            if (!collisionFound) {
                                correctX = playerX;
                                correctZ = playerZ;
                            }
                        }
                    }
                }
                // updates the current location
                playerNode.setTranslation(correctX, 0.5, correctZ);
                // copies the translation of the player node into the camera
                cam.setLocation(correctX, 0.5, correctZ);
                // synchronizes the camera node with the camera
                playerNode.updateFromCamera();
                // updates the previous location
                previousPosition.set(playerNode.getTranslation());
                // synchronizes the camera with the camera node
                cam.setLocation(playerNode.getTranslation());
                if (level.getSkyboxModel() != null)
                    level.getSkyboxModel().setTranslation(playerNode.getTranslation());
                // checks if any object is collected
                for (int i = collectibleObjectsList.size() - 1, collectedSubElementsCount; i >= 0; i--) {
                    final Node collectibleNode = collectibleObjectsList.get(i);
                    PickingUtil.findCollisions(collectibleNode, playerNode, collisionResults);
                    if (collisionResults.getNumber() > 0) {// tries to collect
                                                           // the object (update
                                                           // the player model
                                                           // (MVC))
                        collectedSubElementsCount = playerData.collect(collectibleNode);
                        // if it succeeds, detach the object from the root later
                        if (collectedSubElementsCount > 0) {// removes it from
                                                            // the list of
                                                            // collectible
                                                            // objects
                            collectibleObjectsList.remove(i);
                            if (collectibleNode.getParent() != null)
                                // detach this object from its parent so that it
                                // is no more visible
                                collectibleNode.getParent().detachChild(collectibleNode);
                            final CollectibleUserData<?> collectibleUserData = (CollectibleUserData<?>) collectibleNode
                                    .getUserData();
                            // displays a message when the player picked up
                            // something
                            final String subElementName = collectibleUserData.getSubElementName();
                            if (subElementName != null && !subElementName.isEmpty())
                                headUpDisplayLabel.setText("picked up " + collectedSubElementsCount + " "
                                        + subElementName + (collectedSubElementsCount > 1 ? "s" : ""));
                            else {
                                final String label = collectibleUserData.getLabel();
                                headUpDisplayLabel.setText("picked up " + label);
                            }
                            // plays a sound if available
                            if (collectibleUserData.getPickingUpSoundSampleIdentifier() != null)
                                getSoundManager().play(false, false,
                                        collectibleUserData.getPickingUpSoundSampleIdentifier());
                        }
                    }
                    collisionResults.clear();
                }
                // checks if any teleporter is used
                boolean hasCollision = false;
                for (int i = teleportersList.size() - 1; i >= 0 && !hasCollision; i--) {
                    final Node teleporterNode = teleportersList.get(i);
                    PickingUtil.findCollisions(teleporterNode, playerNode, collisionResults);
                    hasCollision = collisionResults.getNumber() > 0;
                    collisionResults.clear();
                    // if the current position is inside a teleporter
                    if (hasCollision) {
                        /**
                         * The teleporter can be bi-directional. A player who
                         * was being teleported in a direction should not be
                         * immediately teleported in the opposite direction. I
                         * use a flag to avoid this case because applying
                         * naively the algorithm would be problematic as the
                         * previous position is outside the teleporter and the
                         * current position is inside the teleporter.
                         */
                        // if the previous position is not on any teleporter
                        if (!wasBeingTeleported) {// the player enters a
                                                  // teleporter
                            wasBeingTeleported = true;
                            TeleporterUserData teleporterUserData = (TeleporterUserData) teleporterNode.getUserData();
                            final Vector3 teleporterDestination = teleporterUserData.getDestination();
                            final String teleporterDestinationLevelIdentifier = teleporterUserData
                                    .getDestinationLevelIdentifier();
                            // if the teleporter is in the current level
                            if (level.getIdentifier().equals(teleporterDestinationLevelIdentifier)) {
                                // then moves the player
                                playerNode.setTranslation(teleporterDestination);
                                // updates the previous location to avoid any
                                // problem when detecting the collisions
                                previousPosition.set(teleporterDestination);
                                // synchronizes the camera with the camera node
                                cam.setLocation(teleporterDestination);
                                // play a sound if available
                                if (teleporterUserData.getPickingUpSoundSampleIdentifier() != null)
                                    getSoundManager().play(false, false,
                                            teleporterUserData.getPickingUpSoundSampleIdentifier());
                            } else if (playerData.isAlive()) {
                                // otherwise leaves the level
                                MissionStatus missionStatus = MissionStatus.COMPLETED;
                                // checks the objectives, the mission is
                                // completed only if all objectives are
                                // completed
                                for (Objective objective : level.getObjectives())
                                    if (objective.getStatus(gameStats) != ObjectiveStatus.COMPLETED) {
                                        missionStatus = MissionStatus.FAILED;
                                        break;
                                    }
                                // updates the status of the current mission
                                gameStats.setMissionStatus(missionStatus);
                                // passes the previous location and the next
                                // location to the trigger action
                                toGameOverTriggerAction.arguments.setPreviousLevelIdentifier(level.getIdentifier());
                                toGameOverTriggerAction.perform(null, null, -1);
                                if (missionStatus == MissionStatus.COMPLETED) {
                                    // unlocks the next level
                                    profileData.addUnlockedLevelIdentifier(teleporterDestinationLevelIdentifier);
                                    // indicates the next level suggested to the
                                    // player
                                    toGameOverTriggerAction.arguments
                                            .setNextLevelIdentifier(teleporterDestinationLevelIdentifier);
                                    getSoundManager().play(false, false, victory1SoundSampleIdentifier);
                                } else {
                                    toGameOverTriggerAction.arguments.setNextLevelIdentifier(null);
                                    getSoundManager().play(false, false, gameoverSoundSampleIdentifier);
                                }
                            }
                        }
                    }
                }
                // if the player is not on any teleporter
                if (!hasCollision)
                    wasBeingTeleported = false;
                // handles the collisions between enemies and projectiles
                HashSet<Node> projectilesToRemove = new HashSet<>();
                ArrayList<EnemyData> editedEnemiesData = new ArrayList<>();
                // filters data to keep only the valid opponents
                final List<Spatial> reachableOpponents = new ArrayList<>();
                for (Spatial child : getRoot().getChildren()) {
                    if ((enemiesDataMap.keySet().contains(child) || child.equals(playerNode)))
                        reachableOpponents.add(child);
                }
                for (Entry<Node, ProjectileData> projectileEntry : projectilesMap.entrySet()) {
                    final Node projectileNode = projectileEntry.getKey();
                    final ProjectileData projectileData = projectileEntry.getValue();
                    hasCollision = false;
                    // prevents the originator from committing a suicide
                    final Spatial originator = getRoot().getChild(projectileData.getOriginator());
                    reachableOpponents.remove(originator);
                    // sorts the opponents to perform a collision check on the
                    // closest one
                    projectileDataOpponentsComparator.setProjectileData(projectileData);
                    Collections.sort(reachableOpponents, projectileDataOpponentsComparator);
                    projectileDataOpponentsComparator.setProjectileData(null);
                    for (Spatial child : reachableOpponents) {
                        if (enemiesDataMap.keySet().contains(child)) {
                            Ray3 ray = new Ray3(projectileNode.getTranslation(),
                                    projectileNode.getTransform().getMatrix().getColumn(2, null));
                            BoundingPickResults results = new BoundingPickResults();
                            PickingUtil.findPick(child, ray, results);
                            hasCollision = results.getNumber() > 0;
                            results.clear();
                            if (hasCollision) {
                                /**
                                 * TODO - Create a data model (for the enemy)
                                 * containing the current state, the health, the
                                 * ammunition, ... As a first step, it should be
                                 * very limited. On the long term, it will have
                                 * to be homogeneous with the data model used
                                 * for the player so that any enemy can behave
                                 * like a bot in the arena mode - Create another
                                 * controller to modify the view depending on
                                 * the changes in the data model
                                 */
                                // attempts to kill this enemy
                                final EnemyData soldierData = enemiesDataMap.get(child);
                                editedEnemiesData.add(soldierData);
                                final KeyframeController<Mesh> soldierKeyframeController = (KeyframeController<Mesh>) child
                                        .getController(0);
                                if (soldierData.isAlive()) {
                                    soldierData.decreaseHealth(25);
                                    // stops at the last frame of the set in the
                                    // supplied time frame
                                    soldierKeyframeController.setRepeatType(RepeatType.CLAMP);
                                    // selects randomly the death kind
                                    final int localFrameIndex;
                                    if (soldierData.isAlive())
                                        localFrameIndex = 3 + random.nextInt(3);
                                    else {
                                        localFrameIndex = random.nextInt(3);
                                        gameStats.setKilledEnemiesCount(gameStats.getKilledEnemiesCount() + 1);
                                    }
                                    final MD2FrameSet frameSet;
                                    switch (localFrameIndex) {
                                    case 0:
                                        frameSet = MD2FrameSet.DEATH_FALLFORWARD;
                                        break;
                                    case 1:
                                        frameSet = MD2FrameSet.DEATH_FALLBACK;
                                        break;
                                    case 2:
                                        frameSet = MD2FrameSet.DEATH_FALLBACKSLOW;
                                        break;
                                    case 3:
                                        frameSet = MD2FrameSet.PAIN_A;
                                        break;
                                    case 4:
                                        frameSet = MD2FrameSet.PAIN_B;
                                        break;
                                    case 5:
                                        frameSet = MD2FrameSet.PAIN_C;
                                        break;
                                    default:
                                        frameSet = null;
                                    }
                                    if (frameSet != null) {
                                        soldierKeyframeController.setSpeed(frameSet.getFramesPerSecond());
                                        soldierKeyframeController.setCurTime(frameSet.getFirstFrameIndex());
                                        soldierKeyframeController.setMinTime(frameSet.getFirstFrameIndex());
                                        soldierKeyframeController.setMaxTime(frameSet.getLastFrameIndex());
                                        final Mesh soldierWeaponMesh = (Mesh) getRoot()
                                                .getChild((getRoot().getChildren().indexOf(child) + 1));
                                        if (localFrameIndex >= 3) {
                                            final KeyframeController<Mesh> soldierWeaponKeyframeController = (KeyframeController<Mesh>) soldierWeaponMesh
                                                    .getController(0);
                                            soldierWeaponKeyframeController.setSpeed(frameSet.getFramesPerSecond());
                                            soldierWeaponKeyframeController.setCurTime(frameSet.getFirstFrameIndex());
                                            soldierWeaponKeyframeController.setMinTime(frameSet.getFirstFrameIndex());
                                            soldierWeaponKeyframeController.setMaxTime(frameSet.getLastFrameIndex());
                                        } else {
                                            // there are only 173 frames for weapons
                                            soldierWeaponMesh.setVisible(false);
                                        }
                                    }
                                    // plays a sound if the enemy is not dead
                                    final double healthFactor = Math.max(0.0d,
                                            Math.min((double) soldierData.getHealth(), 100.0d)) / 100.0d;
                                    // FIXME store the identifier into another
                                    // data structure within this state
                                    final Enemy enemy = enemyFactory.get("SOLDIER");
                                    final int painSoundSampleCount = enemy.getPainSoundSampleCount();
                                    final int painSoundSampleIndex = painSoundSampleCount - 1
                                            - Math.max(0,
                                                    Math.min(
                                                            (int) Math.rint(Math.floor(
                                                                    healthFactor * (double) painSoundSampleCount)),
                                                    painSoundSampleCount - 1));
                                    final String painSoundSampleIdentifier = enemy
                                            .getPainSoundSampleIdentifier(painSoundSampleIndex);
                                    getSoundManager().play(false, false, painSoundSampleIdentifier);
                                }
                                // FIXME only remove the projectile if it
                                // doesn't pass through the enemy
                                projectilesToRemove.add(projectileNode);
                                break;
                            }
                        } else if (child.equals(playerNode)) {
                            Ray3 ray = new Ray3(projectileNode.getTranslation(),
                                    projectileNode.getTransform().getMatrix().getColumn(2, null));
                            BoundingPickResults results = new BoundingPickResults();
                            PickingUtil.findPick(child, ray, results);
                            hasCollision = results.getNumber() > 0;
                            results.clear();
                            if (hasCollision) {
                                if (playerData.isAlive()) {
                                    playerData.decreaseHealth(10);
                                    final double healthFactor = Math.max(0.0d,
                                            Math.min((double) playerData.getHealth(), 100.0d)) / 100.0d;
                                    final int painSoundSampleIndex = painSoundSampleIdentifiers.length - 1
                                            - Math.max(0,
                                                    Math.min(
                                                            (int) Math.rint(Math.floor(healthFactor
                                                                    * (double) painSoundSampleIdentifiers.length)),
                                                    painSoundSampleIdentifiers.length - 1));
                                    getSoundManager().play(false, false,
                                            painSoundSampleIdentifiers[painSoundSampleIndex]);
                                }
                                // FIXME only remove the projectile if it
                                // doesn't pass through the player
                                projectilesToRemove.add(projectileNode);
                                break;
                            }
                        }
                    }
                    // resets the list of opponents
                    reachableOpponents.add(originator);
                }
                // FIXME only remove "infinite" rays
                // as all projectiles are designed with rays, they shouldn't
                // stay in the data model any longer
                projectilesToRemove.addAll(projectilesMap.keySet());
                // FIXME move this logic into a state machine
                for (Entry<Mesh, EnemyData> enemyEntry : enemiesDataMap.entrySet()) {
                    final EnemyData enemyData = enemyEntry.getValue();
                    if (!editedEnemiesData.contains(enemyData) && enemyData.isAlive()) {
                        final Mesh enemyMesh = enemyEntry.getKey();
                        //FIXME use getParent()?
                        final Mesh enemyWeaponMesh = (Mesh) getRoot()
                                .getChild((getRoot().getChildren().indexOf(enemyMesh) + 1));
                        final KeyframeController<Mesh> enemyKeyframeController = (KeyframeController<Mesh>) enemyMesh
                                .getController(0);
                        final KeyframeController<Mesh> enemyWeaponKeyframeController = (KeyframeController<Mesh>) enemyWeaponMesh
                                .getController(0);
                        // if this enemy is not yet idle and if he has finished
                        // his latest animation
                        if (/* enemyKeyframeController.isRepeatTypeClamp()&& */
                        enemyKeyframeController.getMaxTime() != MD2FrameSet.STAND.getLastFrameIndex()) {
                            if (enemyKeyframeController.getCurTime() > enemyKeyframeController.getMaxTime()) {
                                enemyKeyframeController.setRepeatType(RepeatType.WRAP);
                                // uses the "stand" animation
                                enemyKeyframeController.setSpeed(MD2FrameSet.STAND.getFramesPerSecond());
                                enemyKeyframeController.setCurTime(MD2FrameSet.STAND.getFirstFrameIndex());
                                enemyKeyframeController.setMinTime(MD2FrameSet.STAND.getFirstFrameIndex());
                                enemyKeyframeController.setMaxTime(MD2FrameSet.STAND.getLastFrameIndex());

                                enemyWeaponKeyframeController.setRepeatType(RepeatType.WRAP);
                                enemyWeaponKeyframeController.setSpeed(MD2FrameSet.STAND.getFramesPerSecond());
                                enemyWeaponKeyframeController.setCurTime(MD2FrameSet.STAND.getFirstFrameIndex());
                                enemyWeaponKeyframeController.setMinTime(MD2FrameSet.STAND.getFirstFrameIndex());
                                enemyWeaponKeyframeController.setMaxTime(MD2FrameSet.STAND.getLastFrameIndex());
                            }
                        } else {
                            Long latestDetectionObj = enemiesLatestDetection.get(enemyData);
                            if ((latestDetectionObj == null
                                    || absoluteElapsedTimeInNanoseconds - latestDetectionObj.longValue() >= 1000000000)
                                    && playerData.isAlive()) {
                                enemiesLatestDetection.put(enemyData, Long.valueOf(absoluteElapsedTimeInNanoseconds));
                                // checks whether the player is in front of this
                                // enemy (defensive behavior)
                                final Ray3 fromPlayerToEnemyRay = new Ray3(playerNode.getTranslation(),
                                        playerNode.getTransform().getMatrix().getColumn(2, null));
                                final BoundingPickResults results = new BoundingPickResults();
                                PickingUtil.findPick(enemyMesh, fromPlayerToEnemyRay, results);
                                hasCollision = results.getNumber() > 0;
                                results.clear();
                                if (hasCollision) {
                                    final Ray3 fromEnemyToPlayerRay = new Ray3(enemyMesh.getTranslation(), 
                                            enemyMesh.getTransform().getMatrix().getColumn(2, null));
                                    PickingUtil.findPick(playerNode, fromEnemyToPlayerRay, results);
                                    hasCollision = results.getNumber() > 0;
                                    results.clear();
                                    // checks whether this enemy is in front of the player
                                    if (hasCollision) {
                                        //TODO this enemy should remember that this player is a threat for him
                                        // it's worth opening fire
                                        // uses the "attack" animation
                                        final MD2FrameSet frameSet = MD2FrameSet.ATTACK;
                                        enemyKeyframeController.setRepeatType(RepeatType.CLAMP);
                                        enemyKeyframeController.setSpeed(frameSet.getFramesPerSecond());
                                        enemyKeyframeController.setCurTime(frameSet.getFirstFrameIndex());
                                        enemyKeyframeController.setMinTime(frameSet.getFirstFrameIndex());
                                        enemyKeyframeController.setMaxTime(frameSet.getLastFrameIndex());

                                        enemyWeaponKeyframeController.setRepeatType(RepeatType.CLAMP);
                                        enemyWeaponKeyframeController.setSpeed(frameSet.getFramesPerSecond());
                                        enemyWeaponKeyframeController.setCurTime(frameSet.getFirstFrameIndex());
                                        enemyWeaponKeyframeController.setMinTime(frameSet.getFirstFrameIndex());
                                        enemyWeaponKeyframeController.setMaxTime(frameSet.getLastFrameIndex());

                                        // creates a new projectile
                                        createEnemyProjectile(enemyData, enemyMesh, enemyWeaponMesh);
                                        getSoundManager().play(false, false, enemyShotgunShotSampleIdentifier);
                                    } else {
                                        final MD2FrameSet frameSet = MD2FrameSet.RUN;
                                        enemyKeyframeController.setRepeatType(RepeatType.CLAMP);
                                        enemyKeyframeController.setSpeed(frameSet.getFramesPerSecond());
                                        enemyKeyframeController.setCurTime(frameSet.getFirstFrameIndex());
                                        enemyKeyframeController.setMinTime(frameSet.getFirstFrameIndex());
                                        enemyKeyframeController.setMaxTime(frameSet.getLastFrameIndex());

                                        enemyWeaponKeyframeController.setRepeatType(RepeatType.CLAMP);
                                        enemyWeaponKeyframeController.setSpeed(frameSet.getFramesPerSecond());
                                        enemyWeaponKeyframeController.setCurTime(frameSet.getFirstFrameIndex());
                                        enemyWeaponKeyframeController.setMinTime(frameSet.getFirstFrameIndex());
                                        enemyWeaponKeyframeController.setMaxTime(frameSet.getLastFrameIndex());
                                        
                                        // previous enemy direction, extracts the forward vector of the rotation matrix
                                        final Vector3 previousEnemyDirection = enemyMesh.getTransform().getMatrix().getColumn(2, null).normalizeLocal();
                                        // vector from the enemy to the player
                                        final Vector3 nextEnemyDirection = playerNode.getTranslation().subtract(enemyMesh.getTranslation(), null).normalizeLocal();
                                        // rotation from the previous direction to the next direction
                                        final Quaternion fromPreviousToNextEnemyDirectionRotation = new Quaternion().fromVectorToVector(previousEnemyDirection, nextEnemyDirection);
                                        // previous rotation
                                        final Quaternion startQuat = new Quaternion().fromRotationMatrix(enemyMesh.getRotation());
                                        // next rotation
                                        final Quaternion endQuat = startQuat.multiply(fromPreviousToNextEnemyDirectionRotation, null);
                                        // current rotation
                                        //TODO start rotating here and use SLERP step by step in the next frames instead of rotating completely now
                                        final Quaternion currentQuat = Quaternion.slerp(startQuat, endQuat, 1.0, null);
                                        // current rotation as a matrix
                                        final Matrix3 enemyFacingPlayerMatrix = currentQuat.toRotationMatrix((Matrix3) null);
                                        // applies the current rotation to the enemy and to his weapon
                                        enemyMesh.setRotation(enemyFacingPlayerMatrix);
                                        enemyWeaponMesh.setRotation(enemyFacingPlayerMatrix);
                                    }
                                }
                            }
                        }
                    }
                }
                for (Node projectileToRemove : projectilesToRemove) {
                    projectilesMap.remove(projectileToRemove);
                    getRoot().detachChild(projectileToRemove);
                }
                if (playerData.isAlive()) {
                    fpsc.setKeyRotateSpeed(customMouseAndKeyboardSettings.getKeyRotateSpeed());
                    fpsc.setMouseRotateSpeed(customMouseAndKeyboardSettings.getMouseRotateSpeed());
                    fpsc.setMoveSpeed(customMouseAndKeyboardSettings.getMoveSpeed());
                } else {
                    if (latestPlayerDeath == null) {
                        gameStats.setMissionStatus(MissionStatus.DECEASED);
                        fpsc.setKeyRotateSpeed(0);
                        fpsc.setMouseRotateSpeed(0);
                        fpsc.setMoveSpeed(0);
                        latestPlayerDeath = Long.valueOf(absoluteElapsedTimeInNanoseconds);
                        getSoundManager().play(false, false, gameoverSoundSampleIdentifier);
                    } else {
                        final long latestDeathDuration = absoluteElapsedTimeInNanoseconds
                                - latestPlayerDeath.longValue();
                        final double y = 0.4d
                                - ((((double) Math.max(0, Math.min(latestDeathDuration, 500000000))) / 500000000.0d)
                                        * 0.4d)
                                + 0.1d;
                        playerNode.setTranslation(playerNode.getTranslation().getX(), y,
                                playerNode.getTranslation().getZ());
                        playerNode.getCamera().setLocation(playerNode.getTranslation());
                        if (latestDeathDuration > 500000000) {
                            toGameOverTriggerAction.arguments.setPreviousLevelIdentifier(level.getIdentifier());
                            // the player can't go to the next level when he
                            // dies
                            toGameOverTriggerAction.arguments.setNextLevelIdentifier(null);
                            toGameOverTriggerAction.perform(null, null, -1);
                        }
                    }
                }
                // looks for any changes in the objectives statuses
                final List<Objective> updatedObjectives = new ArrayList<>();
                for (Objective objective : level.getObjectives()) {
                    // retrieves the previous and current objective statuses
                    final ObjectiveStatus previousObjectiveStatus = previousObjectivesStatusesMap.get(objective);
                    final ObjectiveStatus currentObjectiveStatus = objective.getStatus(gameStats);
                    // if the objective status has just changed
                    if (previousObjectiveStatus != currentObjectiveStatus) {
                        updatedObjectives.add(objective);
                        // updates the map
                        previousObjectivesStatusesMap.put(objective, currentObjectiveStatus);
                    }
                }
                if (!updatedObjectives.isEmpty()) {
                    final StringBuilder builder = new StringBuilder();
                    if (updatedObjectives.size() == 1)
                        builder.append("Updated objective:");
                    else
                        builder.append("Updated objectives:");
                    boolean allObjectivesAreCompleted = true;
                    for (Objective objective : updatedObjectives) {
                        builder.append("\n");
                        builder.append(objective.getDescription());
                        builder.append(": ");
                        final ObjectiveStatus status = previousObjectivesStatusesMap.get(objective);
                        builder.append(status.toString());
                        if (status != ObjectiveStatus.COMPLETED && allObjectivesAreCompleted)
                            allObjectivesAreCompleted = false;
                    }
                    // updates the panel
                    objectivesDisplayLabel.setText(builder.toString());
                    if (allObjectivesAreCompleted) {
                        // plays a sound as all updated objectives are completed
                        getSoundManager().play(false, false, victory0SoundSampleIdentifier);
                    }
                }
                // updates the state machine of the player
                playerWithStateMachine.updateLogicalLayer(timer);
            }

            private void createEnemyProjectile(EnemyData enemyData, Mesh enemyMesh, Mesh enemyWeaponMesh) {
                final Vector3 initialLocation = new Vector3(enemyWeaponMesh.getWorldBound().getCenter());
                final String originator = enemyMesh.getName();
                final double initialSpeed = 350.0 / 1000000000.0;
                final double initialAcceleration = 0;
                final Vector3 initialDirection = enemyWeaponMesh.getWorldTransform().getMatrix().getColumn(2, null);
                final long initialTimeInNanos = timer.getElapsedTimeInNanoseconds();
                final ProjectileData projectileData = new ProjectileData(originator, initialLocation, initialSpeed,
                        initialAcceleration, initialDirection, initialTimeInNanos);
                final Node projectileNode = new Node(projectileData.toString());
                NodeHelper.setModelBound(projectileNode, BoundingBox.class);
                projectileNode.setTransform(enemyWeaponMesh.getWorldTransform());
                projectileNode.setTranslation(initialLocation);
                final Mesh projectileMesh = createProjectileMesh(null, projectileData);
                projectileNode.attachChild(projectileMesh);
                projectileNode.addController(new ProjectileController(timer, projectileData));
                // stores it for a further use
                projectilesMap.put(projectileNode, projectileData);
                getRoot().attachChild(projectileNode);
            }

            private Mesh createProjectileMesh(final Weapon weapon, final ProjectileData projectileData) {
                // TODO support several kinds of projectile with different sizes
                Mesh projectileMesh = new Mesh("Mesh@" + projectileData.toString());
                MeshData projectileMeshData = new MeshData();
                projectileMeshData.setVertexBuffer(projectileVertexBuffer);
                projectileMesh.setMeshData(projectileMeshData);
                return (projectileMesh);
            }
        });
    }

    private static final class ProjectileDataOpponentsComparator implements Comparator<Spatial> {

        private ProjectileData projectileData;

        @Override
        public int compare(final Spatial opponent0, final Spatial opponent1) {
            final double distance0 = opponent0.getWorldTranslation().distance(projectileData.getInitialLocation());
            final double distance1 = opponent1.getWorldTranslation().distance(projectileData.getInitialLocation());
            final int compareFactor = distance0 == distance1 ? 0 : distance0 < distance1 ? -1 : 1;
            return (compareFactor);
        }

        public void setProjectileData(final ProjectileData projectileData) {
            this.projectileData = projectileData;
        }
    }

    /**
     * logical entity allowing to manipulate a player used as a link between the
     * state machine and the player data
     * 
     * @author Julien Gouesse
     *
     */
    public static final class LogicalPlayer {

        private final PlayerStateMachine stateMachine;

        private final PlayerData playerData;

        private double elapsedTimeSinceLatestTransitionInSeconds = 0;

        private double initialLatestPutBackProgress = 0, initialEndAttackProgress = 0;

        public LogicalPlayer(final PlayerData playerData) {
            this.playerData = playerData;
            this.stateMachine = new PlayerStateMachine(playerData);
        }

        public void updateLogicalLayer(final ReadOnlyTimer timer) {
            // gets the previous state of the player
            final PlayerState previousPlayerState = getPreviousState();
            // updates its state
            stateMachine.updateLogicalLayer(timer);
            // gets its current state (as is after the update)
            final PlayerState currentPlayerState = getPreviousState();
            // updates the amount of time since the latest transition
            if (previousPlayerState != currentPlayerState)
                elapsedTimeSinceLatestTransitionInSeconds = 0;
            else
                elapsedTimeSinceLatestTransitionInSeconds += timer.getTimePerFrame();
            // updates the player data from its state machine and the elapsed
            // time since the latest transition
            switch (currentPlayerState) {
            case NOT_YET_AVAILABLE: {// there is nothing to do
                break;
            }
            case IDLE: {
                break;
            }
            case PRESS_TRIGGER: {
                playerData.pressTrigger(elapsedTimeSinceLatestTransitionInSeconds);
                break;
            }
            case ATTACK: {
                playerData.attack(elapsedTimeSinceLatestTransitionInSeconds);
                break;
            }
            case WAIT_FOR_ATTACK_END: {
                if (elapsedTimeSinceLatestTransitionInSeconds == 0)
                    initialEndAttackProgress = playerData.computeEndAttackProgress();
                playerData.waitForAttackEnd(elapsedTimeSinceLatestTransitionInSeconds, initialEndAttackProgress);
            }
            case RELEASE_TRIGGER: {
                playerData.releaseTrigger(elapsedTimeSinceLatestTransitionInSeconds);
                break;
            }
            case WAIT_FOR_TRIGGER_RELEASE: {
                playerData.waitForTriggerRelease(elapsedTimeSinceLatestTransitionInSeconds);
                break;
            }
            case RELOAD: {
                break;
            }
            case PULL_OUT: {
                playerData.pullOut(elapsedTimeSinceLatestTransitionInSeconds);
                break;
            }
            case PUT_BACK: {
                if (elapsedTimeSinceLatestTransitionInSeconds == 0)
                    initialLatestPutBackProgress = playerData.computePutBackProgress();
                playerData.putBack(elapsedTimeSinceLatestTransitionInSeconds, initialLatestPutBackProgress);
                break;
            }
            case SELECT_NEXT: {
                break;
            }
            case SELECT_PREVIOUS: {
                break;
            }
            default:
                // it should never happen
            }
        }

        public void tryReload() {
            if (playerData.isAlive())
                stateMachine.fireEvent(PlayerEvent.PUTTING_BACK_BEFORE_RELOADING);
        }

        public void trySelectNextWeapon() {
            if (playerData.isAlive())
                stateMachine.fireEvent(PlayerEvent.PUTTING_BACK_BEFORE_SELECTING_NEXT);
        }

        public void trySelectPreviousWeapon() {
            if (playerData.isAlive())
                stateMachine.fireEvent(PlayerEvent.PUTTING_BACK_BEFORE_SELECTING_PREVIOUS);
        }

        public PlayerState getPreviousState() {
            return (stateMachine.previousState);
        }

        public void tryStartAttacking() {
            if (playerData.isAlive())
                stateMachine.fireEvent(PlayerEvent.PRESSING_TRIGGER);
        }

        public void tryStopAttacking() {
            stateMachine.fireEvent(PlayerEvent.RELEASING_TRIGGER);
        }
    }

    private final void initializeInput(final TransitionTriggerAction<ScenegraphState, String> toPauseMenuTriggerAction,
            final TransitionTriggerAction<ScenegraphState, String> toPauseMenuTriggerActionForExitConfirm,
            final TriggerAction toggleScreenModeAction, final PhysicalLayer physicalLayer) {
        // deregisters all triggers
        if (!getLogicalLayer().getTriggers().isEmpty()) {
            final Set<InputTrigger> triggers = new HashSet<>(getLogicalLayer().getTriggers());
            for (InputTrigger trigger : triggers)
                getLogicalLayer().deregisterTrigger(trigger);
        }
        final Vector3 worldUp = new Vector3(0, 1, 0);
        // sets "drag only" to false to remove the need of pressing a button to
        // move
        fpsc = ExtendedFirstPersonControl.setupTriggers(getLogicalLayer(), worldUp, false, customActionMap);
        // applies the mouse and keyboard settings
        fpsc.setKeyRotateSpeed(customMouseAndKeyboardSettings.getKeyRotateSpeed());
        fpsc.setLookUpDownReversed(customMouseAndKeyboardSettings.isLookUpDownReversed());
        fpsc.setMouseRotateSpeed(customMouseAndKeyboardSettings.getMouseRotateSpeed());
        fpsc.setMoveSpeed(customMouseAndKeyboardSettings.getMoveSpeed());
        final InputTrigger exitPromptTrigger = new InputTrigger(customActionMap.getCondition(Action.QUIT, false),
                new TriggerAction() {
                    @Override
                    public void perform(Canvas source, TwoInputStates inputState, double tpf) {
                        toPauseMenuTriggerActionForExitConfirm.perform(null, null, -1);
                    }
                });
        final TriggerAction nextWeaponAction = new TriggerAction() {
            @Override
            public void perform(Canvas source, TwoInputStates inputState, double tpf) {
                playerWithStateMachine.trySelectNextWeapon();
            }
        };
        final TriggerAction previousWeaponAction = new TriggerAction() {
            @Override
            public void perform(Canvas source, TwoInputStates inputState, double tpf) {
                playerWithStateMachine.trySelectPreviousWeapon();
            }
        };
        final TriggerAction reloadWeaponAction = new TriggerAction() {
            @Override
            public void perform(Canvas source, TwoInputStates inputState, double tpf) {
                playerWithStateMachine.tryReload();
            }
        };
        final TriggerAction startAttackAction = new TriggerAction() {
            @Override
            public void perform(Canvas source, TwoInputStates inputState, double tpf) {
                playerWithStateMachine.tryStartAttacking();
            }
        };
        final TriggerAction stopAttackAction = new TriggerAction() {
            @Override
            public void perform(Canvas source, TwoInputStates inputState, double tpf) {
                playerWithStateMachine.tryStopAttacking();
            }
        };
        /**
         * TODO implement these actions when the state machine is ready to
         * handle them
         */
        final TriggerAction pauseAction = new TriggerAction() {
            @Override
            public void perform(Canvas source, TwoInputStates inputState, double tpf) {
                // TODO: pause
                // TODO: pause the timer
                // timer.setPauseEnabled(true);
                // timer.update();
                toPauseMenuTriggerAction.perform(null, null, -1);
            }
        };
        final TriggerAction crouchAction = new TriggerAction() {
            @Override
            public void perform(Canvas source, TwoInputStates inputState, double tpf) {
            }
        };
        final TriggerAction activateAction = new TriggerAction() {
            @Override
            public void perform(Canvas source, TwoInputStates inputState, double tpf) {
            }
        };
        final TriggerAction startRunningAction = new TriggerAction() {
            @Override
            public void perform(Canvas source, TwoInputStates inputState, double tpf) {
            }
        };
        final TriggerAction stopRunningAction = new TriggerAction() {
            @Override
            public void perform(Canvas source, TwoInputStates inputState, double tpf) {
            }
        };
        final TriggerAction toggleWireframeModeAction = new TriggerAction() {
            @Override
            public void perform(final Canvas source, final TwoInputStates inputState, final double tpf) {
                wireframeState.setEnabled(!wireframeState.isEnabled());
                getRoot().markDirty(DirtyType.RenderState);
            }
        };
        /*
         * final TriggerAction selectWeaponOneAction=new TriggerAction(){
         * 
         * @Override public void perform(Canvas source, TwoInputStates
         * inputState, double tpf){ //playerData.selectWeapon(0,false); } };
         */
        // add some triggers to change weapon, reload and shoot
        final InputTrigger nextWeaponTrigger = new InputTrigger(customActionMap.getCondition(Action.NEXT_WEAPON, false),
                nextWeaponAction);
        final InputTrigger previousWeaponTrigger = new InputTrigger(
                customActionMap.getCondition(Action.PREVIOUS_WEAPON, false), previousWeaponAction);
        final InputTrigger reloadWeaponTrigger = new InputTrigger(customActionMap.getCondition(Action.RELOAD, false),
                reloadWeaponAction);
        final InputTrigger startAttackTrigger = new InputTrigger(customActionMap.getCondition(Action.ATTACK, true),
                startAttackAction);
        final InputTrigger stopAttackTrigger = new InputTrigger(customActionMap.getCondition(Action.ATTACK, false),
                stopAttackAction);
        final InputTrigger pauseTrigger = new InputTrigger(customActionMap.getCondition(Action.PAUSE, false),
                pauseAction);
        final InputTrigger crouchTrigger = new InputTrigger(customActionMap.getCondition(Action.CROUCH, false),
                crouchAction);
        final InputTrigger activateTrigger = new InputTrigger(customActionMap.getCondition(Action.ACTIVATE, false),
                activateAction);
        final InputTrigger startRunningRightTrigger = new InputTrigger(customActionMap.getCondition(Action.RUN, true),
                startRunningAction);
        final InputTrigger stopRunningRightTrigger = new InputTrigger(customActionMap.getCondition(Action.RUN, false),
                stopRunningAction);
        final InputTrigger toggleWireframeModeTrigger = new InputTrigger(
                customActionMap.getCondition(Action.TOGGLE_WIREFRAME_MODE, false), toggleWireframeModeAction);
        final InputTrigger[] triggers = new InputTrigger[] { exitPromptTrigger, nextWeaponTrigger,
                previousWeaponTrigger, reloadWeaponTrigger, startAttackTrigger, pauseTrigger, crouchTrigger,
                activateTrigger, startRunningRightTrigger, stopRunningRightTrigger, stopAttackTrigger,
                toggleWireframeModeTrigger };
        getLogicalLayer().registerInput(canvas, physicalLayer);
        for (InputTrigger trigger : triggers)
            getLogicalLayer().registerTrigger(trigger);
    }

    private final BasicText initializeAmmunitionTextLabel() {
        final BasicText ammoTextLabel = BasicText.createDefaultTextLabel("ammo display", "");
        ammoTextLabel.setTranslation(new Vector3(0, 80, 0));
        ammoTextLabel.addController(new SpatialController<Spatial>() {
            @Override
            public final void update(double time, Spatial caller) {
                if (playerData.isCurrentWeaponAmmunitionCountDisplayable()) {
                    final StringBuffer text = new StringBuffer("AMMO: ");
                    if (playerData.isDualWeaponUseEnabled()) {
                        text.append(playerData.getAmmunitionCountInSecondaryHandedWeapon());
                        text.append(" ");
                    }
                    text.append(playerData.getAmmunitionCountInPrimaryHandedWeapon());
                    text.append(" ");
                    text.append(playerData.getAmmunitionCountInContainer());
                    ammoTextLabel.setText(text.toString());
                } else
                    ammoTextLabel.setText("N/A");
            }
        });
        return (ammoTextLabel);
    }

    private final BasicText initializeFpsTextLabel() {
        final BasicText fpsTextLabel = BasicText.createDefaultTextLabel("FPS display", "");
        fpsTextLabel.setTranslation(new Vector3(0, 20, 0));
        fpsTextLabel.addController(new SpatialController<Spatial>() {

            private double period;

            private int frameCount;

            @Override
            public final void update(double timePerFrame, Spatial caller) {
                if (period > 1) {
                    final int framesPerSecond = (int) Math
                            .round(frameCount > 0 && period > 0 ? frameCount / period : 0);
                    fpsTextLabel.setText(" " + framesPerSecond + " FPS");
                    period = timePerFrame;
                    frameCount = 1;
                } else {
                    period += timePerFrame;
                    frameCount++;
                }
            }
        });
        return (fpsTextLabel);
    }

    private final BasicText initializeHealthTextLabel() {
        final BasicText healthTextLabel = BasicText.createDefaultTextLabel("health display", "");
        healthTextLabel.setTranslation(new Vector3(0, 60, 0));
        healthTextLabel.addController(new SpatialController<Spatial>() {
            @Override
            public final void update(double time, Spatial caller) {
                healthTextLabel.setText("HEALTH: " + playerData.getHealth());
            }
        });
        return (healthTextLabel);
    }

    /**
     * Controller of a basic text to display some temporary messages
     * 
     * @author Julien Gouesse
     *
     */
    private static final class TemporaryMessagesBasicTextController implements SpatialController<BasicText> {

        private String latestText = "";

        private double duration = 0;

        private final double maxDuration;

        private TemporaryMessagesBasicTextController(final double maxDuration) {
            super();
            this.maxDuration = maxDuration;
        }

        @Override
        public final void update(final double time, final BasicText caller) {
            // if the label contains anything
            if (!caller.getText().isEmpty()) {
                // if it contains the same text
                if (latestText.equals(caller.getText())) {
                    // increases the display time
                    duration += time;
                    // if it has been displayed for a too long time
                    if (duration > maxDuration) {
                        // removes it
                        caller.setText("");
                        duration = 0;
                    }
                } else {// otherwise updates the latest text
                    latestText = caller.getText();
                    duration = 0;
                }
            }
        }
    }

    private final BasicText initializeHeadUpDisplayLabel() {
        final BasicText headUpDisplayLabel = BasicText.createDefaultTextLabel("Head-up display", "");
        headUpDisplayLabel.setTranslation(new Vector3(0, 40, 0));
        headUpDisplayLabel.addController(new TemporaryMessagesBasicTextController(3));
        return (headUpDisplayLabel);
    }

    private final BasicText initializeObjectivesDisplayLabel() {
        final BasicText objectivesDisplayLabel = BasicText.createDefaultTextLabel("Objectives display", "");
        objectivesDisplayLabel.setTranslation(new Vector3(0, 200, 0));
        objectivesDisplayLabel.addController(new TemporaryMessagesBasicTextController(3));
        return (objectivesDisplayLabel);
    }

    private final TeleporterFactory initializeTeleporterFactory() {
        final TeleporterFactory teleporterFactory = new TeleporterFactory();
        teleporterFactory.addNewTeleporter("", "", "/sounds/teleporter_use.ogg");
        return (teleporterFactory);
    }

    private final MedikitFactory initializeMedikitFactory() {
        final MedikitFactory medikitFactory = new MedikitFactory();
        medikitFactory.addNewMedikit("first aid kit", "FIRST_AID_KIT", "/images/medikit.png", "/sounds/powerup.ogg",
                10);
        medikitFactory.addNewMedikit("small medikit", "SMALL_MEDIKIT", "/images/medikit.png", "/sounds/powerup.ogg",
                25);
        medikitFactory.addNewMedikit("large medikit", "LARGE_MEDIKIT", "/images/medikit.png", "/sounds/powerup.ogg",
                100);
        return (medikitFactory);
    }

    private final EnemyFactory initializeEnemyFactory() {
        final EnemyFactory enemyFactory = new EnemyFactory();
        enemyFactory.addNewEnemy("soldier", "SOLDIER", "/abin/soldier.abin",
                new String[] { "/sounds/pain1.ogg", "/sounds/pain2.ogg", "/sounds/pain3.ogg", "/sounds/pain4.ogg",
                        "/sounds/pain5.ogg", "/sounds/pain6.ogg" });
        return (enemyFactory);
    }

    private final LevelFactory initializeLevelFactory() {
        // TODO split the Level class into 2 classes so that the builder manages
        // the models
        final LevelFactory levelFactory = new LevelFactory();
        {
            final Map<String, ReadOnlyVector3[]> enemyPositionsMap = new HashMap<>();
            enemyPositionsMap.put("SOLDIER", new ReadOnlyVector3[] { new Vector3(118.5, 0.4, 219) });
            final Map<String, ReadOnlyVector3[]> medikitPositions = new HashMap<>();
            medikitPositions.put("SMALL_MEDIKIT", new ReadOnlyVector3[] { new Vector3(112.5, 0.1, 220.5) });
            final Map<String, ReadOnlyVector3[]> weaponPositionsMap = new HashMap<>();
            weaponPositionsMap.put("PISTOL_9MM", new ReadOnlyVector3[] { new Vector3(114.5, 0.1, 219.0) });
            weaponPositionsMap.put("MAG_60", new ReadOnlyVector3[] { new Vector3(115.5, 0.1, 219.0) });
            final Map<String, ReadOnlyVector3[]> ammoBoxPositionsMap = new HashMap<>();
            ammoBoxPositionsMap.put("SMALL_BOX_OF_9MM_BULLETS",
                    new ReadOnlyVector3[] { new Vector3(112.5, 0.1, 222.5) });
            final Map<String, Entry<String, ReadOnlyVector3[]>> teleporterPositionsMap = new HashMap<>();
            teleporterPositionsMap.put("", new AbstractMap.SimpleImmutableEntry<>("1",
                    new ReadOnlyVector3[] { new Vector3(116.5, 0, 213.5), new Vector3(120.5, 0, 214.5) }));
            levelFactory.addNewLevel("Tutorial", "0", "/abin/LID0.abin", null, enemyPositionsMap, medikitPositions,
                    weaponPositionsMap, ammoBoxPositionsMap, null, teleporterPositionsMap,
                    new KillAllEnemiesObjective());
        }
        {
            final Map<String, ReadOnlyVector3[]> enemyPositionsMap = new HashMap<>();
            enemyPositionsMap.put("SOLDIER",
                    new ReadOnlyVector3[] { new Vector3(118.5, 0.4, 219), new Vector3(117.5, 0.4, 219) });
            final Map<String, ReadOnlyVector3[]> medikitPositions = new HashMap<>();
            medikitPositions.put("SMALL_MEDIKIT", new ReadOnlyVector3[] { new Vector3(112.5, 0.1, 220.5) });
            final Map<String, ReadOnlyVector3[]> weaponPositionsMap = new HashMap<>();
            weaponPositionsMap.put("PISTOL_9MM", new ReadOnlyVector3[] { new Vector3(114.5, 0.1, 219.0) });
            weaponPositionsMap.put("MAG_60", new ReadOnlyVector3[] { new Vector3(115.5, 0.1, 219.0) });
            final Map<String, ReadOnlyVector3[]> ammoBoxPositionsMap = new HashMap<>();
            ammoBoxPositionsMap.put("SMALL_BOX_OF_9MM_BULLETS",
                    new ReadOnlyVector3[] { new Vector3(112.5, 0.1, 222.5) });
            final Map<String, Entry<String, ReadOnlyVector3[]>> teleporterPositionsMap = new HashMap<>();
            teleporterPositionsMap.put("",
                    new AbstractMap.SimpleImmutableEntry<>("2", new ReadOnlyVector3[] { new Vector3(75.5, 0, 129.5) }));
            levelFactory.addNewLevel("Museum", "1", "/abin/LID1.abin", "/abin/LID1.collision.abin", enemyPositionsMap,
                    medikitPositions, weaponPositionsMap, ammoBoxPositionsMap, null, teleporterPositionsMap,
                    new KillAllEnemiesObjective());
        }
        {
            final Map<String, Entry<String, ReadOnlyVector3[]>> teleporterPositionsMap = new HashMap<>();
            teleporterPositionsMap.put("",
                    new AbstractMap.SimpleImmutableEntry<>("3", new ReadOnlyVector3[] { new Vector3(0, 0, 10) }));
            levelFactory.addNewLevel("Outdoor", "2", "/abin/LID2.abin", null, null, null, null, null, "BLUE_SKY",
                    teleporterPositionsMap);
        }
        {
            levelFactory.addNewLevel("Bagnolet", "3", "/abin/LID3.abin", null, null, null, null, null, "BLUE_SKY",
                    null);
        }
        return (levelFactory);
    }

    private final SkyboxFactory initializeSkyboxFactory() {
        final SkyboxFactory skyboxFactory = new SkyboxFactory();
        skyboxFactory.addNewSkybox("blue sky", "BLUE_SKY", new String[] { "/images/1.jpg", "/images/2.jpg",
                "/images/3.jpg", "/images/4.jpg", "/images/5.jpg", "/images/6.jpg" });
        return (skyboxFactory);
    }

    private final AmmunitionFactory initializeAmmunitionFactory() {
        final AmmunitionFactory ammunitionFactory = new AmmunitionFactory();
        /** American assault rifle */
        ammunitionFactory.addNewAmmunition("5.56mm bullet", "BULLET_5_56MM");
        /** Russian assault rifle */
        ammunitionFactory.addNewAmmunition("7.62mm bullet", "BULLET_7_62MM");
        /** American pistols and sub-machine guns */
        ammunitionFactory.addNewAmmunition("9mm bullet", "BULLET_9MM");
        /** Russian pistols */
        ammunitionFactory.addNewAmmunition("10mm bullet", "BULLET_10MM");
        /** cartridge */
        ammunitionFactory.addNewAmmunition("cartridge", "CARTRIDGE");
        /** power */
        ammunitionFactory.addNewAmmunition("energy cell", "ENERGY_CELL");
        /** Russian middle range anti-tank rocket launchers */
        ammunitionFactory.addNewAmmunition("105mm anti tank rocket", "ANTI_TANK_ROCKET_105MM");
        return (ammunitionFactory);
    }

    private final AmmunitionBoxFactory initializeAmmunitionBoxFactory() {
        final AmmunitionBoxFactory ammunitionBoxFactory = new AmmunitionBoxFactory();
        ammunitionBoxFactory.addNewAmmunitionBox("small box of 9mm bullets", "SMALL_BOX_OF_9MM_BULLETS",
                "/sounds/pickup_weapon.ogg", ammunitionFactory.get("BULLET_9MM"), "/images/ammo.png", 30);
        return (ammunitionBoxFactory);
    }

    private final WeaponFactory initializeWeaponFactory() {
        final WeaponFactory weaponFactory = new WeaponFactory();
        weaponFactory.addNewWeapon("a pistol (9mm)", "PISTOL_9MM", "/abin/pistol2.abin", "/sounds/pickup_weapon.ogg",
                "/sounds/pistol9mm_shot.ogg", "/sounds/pistol9mm_reload.ogg", true, 8,
                ammunitionFactory.get("BULLET_9MM"), 1, 500, true);
        weaponFactory.addNewWeapon("a pistol (10mm)", "PISTOL_10MM", "/abin/pistol.abin", "/sounds/pickup_weapon.ogg",
                null, null, true, 10, ammunitionFactory.get("BULLET_10MM"), 1, 500, true);
        weaponFactory.addNewWeapon("a Mag 60", "MAG_60", "/abin/pistol3.abin", "/sounds/pickup_weapon.ogg",
                "/sounds/mag60_shot.ogg", "/sounds/mag60_reload.ogg", true, 30, ammunitionFactory.get("BULLET_9MM"), 1,
                100, true);
        weaponFactory.addNewWeapon("an uzi", "UZI", "/abin/uzi.abin", "/sounds/pickup_weapon.ogg", null, null, true, 20,
                ammunitionFactory.get("BULLET_9MM"), 1, 100, true);
        weaponFactory.addNewWeapon("a smach", "SMACH", "/abin/smach.abin", "/sounds/pickup_weapon.ogg", null, null,
                true, 35, ammunitionFactory.get("BULLET_5_56MM"), 1, 100, true);
        weaponFactory.addNewWeapon("a laser", "LASER", "/abin/laser.abin", "/sounds/pickup_weapon.ogg", null, null,
                true, 15, ammunitionFactory.get("ENERGY_CELL"), 1, 1000, false);
        weaponFactory.addNewWeapon("a shotgun", "SHOTGUN", "/abin/shotgun.abin", "/sounds/pickup_weapon.ogg", null,
                null, false, 3, ammunitionFactory.get("CARTRIDGE"), 1, 1500, false);
        weaponFactory.addNewWeapon("a rocket launcher", "ROCKET_LAUNCHER", "/abin/rocketlauncher.abin",
                "/sounds/pickup_weapon.ogg", null, null, false, 1, ammunitionFactory.get("ANTI_TANK_ROCKET_105MM"), 1,
                2000, false);
        return (weaponFactory);
    }

    protected void setLevelIdentifier(final String levelIdentifier) {
        final Level level = levelFactory.get(levelIdentifier);
        this.level = level;
    }

    public String getLevelLabel() {
        return (level.getLabel());
    }

    /**
     * loads the sound samples
     */
    private final void loadSounds() {
        final int teleporterCount = teleporterFactory.getSize();
        for (int teleporterIndex = 0; teleporterIndex < teleporterCount; teleporterIndex++) {
            final Teleporter teleporter = teleporterFactory.get(teleporterIndex);
            final String pickingUpSoundSamplePath = teleporter.getPickingUpSoundSamplePath();
            if (pickingUpSoundSamplePath != null) {
                final URL pickingUpSoundSampleUrl = GameState.class.getResource(pickingUpSoundSamplePath);
                if (pickingUpSoundSampleUrl != null) {
                    final String pickingUpSoundSampleIdentifier = getSoundManager().loadSound(pickingUpSoundSampleUrl);
                    if (pickingUpSoundSampleIdentifier != null)
                        teleporter.setPickingUpSoundSampleIdentifier(pickingUpSoundSampleIdentifier);
                }
            }
        }
        final int medikitCount = medikitFactory.getSize();
        for (int medikitIndex = 0; medikitIndex < medikitCount; medikitIndex++) {
            final Medikit medikit = medikitFactory.get(medikitIndex);
            final String pickingUpSoundSamplePath = medikit.getPickingUpSoundSamplePath();
            if (pickingUpSoundSamplePath != null) {
                final URL pickingUpSoundSampleUrl = GameState.class.getResource(pickingUpSoundSamplePath);
                if (pickingUpSoundSampleUrl != null) {
                    final String pickingUpSoundSampleIdentifier = getSoundManager().loadSound(pickingUpSoundSampleUrl);
                    if (pickingUpSoundSampleIdentifier != null)
                        medikit.setPickingUpSoundSampleIdentifier(pickingUpSoundSampleIdentifier);
                }
            }
        }
        final int ammoBoxCount = ammunitionBoxFactory.getSize();
        for (int ammoBoxIndex = 0; ammoBoxIndex < ammoBoxCount; ammoBoxIndex++) {
            final AmmunitionBox ammoBox = ammunitionBoxFactory.get(ammoBoxIndex);
            final String pickingUpSoundSamplePath = ammoBox.getPickingUpSoundSamplePath();
            if (pickingUpSoundSamplePath != null) {
                final URL pickingUpSoundSampleUrl = GameState.class.getResource(pickingUpSoundSamplePath);
                if (pickingUpSoundSampleUrl != null) {
                    final String pickingUpSoundSampleIdentifier = getSoundManager().loadSound(pickingUpSoundSampleUrl);
                    if (pickingUpSoundSampleIdentifier != null)
                        ammoBox.setPickingUpSoundSampleIdentifier(pickingUpSoundSampleIdentifier);
                }
            }
        }
        final int enemyCount = enemyFactory.getSize();
        for (int enemyIndex = 0; enemyIndex < enemyCount; enemyIndex++) {
            final Enemy enemy = enemyFactory.get(enemyIndex);
            final int painSoundSampleCount = enemy.getPainSoundSampleCount();
            for (int painSoundSampleIndex = 0; painSoundSampleIndex < painSoundSampleCount; painSoundSampleIndex++) {
                final String painSoundSamplePath = enemy.getPainSoundSamplePath(painSoundSampleIndex);
                if (painSoundSamplePath != null) {
                    final URL painSoundSampleUrl = GameState.class.getResource(painSoundSamplePath);
                    if (painSoundSampleUrl != null) {
                        final String painSoundSampleIdentifier = getSoundManager().loadSound(painSoundSampleUrl);
                        if (painSoundSampleIdentifier != null)
                            enemy.setPainSoundSampleIdentifier(painSoundSampleIndex, painSoundSampleIdentifier);
                    }
                }
            }
        }
        final int weaponCount = weaponFactory.getSize();
        for (int weaponIndex = 0; weaponIndex < weaponCount; weaponIndex++) {
            final Weapon weapon = weaponFactory.get(weaponIndex);
            final String pickingUpSoundSamplePath = weapon.getPickingUpSoundSamplePath();
            if (pickingUpSoundSamplePath != null) {
                final URL pickingUpSoundSampleUrl = GameState.class.getResource(pickingUpSoundSamplePath);
                if (pickingUpSoundSampleUrl != null) {
                    final String pickingUpSoundSampleSourcename = getSoundManager().loadSound(pickingUpSoundSampleUrl);
                    if (pickingUpSoundSampleSourcename != null)
                        weapon.setPickingUpSoundSampleIdentifier(pickingUpSoundSampleSourcename);
                }
            }
            final String blowOrShotSoundSamplePath = weapon.getBlowOrShotSoundSamplePath();
            if (blowOrShotSoundSamplePath != null) {
                final URL blowOrShotSoundSampleUrl = GameState.class.getResource(blowOrShotSoundSamplePath);
                if (blowOrShotSoundSampleUrl != null) {
                    final String blowOrShotSoundSampleIdentifier = getSoundManager()
                            .loadSound(blowOrShotSoundSampleUrl);
                    if (blowOrShotSoundSampleIdentifier != null)
                        weapon.setBlowOrShotSoundSampleIdentifier(blowOrShotSoundSampleIdentifier);
                }
            }
            final String reloadSoundSamplePath = weapon.getReloadSoundSamplePath();
            if (reloadSoundSamplePath != null) {
                final URL reloadSoundSampleUrl = GameState.class.getResource(reloadSoundSamplePath);
                if (reloadSoundSampleUrl != null) {
                    final String reloadSoundSampleIdentifier = getSoundManager().loadSound(reloadSoundSampleUrl);
                    if (reloadSoundSampleIdentifier != null)
                        weapon.setReloadSoundSampleIdentifier(reloadSoundSampleIdentifier);
                }
            }
        }
        for (int painSoundIndex = 0; painSoundIndex < painSoundSamplePaths.length; painSoundIndex++) {
            if (painSoundSamplePaths[painSoundIndex] != null && painSoundSampleIdentifiers[painSoundIndex] == null) {
                final URL painSoundSampleUrl = GameState.class.getResource(painSoundSamplePaths[painSoundIndex]);
                if (painSoundSampleUrl != null)
                    painSoundSampleIdentifiers[painSoundIndex] = getSoundManager().loadSound(painSoundSampleUrl);
            }
        }
        if (enemyShotgunShotSamplePath != null && enemyShotgunShotSampleIdentifier == null) {
            final URL enemyShotgunShotSampleUrl = GameState.class.getResource(enemyShotgunShotSamplePath);
            if (enemyShotgunShotSampleUrl != null)
                enemyShotgunShotSampleIdentifier = getSoundManager().loadSound(enemyShotgunShotSampleUrl);
        }
        if (gameoverSoundSamplePath != null && gameoverSoundSampleIdentifier == null) {
            final URL gameoverSoundSampleUrl = GameState.class.getResource(gameoverSoundSamplePath);
            if (gameoverSoundSampleUrl != null)
                gameoverSoundSampleIdentifier = getSoundManager().loadSound(gameoverSoundSampleUrl);
        }
        if (victory0SoundSamplePath != null && victory0SoundSampleIdentifier == null) {
            final URL victorySoundSampleUrl = GameState.class.getResource(victory0SoundSamplePath);
            if (victorySoundSampleUrl != null)
                victory0SoundSampleIdentifier = getSoundManager().loadSound(victorySoundSampleUrl);
        }
        if (victory1SoundSamplePath != null && victory1SoundSampleIdentifier == null) {
            final URL victorySoundSampleUrl = GameState.class.getResource(victory1SoundSamplePath);
            if (victorySoundSampleUrl != null)
                victory1SoundSampleIdentifier = getSoundManager().loadSound(victorySoundSampleUrl);
        }
    }

    /**
     * unloads the sound samples
     */
    private final void unloadSounds() {
        final int teleporterCount = teleporterFactory.getSize();
        for (int teleporterIndex = 0; teleporterIndex < teleporterCount; teleporterIndex++) {
            final Teleporter teleporter = teleporterFactory.get(teleporterIndex);
            final String pickingUpSoundSamplePath = teleporter.getPickingUpSoundSamplePath();
            if (pickingUpSoundSamplePath != null) {
                final URL pickingUpSoundSampleUrl = GameState.class.getResource(pickingUpSoundSamplePath);
                if (pickingUpSoundSampleUrl != null) {
                    getSoundManager().unloadSound(pickingUpSoundSampleUrl);
                    if (teleporter.getPickingUpSoundSampleIdentifier() != null)
                        teleporter.setPickingUpSoundSampleIdentifier(null);
                }
            }
        }
        final int medikitCount = medikitFactory.getSize();
        for (int medikitIndex = 0; medikitIndex < medikitCount; medikitIndex++) {
            final Medikit medikit = medikitFactory.get(medikitIndex);
            final String pickingUpSoundSamplePath = medikit.getPickingUpSoundSamplePath();
            if (pickingUpSoundSamplePath != null) {
                final URL pickingUpSoundSampleUrl = GameState.class.getResource(pickingUpSoundSamplePath);
                if (pickingUpSoundSampleUrl != null) {
                    getSoundManager().unloadSound(pickingUpSoundSampleUrl);
                    if (medikit.getPickingUpSoundSampleIdentifier() != null)
                        medikit.setPickingUpSoundSampleIdentifier(null);
                }
            }
        }
        final int ammoBoxCount = ammunitionBoxFactory.getSize();
        for (int ammoBoxIndex = 0; ammoBoxIndex < ammoBoxCount; ammoBoxIndex++) {
            final AmmunitionBox ammoBox = ammunitionBoxFactory.get(ammoBoxIndex);
            final String pickingUpSoundSamplePath = ammoBox.getPickingUpSoundSamplePath();
            if (pickingUpSoundSamplePath != null) {
                final URL pickingUpSoundSampleUrl = GameState.class.getResource(pickingUpSoundSamplePath);
                if (pickingUpSoundSampleUrl != null) {
                    getSoundManager().unloadSound(pickingUpSoundSampleUrl);
                    if (ammoBox.getPickingUpSoundSampleIdentifier() != null)
                        ammoBox.setPickingUpSoundSampleIdentifier(null);
                }
            }
        }
        final int enemyCount = enemyFactory.getSize();
        for (int enemyIndex = 0; enemyIndex < enemyCount; enemyIndex++) {
            final Enemy enemy = enemyFactory.get(enemyIndex);
            final int painSoundSampleCount = enemy.getPainSoundSampleCount();
            for (int painSoundSampleIndex = 0; painSoundSampleIndex < painSoundSampleCount; painSoundSampleIndex++) {
                final String painSoundSamplePath = enemy.getPainSoundSamplePath(painSoundSampleIndex);
                if (painSoundSamplePath != null) {
                    final URL painSoundSampleUrl = GameState.class.getResource(painSoundSamplePath);
                    if (painSoundSampleUrl != null) {
                        getSoundManager().unloadSound(painSoundSampleUrl);
                        if (enemy.getPainSoundSampleIdentifier(painSoundSampleIndex) != null)
                            enemy.setPainSoundSampleIdentifier(painSoundSampleIndex, null);
                    }
                }
            }
        }
        final int weaponCount = weaponFactory.getSize();
        for (int weaponIndex = 0; weaponIndex < weaponCount; weaponIndex++) {
            final Weapon weapon = weaponFactory.get(weaponIndex);
            final String pickingUpSoundSamplePath = weapon.getPickingUpSoundSamplePath();
            if (pickingUpSoundSamplePath != null) {
                final URL pickingUpSoundSampleUrl = GameState.class.getResource(pickingUpSoundSamplePath);
                if (pickingUpSoundSampleUrl != null) {
                    getSoundManager().unloadSound(pickingUpSoundSampleUrl);
                    if (weapon.getPickingUpSoundSampleIdentifier() != null)
                        weapon.setPickingUpSoundSampleIdentifier(null);
                }
            }
            final String blowOrShotSoundSamplePath = weapon.getBlowOrShotSoundSamplePath();
            if (blowOrShotSoundSamplePath != null) {
                final URL blowOrShotSoundSampleUrl = GameState.class.getResource(blowOrShotSoundSamplePath);
                if (blowOrShotSoundSampleUrl != null) {
                    getSoundManager().unloadSound(blowOrShotSoundSampleUrl);
                    if (weapon.getBlowOrShotSoundSampleIdentifier() != null)
                        weapon.setBlowOrShotSoundSampleIdentifier(null);
                }
            }
            final String reloadSoundSamplePath = weapon.getReloadSoundSamplePath();
            if (reloadSoundSamplePath != null) {
                final URL reloadSoundSampleUrl = GameState.class.getResource(reloadSoundSamplePath);
                if (reloadSoundSampleUrl != null) {
                    getSoundManager().unloadSound(reloadSoundSampleUrl);
                    if (weapon.getReloadSoundSampleIdentifier() != null)
                        weapon.setReloadSoundSampleIdentifier(null);
                }
            }
        }
        for (int painSoundIndex = 0; painSoundIndex < painSoundSamplePaths.length; painSoundIndex++) {
            if (painSoundSamplePaths[painSoundIndex] != null && painSoundSampleIdentifiers[painSoundIndex] != null) {
                final URL painSoundSampleUrl = GameState.class.getResource(painSoundSamplePaths[painSoundIndex]);
                if (painSoundSampleUrl != null)
                    getSoundManager().unloadSound(painSoundSampleUrl);
                painSoundSampleIdentifiers[painSoundIndex] = null;
            }
        }
        if (enemyShotgunShotSamplePath != null && enemyShotgunShotSampleIdentifier != null) {
            final URL enemyShotgunShotSampleUrl = GameState.class.getResource(enemyShotgunShotSamplePath);
            if (enemyShotgunShotSampleUrl != null)
                getSoundManager().unloadSound(enemyShotgunShotSampleUrl);
            enemyShotgunShotSampleIdentifier = null;
        }
        if (gameoverSoundSamplePath != null && gameoverSoundSampleIdentifier != null) {
            final URL gameoverSoundSampleUrl = GameState.class.getResource(gameoverSoundSamplePath);
            if (gameoverSoundSampleUrl != null)
                getSoundManager().unloadSound(gameoverSoundSampleUrl);
            gameoverSoundSampleIdentifier = null;
        }
        if (victory0SoundSamplePath != null && victory0SoundSampleIdentifier != null) {
            final URL victorySoundSampleUrl = GameState.class.getResource(victory0SoundSamplePath);
            if (victorySoundSampleUrl != null)
                getSoundManager().unloadSound(victorySoundSampleUrl);
            victory0SoundSampleIdentifier = null;
        }
        if (victory1SoundSamplePath != null && victory1SoundSampleIdentifier != null) {
            final URL victorySoundSampleUrl = GameState.class.getResource(victory1SoundSamplePath);
            if (victorySoundSampleUrl != null)
                getSoundManager().unloadSound(victorySoundSampleUrl);
            victory1SoundSampleIdentifier = null;
        }
    }

    private final Box loadPoster(final String filename, final double xExtent, final double yExtent,
            final double zExtent, final float xRatio, final float yRatio) {
        // creates the thin box representing the poster
        final Box poster = new Box(filename + " Poster", Vector3.ZERO, xExtent, yExtent, zExtent);
        // retrieves the buffer containing the texture coordinates
        final FloatBuffer posterTextureCoordsBuffer = poster.getMeshData().getTextureBuffer(0);
        posterTextureCoordsBuffer.rewind();
        // uses a ratio as the size of the image's part isn't a power of two
        final float left = (1.0f - xRatio) / 2.0f;
        final float right = 1.0f - left;
        final float bottom = (1.0f - yRatio) / 2.0f;
        final float top = 1.0f - bottom;
        for (int i = 0; i < 6; i++) {
            // displays the poster only on the front face
            if (i == 2) {
                posterTextureCoordsBuffer.put(right).put(bottom);
                posterTextureCoordsBuffer.put(left).put(bottom);
                posterTextureCoordsBuffer.put(left).put(top);
                posterTextureCoordsBuffer.put(right).put(top);
            } else {
                posterTextureCoordsBuffer.put(0).put(0);
                posterTextureCoordsBuffer.put(0).put(0);
                posterTextureCoordsBuffer.put(0).put(0);
                posterTextureCoordsBuffer.put(0).put(0);
            }
        }
        posterTextureCoordsBuffer.rewind();
        poster.setModelBound(new BoundingBox());
        // creates the texture of the poster's image
        final TextureState ts = new TextureState();
        ts.setEnabled(true);
        ts.setTexture(TextureManager.load(filename, Texture.MinificationFilter.Trilinear, true));
        poster.setRenderState(ts);
        return poster;
    }
    
    private final void loadLevelModel() {
        final Node levelMainModel = level.loadMainModel();
        if ("1".equals(level.getIdentifier())) {
            // adds the posters into the level
            final Box butWhatDoesThePoliceItBurstsTheEyesPoster = loadPoster("Mais_que_fait_la_police_ca_crève_les_yeux.png", 0.5, 0.5, 0.01, (float) 0.6767578125, (float) 0.6767578125);
            butWhatDoesThePoliceItBurstsTheEyesPoster.setTranslation(new Vector3(115.5, 0.5, 214.01));
            levelMainModel.attachChild(butWhatDoesThePoliceItBurstsTheEyesPoster);
            final Box communismWillWinOnAllFrontsPoster = loadPoster("Kommunismus_wird_an_allen_Fronten_siegen.png", 2.5, 0.1, 0.01, 1, (float) 0.06);
            communismWillWinOnAllFrontsPoster.setTranslation(new Vector3(75, 0.9, 129.5));
            communismWillWinOnAllFrontsPoster.setRotation(new Quaternion().fromAngleAxis(Math.PI / 2, new Vector3(0, 1, 0)));
            levelMainModel.attachChild(communismWillWinOnAllFrontsPoster);
        }
        getRoot().attachChild(levelMainModel);
    }

    private final void loadOutdoor() {
        // TODO load separately the outdoor if any
    }

    private final void performInitialBasicSetup() {
        if ("0".equals(level.getIdentifier()) || "1".equals(level.getIdentifier())) {
            // the two first levels use a collision map
            level.readCollisionMap();
        }
        level.readCollisionVolumes();
        // FIXME it should not be hard-coded
        // TODO get the location from the argument of the transition
        switch (level.getIdentifier()) {
        case "0":
        case "1": {
            currentCamLeft.set(-1, 0, 0);
            currentCamUp.set(0, 1, 0);
            currentCamDirection.set(0, 0, -1);
            currentCamLocation.set(115, 0.5, 223);
            break;
        }
        case "2": {
            currentCamLeft.set(1, 0, 0);
            currentCamUp.set(0, 1, 0);
            currentCamDirection.set(0, 0, 1);
            currentCamLocation.set(0, 0, 0);
            break;
        }
        case "3": {
            currentCamLeft.set(1, 0, 0);
            currentCamUp.set(0, 1, 0);
            currentCamDirection.set(0, 0, 1);
            currentCamLocation.set(0, 0, 5);
            break;
        }
        }
        previousPosition.set(currentCamLocation);
        currentFrustumNear = 0.1;
        currentFrustumFar = 200;
        // attaches the player itself
        getRoot().attachChild(playerNode);
        // attaches the ammunition display node
        getRoot().attachChild(ammoTextLabel);
        // attaches the FPS display node
        getRoot().attachChild(fpsTextLabel);
        // attaches the health display node
        getRoot().attachChild(healthTextLabel);
        // attaches the HUD node
        getRoot().attachChild(headUpDisplayLabel);
        // attaches the objectives node
        getRoot().attachChild(objectivesDisplayLabel);
        // resets the latest player's death
        latestPlayerDeath = null;
        // resets player's stats
        gameStats = new GameStatistics();
        // sets the statistics of each action
        toPauseMenuTriggerAction.arguments.setGameStatistics(gameStats);
        toPauseMenuTriggerActionForExitConfirm.arguments.setGameStatistics(gameStats);
        toGameOverTriggerAction.arguments.setGameStatistics(gameStats);
        toPauseMenuTriggerAction.arguments.setPreviousLevelIdentifier(level.getIdentifier());
        toPauseMenuTriggerActionForExitConfirm.arguments.setPreviousLevelIdentifier(level.getIdentifier());
        // the player cannot go to the next level when leaving or aborting
        toPauseMenuTriggerAction.arguments.setNextLevelIdentifier(null);
        toPauseMenuTriggerActionForExitConfirm.arguments.setNextLevelIdentifier(null);
        // resurrects the player
        playerData.respawn();
        // TODO resets the parameters to the latest saved values
    }

    private final void performTerminalBasicCleanup() {
        if (level.getMainModel() != null)
            level.getMainModel().detachAllChildren();
        if (level.getSkyboxModel() != null)
            level.getSkyboxModel().detachAllChildren();
        // clears the list of objects that can be picked up
        collectibleObjectsList.clear();
        // clears the list of teleporters
        teleportersList.clear();
        // clears the data model used for the enemies
        enemiesDataMap.clear();
        enemiesLatestDetection.clear();
        // removes all previously attached children
        getRoot().detachAllChildren();
        previousObjectivesStatusesMap = null;
        toPauseMenuTriggerAction.arguments.setObjectives(null);
        toPauseMenuTriggerActionForExitConfirm.arguments.setObjectives(null);
        toGameOverTriggerAction.arguments.setObjectives(null);
        level = null;
    }

    private static final class KillAllEnemiesObjective extends Objective {

        private KillAllEnemiesObjective() {
            super("Kill all enemies");
        }

        @Override
        public ObjectiveStatus getStatus(final GameStatistics gameStats) {
            if (gameStats.getEnemiesCount() == gameStats.getKilledEnemiesCount())
                return (ObjectiveStatus.COMPLETED);
            else
                return (ObjectiveStatus.UNCOMPLETED);
        }
    }

    private final void performTerminalBasicSetup() {
        // adds a bounding box to each collectible object
        for (Node collectible : collectibleObjectsList)
            NodeHelper.setModelBound(collectible, BoundingBox.class);
        for (Node currentTeleporter : teleportersList)
            NodeHelper.setModelBound(currentTeleporter, BoundingBox.class);
        // resets the timer at the end of all long operations performed while
        // loading
        timer.reset();
        if (level.getSkyboxModel() != null)
            level.getSkyboxModel().setTranslation(currentCamLocation);
        gameStats.setEnemiesCount(enemiesDataMap.size());
        previousObjectivesStatusesMap = new HashMap<>();
        toPauseMenuTriggerAction.arguments.setObjectives(level.getObjectives());
        toPauseMenuTriggerActionForExitConfirm.arguments.setObjectives(level.getObjectives());
        toGameOverTriggerAction.arguments.setObjectives(level.getObjectives());
        // shows the initial objective(s) at the beginning
        if (!level.getObjectives().isEmpty()) {
            final StringBuilder builder = new StringBuilder();
            if (level.getObjectives().size() == 1)
                builder.append("Objective:");
            else
                builder.append("Objectives:");
            for (Objective objective : level.getObjectives()) {
                previousObjectivesStatusesMap.put(objective, objective.getStatus(gameStats));
                builder.append("\n");
                builder.append(objective.getDescription());
            }
            final String text = builder.toString();
            objectivesDisplayLabel.setText(text);
        }
    }

    private final void performInitialBasicCleanup() {
        // detaches the player itself
        getRoot().detachChild(playerNode);
        // detaches the ammunition display node
        getRoot().detachChild(ammoTextLabel);
        // detaches the FPS display node
        getRoot().detachChild(fpsTextLabel);
        // detaches the health display node
        getRoot().detachChild(healthTextLabel);
        // detaches the HUD node
        getRoot().detachChild(headUpDisplayLabel);
        // detaches the objectives node
        getRoot().detachChild(objectivesDisplayLabel);
        // transfers the game statistics into the player's statistics
        profileData.updateGamesStatistics(gameStats);
        // unsets player's stats
        gameStats = null;
        // unsets the statistics and the objectives of each action
        toPauseMenuTriggerAction.arguments.setGameStatistics(null);
        toPauseMenuTriggerActionForExitConfirm.arguments.setGameStatistics(null);
        toGameOverTriggerAction.arguments.setGameStatistics(null);
        // resets the timer at the beginning of all long operations performed
        // while unloading
        timer.reset();
        // detaches some nodes from the root to prevent Java from using them
        // while releasing their resources
        if (level.getMainModel() != null)
            getRoot().detachChild(level.getMainModel());
        if (level.getSkyboxModel() != null)
            getRoot().detachChild(level.getSkyboxModel());
    }

    private static final class VBODeleterVisitor implements Visitor {

        private final Renderer renderer;

        private VBODeleterVisitor(final Renderer renderer) {
            super();
            this.renderer = renderer;
        }

        @Override
        public void visit(final Spatial spatial) {
            if (spatial instanceof Mesh)
                deleteVBOs((Mesh) spatial);
        }

        private final void deleteVBOs(final Mesh disposableMesh) {
            final HashSet<MeshData> meshDataSet = new HashSet<>();
            // checks the data of the mesh
            if (disposableMesh.getMeshData() != null) {
                meshDataSet.add(disposableMesh.getMeshData());
                disposableMesh.setMeshData(null);
            }
            // checks the data of the key frames
            for (SpatialController<?> controller : disposableMesh.getControllers())
                if (controller != null && controller instanceof KeyframeController) {
                    final KeyframeController<?> keyframeController = (KeyframeController<?>) controller;
                    if (keyframeController._keyframes != null) {
                        for (PointInTime pit : keyframeController._keyframes)
                            if (pit._newShape != null) {
                                final MeshData meshData = pit._newShape.getMeshData();
                                if (meshData != null) {
                                    meshDataSet.add(meshData);
                                    pit._newShape.setMeshData(null);
                                }
                            }
                    }
                }
            for (MeshData meshData : meshDataSet) {// deletes the OpenGL
                                                   // identifier of the VBOs and
                                                   // releases the native memory
                                                   // of their direct NIO
                                                   // buffers
                final FloatBufferData vertexBufferData = meshData.getVertexCoords();
                if (vertexBufferData != null) {
                    renderer.deleteVBOs(vertexBufferData);
                    meshData.setVertexCoords(null);
                }
                final FloatBufferData colorBufferData = meshData.getColorCoords();
                if (colorBufferData != null) {
                    renderer.deleteVBOs(colorBufferData);
                    meshData.setColorCoords(null);
                }
                final IndexBufferData<?> indexBufferData = meshData.getIndices();
                if (indexBufferData != null) {
                    renderer.deleteVBOs(indexBufferData);
                    meshData.setIndices(null);
                }
                final FloatBufferData fogBufferData = meshData.getFogCoords();
                if (fogBufferData != null) {
                    renderer.deleteVBOs(fogBufferData);
                    meshData.setFogCoords(null);
                }
                final FloatBufferData intervealedBufferData = meshData.getInterleavedData();
                if (intervealedBufferData != null) {
                    renderer.deleteVBOs(intervealedBufferData);
                    meshData.setInterleavedData(null);
                }
                final FloatBufferData normalBufferData = meshData.getNormalCoords();
                if (normalBufferData != null) {
                    renderer.deleteVBOs(normalBufferData);
                    meshData.setNormalCoords(null);
                }
                final FloatBufferData tangentBufferData = meshData.getTangentCoords();
                if (tangentBufferData != null) {
                    renderer.deleteVBOs(tangentBufferData);
                    meshData.setTangentCoords(null);
                }
                final List<FloatBufferData> textureCoordsList = meshData.getTextureCoords();
                if (textureCoordsList != null && !textureCoordsList.isEmpty()) {
                    for (FloatBufferData textureCoords : textureCoordsList)
                        if (textureCoords != null)
                            renderer.deleteVBOs(textureCoords);
                    textureCoordsList.clear();
                }
            }
        }
    }

    /**
     * unloads direct NIO buffers
     */
    private final void performDirectNioBuffersCleanup() {
        // stores the spatials whose direct NIO buffers need to be disposed
        final HashSet<Spatial> disposableSpatials = new HashSet<>();
        // gets the renderer
        final Renderer renderer = canvas.getCanvasRenderer().getRenderer();
        // TODO destroy the morph meshes and the template meshes of enemies
        // TODO use templates to create weapons and do the same than above with
        // them (get them from the list of collectible objects and from the
        // camera node)
        disposableSpatials.addAll(level.removeDisposableSpatials());
        // performs the destruction with a single callable
        GameTaskQueueManager.getManager(canvas.getCanvasRenderer().getRenderContext()).getQueue(GameTaskQueue.RENDER)
                .enqueue(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        // builds the visitor
                        final VBODeleterVisitor deleter = new VBODeleterVisitor(renderer);
                        // runs it on all disposable spatials
                        for (Spatial spatial : disposableSpatials)
                            spatial.acceptVisitor(deleter, false);
                        // clears the list of disposable spatials as it is now
                        // useless
                        disposableSpatials.clear();
                        return null;
                    }
                });
    }

    private static final class TextureDeleterVisitor implements Visitor {

        private final Renderer renderer;

        private TextureDeleterVisitor(final Renderer renderer) {
            super();
            this.renderer = renderer;
        }

        @Override
        public void visit(final Spatial spatial) {
            deleteTextures(spatial);
        }

        private void deleteTextures(final Spatial disposableSpatial) {
            final TextureState textureState = (TextureState) disposableSpatial.getLocalRenderState(StateType.Texture);
            if (textureState != null) {
                // loops on all texture units
                for (int textureUnit = 0; textureUnit < textureState.getMaxTextureIndexUsed(); textureUnit++) {
                    final Texture texture = textureState.getTexture(textureUnit);
                    if (texture != null) {
                        // deletes the OpenGL identifier of the texture and
                        // releases the native memory of its direct NIO buffer
                        renderer.deleteTexture(texture);
                    }
                }
                // removes the textures from the texture state
                textureState.clearTextures();
                // removes the texture state from this spatial
                disposableSpatial.clearRenderState(StateType.Texture);
            }
        }
    }

    /**
     * unloads the textures
     */
    private final void performTexturesDataCleanup() {
        // stores the spatials whose textures need to be disposed
        final HashSet<Spatial> disposableSpatials = new HashSet<>();
        // gets the renderer
        final Renderer renderer = canvas.getCanvasRenderer().getRenderer();
        if (level.getMainModel() != null)
            disposableSpatials.add(level.getMainModel());
        if (level.getSkyboxModel() != null)
            disposableSpatials.add(level.getSkyboxModel());
        // performs the destruction with a single callable
        GameTaskQueueManager.getManager(canvas.getCanvasRenderer().getRenderContext()).getQueue(GameTaskQueue.RENDER)
                .enqueue(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        // builds the visitor
                        final TextureDeleterVisitor deleter = new TextureDeleterVisitor(renderer);
                        // runs it on all disposable spatials
                        for (Spatial spatial : disposableSpatials)
                            spatial.acceptVisitor(deleter, false);
                        // clears the list of disposable spatials as it is now
                        // useless
                        disposableSpatials.clear();
                        return null;
                    }
                });
    }

    private final void loadSkybox() {
        final com.ardor3d.scenegraph.extension.Skybox skyboxModel = level.loadSkyboxModel(skyboxFactory);
        if (skyboxModel != null)
            getRoot().attachChild(skyboxModel);
    }

    private final void loadTeleporters() {
        final List<Node> teleporterNodes = level.loadTeleporterModels(teleporterFactory);
        if (teleporterNodes != null && !teleporterNodes.isEmpty()) {
            teleportersList.addAll(teleporterNodes);
            for (final Node teleporterNode : teleporterNodes)
                getRoot().attachChild(teleporterNode);
        }
    }

    private final void loadMedikits() {
        final List<Node> medikitNodes = level.loadMedikitModels(medikitFactory);
        if (medikitNodes != null && !medikitNodes.isEmpty()) {
            collectibleObjectsList.addAll(medikitNodes);
            for (final Node medikitNode : medikitNodes)
                getRoot().attachChild(medikitNode);
        }
    }

    private final void loadWeapons() {
        final List<Node> weaponNodes = level.loadWeaponModels(weaponFactory);
        if (weaponNodes != null && !weaponNodes.isEmpty()) {
            collectibleObjectsList.addAll(weaponNodes);
            for (final Node weaponNode : weaponNodes)
                getRoot().attachChild(weaponNode);
        }
    }

    private final void loadAmmunitions() {
        final List<Node> ammoBoxNodes = level.loadAmmoBoxModels(ammunitionBoxFactory);
        if (ammoBoxNodes != null && !ammoBoxNodes.isEmpty()) {
            collectibleObjectsList.addAll(ammoBoxNodes);
            for (final Node ammoBoxNode : ammoBoxNodes)
                getRoot().attachChild(ammoBoxNode);
        }
    }

    private final void loadEnemies() {
        final List<Mesh> enemyMeshes = level.loadEnemyModels(enemyFactory);
        if (enemyMeshes != null && !enemyMeshes.isEmpty()) {// TODO separate the
                                                            // body and the
                                                            // weapon(s)
            boolean isEnemyWeaponMesh = false;
            for (final Mesh enemyMesh : enemyMeshes) {
                getRoot().attachChild(enemyMesh);
                if (!isEnemyWeaponMesh) {
                    final EnemyData enemyData = new EnemyData();
                    enemiesDataMap.put(enemyMesh, enemyData);
                }
                isEnemyWeaponMesh = !isEnemyWeaponMesh;
            }
        }
    }

    private final void preloadTextures() {
        final CanvasRenderer canvasRenderer = canvas.getCanvasRenderer();
        final RenderContext renderContext = canvasRenderer.getRenderContext();
        final Renderer renderer = canvasRenderer.getRenderer();
        GameTaskQueueManager.getManager(renderContext).getQueue(GameTaskQueue.RENDER).enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TextureManager.preloadCache(renderer);
                return null;
            }
        });
    }

    public final void cleanup() {
        taskManager.enqueueTask(new Runnable() {
            @Override
            public final void run() {
                unloadSounds();
            }
        });
        taskManager.enqueueTask(new Runnable() {
            @Override
            public final void run() {
                performInitialBasicCleanup();
            }
        });
        taskManager.enqueueTask(new Runnable() {
            @Override
            public final void run() {
                performDirectNioBuffersCleanup();
            }
        });
        taskManager.enqueueTask(new Runnable() {
            @Override
            public final void run() {
                performTexturesDataCleanup();
            }
        });
        taskManager.enqueueTask(new Runnable() {
            @Override
            public final void run() {
                performTerminalBasicCleanup();
            }
        });
    }

    @Override
    public final void init() {
        taskManager.enqueueTask(new Runnable() {
            @Override
            public final void run() {
                loadSounds();
            }
        });
        taskManager.enqueueTask(new Runnable() {
            @Override
            public final void run() {
                performInitialBasicSetup();
            }
        });
        // Load level model
        taskManager.enqueueTask(new Runnable() {
            @Override
            public final void run() {
                loadLevelModel();
            }
        });
        taskManager.enqueueTask(new Runnable() {
            @Override
            public final void run() {
                loadOutdoor();
            }
        });
        taskManager.enqueueTask(new Runnable() {
            @Override
            public final void run() {
                loadSkybox();
            }
        });
        taskManager.enqueueTask(new Runnable() {
            @Override
            public final void run() {
                loadTeleporters();
            }
        });
        taskManager.enqueueTask(new Runnable() {
            @Override
            public final void run() {
                loadMedikits();
            }
        });
        taskManager.enqueueTask(new Runnable() {
            @Override
            public final void run() {
                loadWeapons();
            }
        });
        taskManager.enqueueTask(new Runnable() {
            @Override
            public final void run() {
                loadAmmunitions();
            }
        });
        taskManager.enqueueTask(new Runnable() {
            @Override
            public final void run() {
                loadEnemies();
            }
        });
        taskManager.enqueueTask(new Runnable() {
            @Override
            public final void run() {
                preloadTextures();
            }
        });
        taskManager.enqueueTask(new Runnable() {
            @Override
            public final void run() {
                performTerminalBasicSetup();
            }
        });
    }

    @Override
    public void setEnabled(final boolean enabled) {
        final boolean wasEnabled = isEnabled();
        super.setEnabled(enabled);
        if (wasEnabled != isEnabled()) {
            if (isEnabled()) {// FIXME this is a source of bugs, rather do that
                              // during the initialization
                mouseManager.setGrabbed(GrabbedState.NOT_GRABBED);
                final Camera cam = canvas.getCanvasRenderer().getCamera();
                mouseManager.setPosition(cam.getWidth() / 2, cam.getHeight() / 2);
                mouseManager.setGrabbed(GrabbedState.GRABBED);
                if (customMouseAndKeyboardSettings.isMousePointerNeverHidden()) {
                    GameTaskQueueManager.getManager(canvas.getCanvasRenderer().getRenderContext())
                            .getQueue(GameTaskQueue.RENDER).enqueue(new Callable<Void>() {
                                @Override
                                public Void call() throws Exception {
                                    ((JoglNewtWindow) canvas).getNewtWindow().setPointerVisible(true);
                                    return null;
                                }
                            });
                }
                // the resolution of the screen might have been modified in the
                // graphical user interface
                // updates all elements whose positions should be bound to the
                // resolution of the screen
                updateCrosshairNode();
                /*
                 * checks whether the custom action map has been modified by
                 * comparing it to the default one or if the input triggers have
                 * never been initialized
                 */
                if (fpsc == null || !customActionMap.equals(defaultActionMap)
                        || !customMouseAndKeyboardSettings.equals(defaultMouseAndKeyboardSettings)) {// (re)initializes
                                                                                                     // input
                                                                                                     // triggers
                    initializeInput(toPauseMenuTriggerAction, toPauseMenuTriggerActionForExitConfirm,
                            toggleScreenModeAction, physicalLayer);
                }
            }
        }
    }
}
