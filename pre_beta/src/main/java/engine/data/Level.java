/**
 * Copyright (c) 2006-2019 Julien Gouesse
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
package engine.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.extension.model.util.KeyframeController;
import com.ardor3d.extension.model.util.KeyframeController.PointInTime;
import com.ardor3d.image.Image;
import com.ardor3d.image.Texture;
import com.ardor3d.image.util.ImageLoaderUtil;
import com.ardor3d.image.util.ImageUtils;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Quaternion;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.ComplexSpatialController.RepeatType;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.export.binary.BinaryImporter;
import com.ardor3d.util.resource.URLResourceSource;

import engine.data.common.AmmunitionBox;
import engine.data.common.AmmunitionBoxFactory;
import engine.data.common.Medikit;
import engine.data.common.MedikitFactory;
import engine.data.common.Teleporter;
import engine.data.common.TeleporterFactory;
import engine.data.common.userdata.AmmunitionBoxUserData;
import engine.data.common.userdata.MedikitUserData;
import engine.data.common.userdata.TeleporterUserData;
import engine.data.common.userdata.WeaponUserData;
import engine.misc.MD2FrameSet;
import engine.misc.NodeHelper;
import engine.weaponry.Weapon;
import engine.weaponry.WeaponFactory;

/**
 * Data model of the level
 * 
 * @author Julien Gouesse
 *
 */
public class Level implements Comparable<Level> {
    /** human readable name */
    private final String label;
    /** unique identifier, must be greater than or equal to zero */
    private final String identifier;
    /** name of the resource, i.e the binary file containing the 3D spatial */
    private final String resourceName;
    /**
     * name of the resource containing the bounding volumes used in the
     * collision system
     */
    private final String boundingBoxListResourceName;
    /** positions of the enemies */
    private final Map<String, ReadOnlyVector3[]> enemyPositionsMap;
    /** positions of the medikits */
    private final Map<String, ReadOnlyVector3[]> medikitPositionsMap;
    /** map of the weapon positions sorted by type */
    private final Map<String, ReadOnlyVector3[]> weaponPositionsMap;
    /** map of the ammunition box positions sorted by type */
    private final Map<String, ReadOnlyVector3[]> ammoBoxPositionsMap;
    /** sky box identifier, can be null if there is no sky box */
    private final String skyboxIdentifier;
    /** map of the teleporter positions sorted by type */
    private final Map<String, Entry<String, ReadOnlyVector3[]>> teleporterPositionsMap;
    /** objectives of the mission */
    private final List<Objective> objectives;
    // TODO move the fields below into another class
    /** binary importer used to import all scenegraph objects of this level */
    private final BinaryImporter binaryImporter;
    /** sky box */
    private com.ardor3d.scenegraph.extension.Skybox skyboxModel;
    /** root node whose hierarchy contains the geometry of the main model */
    private Node mainModel;
    /**
     * @deprecated this collision map is a temporary solution, the real
     *             collision system will have to use the 3D mesh instead of a
     *             flat 2D array
     */
    @Deprecated
    private boolean[][] collisionMap;

    private List<BoundingBox> boundingBoxList;

    public Level(final String label, final String identifier, final String resourceName,
            final String boundingBoxListResourceName, final Map<String, ReadOnlyVector3[]> enemyPositionsMap,
            final Map<String, ReadOnlyVector3[]> medikitPositionsMap,
            final Map<String, ReadOnlyVector3[]> weaponPositionsMap,
            final Map<String, ReadOnlyVector3[]> ammoBoxPositionsMap, final String skyboxIdentifier,
            final Map<String, Entry<String, ReadOnlyVector3[]>> teleporterPositionsMap, final Objective... objectives) {
        super();
        this.label = Objects.requireNonNull(label, "the label must not be null");
        this.identifier = Objects.requireNonNull(identifier, "the identifier must not be null");
        this.resourceName = resourceName;
        this.boundingBoxListResourceName = boundingBoxListResourceName;
        this.enemyPositionsMap = enemyPositionsMap;
        this.medikitPositionsMap = medikitPositionsMap;
        this.weaponPositionsMap = weaponPositionsMap;
        this.ammoBoxPositionsMap = ammoBoxPositionsMap;
        this.skyboxIdentifier = skyboxIdentifier;
        this.teleporterPositionsMap = teleporterPositionsMap;
        this.boundingBoxList = null;
        final List<Objective> localObjectives = new ArrayList<>();
        if (objectives != null && objectives.length > 0)
            localObjectives.addAll(Arrays.asList(objectives));
        this.objectives = Collections.unmodifiableList(localObjectives);
        // TODO move it into another class
        this.binaryImporter = new BinaryImporter();
    }

