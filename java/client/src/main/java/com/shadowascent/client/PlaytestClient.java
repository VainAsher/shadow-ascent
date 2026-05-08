package com.shadowascent.client;

import com.shadowascent.core.GameConfig;
import com.shadowascent.core.GameState;
import com.shadowascent.core.HubManager;
import com.shadowascent.core.Mission;
import com.shadowascent.core.MissionManager;
import com.shadowascent.core.NPC;
import com.shadowascent.core.StoryState;
import com.shadowascent.core.physics.PhysicsConstants;
import com.shadowascent.core.physics.PhysicsState;
import com.shadowascent.core.physics.SpatialHash;
import com.shadowascent.core.physics.TileRect;
import com.shadowascent.core.physics.TileType;
import com.shadowascent.core.world.progression.WorldProgressionGraph;
import com.shadowascent.core.world.progression.WorldProgressionGraph.ProgressionNode;
import com.shadowascent.core.world.sections.SectionTemplateLibrary;
import com.shadowascent.core.world.streaming.MutationOverlay;
import com.shadowascent.core.world.streaming.OverlayPayloadCodec;
import com.shadowascent.core.world.streaming.RegionInstance;
import com.shadowascent.core.world.streaming.RegionLoadException;
import com.shadowascent.core.world.streaming.RegionLoader;
import com.shadowascent.core.world.streaming.RegionManifest;
import com.shadowascent.core.world.streaming.RegionalStreamingConstraintValidator;
import com.shadowascent.core.world.streaming.ZoneOverride;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import com.shadowascent.core.simulation.EchoPuzzleEvaluator;
import com.shadowascent.core.simulation.EchoPuzzleSolution;
import com.shadowascent.core.simulation.SimEcho;
import com.shadowascent.core.simulation.SimInventory;
import com.shadowascent.core.simulation.SimShop;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Minimal playable client for human QA playtests.
 *
 * This is intentionally lightweight: it validates mission/hub progression through
 * direct player interaction before full rendering/combat stacks are imported.
 */
public final class PlaytestClient {
    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 720;
    private static final float RUN_SPEED_MULT = 0.72f;
    private static final float PRECISION_WALK_SPEED_MULT = 0.55f;
    private static final float JUMP_POWER_MULT = 0.88f;
    private static final float DOUBLE_JUMP_POWER_MULT = 0.88f;
    private static final float WALL_JUMP_X_MULT = 0.90f;
    private static final float WALL_JUMP_Y_MULT = 1.25f;
    private static final float DASH_SPEED_MULT = 0.92f;
    private static final float DASH_DURATION_MULT = 0.78f;
    private static final float DASH_CONTROL_LOCK_SECONDS = 0.03f;
    private static final float DASH_FEEDBACK_COOLDOWN_SECONDS = 0.45f;
    private static final float PLAYER_MARGIN_X = 50f;
    private static final float PLAYER_MARGIN_Y_TOP = 70f;
    private static final float PLAYER_MARGIN_Y_BOTTOM = 50f;
    private static final float PLAYER_RADIUS = 14f;
    private static final float FLOOR_Y = WINDOW_HEIGHT - PLAYER_MARGIN_Y_BOTTOM;
    private static final float CEILING_Y = PLAYER_MARGIN_Y_TOP + PLAYER_RADIUS;
    private static final float WORLD_WIDTH = 3450f;
    private static final float WORLD_LEFT_X = PLAYER_MARGIN_X;
    private static final float WORLD_RIGHT_X = WORLD_LEFT_X + WORLD_WIDTH;
    private static final float HUB_ROOM_END_X = WORLD_LEFT_X + 860f;
    private static final float FORGE_ROOM_END_X = HUB_ROOM_END_X + 860f;
    private static final float SHAFT_ROOM_END_X = FORGE_ROOM_END_X + 860f;
    private static final float SUMMIT_ROOM_END_X = WORLD_RIGHT_X;
    private static final float PLAYER_SPAWN_X = WORLD_LEFT_X + 180f;
    private static final float CAMERA_LERP_FACTOR = 8.0f;
    private static final float CAMERA_LOOK_AHEAD = 10.0f;
    private static final float WALL_JUMP_INPUT_LOCK_SECONDS = 0.20f;
    private static final float WALL_STAMINA_DRAIN_MULT = 1.6f;
    private static final float WALL_STAMINA_AIR_REGEN_MULT = 0.35f;
    private static final float INTERACT_RADIUS = 70f;
    private static final float FEEDBACK_FLASH_SECONDS = 4.5f;
    private static final String ABILITY_TRIGGER_FLAG_PREFIX = "playtest_trigger_";
    private static final String ENCOUNTER_CLEAR_FLAG_PREFIX = "playtest_encounter_clear_";
    private static final float ATTACK_RANGE = 72f;
    private static final float ATTACK_COOLDOWN_SECONDS = 0.32f;
    private static final float ENCOUNTER_DEFAULT_TELEGRAPH_SECONDS = 1.05f;
    private static final float ENCOUNTER_DEFAULT_VULNERABLE_SECONDS = 0.95f;
    private static final float ENCOUNTER_DEFAULT_RECOVER_SECONDS = 1.15f;
    private static final float SNAPSHOT_INTERVAL_SECONDS = 30f;
    private static final int PLAYER_MAX_HEALTH = 3;
    private static final int ENCOUNTER_MISS_DAMAGE = 1;
    private static final float DEATH_RESET_SECONDS = 2.0f;
    private static final long WORLD_SEED = 1337L;
    private static final float SHOP_NPC_X = WORLD_LEFT_X + 300f;
    private static final DateTimeFormatter EVIDENCE_FILE_TS =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter EVIDENCE_EVENT_TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

    private final GameState gameState;
    private final StoryState storyState;
    private final MissionManager missionManager;
    private final HubManager hubManager;
    private final StoryManager storyManager;
    private MissionUiCoordinator missionUiCoordinator;

    private final EchoPuzzleSolution summitEchoPuzzle = EchoPuzzleSolution.ofKills("summit_echo_room_1", 1);
    private final EchoPuzzleEvaluator echoPuzzleEvaluator = new EchoPuzzleEvaluator();

    private final Set<Integer> pressedKeys;
    private final SpatialHash collisionHash;
    private final List<TileRect> collisionTiles;
    private final List<TileRect> dynamicCollisionTiles;
    private final TraversalSubsystem traversalSubsystem;
    private final CombatSubsystem combatSubsystem;
    private final Set<String> clearedCombatEncounterIds;
    private final MovementSessionMetrics movementMetrics;
    private final UISubsystem uiSubsystem;
    private final List<RegionInstance> activeRegions;
    private final WorldProgressionGraph progressionGraph;
    private String currentRegionId;
    private Map<String, List<ZoneOverride>> savedOverlays;
    private List<String> streamingConstraintWarnings = new ArrayList<>();

    private final PhysicsState playerPhysics;
    private float lastMoveDirX = 1f;
    private boolean isDashing;
    private float dashTimerSeconds;
    private float dashCooldownSeconds;
    private float dashControlLockSeconds;
    private float dashDirectionX = 1f;
    private float dashFeedbackCooldownSeconds;
    private float coyoteTimerSeconds;
    private float wallCoyoteTimerSeconds;
    private int lastWallDir = 1;
    private float jumpBufferSeconds;
    private int jumpCount;
    private float wallJumpLockTimerSeconds;
    private float wallStaminaSeconds = PhysicsConstants.MAX_WALL_STAMINA;
    private boolean wallExhaustedAwaitGround;
    private int playerHealth;
    private boolean playerDead;
    private float deathResetTimerSeconds;

    private boolean queueInteract;
    private boolean queueStartMission;
    private boolean queueResolveObjective;
    private boolean queueJump;
    private boolean queueDash;
    private boolean queueAttack;
    private boolean queueSave;
    private boolean queueLoad;
    private boolean queueInventoryToggle;
    private boolean queueCraftToggle;
    private boolean queuePanelUp;
    private boolean queuePanelDown;
    private boolean queuePanelLeft;
    private boolean queuePanelRight;
    private boolean queuePanelAction;
    private boolean queuePanelClose;

    private SimInventory playerInventory;
    private InventoryPanel inventoryPanel;
    private ShopPanel shopPanel;
    private CraftingPanel craftingPanel;
    private SimShop hubShop;
    private String groundedMovingPlatformId;
    private float attackCooldownSeconds;
    private float cameraX;

    private long lastTickNanos;

    private final Path savePath = Path.of("save", "playtest_slot1.sav");
    private final Path evidenceDirPath = Path.of("logs", "playtest");
    private Path evidenceLogPath;
    private BufferedWriter evidenceWriter;
    private float sessionElapsedSeconds;
    private float nextSnapshotSeconds;