    @Deprecated
    public final void readCollisionMap() {
        final URL mapUrl = Level.class.getResource("/images/containermap.png");
        final URLResourceSource mapSource = new URLResourceSource(mapUrl);
        final Image map = ImageLoaderUtil.loadImage(mapSource, false);
        collisionMap = new boolean[map.getWidth()][map.getHeight()];
        for (int y = 0; y < map.getHeight(); y++)
            for (int x = 0; x < map.getWidth(); x++) {
                final int argb = ImageUtils.getARGB(map, x, y);
                collisionMap[x][y] = (argb == ColorRGBA.BLUE.asIntARGB());
            }
    }

    public final void readCollisionVolumes() {
        if (boundingBoxList == null && boundingBoxListResourceName != null) {
            final URL url = getClass().getResource(boundingBoxListResourceName);
            // if the file can be found
            if (url != null) {// creates the list to store the bounding boxes
                final ArrayList<BoundingBox> localBoundingBoxList = new ArrayList<>();
                // uses a try with resource to ensure that there is no leak
                try (final InputStream inputStream = url.openStream()) {
                    // loops on the savable instances in the stream
                    while (inputStream.available() > 0) {
                        // loads a bounding box
                        final BoundingBox boundingBox = (BoundingBox) binaryImporter.load(inputStream);
                        // adds it into the list
                        localBoundingBoxList.add(boundingBox);
                    }
                    boundingBoxList = Collections.unmodifiableList(localBoundingBoxList);
                } catch (IOException ioe) {
                    throw new RuntimeException("level collision volumes loading failed", ioe);
                }
            }
        }
    }

    public final List<BoundingBox> getCollisionVolumes() {
        return (boundingBoxList);
    }

    @SuppressWarnings("unchecked")
    public List<Mesh> loadEnemyModels(final EnemyFactory enemyFactory) {
        List<Mesh> enemyMeshes = null;
        if (enemyPositionsMap != null && !enemyPositionsMap.isEmpty()) {
            final int enemyCount = enemyFactory.getSize();
            for (int enemyIndex = 0; enemyIndex < enemyCount; enemyIndex++) {
                final Enemy enemy = enemyFactory.get(enemyIndex);
                final String enemyIdentifier = enemyFactory.getStringIdentifier(enemy);
                final ReadOnlyVector3[] enemiesPos = enemyPositionsMap.get(enemyIdentifier);
                if (enemiesPos != null && enemiesPos.length != 0) {
                    if (enemyMeshes == null)
                        enemyMeshes = new ArrayList<>();
                    try {
                        final Mesh weaponNodeTemplate = (Mesh) binaryImporter
                                .load(getClass().getResource("/abin/weapon.abin"));
                        weaponNodeTemplate.setRotation(new Quaternion().fromEulerAngles(-Math.PI / 2, 0, -Math.PI / 2));
                        weaponNodeTemplate.setScale(0.015);
                        // the transform of the mesh mustn't be polluted by the
                        // initial rotation and scale as it is used to know the
                        // orientation of the weapon
                        NodeHelper.applyTransformToMeshData(weaponNodeTemplate);
                        weaponNodeTemplate.updateModelBound();
                        weaponNodeTemplate.updateWorldBound(true);
                        final KeyframeController<Mesh> weaponKeyframeControllerTemplate = (KeyframeController<Mesh>) weaponNodeTemplate
                                .getController(0);
                        for (PointInTime pit : weaponKeyframeControllerTemplate._keyframes) {
                            pit._newShape.setScale(0.015);
                            pit._newShape.setRotation(new Quaternion().fromEulerAngles(-Math.PI / 2, 0, -Math.PI / 2));
                            NodeHelper.applyTransformToMeshData(pit._newShape);
                            pit._newShape.updateModelBound();
                            pit._newShape.updateWorldBound(true);
                        }
                        final String enemyResourceName = enemy.getResourceName();
                        final Mesh enemyNodeTemplate = (Mesh) binaryImporter
                                .load(getClass().getResource(enemyResourceName));
                        enemyNodeTemplate.setRotation(new Quaternion().fromEulerAngles(-Math.PI / 2, 0, -Math.PI / 2));
                        enemyNodeTemplate.setScale(0.015);
                        NodeHelper.applyTransformToMeshData(enemyNodeTemplate);
                        enemyNodeTemplate.updateModelBound();
                        enemyNodeTemplate.updateWorldBound(true);
                        final KeyframeController<Mesh> soldierKeyframeControllerTemplate = (KeyframeController<Mesh>) enemyNodeTemplate
                                .getController(0);
                        for (PointInTime pit : soldierKeyframeControllerTemplate._keyframes) {
                            pit._newShape.setScale(0.015);
                            pit._newShape.setRotation(new Quaternion().fromEulerAngles(-Math.PI / 2, 0, -Math.PI / 2));
                            NodeHelper.applyTransformToMeshData(pit._newShape);
                            pit._newShape.updateModelBound();
                            pit._newShape.updateWorldBound(true);
                        }
                        for (ReadOnlyVector3 enemyPos : enemiesPos) {
                            final Mesh enemyNode = NodeHelper.makeCopy(enemyNodeTemplate, true);
                            enemyNode.setName("enemy@" + enemyNode.hashCode());
                            enemyNode.setTranslation(enemyPos);
                            final KeyframeController<Mesh> enemyKeyframeController = (KeyframeController<Mesh>) enemyNode
                                    .getController(0);
                            enemyKeyframeController.setUpdateBounding(true);
                            // loops on all frames of the set in the supplied
                            // time frame
                            enemyKeyframeController.setRepeatType(RepeatType.WRAP);
                            // uses the "stand" animation
                            enemyKeyframeController.setSpeed(MD2FrameSet.STAND.getFramesPerSecond());
                            enemyKeyframeController.setCurTime(MD2FrameSet.STAND.getFirstFrameIndex());
                            enemyKeyframeController.setMinTime(MD2FrameSet.STAND.getFirstFrameIndex());
                            enemyKeyframeController.setMaxTime(MD2FrameSet.STAND.getLastFrameIndex());
                            final Mesh weaponNode = NodeHelper.makeCopy(weaponNodeTemplate, true);
                            weaponNode.setName("weapon of " + enemyNode.getName());
                            weaponNode.setTranslation(enemyPos);
                            final KeyframeController<Mesh> weaponKeyframeController = (KeyframeController<Mesh>) weaponNode
                                    .getController(0);
                            // loops on all frames of the set in the supplied
                            // time frame
                            weaponKeyframeController.setRepeatType(RepeatType.WRAP);
                            // uses the "stand" animation
                            weaponKeyframeController.setSpeed(MD2FrameSet.STAND.getFramesPerSecond());
                            weaponKeyframeController.setCurTime(MD2FrameSet.STAND.getFirstFrameIndex());
                            weaponKeyframeController.setMinTime(MD2FrameSet.STAND.getFirstFrameIndex());
                            weaponKeyframeController.setMaxTime(MD2FrameSet.STAND.getLastFrameIndex());
                            enemyMeshes.add(enemyNode);
                            enemyMeshes.add(weaponNode);
                        }
                    } catch (IOException ioe) {
                        throw new RuntimeException("enemies loading failed", ioe);
                    }
                }
            }
        }
        return (enemyMeshes);
    }

    public List<Node> loadTeleporterModels(final TeleporterFactory teleporterFactory) {
        List<Node> teleporterNodes = null;
        if (teleporterPositionsMap != null && !teleporterPositionsMap.isEmpty()) {
            final int teleporterCount = teleporterFactory.getSize();
            for (int teleporterIndex = 0; teleporterIndex < teleporterCount; teleporterIndex++) {
                final Teleporter teleporter = teleporterFactory.get(teleporterIndex);
                final String teleporterIdentifier = teleporterFactory.getStringIdentifier(teleporter);
                final Entry<String, ReadOnlyVector3[]> entry = teleporterPositionsMap.get(teleporterIdentifier);
                if (entry != null) {
                    final ReadOnlyVector3[] teleportersPos = entry.getValue();
                    if (teleportersPos != null && teleportersPos.length != 0) {
                        if (teleporterNodes == null)
                            teleporterNodes = new ArrayList<>();
                        final String teleportersDestinationLevelIdentifier = entry.getKey();
                        final String teleporterLabel = teleporter.getLabel();
                        for (final ReadOnlyVector3 teleporterPos : teleportersPos) {
                            final Node teleporterNode = new Node(teleporterLabel);
                            final Box teleporterBox = new Box(teleporterLabel, new Vector3(0, 0, 0), 0.5, 0.05, 0.5);
                            teleporterBox.setRandomColors();
                            teleporterNode.setTranslation(teleporterPos);
                            teleporterNode.attachChild(teleporterBox);
                            // TODO set the destination
                            teleporterNode.setUserData(
                                    new TeleporterUserData(teleporter, null, teleportersDestinationLevelIdentifier));
                            teleporterNodes.add(teleporterNode);
                        }
                    }
                }
            }
        }
        return (teleporterNodes);
    }