    private PlaytestClient() {
        this.gameState = new GameState();
        this.storyState = gameState.getStoryState();
        this.missionManager = gameState.getMissionManager();
        this.hubManager = gameState.getHubManager();
        this.storyManager = new StoryManager(storyState, hubManager, missionManager, this::log);

        this.pressedKeys = new HashSet<>();
        this.collisionHash = new SpatialHash();
        this.collisionTiles = new ArrayList<>();
        this.dynamicCollisionTiles = new ArrayList<>();
        this.traversalSubsystem = new TraversalSubsystem(this::onAbilityTriggerActivated, PLAYER_RADIUS);
        this.combatSubsystem = new CombatSubsystem(this::onCombatEncounterCleared);
        this.clearedCombatEncounterIds = new HashSet<>();
        this.activeRegions = new ArrayList<>();
        this.progressionGraph = buildPlaytestProgressionGraph();
        this.currentRegionId = "hub_lantern_heights";
        this.savedOverlays = new HashMap<>();
        this.movementMetrics = new MovementSessionMetrics();

        this.playerPhysics = new PhysicsState(
                PLAYER_SPAWN_X,
                FLOOR_Y,
                PhysicsConstants.PLAYER_WIDTH,
                PhysicsConstants.PLAYER_HEIGHT);
        this.playerPhysics.onGround = true;
        this.coyoteTimerSeconds = PhysicsConstants.COYOTE_TIME;
        this.groundedMovingPlatformId = null;
        this.attackCooldownSeconds = 0f;
        this.cameraX = cameraMinX();
        this.sessionElapsedSeconds = 0f;
        this.nextSnapshotSeconds = SNAPSHOT_INTERVAL_SECONDS;
        this.playerHealth = PLAYER_MAX_HEALTH;
        this.playerDead = false;
        this.deathResetTimerSeconds = 0f;

        WorldGeometry geometry = new WorldGeometry(
                WORLD_LEFT_X, WORLD_RIGHT_X,
                FLOOR_Y, CEILING_Y,
                WINDOW_WIDTH, WINDOW_HEIGHT,
                PLAYER_RADIUS, INTERACT_RADIUS,
                HUB_ROOM_END_X, FORGE_ROOM_END_X, SHAFT_ROOM_END_X, SUMMIT_ROOM_END_X);
        this.uiSubsystem = new UISubsystem(
                storyState, missionManager,
                traversalSubsystem, combatSubsystem,
                collisionTiles, geometry,
                msg -> writeEvidenceLine("EVENT", msg));
        this.missionUiCoordinator = new MissionUiCoordinator(
                storyState, missionManager,
                msg -> uiSubsystem.setMissionFeedback(msg, FEEDBACK_FLASH_SECONDS),
                this::log,
                FEEDBACK_FLASH_SECONDS);

        this.playerInventory = new SimInventory();
        this.playerInventory.addCurrency(100);
        this.playerInventory.addItem("weapon_dagger", 1);
        this.playerInventory.addItem("health_potion", 3);
        this.playerInventory.addItem("material_iron", 4);
        this.hubShop = new SimShop("merchant_npc", 2, 12345L);
        this.inventoryPanel = new InventoryPanel(playerInventory);
        this.shopPanel = new ShopPanel();
        this.craftingPanel = new CraftingPanel();

        initializeEvidenceLogging();
        initializeCollisionLayout();
        refreshOverlayHud();
        seedPlaytestState();
        restoreActivatedAbilityTriggersFromFlags();
        restoreClearedCombatEncountersFromFlags();
        refreshDynamicCollisionTiles();
        uiSubsystem.setMissionFeedback("No active mission. Press ENTER to start.", FEEDBACK_FLASH_SECONDS);
        uiSubsystem.seedAbilitySnapshot(storyState.getAbilities());
        cameraX = clamp(playerPhysics.x - (WINDOW_WIDTH * 0.5f), cameraMinX(), cameraMaxX());
        this.lastTickNanos = System.nanoTime();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PlaytestClient().launch());
    }

    private UISubsystem.RenderState buildRenderState() {
        return new UISubsystem.RenderState(
                playerPhysics.x,
                playerPhysics.y,
                cameraX,
                isDashing,
                dashTimerSeconds,
                dashCooldownSeconds,
                playerHealth,
                playerDead,
                jumpCount,
                wallStaminaSeconds,
                wallExhaustedAwaitGround,
                attackCooldownSeconds,
                clearedCombatEncounterIds);
    }

    private void seedPlaytestState() {
        storyManager.seedState();
        if (evidenceLogPath != null) {
            log("Session evidence log: " + evidenceLogPath.toAbsolutePath());
        }
    }

    private void launch() {
        JFrame frame = new JFrame(GameConfig.TITLE + " - Playtest Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdownEvidenceLogging();
            }
        });
        frame.setContentPane(new PlayPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private final class PlayPanel extends JPanel implements ActionListener {
        private final Timer timer;

        private PlayPanel() {
            setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
            setFocusable(true);
            setFocusTraversalKeysEnabled(false);
            configureKeyBindings();
            timer = new Timer(16, this);
            timer.start();
        }

        @Override
        public void addNotify() {
            super.addNotify();
            SwingUtilities.invokeLater(this::requestFocusInWindow);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            uiSubsystem.drawFrame(g, buildRenderState());
            inventoryPanel.draw(g, getWidth(), getHeight());
            shopPanel.draw(g, getWidth(), getHeight());
            craftingPanel.draw(g, getWidth(), getHeight());
            g.dispose();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            tick();
            repaint();
        }

        private void configureKeyBindings() {
            bindHold(KeyEvent.VK_A);
            bindHold(KeyEvent.VK_D);
            bindHold(KeyEvent.VK_S);
            bindHold(KeyEvent.VK_ALT);

            bindPress("interact", KeyEvent.VK_E, () -> queueInteract = true);
            bindPress("start_mission_enter", KeyEvent.VK_ENTER, () -> {
                if (anyPanelOpen()) queuePanelAction = true;
                else queueStartMission = true;
            });
            bindPress("start_mission_tab", KeyEvent.VK_TAB, () -> queueStartMission = true);
            bindPress("toggle_inventory", KeyEvent.VK_I, () -> queueInventoryToggle = true);
            bindPress("toggle_crafting", KeyEvent.VK_T, () -> queueCraftToggle = true);
            bindPress("panel_up",    KeyEvent.VK_UP,    () -> queuePanelUp    = true);
            bindPress("panel_down",  KeyEvent.VK_DOWN,  () -> queuePanelDown  = true);
            bindPress("panel_left",  KeyEvent.VK_LEFT,  () -> queuePanelLeft  = true);
            bindPress("panel_right", KeyEvent.VK_RIGHT, () -> queuePanelRight = true);
            bindPress("panel_close", KeyEvent.VK_ESCAPE, () -> queuePanelClose = true);
            bindPress("jump", KeyEvent.VK_SPACE, () -> queueJump = true);
            bindPress("resolve_objective", KeyEvent.VK_R, () -> queueResolveObjective = true);
            bindPress("dash", KeyEvent.VK_SHIFT, () -> queueDash = true);
            bindPress("dash_shift_masked", KeyEvent.VK_SHIFT, InputEvent.SHIFT_DOWN_MASK, () -> queueDash = true);
            bindPress("dash_alt_key", KeyEvent.VK_C, () -> queueDash = true);
            bindPress("attack", KeyEvent.VK_F, () -> queueAttack = true);
            bindPress("toggle_minimap", KeyEvent.VK_M, () -> uiSubsystem.toggleMinimap());
            bindPress("save", KeyEvent.VK_F5, () -> queueSave = true);
            bindPress("load", KeyEvent.VK_F9, () -> queueLoad = true);
        }

        private void bindHold(int keyCode) {
            String heldAction = "held_" + keyCode;
            String releasedAction = "released_" + keyCode;
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(keyCode, 0, false), heldAction);
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(keyCode, 0, true), releasedAction);
            // Also catch press/release when SHIFT is held so that dashing with SHIFT does not
            // drop the held-key state or silently swallow the release event for A/D.
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(keyCode, InputEvent.SHIFT_DOWN_MASK, false), heldAction);
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(keyCode, InputEvent.SHIFT_DOWN_MASK, true), releasedAction);
            getActionMap().put(heldAction, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    pressedKeys.add(keyCode);
                }
            });
            getActionMap().put(releasedAction, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    pressedKeys.remove(keyCode);
                }
            });
        }

        private void bindPress(String actionKey, int keyCode, Runnable action) {
            String pressedAction = "press_" + actionKey;
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(keyCode, 0, false), pressedAction);
            getActionMap().put(pressedAction, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    action.run();
                }
            });
        }

        private void bindPress(String actionKey, int keyCode, int modifiers, Runnable action) {
            String pressedAction = "press_" + actionKey + "_" + modifiers;
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(keyCode, modifiers, false), pressedAction);
            getActionMap().put(pressedAction, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    action.run();
                }
            });
        }
    }

    private void tick() {
        long now = System.nanoTime();
        float dt = Math.max(0.001f, (now - lastTickNanos) / 1_000_000_000f);
        dt = Math.min(dt, PhysicsConstants.MAX_FRAME_TIME);
        lastTickNanos = now;
        sessionElapsedSeconds += dt;
        attackCooldownSeconds = Math.max(0f, attackCooldownSeconds - dt);
        dashFeedbackCooldownSeconds = Math.max(0f, dashFeedbackCooldownSeconds - dt);
        if (playerDead) {
            deathResetTimerSeconds = Math.max(0f, deathResetTimerSeconds - dt);
            if (deathResetTimerSeconds <= 0f) {
                resetPlayerAfterDeath();
            }
        }
        updateTraversalDynamics(dt);

        float inputX = 0f;
        if (pressedKeys.contains(KeyEvent.VK_A)) {
            inputX -= 1f;
        }
        if (pressedKeys.contains(KeyEvent.VK_D)) {
            inputX += 1f;
        }
        boolean fastFallPressed = pressedKeys.contains(KeyEvent.VK_S);
        boolean precisionWalkHeld = pressedKeys.contains(KeyEvent.VK_ALT);

        if (inputX != 0f) {
            lastMoveDirX = inputX;
        }

        if (queueJump) {
            queueJump = false;
            jumpBufferSeconds = PhysicsConstants.JUMP_BUFFER_TIME;
        }

        if (queueDash) {
            queueDash = false;
            startDash(inputX);
        }

        float previousFrameX = playerPhysics.x;
        float previousFrameY = playerPhysics.y;
        updateMovement(dt, inputX, fastFallPressed, precisionWalkHeld);
        movementMetrics.recordFrame(dt, previousFrameX, previousFrameY, playerPhysics);
        checkAndApplyRegionTransition();
        combatSubsystem.update(playerPhysics.x, playerPhysics.y, storyState::hasAbility, this::log, dt);
        updateCamera(dt);

        if (queueAttack) {
            queueAttack = false;
            performCombatAttack();
        }

        if (queueStartMission) {
            queueStartMission = false;
            startNextAvailableMission();
        }
        if (queueInteract) {
            queueInteract = false;
            interactNearestNpc();
        }
        if (queueResolveObjective) {
            queueResolveObjective = false;
            resolveActiveObjectiveShortcut();
        }
        if (queueSave) {
            queueSave = false;
            saveState();
        }
        if (queueLoad) {
            queueLoad = false;
            loadState();
        }

        processPanelInputs();
        uiSubsystem.tickAndUpdate(playerPhysics.x, playerPhysics.y, dt);
        emitPeriodicSnapshotIfDue();
    }

    private void startDash(float inputX) {
        if (isDashing) {
            dashFeedback("Dash already active.");
            return;
        }
        if (dashCooldownSeconds > 0f) {
            dashFeedback(String.format(Locale.ROOT, "Dash cooling down: %.2fs", Math.max(0f, dashCooldownSeconds)));
            return;
        }
        if (!storyState.hasAbility("dash")) {
            dashFeedback("Dash is locked. Complete dojo training first.");
            return;
        }

        float dashX = inputX;
        if (dashX == 0f) {
            dashX = lastMoveDirX;
        }
        if (dashX == 0f) {
            dashX = 1f;
        }

        isDashing = true;
        dashDirectionX = dashX >= 0f ? 1f : -1f;
        dashTimerSeconds = PhysicsConstants.DASH_DURATION * DASH_DURATION_MULT;
        dashControlLockSeconds = Math.min(DASH_CONTROL_LOCK_SECONDS, dashTimerSeconds);
        playerPhysics.vx = dashDirectionX * PhysicsConstants.DASH_SPEED * DASH_SPEED_MULT;
        playerPhysics.vy = 0f;
        playerPhysics.onGround = false;
        log(String.format(Locale.ROOT, "Dash start vx=%.2f lock=%.3fs", playerPhysics.vx, dashControlLockSeconds));
        movementMetrics.recordDashStart();
    }

    private void updateMovement(float dt, float inputX, boolean fastFallPressed, boolean precisionWalkHeld) {
        if (playerDead) {
            return;
        }
        float tickScale = dt * PhysicsConstants.TARGET_FPS;
        if (jumpBufferSeconds > 0f) {
            jumpBufferSeconds -= dt;
        }
        if (dashCooldownSeconds > 0f) {
            dashCooldownSeconds -= dt;
        }
        if (coyoteTimerSeconds > 0f && !playerPhysics.onGround) {
            coyoteTimerSeconds -= dt;
        }
        if (wallCoyoteTimerSeconds > 0f && !playerPhysics.onWall) {
            wallCoyoteTimerSeconds -= dt;
        }
        if (wallJumpLockTimerSeconds > 0f) {
            wallJumpLockTimerSeconds -= dt;
        }

        updateWallStamina(dt);

        if (isDashing) {
            if (playerPhysics.onWall) {
                endDash();
            } else {
                dashTimerSeconds -= dt;
                dashControlLockSeconds = Math.max(0f, dashControlLockSeconds - dt);
                if (dashTimerSeconds <= 0f) {
                    endDash();
                } else {
                    if (dashControlLockSeconds <= 0f && inputX != 0f) {
                        float requestedDir = inputX > 0f ? 1f : -1f;
                        if (requestedDir != dashDirectionX) {
                            endDash();
                        } else {
                            dashDirectionX = requestedDir;
                        }
                    }
                    if (isDashing) {
                        playerPhysics.vx = dashDirectionX * PhysicsConstants.DASH_SPEED * DASH_SPEED_MULT;
                    }
                }
            }
        }

        if (!isDashing && wallJumpLockTimerSeconds <= 0f) {
            float speedMult = precisionWalkHeld ? PRECISION_WALK_SPEED_MULT : RUN_SPEED_MULT;
            float maxSpeed = PhysicsConstants.MAX_RUN_SPEED * speedMult;
            if (inputX != 0f) {
                playerPhysics.vx = inputX * maxSpeed;
            } else {
                float friction = (float) Math.pow(PhysicsConstants.GROUND_FRICTION, tickScale);
                playerPhysics.vx *= friction;
                if (Math.abs(playerPhysics.vx) < 0.03f) {
                    playerPhysics.vx = 0f;
                }
            }
        }

        boolean canGroundJump = playerPhysics.onGround || coyoteTimerSeconds > 0f;
        boolean canWallJump = (playerPhysics.onWall || wallCoyoteTimerSeconds > 0f)
                && !canGroundJump
                && !wallExhaustedAwaitGround;
        boolean canDoubleJump = !canGroundJump && !canWallJump && jumpCount == 1 && !isDashing;
        if (jumpBufferSeconds > 0f && canGroundJump && jumpCount == 0) {
            playerPhysics.vy = -PhysicsConstants.JUMP_POWER * JUMP_POWER_MULT;
            playerPhysics.onGround = false;
            coyoteTimerSeconds = 0f;
            jumpBufferSeconds = 0f;
            jumpCount = 1;
            movementMetrics.recordGroundJump();
        } else if (jumpBufferSeconds > 0f && canWallJump) {
            int wallDir = playerPhysics.onWall
                    ? playerPhysics.wallDir
                    : (lastWallDir == 0 ? (lastMoveDirX >= 0f ? 1 : -1) : lastWallDir);
            if (wallDir == 0) {
                wallDir = 1;
            }
            playerPhysics.vx = -wallDir * PhysicsConstants.WALL_JUMP_POWER_X * WALL_JUMP_X_MULT;
            playerPhysics.vy = -PhysicsConstants.WALL_JUMP_POWER_Y * WALL_JUMP_Y_MULT;
            playerPhysics.onWall = false;
            playerPhysics.wallDir = 0;
            wallCoyoteTimerSeconds = 0f;
            jumpBufferSeconds = 0f;
            lastMoveDirX = -wallDir;
            jumpCount = 0;
            wallJumpLockTimerSeconds = WALL_JUMP_INPUT_LOCK_SECONDS;
            movementMetrics.recordWallJump();
        } else if (jumpBufferSeconds > 0f && canDoubleJump) {
            playerPhysics.vy = -PhysicsConstants.DOUBLE_JUMP_POWER * DOUBLE_JUMP_POWER_MULT;
            jumpBufferSeconds = 0f;
            jumpCount = 2;
            movementMetrics.recordDoubleJump();
        }

        float gravityMult = playerPhysics.vy < 0f ? 1f : PhysicsConstants.FALL_GRAVITY_MULT;
        if (fastFallPressed && playerPhysics.vy > 0f) {
            gravityMult *= PhysicsConstants.FAST_FALL_MULT;
        }
        if (!playerPhysics.onGround) {
            playerPhysics.vy += PhysicsConstants.GRAVITY * gravityMult * tickScale;
            if (playerPhysics.vy > PhysicsConstants.MAX_FALL_SPEED) {
                playerPhysics.vy = PhysicsConstants.MAX_FALL_SPEED;
            }
        }

        float previousX = playerPhysics.x;
        float previousY = playerPhysics.y;
        playerPhysics.x += playerPhysics.vx * tickScale;
        resolveHorizontalCollisions(previousX);
        playerPhysics.y += playerPhysics.vy * tickScale;
        resolveVerticalCollisions(previousY);
        clampPlayerToArenaBounds();
    }

    private void dashFeedback(String message) {
        uiSubsystem.setMissionFeedback(message, FEEDBACK_FLASH_SECONDS);
        if (dashFeedbackCooldownSeconds <= 0f) {
            log(message);
            dashFeedbackCooldownSeconds = DASH_FEEDBACK_COOLDOWN_SECONDS;
        }
    }

    private void endDash() {
        if (!isDashing) {
            return;
        }
        isDashing = false;
        dashTimerSeconds = 0f;
        dashControlLockSeconds = 0f;
        dashCooldownSeconds = PhysicsConstants.DASH_COOLDOWN;
    }

    private void updateTraversalDynamics(float dt) {
        traversalSubsystem.update(playerPhysics.x, playerPhysics.y, isDashing, storyState::hasAbility, dt);
        if (playerPhysics.onGround && groundedMovingPlatformId != null && !groundedMovingPlatformId.isBlank()) {
            TraversalSubsystem.PlatformCarry carry = traversalSubsystem.carryFor(groundedMovingPlatformId);
            if (carry != null) {
                playerPhysics.x += carry.dx();
                playerPhysics.y += carry.dy();
            }
        }
        refreshDynamicCollisionTiles();
    }

    private void refreshDynamicCollisionTiles() {
        dynamicCollisionTiles.clear();
        dynamicCollisionTiles.addAll(buildAllDynamicTiles());
        collisionHash.setDynamicTiles(dynamicCollisionTiles);
    }

    private List<TileRect> buildAllDynamicTiles() {
        List<TileRect> tiles = new ArrayList<>(
                traversalSubsystem.buildDynamicTiles(clearedCombatEncounterIds, storyState::hasAbility));
        for (RegionInstance r : activeRegions) {
            tiles.addAll(r.overlayTiles());
        }
        return tiles;
    }

    private void refreshCollisionHashFromRegions() {
        collisionHash.clear();
        for (TileRect tile : collisionTiles) {
            collisionHash.insert(tile);
        }
        collisionHash.setDynamicTiles(buildAllDynamicTiles());
    }

    private String resolveRegionIdForX(float x) {
        if (x <= HUB_ROOM_END_X) return "hub_lantern_heights";
        if (x <= FORGE_ROOM_END_X) return "dungeon_forge_terrace_a";
        return "region_hollow_shaft";
    }

    private void checkAndApplyRegionTransition() {
        String detected = resolveRegionIdForX(playerPhysics.x);
        if (!detected.equals(currentRegionId)) {
            transitionToRegion(detected);
        }
    }

    private void transitionToRegion(String newRegionId) {
        String prev = currentRegionId;
        currentRegionId = newRegionId;
        activeRegions.clear();
        streamingConstraintWarnings.clear();
        try {
            RegionLoader loader = new RegionLoader(
                    SectionTemplateLibrary.loadDefault(),
                    new RegionalStreamingConstraintValidator());
            activeRegions.addAll(loader.loadNeighborhood(
                    progressionGraph, currentRegionId, 1, WORLD_SEED, savedOverlays));
            collectStreamingWarnings(activeRegions);
        } catch (RegionLoadException e) {
            System.out.println("[WARN] PlaytestClient: region transition blocked: " + e.getMessage());
            for (RegionalStreamingConstraintValidator.ValidationIssue issue : e.validationResult().issues()) {
                streamingConstraintWarnings.add(
                        "[" + issue.kind() + "] " + issue.regionId() + ": " + issue.message());
            }
        } catch (Exception e) {
            System.out.println("[WARN] PlaytestClient: region transition failed: " + e.getMessage());
        }
        refreshCollisionHashFromRegions();
        refreshOverlayHud();
        writeEvidenceLine("REGION_TRANSITION",
                "from=" + prev + " to=" + currentRegionId
                + " playerX=" + String.format(Locale.ROOT, "%.1f", playerPhysics.x));
        writeEvidenceLine("MUTATION_OVERLAY", buildOverlaySummary());
        log("Region: " + prev + " → " + currentRegionId);
    }

    private void restoreActivatedAbilityTriggersFromFlags() {
        storyManager.restoreAbilityTriggers(traversalSubsystem, ABILITY_TRIGGER_FLAG_PREFIX);
    }

    private void restoreClearedCombatEncountersFromFlags() {
        storyManager.restoreCombatEncounters(combatSubsystem, clearedCombatEncounterIds, ENCOUNTER_CLEAR_FLAG_PREFIX);
    }

    private float cameraMinX() {
        return WORLD_LEFT_X - PLAYER_MARGIN_X;
    }

    private float cameraMaxX() {
        return Math.max(cameraMinX(), WORLD_RIGHT_X - (WINDOW_WIDTH - PLAYER_MARGIN_X));
    }

    private void updateCamera(float dt) {
        float lookAhead = playerPhysics.vx * CAMERA_LOOK_AHEAD;
        float target = playerPhysics.x - (WINDOW_WIDTH * 0.5f) + lookAhead;
        target = clamp(target, cameraMinX(), cameraMaxX());
        float lerp = Math.min(1f, Math.max(0.05f, dt * CAMERA_LERP_FACTOR));
        cameraX += (target - cameraX) * lerp;
        cameraX = clamp(cameraX, cameraMinX(), cameraMaxX());
    }

    private void clampPlayerToArenaBounds() {
        if (playerPhysics.x < WORLD_LEFT_X) {
            playerPhysics.x = WORLD_LEFT_X;
            playerPhysics.vx = 0f;
            playerPhysics.onWall = true;
            playerPhysics.wallDir = -1;
            lastWallDir = -1;
        }
        if (playerPhysics.x > WORLD_RIGHT_X) {
            playerPhysics.x = WORLD_RIGHT_X;
            playerPhysics.vx = 0f;
            playerPhysics.onWall = true;
            playerPhysics.wallDir = 1;
            lastWallDir = 1;
        }

        if (playerPhysics.y > FLOOR_Y) {
            playerPhysics.y = FLOOR_Y;
            playerPhysics.vy = 0f;
            playerPhysics.onGround = true;
            coyoteTimerSeconds = PhysicsConstants.COYOTE_TIME;
            jumpCount = 0;
            groundedMovingPlatformId = null;
        }
        if (playerPhysics.y < CEILING_Y) {
            playerPhysics.y = CEILING_Y;
            if (playerPhysics.vy < 0f) {
                playerPhysics.vy = 0f;
            }
        }

        if (playerPhysics.onWall && !playerPhysics.onGround) {
            wallCoyoteTimerSeconds = PhysicsConstants.COYOTE_TIME;
            playerPhysics.vy = Math.min(playerPhysics.vy, PhysicsConstants.WALL_SLIDE_SPEED);
        }
    }

    private void resolveHorizontalCollisions(float previousX) {
        playerPhysics.onWall = false;
        playerPhysics.wallDir = 0;

        float probeX = playerPhysics.x - PLAYER_RADIUS;
        float probeY = playerPhysics.y - PLAYER_RADIUS;
        float probeSize = PLAYER_RADIUS * 2f;
        List<TileRect> candidates = collisionHash.candidates(probeX, probeY, probeSize, probeSize);
        float previousLeft = previousX - PLAYER_RADIUS;
        float previousRight = previousX + PLAYER_RADIUS;

        for (TileRect tile : candidates) {
            if (tile.isPlatform()) {
                continue;
            }
            TileType tileType = tile.tileTypeEnum();
            if (tileType == TileType.AIR || tileType == TileType.WATER || tileType == TileType.GAS) {
                continue;
            }
            if (!overlapsPlayer(tile)) {
                continue;
            }

            float currentLeft = playerPhysics.x - PLAYER_RADIUS;
            float currentRight = playerPhysics.x + PLAYER_RADIUS;
            boolean hitFromLeft = playerPhysics.vx > 0f && previousRight <= tile.x() + 1.0f;
            boolean hitFromRight = playerPhysics.vx < 0f && previousLeft >= tile.right() - 1.0f;

            if (hitFromLeft) {
                playerPhysics.x = tile.x() - PLAYER_RADIUS;
                playerPhysics.vx = 0f;
                playerPhysics.onWall = true;
                playerPhysics.wallDir = 1;
                lastWallDir = 1;
            } else if (hitFromRight) {
                playerPhysics.x = tile.right() + PLAYER_RADIUS;
                playerPhysics.vx = 0f;
                playerPhysics.onWall = true;
                playerPhysics.wallDir = -1;
                lastWallDir = -1;
            } else {
                float overlapLeft = currentRight - tile.x();
                float overlapRight = tile.right() - currentLeft;
                if (overlapLeft > 0f && overlapLeft < overlapRight) {
                    playerPhysics.x -= overlapLeft;
                    playerPhysics.onWall = true;
                    playerPhysics.wallDir = 1;
                    lastWallDir = 1;
                } else if (overlapRight > 0f) {
                    playerPhysics.x += overlapRight;
                    playerPhysics.onWall = true;
                    playerPhysics.wallDir = -1;
                    lastWallDir = -1;
                }
                playerPhysics.vx = 0f;
            }

            if (tileType == TileType.DOOR_LOCKED) {
                uiSubsystem.notifyAbilityGateBlocked(tile, clearedCombatEncounterIds);
            }
        }
    }

    private void resolveVerticalCollisions(float previousY) {
        playerPhysics.onGround = false;
        groundedMovingPlatformId = null;
        float probeX = playerPhysics.x - PLAYER_RADIUS;
        float probeY = playerPhysics.y - PLAYER_RADIUS;
        float probeSize = PLAYER_RADIUS * 2f;
        List<TileRect> candidates = collisionHash.candidates(probeX, probeY, probeSize, probeSize);
        float previousTop = previousY - PLAYER_RADIUS;
        float previousBottom = previousY + PLAYER_RADIUS;

        for (TileRect tile : candidates) {
            if (!overlapsPlayer(tile)) {
                continue;
            }

            TileType tileType = tile.tileTypeEnum();
            if (tileType == TileType.WATER) {
                playerPhysics.inWater = true;
                continue;
            }
            if (tileType == TileType.GAS) {
                playerPhysics.inGas = true;
                continue;
            }
            if (tileType == TileType.AIR) {
                continue;
            }

            float playerTop = playerPhysics.y - PLAYER_RADIUS;
            float playerBottom = playerPhysics.y + PLAYER_RADIUS;
            float overlapTop = playerBottom - tile.y();
            float overlapBottom = tile.bottom() - playerTop;

            if (tile.isPlatform()) {
                float landingGrace = Math.max(PhysicsConstants.PLATFORM_GRACE_PIXELS, Math.abs(playerPhysics.vy) + 1f);
                if (playerPhysics.vy >= 0f
                        && previousBottom <= tile.y() + landingGrace
                        && playerBottom >= tile.y()
                        && overlapTop > 0f
                        && overlapTop < tile.h()) {
                    playerPhysics.y = tile.y() - PLAYER_RADIUS;
                    playerPhysics.vy = 0f;
                    playerPhysics.onGround = true;
                    coyoteTimerSeconds = PhysicsConstants.COYOTE_TIME;
                    jumpCount = 0;
                    groundedMovingPlatformId = traversalSubsystem.platformIdForTile(tile);
                }
                continue;
            }

            if (playerPhysics.vy >= 0f
                    && previousBottom <= tile.y() + Math.max(1f, Math.abs(playerPhysics.vy) + 1f)
                    && overlapTop >= 0f
                    && overlapTop < overlapBottom) {
                playerPhysics.y = tile.y() - PLAYER_RADIUS;
                playerPhysics.vy = 0f;
                playerPhysics.onGround = true;
                coyoteTimerSeconds = PhysicsConstants.COYOTE_TIME;
                jumpCount = 0;
                groundedMovingPlatformId = null;
            } else if (playerPhysics.vy < 0f
                    && previousTop >= tile.bottom() - Math.max(1f, Math.abs(playerPhysics.vy) + 1f)
                    && overlapBottom < overlapTop) {
                playerPhysics.y = tile.bottom() + PLAYER_RADIUS;
                playerPhysics.vy = 0f;
            }
        }
    }

    private boolean overlapsPlayer(TileRect tile) {
        float playerLeft = playerPhysics.x - PLAYER_RADIUS;
        float playerRight = playerPhysics.x + PLAYER_RADIUS;
        float playerTop = playerPhysics.y - PLAYER_RADIUS;
        float playerBottom = playerPhysics.y + PLAYER_RADIUS;
        return playerLeft < tile.right()
                && playerRight > tile.x()
                && playerTop < tile.bottom()
                && playerBottom > tile.y();
    }


    private void performCombatAttack() {
        if (attackCooldownSeconds > 0f) {
            return;
        }
        attackCooldownSeconds = ATTACK_COOLDOWN_SECONDS;

        CombatSubsystem.AttackOutcome outcome = combatSubsystem.tryAttack(
                playerPhysics.x, playerPhysics.y, storyState::hasAbility, ATTACK_RANGE);
        switch (outcome.type()) {
            case NO_TARGET ->
                uiSubsystem.setMissionFeedback("Attack whiffs: no encounter target in range.", 1.4f);
            case ABILITY_LOCKED ->
                uiSubsystem.setMissionFeedback("Technique locked: requires " + outcome.requiredAbility() + ".", FEEDBACK_FLASH_SECONDS);
            case MISS_TIMING -> {
                log("Attack mistimed on " + outcome.encounterName() + " (state=" + outcome.phaseLabel() + ").");
                uiSubsystem.setMissionFeedback("Mistimed: wait for " + outcome.encounterName() + " vulnerable window.", 1.8f);
                takeDamage(ENCOUNTER_MISS_DAMAGE, "Mistimed attack on " + outcome.encounterName());
            }
            case HIT -> {
                int remaining = outcome.hitsRemaining();
                log("Hit " + outcome.encounterName() + " (" + Math.max(0, remaining) + " hits remaining).");
                uiSubsystem.setMissionFeedback("Hit " + outcome.encounterName() + " (" + Math.max(0, remaining) + " left)", FEEDBACK_FLASH_SECONDS);
            }
        }
    }

    private void onCombatEncounterCleared(CombatEncounter encounter) {
        if (!clearedCombatEncounterIds.add(encounter.id())) {
            return;
        }
        storyState.setFlag(ENCOUNTER_CLEAR_FLAG_PREFIX + encounter.id());
        refreshDynamicCollisionTiles();
        log("Encounter cleared: " + encounter.displayName() + ".");
        uiSubsystem.setMissionFeedback("Encounter cleared: " + encounter.displayName(), FEEDBACK_FLASH_SECONDS);
        applyCombatEncounterObjectiveProgress(encounter);
        if ("echo_puzzle_sentinel".equals(encounter.id())) {
            checkEchoPuzzle();
        }
    }

    private void checkEchoPuzzle() {
        SimEcho stub = new SimEcho("summit_echo_room_stub", 0, 0f, 0f, null, false, "default", "combat");
        stub.echoKillCount = 1;
        EchoPuzzleEvaluator.Result result = echoPuzzleEvaluator.evaluate(stub, summitEchoPuzzle);
        if (result.passed()) {
            log("PUZZLE_PASSED: " + summitEchoPuzzle.puzzleId());
            uiSubsystem.addEventLogLine("Echo Puzzle: " + summitEchoPuzzle.puzzleId() + " PASSED");
            uiSubsystem.setMissionFeedback("Echo Puzzle Passed!", FEEDBACK_FLASH_SECONDS);
            storyState.setFlag("echo_puzzle_summit_cleared");
        } else {
            log("PUZZLE_FAILED: " + summitEchoPuzzle.puzzleId());
            uiSubsystem.addEventLogLine("Echo Puzzle: " + summitEchoPuzzle.puzzleId() + " FAILED");
        }
    }

    private void onAbilityTriggerActivated(AbilityTrigger trigger) {
        storyState.setFlag(ABILITY_TRIGGER_FLAG_PREFIX + trigger.id());
        refreshDynamicCollisionTiles();
        log("Activated " + trigger.displayName() + ".");
        uiSubsystem.setMissionFeedback("Activated " + trigger.displayName() + ".", FEEDBACK_FLASH_SECONDS);
        applyAbilityTriggerObjectiveProgress(trigger);
    }

    private void takeDamage(int damage, String reason) {
        if (playerDead) {
            return;
        }
        playerHealth = Math.max(0, playerHealth - damage);
        movementMetrics.recordDamageTaken();
        log("Player took " + damage + " damage (" + reason + "). Health: " + playerHealth + "/" + PLAYER_MAX_HEALTH);
        uiSubsystem.setMissionFeedback("Damage: " + reason + " (Health: " + playerHealth + ")", FEEDBACK_FLASH_SECONDS);
        if (playerHealth <= 0) {
            onPlayerDeath();
        }
    }

    private void onPlayerDeath() {
        playerDead = true;
        deathResetTimerSeconds = DEATH_RESET_SECONDS;
        movementMetrics.recordDeath();
        log("Player died. Resetting in " + DEATH_RESET_SECONDS + " seconds.");
        uiSubsystem.setMissionFeedback("Player defeated! Resetting...", FEEDBACK_FLASH_SECONDS);
    }

    private void resetPlayerAfterDeath() {
        playerHealth = PLAYER_MAX_HEALTH;
        playerDead = false;
        playerPhysics.x = PLAYER_SPAWN_X;
        playerPhysics.y = FLOOR_Y;
        playerPhysics.vx = 0f;
        playerPhysics.vy = 0f;
        playerPhysics.onGround = true;
        coyoteTimerSeconds = PhysicsConstants.COYOTE_TIME;
        wallCoyoteTimerSeconds = 0f;
        jumpCount = 0;
        wallJumpLockTimerSeconds = 0f;
        wallStaminaSeconds = PhysicsConstants.MAX_WALL_STAMINA;
        wallExhaustedAwaitGround = false;
        isDashing = false;
        dashTimerSeconds = 0f;
        dashCooldownSeconds = 0f;
        groundedMovingPlatformId = null;
        cameraX = cameraMinX();
        log("Player reset after death.");
        uiSubsystem.setMissionFeedback("Reset complete. Health restored.", FEEDBACK_FLASH_SECONDS);
    }

    private void applyCombatEncounterObjectiveProgress(CombatEncounter encounter) {
        missionUiCoordinator.applyCombatEncounterObjectiveProgress(encounter);
    }

    private void applyAbilityTriggerObjectiveProgress(AbilityTrigger trigger) {
        missionUiCoordinator.applyAbilityTriggerObjectiveProgress(trigger);
    }

    private String buildOverlaySummary() {
        List<String> parts = new ArrayList<>(streamingConstraintWarnings);
        for (RegionInstance r : activeRegions) {
            if (!r.appliedOverrides().isEmpty()) {
                String kinds = r.appliedOverrides().stream()
                        .map(ZoneOverride::overlayKind)
                        .collect(java.util.stream.Collectors.joining(","));
                parts.add(r.regionId() + ":" + kinds);
            }
        }
        return parts.isEmpty() ? "Region Overlays: none" : "Region Overlays: " + String.join(" | ", parts);
    }

    private void refreshOverlayHud() {
        uiSubsystem.setOverlayStatusLine(buildOverlaySummary());
    }

    private void collectStreamingWarnings(List<RegionInstance> regions) {
        streamingConstraintWarnings.clear();
        if (regions.isEmpty()) return;
        List<RegionManifest> manifests = regions.stream()
                .map(RegionInstance::manifest)
                .collect(java.util.stream.Collectors.toList());
        RegionalStreamingConstraintValidator validator = new RegionalStreamingConstraintValidator();
        RegionalStreamingConstraintValidator.ValidationResult result =
                validator.validate(progressionGraph, manifests,
                        new ArrayList<>(storyState.getAbilities()));
        for (RegionalStreamingConstraintValidator.ValidationIssue issue : result.issues()) {
            if ("DISCONNECTED".equals(issue.kind()) || "SOCKET_MISMATCH".equals(issue.kind())) {
                streamingConstraintWarnings.add(
                        "[" + issue.kind() + "] " + issue.regionId() + ": " + issue.message());
            }
        }
    }

    private static WorldProgressionGraph buildPlaytestProgressionGraph() {
        ProgressionNode hub = new ProgressionNode(
                "hub_lantern_heights",
                WorldProgressionGraph.NodeKind.CENTRAL_HUB,
                "lantern",
                List.of(), List.of(),
                List.of("dungeon_forge_terrace_a"),
                List.of(), "low", false);
        ProgressionNode forge = new ProgressionNode(
                "dungeon_forge_terrace_a",
                WorldProgressionGraph.NodeKind.REGION_HUB,
                "lantern",
                List.of(), List.of("dash"),
                List.of("region_hollow_shaft"),
                List.of(), "medium", false);
        ProgressionNode shaft = new ProgressionNode(
                "region_hollow_shaft",
                WorldProgressionGraph.NodeKind.DUNGEON,
                "hollow",
                List.of("dash"), List.of("combat_basic"),
                List.of(),
                List.of(), "high", false);
        return new WorldProgressionGraph(
                WORLD_SEED, hub, List.of(forge), List.of(), List.of(shaft),
                List.of(hub, forge, shaft), "playtest");
    }

    private void initializeCollisionLayout() {
        collisionTiles.clear();
        collisionHash.clear();
        activeRegions.clear();
        traversalSubsystem.clearAll();
        combatSubsystem.clearAll();

        try {
            RegionLoader loader = new RegionLoader(
                    SectionTemplateLibrary.loadDefault(),
                    new RegionalStreamingConstraintValidator());
            activeRegions.addAll(loader.loadNeighborhood(
                    progressionGraph, currentRegionId, 1, WORLD_SEED, savedOverlays));
            collectStreamingWarnings(activeRegions);
            for (RegionInstance r : activeRegions) {
                for (TileRect tile : r.staticTiles()) {
                    collisionTiles.add(tile);
                    collisionHash.insert(tile);
                }
            }
        } catch (RegionLoadException e) {
            System.out.println("[WARN] PlaytestClient: region neighborhood load blocked: " + e.getMessage());
            for (RegionalStreamingConstraintValidator.ValidationIssue issue : e.validationResult().issues()) {
                streamingConstraintWarnings.add(
                        "[" + issue.kind() + "] " + issue.regionId() + ": " + issue.message());
            }
        } catch (Exception e) {
            System.out.println("[WARN] PlaytestClient: region neighborhood load failed: " + e.getMessage());
        }

        // Room 1: Lantern Hub training lanes — static tiles loaded from hub_lantern_heights.json.
        addMovingPlatform("lift_west", 348f, FLOOR_Y - 134f, 130f, 14f, 0f, 62f, 3.25f);
        addAbilityGate("dash_seal", "dash", "Dash Seal", HUB_ROOM_END_X - 26f, CEILING_Y, 24f, FLOOR_Y - CEILING_Y);

        // Room 2: Forge terraces and moving bridge — static tiles loaded from dungeon_forge_terrace_a.json.
        addMovingPlatform("bridge_east", 1512f, FLOOR_Y - 246f, 136f, 14f, 116f, 0f, 4.10f);
        addAbilityTrigger(
                "dash_relay_west",
                "dash",
                "Dash Relay Sigil",
                TriggerMode.DASH_PASS,
                1412f,
                FLOOR_Y - 306f,
                58f,
                58f,
                List.of("dash", "practice", "enter", "climb"));
        addTriggeredPlatform("dash_relay_bridge", "dash_relay_west", 1688f, FLOOR_Y - 286f, 136f, 14f);
        addCombatEncounter(
                "mistwood_beast_echo",
                "",
                "Mistwood Beast Echo",
                1568f,
                FLOOR_Y - 156f,
                92f,
                2,
                ENCOUNTER_DEFAULT_TELEGRAPH_SECONDS + 0.10f,
                ENCOUNTER_DEFAULT_VULNERABLE_SECONDS - 0.05f,
                ENCOUNTER_DEFAULT_RECOVER_SECONDS + 0.05f,
                EncounterPattern.STANDARD,
                List.of("beast", "defeat", "guard", "mistwood"));
        addAbilityGate("combat_ward", "combat_basic", "Combat Ward", FORGE_ROOM_END_X - 24f, CEILING_Y, 20f, FLOOR_Y - CEILING_Y);

        // Rooms 3+4: Hollow shaft + summit — static tiles loaded from region_hollow_shaft.json.
        addAbilityTrigger(
                "combat_forge_altar",
                "combat_basic",
                "Combat Forge Altar",
                TriggerMode.INTERACT,
                2214f,
                FLOOR_Y - 238f,
                64f,
                64f,
                List.of("defeat", "guard", "combat", "restore", "balance"));
        addTriggeredPlatform("combat_forge_lift", "combat_forge_altar", 2482f, FLOOR_Y - 334f, 142f, 14f);
        addCombatEncounter(
                "forge_guardian_echo",
                "combat_basic",
                "Forge Guardian Echo",
                2334f,
                FLOOR_Y - 196f,
                104f,
                3,
                ENCOUNTER_DEFAULT_TELEGRAPH_SECONDS - 0.10f,
                ENCOUNTER_DEFAULT_VULNERABLE_SECONDS - 0.15f,
                ENCOUNTER_DEFAULT_RECOVER_SECONDS - 0.10f,
                EncounterPattern.FAST,
                List.of("defeat", "guard", "combat", "restore", "balance"));
        addCombatBarrier(
                "summit_combat_barrier",
                "forge_guardian_echo",
                "Summit Combat Seal",
                SHAFT_ROOM_END_X - 24f,
                CEILING_Y,
                22f,
                FLOOR_Y - CEILING_Y);

        // Echo puzzle room — summit shrine. One echo kill required to open the passage.
        addCombatEncounter(
                "echo_puzzle_sentinel",
                "combat_basic",
                "Echo Puzzle Sentinel",
                2750f,
                FLOOR_Y - 156f,
                80f,
                1,
                ENCOUNTER_DEFAULT_TELEGRAPH_SECONDS,
                ENCOUNTER_DEFAULT_VULNERABLE_SECONDS,
                ENCOUNTER_DEFAULT_RECOVER_SECONDS,
                EncounterPattern.STANDARD,
                List.of("echo", "puzzle", "sentinel", "summit"));
    }

    private void addMovingPlatform(
            String id,
            float originX,
            float originY,
            float width,
            float height,
            float travelX,
            float travelY,
            float periodSeconds) {
        traversalSubsystem.addMovingPlatform(id, originX, originY, width, height, travelX, travelY, periodSeconds);
    }

    private void addAbilityGate(
            String id,
            String requiredAbility,
            String displayName,
            float x,
            float y,
            float width,
            float height) {
        traversalSubsystem.addAbilityGate(id, requiredAbility, displayName, x, y, width, height);
    }

    private void addAbilityTrigger(
            String id,
            String requiredAbility,
            String displayName,
            TriggerMode mode,
            float x,
            float y,
            float width,
            float height,
            List<String> objectiveKeywords) {
        traversalSubsystem.addAbilityTrigger(id, requiredAbility, displayName, mode, x, y, width, height, objectiveKeywords);
    }

    private void addTriggeredPlatform(
            String id,
            String requiredTriggerId,
            float x,
            float y,
            float width,
            float height) {
        traversalSubsystem.addTriggeredPlatform(id, requiredTriggerId, x, y, width, height);
    }

    private void addCombatEncounter(
            String id,
            String requiredAbility,
            String displayName,
            float centerX,
            float centerY,
            float activationRadius,
            int requiredHits,
            float telegraphSeconds,
            float vulnerableSeconds,
            float recoverSeconds,
            EncounterPattern pattern,
            List<String> objectiveKeywords) {
        combatSubsystem.addEncounter(
                id,
                requiredAbility,
                displayName,
                centerX,
                centerY,
                activationRadius,
                requiredHits,
                telegraphSeconds,
                vulnerableSeconds,
                recoverSeconds,
                pattern,
                objectiveKeywords);
    }

    private void addCombatBarrier(
            String id,
            String requiredEncounterId,
            String displayName,
            float x,
            float y,
            float width,
            float height) {
        traversalSubsystem.addCombatBarrier(id, requiredEncounterId, displayName, x, y, width, height);
    }

    private void updateWallStamina(float dt) {
        if (playerPhysics.onGround) {
            wallStaminaSeconds = PhysicsConstants.MAX_WALL_STAMINA;
            wallExhaustedAwaitGround = false;
            return;
        }

        if (playerPhysics.onWall && !playerPhysics.onGround) {
            if (!wallExhaustedAwaitGround) {
                wallStaminaSeconds = Math.max(
                        0f,
                        wallStaminaSeconds - (PhysicsConstants.STAMINA_REGEN_RATE * WALL_STAMINA_DRAIN_MULT * dt));
                if (wallStaminaSeconds <= PhysicsConstants.EXHAUST_THRESHOLD) {
                    wallExhaustedAwaitGround = true;
                    wallCoyoteTimerSeconds = 0f;
                    movementMetrics.recordWallExhaustion();
                }
            } else {
                // Exhaustion penalty: lose controlled cling and slide faster until touching ground.
                playerPhysics.vy = Math.max(playerPhysics.vy, PhysicsConstants.WALL_FRICTION_CLAMP * PhysicsConstants.EXHAUST_PENALTY);
            }
            return;
        }

        if (!wallExhaustedAwaitGround) {
            wallStaminaSeconds = Math.min(
                    PhysicsConstants.MAX_WALL_STAMINA,
                    wallStaminaSeconds + (PhysicsConstants.STAMINA_REGEN_RATE * WALL_STAMINA_AIR_REGEN_MULT * dt));
        }
    }

    private List<NPC> activeNpcsSorted() {
        List<NPC> active = new ArrayList<>();
        for (NPC npc : storyState.getAllNPCs().values()) {
            if (npc.isActive()) {
                active.add(npc);
            }
        }
        active.sort(Comparator.comparing(NPC::getId));
        return active;
    }

    private void startNextAvailableMission() {
        if (storyState.getActiveMissionId() != null) {
            log("Mission already active: " + storyState.getActiveMissionId());
            return;
        }

        List<Mission> available = missionManager.getAvailableMissions().stream()
                .sorted(Comparator.comparing(Mission::getId))
                .toList();

        if (available.isEmpty()) {
            log("No available missions right now.");
            return;
        }

        Mission selected = available.get(0);
        boolean started = missionManager.startMission(selected.getId());
        if (started) {
            log("Started mission: " + selected.getDisplayName());
            uiSubsystem.setMissionFeedback("Mission started: " + selected.getDisplayName(), FEEDBACK_FLASH_SECONDS);
        } else {
            log("Failed to start mission: " + selected.getId());
            uiSubsystem.setMissionFeedback("Mission start failed: " + selected.getId(), FEEDBACK_FLASH_SECONDS);
        }

        storyManager.refreshFromStoryChange();
    }

    private void interactNearestNpc() {
        NPC nearest = null;
        float nearestDistance = Float.MAX_VALUE;
        Map<String, Point2D.Float> npcPositions = uiSubsystem.getNpcPositions();

        for (NPC npc : activeNpcsSorted()) {
            Point2D.Float point = npcPositions.get(npc.getId());
            if (point == null) {
                continue;
            }
            float distance = distance(point.x, point.y, playerPhysics.x, playerPhysics.y);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = npc;
            }
        }

        if (nearest == null || nearestDistance > INTERACT_RADIUS) {
            // Check for hub shop fixture before falling through to traversal triggers
            if (Math.abs(playerPhysics.x - SHOP_NPC_X) <= INTERACT_RADIUS && !anyPanelOpen()) {
                inventoryPanel.close();
                craftingPanel.close();
                shopPanel.open(hubShop, playerInventory);
                uiSubsystem.addEventLogLine("Shop: merchant_npc opened.");
                writeEvidenceLine("OVERLAY", "shop_open npcId=merchant_npc");
                return;
            }
            TraversalSubsystem.InteractOutcome outcome = traversalSubsystem.tryInteract(
                    playerPhysics.x, playerPhysics.y, INTERACT_RADIUS + 18f, storyState::hasAbility);
            switch (outcome.type()) {
                case ACTIVATED -> {}
                case ALREADY_ACTIVE -> log(outcome.triggerName() + " is already active.");
                case ABILITY_LOCKED -> {
                    log(outcome.triggerName() + " is inert. Requires ability `" + outcome.requiredAbility() + "`.");
                    uiSubsystem.setMissionFeedback("Ability required: " + outcome.requiredAbility(), FEEDBACK_FLASH_SECONDS);
                }
                case NO_TRIGGER -> log("No NPC nearby to interact with.");
            }
            return;
        }

        String dialogue = hubManager.getNPCDialogue(nearest.getId());
        if (dialogue.isBlank()) {
            dialogue = "...";
        }
        log(nearest.getDisplayName() + ": " + dialogue);

        applyNpcObjectiveProgress(nearest.getId());
        storyManager.refreshFromStoryChange();
    }

    private void applyNpcObjectiveProgress(String npcId) {
        String missionId = storyState.getActiveMissionId();
        if (missionId == null) {
            return;
        }

        switch (missionId) {
            case "village_bonds" -> {
                Map<String, String> objectiveByNpc = Map.of(
                        "SAMSON", "talk_to_samson",
                        "SOPHIA", "talk_to_sophia",
                        "MARCEL", "talk_to_marcel",
                        "HAZEL", "talk_to_hazel");
                String objective = objectiveByNpc.get(npcId);
                if (objective != null) {
                    advanceObjective(missionId, objective, 1);
                }
            }
            case "dojo_practice" -> {
                if ("INSTRUCTOR_TAI".equals(npcId) || "TAI".equals(npcId)) {
                    if (!isObjectiveComplete(missionId, "practice_forms")) {
                        advanceObjective(missionId, "practice_forms", 1);
                    } else {
                        advanceObjective(missionId, "learn_dash", 1);
                    }
                }
            }
            case "veil_request" -> {
                if ("VEIL_MAIDEN".equals(npcId) || "LINZI".equals(npcId)) {
                    if (!isObjectiveComplete(missionId, "hear_veil_offer")) {
                        advanceObjective(missionId, "hear_veil_offer", 1);
                    } else {
                        advanceObjective(missionId, "accept_or_decline", 1);
                    }
                }
            }
            default -> {
                // For contract-generated side quests, social interaction can resolve step-style objectives.
                Mission mission = storyState.getMission(missionId);
                if (mission == null) {
                    return;
                }
                for (String objective : mission.getObjectives()) {
                    String normalized = objective.toLowerCase(Locale.ROOT);
                    if ((normalized.contains("resolve") || normalized.contains("read") || normalized.contains("follow"))
                            && !mission.isObjectiveComplete(objective)) {
                        advanceObjective(missionId, objective, 1);
                        break;
                    }
                }
            }
        }
    }

    private void resolveActiveObjectiveShortcut() {
        String missionId = storyState.getActiveMissionId();
        if (missionId == null) {
            log("No active mission to resolve.");
            return;
        }

        switch (missionId) {
            case "mistwood_beast" -> advanceObjective(missionId, "defeat_beast", 1);
            case "hollow_descent" -> {
                if (!isObjectiveComplete(missionId, "enter_hollow_depths")) {
                    advanceObjective(missionId, "enter_hollow_depths", 1);
                } else {
                    advanceObjective(missionId, "find_veil_source", 1);
                }
            }
            case "lantern_restoration" -> {
                if (!isObjectiveComplete(missionId, "collect_lantern_pieces")) {
                    advanceObjective(missionId, "collect_lantern_pieces", 1);
                } else {
                    advanceObjective(missionId, "restore_lanterns", 1);
                }
            }
            case "monastery_arrival" -> {
                if (!isObjectiveComplete(missionId, "climb_to_monastery")) {
                    advanceObjective(missionId, "climb_to_monastery", 1);
                } else {
                    advanceObjective(missionId, "enter_monastery", 1);
                }
            }
            case "yin_yang_balance" -> {
                if (!isObjectiveComplete(missionId, "understand_yin_yang")) {
                    advanceObjective(missionId, "understand_yin_yang", 1);
                } else {
                    advanceObjective(missionId, "achieve_balance", 1);
                }
            }
            default -> resolveFirstPendingObjective(missionId);
        }

        storyManager.refreshFromStoryChange();
    }

    private void resolveFirstPendingObjective(String missionId) {
        Mission mission = storyState.getMission(missionId);
        if (mission == null) {
            return;
        }
        for (String objective : mission.getObjectives()) {
            if (!mission.isObjectiveComplete(objective)) {
                advanceObjective(missionId, objective, 1);
                return;
            }
        }
        log("All objectives already complete for " + missionId + ".");
    }

    private boolean isObjectiveComplete(String missionId, String objectiveId) {
        Mission mission = storyState.getMission(missionId);
        return mission != null && mission.isObjectiveComplete(objectiveId);
    }

    private void advanceObjective(String missionId, String objectiveId, int amount) {
        missionUiCoordinator.advanceObjective(missionId, objectiveId, amount);
    }

    private boolean anyPanelOpen() {
        return inventoryPanel.isVisible() || shopPanel.isVisible() || craftingPanel.isVisible();
    }

    private void processPanelInputs() {
        if (queueInventoryToggle) {
            queueInventoryToggle = false;
            shopPanel.close();
            craftingPanel.close();
            inventoryPanel.toggle();
            if (inventoryPanel.isVisible()) writeEvidenceLine("OVERLAY", "inventory_open");
        }
        if (queueCraftToggle) {
            queueCraftToggle = false;
            shopPanel.close();
            inventoryPanel.close();
            if (craftingPanel.isVisible()) {
                craftingPanel.close();
            } else {
                craftingPanel.open(playerInventory);
                writeEvidenceLine("OVERLAY", "crafting_open");
            }
        }
        if (queuePanelClose) {
            queuePanelClose = false;
            inventoryPanel.close();
            shopPanel.close();
            craftingPanel.close();
        }
        if (queuePanelUp) {
            queuePanelUp = false;
            if (inventoryPanel.isVisible()) inventoryPanel.moveUp();
            else if (shopPanel.isVisible())  shopPanel.moveUp();
            else if (craftingPanel.isVisible()) craftingPanel.moveUp();
        }
        if (queuePanelDown) {
            queuePanelDown = false;
            if (inventoryPanel.isVisible()) inventoryPanel.moveDown();
            else if (shopPanel.isVisible())  shopPanel.moveDown();
            else if (craftingPanel.isVisible()) craftingPanel.moveDown();
        }
        if (queuePanelLeft) {
            queuePanelLeft = false;
            if (inventoryPanel.isVisible()) inventoryPanel.moveLeft();
            else if (shopPanel.isVisible())  shopPanel.toggleFocus();
        }
        if (queuePanelRight) {
            queuePanelRight = false;
            if (inventoryPanel.isVisible()) inventoryPanel.moveRight();
            else if (shopPanel.isVisible())  shopPanel.toggleFocus();
        }
        if (queuePanelAction) {
            queuePanelAction = false;
            if (inventoryPanel.isVisible()) {
                String feedback = inventoryPanel.useSelected();
                if (feedback != null) {
                    uiSubsystem.addEventLogLine("Inventory: " + feedback);
                    writeEvidenceLine("OVERLAY", "inventory_action=" + feedback);
                }
            } else if (shopPanel.isVisible()) {
                ShopPanel.TradeRequest req = shopPanel.performAction();
                if (req != null) {
                    String dir = req.isBuy() ? "bought" : "sold";
                    uiSubsystem.addEventLogLine("Shop: " + dir + " " + req.itemId());
                    writeEvidenceLine("OVERLAY", "shop_trade=" + dir + ":" + req.itemId());
                }
            } else if (craftingPanel.isVisible()) {
                String feedback = craftingPanel.craftSelected();
                if (feedback != null) {
                    uiSubsystem.addEventLogLine("Craft: " + feedback);
                    writeEvidenceLine("OVERLAY", "craft_action=" + feedback);
                }
            }
        }
    }

    private void saveState() {
        try {
            MutationOverlay overlay = new MutationOverlay();
            String overlaysB64 = OverlayPayloadCodec.encodeToB64(
                    overlay.extractSaveState(activeRegions));
            gameState.save(savePath, overlaysB64);
            writeEvidenceLine("MUTATION_OVERLAY_SAVE", buildOverlaySummary());
            log("Saved playtest state to " + savePath.toAbsolutePath());
        } catch (Exception ex) {
            log("Save failed: " + ex.getMessage());
        }
    }

    private void loadState() {
        try {
            gameState.load(savePath);
            String overlaysB64 = gameState.loadOverlaysB64(savePath);
            savedOverlays = OverlayPayloadCodec.decodeFromB64(overlaysB64);
            activeRegions.clear();
            streamingConstraintWarnings.clear();
            try {
                RegionLoader loader = new RegionLoader(
                        SectionTemplateLibrary.loadDefault(),
                        new RegionalStreamingConstraintValidator());
                activeRegions.addAll(loader.loadNeighborhood(
                        progressionGraph, currentRegionId, 1, WORLD_SEED, savedOverlays));
                collectStreamingWarnings(activeRegions);
            } catch (RegionLoadException regionEx) {
                System.out.println("[WARN] PlaytestClient: region reload blocked: "
                        + regionEx.getMessage());
                for (RegionalStreamingConstraintValidator.ValidationIssue issue
                        : regionEx.validationResult().issues()) {
                    streamingConstraintWarnings.add(
                            "[" + issue.kind() + "] " + issue.regionId() + ": " + issue.message());
                }
            } catch (Exception regionEx) {
                System.out.println("[WARN] PlaytestClient: region reload after load failed: "
                        + regionEx.getMessage());
            }
            refreshCollisionHashFromRegions();
            storyManager.refreshFromStoryChange();
            restoreActivatedAbilityTriggersFromFlags();
            restoreClearedCombatEncountersFromFlags();
            refreshDynamicCollisionTiles();
            uiSubsystem.seedAbilitySnapshot(storyState.getAbilities());
            refreshOverlayHud();
            writeEvidenceLine("MUTATION_OVERLAY_LOAD", buildOverlaySummary());
            cameraX = clamp(playerPhysics.x - (WINDOW_WIDTH * 0.5f), cameraMinX(), cameraMaxX());
            log("Loaded playtest state from " + savePath.toAbsolutePath());
        } catch (Exception ex) {
            log("Load failed: " + ex.getMessage());
        }
    }

    private void initializeEvidenceLogging() {
        try {
            Files.createDirectories(evidenceDirPath);
            String fileName = "playtest_session_" + EVIDENCE_FILE_TS.format(Instant.now()) + ".log";
            evidenceLogPath = evidenceDirPath.resolve(fileName);
            evidenceWriter = Files.newBufferedWriter(
                    evidenceLogPath,
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE);
            writeEvidenceLine("SESSION_START", "session_created=true file=" + evidenceLogPath.toAbsolutePath());
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownEvidenceLogging));
        } catch (IOException ex) {
            evidenceWriter = null;
            evidenceLogPath = null;
        }
    }

    private void shutdownEvidenceLogging() {
        if (evidenceWriter == null) {
            return;
        }
        try {
            writeEvidenceLine("MOVEMENT_SIGNOFF", movementMetrics.finalSummary(sessionElapsedSeconds));
            writeEvidenceLine("SESSION_END", "session_elapsed_seconds=" + String.format(Locale.ROOT, "%.2f", sessionElapsedSeconds));
            evidenceWriter.flush();
            evidenceWriter.close();
        } catch (IOException ignored) {
            // best effort on shutdown
        } finally {
            evidenceWriter = null;
        }
    }

    private void writeEvidenceLine(String kind, String message) {
        if (evidenceWriter == null) {
            return;
        }
        String safeKind = (kind == null || kind.isBlank()) ? "EVENT" : kind;
        String safeMessage = message == null ? "" : message.replace('\n', ' ').replace('\r', ' ');
        String act = storyState == null ? "UNKNOWN" : String.valueOf(storyState.getCurrentAct());
        String plateau = storyState == null ? "UNKNOWN" : String.valueOf(storyState.getCurrentPlateau());
        String mission = storyState == null || storyState.getActiveMissionId() == null
                ? "none"
                : storyState.getActiveMissionId();
        String line = String.format(
                Locale.ROOT,
                "%s | +%.2fs | %s | act=%s | plateau=%s | mission=%s | pos=(%.1f,%.1f) | %s",
                EVIDENCE_EVENT_TS.format(Instant.now()),
                sessionElapsedSeconds,
                safeKind,
                act,
                plateau,
                mission,
                playerPhysics.x,
                playerPhysics.y,
                safeMessage);
        try {
            evidenceWriter.write(line);
            evidenceWriter.newLine();
            evidenceWriter.flush();
        } catch (IOException ignored) {
            // best effort logging; do not interrupt runtime
        }
    }

    private void emitPeriodicSnapshotIfDue() {
        if (sessionElapsedSeconds < nextSnapshotSeconds) {
            return;
        }
        String summary = String.format(
                Locale.ROOT,
                "hub=%s available_missions=%d abilities=%d encounters=%d/%d movement={%s}",
                storyState.getCurrentHubState(),
                missionManager.getAvailableMissions().size(),
                storyState.getAbilities().size(),
                clearedCombatEncounterIds.size(),
                combatSubsystem.allEncounters().size(),
                movementMetrics.snapshotSummary(sessionElapsedSeconds));
        writeEvidenceLine("SNAPSHOT", summary);
        nextSnapshotSeconds += SNAPSHOT_INTERVAL_SECONDS;
    }

    private void log(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        uiSubsystem.addEventLogLine(message);
    }

    private static final class MovementSessionMetrics {
        private float totalDistance;
        private float horizontalDistance;
        private float verticalDistance;
        private float maxAbsHorizontalSpeed;
        private float maxAbsVerticalSpeed;
        private float airborneSeconds;
        private float groundedSeconds;
        private float wallContactSeconds;
        private int groundJumpCount;
        private int wallJumpCount;
        private int doubleJumpCount;
        private int dashCount;
        private int wallExhaustCount;
        private int damageEvents;
        private int deathCount;

        private void recordFrame(float dt, float previousX, float previousY, PhysicsState physics) {
            if (physics == null || dt <= 0f) {
                return;
            }
            float dx = physics.x - previousX;
            float dy = physics.y - previousY;
            horizontalDistance += Math.abs(dx);
            verticalDistance += Math.abs(dy);
            totalDistance += (float) Math.sqrt((dx * dx) + (dy * dy));
            maxAbsHorizontalSpeed = Math.max(maxAbsHorizontalSpeed, Math.abs(physics.vx));
            maxAbsVerticalSpeed = Math.max(maxAbsVerticalSpeed, Math.abs(physics.vy));
            if (physics.onGround) {
                groundedSeconds += dt;
            } else {
                airborneSeconds += dt;
            }
            if (physics.onWall && !physics.onGround) {
                wallContactSeconds += dt;
            }
        }

        private void recordGroundJump() {
            groundJumpCount++;
        }

        private void recordWallJump() {
            wallJumpCount++;
        }

        private void recordDoubleJump() {
            doubleJumpCount++;
        }

        private void recordDashStart() {
            dashCount++;
        }

        private void recordWallExhaustion() {
            wallExhaustCount++;
        }

        private void recordDamageTaken() {
            damageEvents++;
        }

        private void recordDeath() {
            deathCount++;
        }

        private String snapshotSummary(float elapsedSeconds) {
            float elapsed = Math.max(0.001f, elapsedSeconds);
            float avgHorizontalSpeed = horizontalDistance / elapsed;
            return String.format(
                    Locale.ROOT,
                    "dist=%.1f avg_h=%.2f jumps=%d/%d/%d dashes=%d wall_exhaust=%d",
                    totalDistance,
                    avgHorizontalSpeed,
                    groundJumpCount,
                    wallJumpCount,
                    doubleJumpCount,
                    dashCount,
                    wallExhaustCount);
        }

        private String finalSummary(float elapsedSeconds) {
            float elapsed = Math.max(0.001f, elapsedSeconds);
            float avgHorizontalSpeed = horizontalDistance / elapsed;
            float avgTotalSpeed = totalDistance / elapsed;
            float airborneRatio = airborneSeconds / elapsed;
            float groundedRatio = groundedSeconds / elapsed;
            return String.format(
                    Locale.ROOT,
                    "session_seconds=%.2f total_distance=%.1f horizontal_distance=%.1f vertical_distance=%.1f "
                            + "avg_horizontal_speed=%.2f avg_total_speed=%.2f peak_abs_vx=%.2f peak_abs_vy=%.2f "
                            + "airborne_ratio=%.3f grounded_ratio=%.3f wall_contact_seconds=%.2f "
                            + "jumps_ground=%d jumps_wall=%d jumps_double=%d dashes=%d wall_exhaust_events=%d "
                            + "damage_events=%d deaths=%d",
                    elapsedSeconds,
                    totalDistance,
                    horizontalDistance,
                    verticalDistance,
                    avgHorizontalSpeed,
                    avgTotalSpeed,
                    maxAbsHorizontalSpeed,
                    maxAbsVerticalSpeed,
                    airborneRatio,
                    groundedRatio,
                    wallContactSeconds,
                    groundJumpCount,
                    wallJumpCount,
                    doubleJumpCount,
                    dashCount,
                    wallExhaustCount,
                    damageEvents,
                    deathCount);
        }
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float distance(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