    public List<Node> loadMedikitModels(final MedikitFactory medikitFactory) {
        List<Node> medikitNodes = null;
        if (medikitPositionsMap != null && !medikitPositionsMap.isEmpty()) {
            final int medikitCount = medikitFactory.getSize();
            for (int medikitIndex = 0; medikitIndex < medikitCount; medikitIndex++) {
                final Medikit medikit = medikitFactory.get(medikitIndex);
                final String medikitIdentifier = medikitFactory.getStringIdentifier(medikit);
                final ReadOnlyVector3[] medikitsPos = medikitPositionsMap.get(medikitIdentifier);
                if (medikitsPos != null && medikitsPos.length != 0) {
                    if (medikitNodes == null)
                        medikitNodes = new ArrayList<>();
                    final String medikitLabel = medikit.getLabel();
                    final String textureResourceName = medikit.getTextureResourceName();
                    final Box medikitBox = new Box(medikitLabel, new Vector3(0, 0, 0), 0.1, 0.1, 0.1);
                    final TextureState ts = new TextureState();
                    ts.setTexture(
                            TextureManager.load(new URLResourceSource(getClass().getResource(textureResourceName)),
                                    Texture.MinificationFilter.Trilinear, true));
                    medikitBox.setRenderState(ts);
                    for (final ReadOnlyVector3 medikitPos : medikitsPos) {
                        final Node medikitNode = new Node(medikitLabel);
                        medikitNode.setTranslation(medikitPos);
                        medikitNode.attachChild(medikitBox);
                        medikitNode.setUserData(new MedikitUserData(medikit));
                        medikitNodes.add(medikitNode);
                    }
                }
            }
        }

        return (medikitNodes);
    }

    public List<Node> loadAmmoBoxModels(final AmmunitionBoxFactory ammunitionBoxFactory) {
        List<Node> ammoBoxNodes = null;
        if (ammoBoxPositionsMap != null && !ammoBoxPositionsMap.isEmpty()) {
            final int ammoBoxCount = ammunitionBoxFactory.getSize();
            for (int ammoBoxIndex = 0; ammoBoxIndex < ammoBoxCount; ammoBoxIndex++) {
                final AmmunitionBox ammoBox = ammunitionBoxFactory.get(ammoBoxIndex);
                final String ammoBoxIdentifier = ammunitionBoxFactory.getStringIdentifier(ammoBox);
                final ReadOnlyVector3[] ammoBoxesPos = ammoBoxPositionsMap.get(ammoBoxIdentifier);
                if (ammoBoxesPos != null && ammoBoxesPos.length != 0) {
                    if (ammoBoxNodes == null)
                        ammoBoxNodes = new ArrayList<>();
                    final String ammoLabel = ammoBox.getLabel();
                    final String ammoTextureResourceName = ammoBox.getTextureResourceName();
                    // TODO create a template node and copy it
                    for (final ReadOnlyVector3 ammoBoxPos : ammoBoxesPos) {
                        final Node ammoBoxNode = new Node(ammoLabel);
                        final Box ammoBoxBox = new Box(ammoLabel, new Vector3(0, 0, 0), 0.1, 0.1, 0.1);
                        final TextureState ts = new TextureState();
                        ts.setTexture(TextureManager.load(
                                new URLResourceSource(getClass().getResource(ammoTextureResourceName)),
                                Texture.MinificationFilter.Trilinear, true));
                        ammoBoxBox.setRenderState(ts);
                        ammoBoxNode.setTranslation(ammoBoxPos);
                        ammoBoxNode.attachChild(ammoBoxBox);
                        ammoBoxNode.setUserData(new AmmunitionBoxUserData(ammunitionBoxFactory.get(ammoBoxIdentifier)));
                        ammoBoxNodes.add(ammoBoxNode);
                    }
                }
            }
        }
        return (ammoBoxNodes);
    }

    public List<Node> loadWeaponModels(final WeaponFactory weaponFactory) {
        List<Node> weaponNodes = null;
        if (weaponPositionsMap != null && weaponPositionsMap.size() != 0) {// N.B:
                                                                           // only
                                                                           // show
                                                                           // working
                                                                           // weapons
            try {/*
                  * uziNode.setTranslation(111.5,0.15,219);
                  * smachNode.setTranslation(112.5,0.15,219);
                  * pistolNode.setTranslation(113.5,0.1,219);
                  * duplicatePistolNode.setTranslation(113.5,0.1,217);
                  * laserNode.setTranslation(116.5,0.1,219);
                  * shotgunNode.setTranslation(117.5,0.1,219);
                  * rocketLauncherNode.setTranslation(117.5,0.1,222);
                  */
                // TODO store the template nodes in order to use them during the
                // cleanup
                // TODO move the transforms into the binary files and convert
                // them into Wavefront OBJ
                final int weaponCount = weaponFactory.getSize();
                for (int weaponIndex = 0; weaponIndex < weaponCount; weaponIndex++) {
                    final Weapon weapon = weaponFactory.get(weaponIndex);
                    final String weaponIdentifier = weaponFactory.getStringIdentifier(weapon);
                    final ReadOnlyVector3[] weaponsPos = weaponPositionsMap.get(weaponIdentifier);
                    if (weaponsPos != null && weaponsPos.length != 0) {
                        if (weaponNodes == null)
                            weaponNodes = new ArrayList<>();
                        final String weaponLabel = weapon.getLabel();
                        final String weaponResourceName = weapon.getResourceName();
                        final Node weaponTemplateNode = (Node) binaryImporter
                                .load(getClass().getResource(weaponResourceName));
                        weaponTemplateNode.setName(weaponLabel);
                        final boolean digitalWatermarkEnabled, primary;
                        switch (weaponIdentifier) {
                        case "PISTOL_9MM": {// removes the bullet as it is not
                                            // necessary now
                            ((Node) weaponTemplateNode.getChild(0)).detachChildAt(2);
                            weaponTemplateNode.setScale(0.02);
                            weaponTemplateNode
                                    .setRotation(new Quaternion().fromAngleAxis(-Math.PI / 2, new Vector3(1, 0, 0)));
                            digitalWatermarkEnabled = false;
                            primary = true;
                            break;
                        }
                        case "MAG_60": {
                            weaponTemplateNode.setScale(0.02);
                            digitalWatermarkEnabled = false;
                            primary = true;
                            break;
                        }
                        case "UZI": {
                            weaponTemplateNode.setScale(0.2);
                            digitalWatermarkEnabled = false;
                            primary = true;
                            break;
                        }
                        case "SMACH": {
                            weaponTemplateNode.setScale(0.2);
                            digitalWatermarkEnabled = false;
                            primary = true;
                            break;
                        }
                        case "PISTOL_10MM": {
                            weaponTemplateNode.setScale(0.001);
                            weaponTemplateNode.setRotation(
                                    new Quaternion().fromEulerAngles(Math.PI / 2, -Math.PI / 4, Math.PI / 2));
                            digitalWatermarkEnabled = false;
                            primary = true;
                            break;
                        }
                        case "ROCKET_LAUNCHER": {// removes the scope
                            weaponTemplateNode.detachChildAt(0);
                            weaponTemplateNode.setScale(0.08);
                            weaponTemplateNode
                                    .setRotation(new Quaternion().fromAngleAxis(-Math.PI, new Vector3(0, 1, 0)));
                            digitalWatermarkEnabled = false;
                            primary = true;
                            break;
                        }
                        case "SHOTGUN": {
                            weaponTemplateNode.setScale(0.1);
                            digitalWatermarkEnabled = false;
                            primary = true;
                            break;
                        }
                        case "LASER": {
                            weaponTemplateNode.setScale(0.02);
                            digitalWatermarkEnabled = false;
                            primary = true;
                            break;
                        }
                        default: {
                            digitalWatermarkEnabled = false;
                            primary = true;
                        }
                        }
                        for (final ReadOnlyVector3 weaponPos : weaponsPos) {
                            final Node weaponNode = weaponTemplateNode.makeCopy(false);
                            weaponNode.setTranslation(weaponPos);
                            weaponNode.setUserData(
                                    new WeaponUserData(weapon, new Matrix3(weaponTemplateNode.getRotation()),
                                            PlayerData.NO_UID, digitalWatermarkEnabled, primary));
                            weaponNodes.add(weaponNode);
                        }
                    }
                }
            } catch (IOException ioe) {
                throw new RuntimeException("weapons loading failed", ioe);
            }
        }
        return (weaponNodes);
    }

    public Node loadMainModel() {
        if (mainModel == null) {
            try {
                mainModel = (Node) binaryImporter.load(getClass().getResource(resourceName));
            } catch (IOException ioe) {
                throw new RuntimeException("level loading failed", ioe);
            }
        }
        return (mainModel);
    }

    public Node getMainModel() {
        return (mainModel);
    }

    public com.ardor3d.scenegraph.extension.Skybox loadSkyboxModel(SkyboxFactory skyboxFactory) {
        if (skyboxModel == null && skyboxIdentifier != null) {
            final Skybox skybox = skyboxFactory.get(skyboxIdentifier);
            if (skybox != null) {
                final String skyboxLabel = skybox.getLabel();
                skyboxModel = new com.ardor3d.scenegraph.extension.Skybox(skyboxLabel, 64, 64, 64);
                final Texture north = TextureManager.load(
                        new URLResourceSource(getClass().getResource(skybox.getTextureResourceName(0))),
                        Texture.MinificationFilter.BilinearNearestMipMap, true);
                final Texture south = TextureManager.load(
                        new URLResourceSource(getClass().getResource(skybox.getTextureResourceName(2))),
                        Texture.MinificationFilter.BilinearNearestMipMap, true);
                final Texture east = TextureManager.load(
                        new URLResourceSource(getClass().getResource(skybox.getTextureResourceName(1))),
                        Texture.MinificationFilter.BilinearNearestMipMap, true);
                final Texture west = TextureManager.load(
                        new URLResourceSource(getClass().getResource(skybox.getTextureResourceName(3))),
                        Texture.MinificationFilter.BilinearNearestMipMap, true);
                final Texture up = TextureManager.load(
                        new URLResourceSource(getClass().getResource(skybox.getTextureResourceName(5))),
                        Texture.MinificationFilter.BilinearNearestMipMap, true);
                final Texture down = TextureManager.load(
                        new URLResourceSource(getClass().getResource(skybox.getTextureResourceName(4))),
                        Texture.MinificationFilter.BilinearNearestMipMap, true);
                skyboxModel.setTexture(com.ardor3d.scenegraph.extension.Skybox.Face.North, north);
                skyboxModel.setTexture(com.ardor3d.scenegraph.extension.Skybox.Face.West, west);
                skyboxModel.setTexture(com.ardor3d.scenegraph.extension.Skybox.Face.South, south);
                skyboxModel.setTexture(com.ardor3d.scenegraph.extension.Skybox.Face.East, east);
                skyboxModel.setTexture(com.ardor3d.scenegraph.extension.Skybox.Face.Up, up);
                skyboxModel.setTexture(com.ardor3d.scenegraph.extension.Skybox.Face.Down, down);
            }
        }
        return (skyboxModel);
    }

    public com.ardor3d.scenegraph.extension.Skybox getSkyboxModel() {
        return (skyboxModel);
    }

    /**
     * Removes the disposable spatials from the level
     * 
     * @return the list of removed disposable spatials
     */
    public List<Spatial> removeDisposableSpatials() {
        final List<Spatial> disposableSpatials = new ArrayList<>();
        if (this.skyboxModel != null) {
            disposableSpatials.add(this.skyboxModel);
            this.skyboxModel = null;
        }
        if (this.mainModel != null) {
            disposableSpatials.add(this.mainModel);
            this.mainModel = null;
        }
        return (disposableSpatials);
    }

    public String getIdentifier() {
        return (identifier);
    }

    @Override
    public boolean equals(final Object o) {
        final boolean result;
        if (o == null || !(o instanceof Level))
            result = false;
        else {
            final Level level = (Level) o;
            result = getLabel().equals(level.getLabel());
        }
        return (result);
    }

    @Override
    public int hashCode() {
        return (label.hashCode());
    }

    public String getLabel() {
        return (label);
    }

    @Override
    public String toString() {
        return (label);
    }

    @Override
    public int compareTo(final Level level) {
        return (label.compareTo(level.label));
    }

    @Deprecated
    public boolean[][] getCollisionMap() {
        return (collisionMap);
    }

    public List<Objective> getObjectives() {
        return (objectives);
    }

    public Map<String, ReadOnlyVector3[]> getEnemyPositionsMap() {
        return (enemyPositionsMap);
    }

    public String getResourceName() {
        return (resourceName);
    }
}
