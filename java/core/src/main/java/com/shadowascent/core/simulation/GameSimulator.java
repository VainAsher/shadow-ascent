package com.shadowascent.core.simulation;

import com.shadowascent.core.physics.CollisionWorld;
import com.shadowascent.core.physics.PhysicsConstants;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Server-authoritative simulation coordinator.
 *
 * Bounded Wave 4 slice: entity registry + tick coordinator + event drain.
 * Wires SimPlayer/SimEnemy/SimBoss/SimNPC/SimPickup and PlayerInputController
 * into a coherent per-tick loop. Does NOT port the full donor GameSimulator monolith
 * (XL / High risk) — this is the first bounded slice of that decomposition.
 */
public final class GameSimulator {

    // ── Enemy type default stat table ─────────────────────────────────────────

    private record EnemyDefaults(int w, int h, int maxHp, int dmg,
                                 float speed, float detect, float atkRange, float patrolHalfRange) {}

    private static final Map<String, EnemyDefaults> ENEMY_DEFAULTS;
    static {
        Map<String, EnemyDefaults> m = new LinkedHashMap<>();
        m.put("goblin",   new EnemyDefaults(32, 56, 3, 1,  60f, 180f,  48f, 128f));
        m.put("bat",      new EnemyDefaults(28, 24, 2, 1,  80f, 200f,  40f, 160f));
        m.put("slime",    new EnemyDefaults(40, 36, 5, 1,  30f, 120f,  40f,  80f));
        m.put("skeleton", new EnemyDefaults(32, 64, 4, 2,  50f, 200f,  56f, 128f));
        m.put("wolf",     new EnemyDefaults(48, 44, 4, 2, 100f, 240f,  72f, 192f));
        ENEMY_DEFAULTS = Collections.unmodifiableMap(m);
    }

    private static final EnemyDefaults DEFAULT_ENEMY =
            new EnemyDefaults(32, 56, 3, 1, 60f, 180f, 48f, 128f);

    private static final float ENEMY_ATTACK_COOLDOWN =
            SimEnemy.ATTACK_WINDUP_TIME + SimEnemy.ATTACK_ACTIVE_TIME + SimEnemy.ATTACK_RECOVERY_TIME;
    private static final float RESPAWN_DELAY  = 3.0f;
    private static final float REVIVE_RANGE   = 80f;
    // Stub floor — P2 replaces with CollisionWorld AABB resolution

    // ── State ─────────────────────────────────────────────────────────────────

    private final Map<String, SimPlayer>  players        = new LinkedHashMap<>();
    private final List<SimEnemy>          enemies        = new ArrayList<>();
    private final List<SimBoss>           bosses         = new ArrayList<>();
    private final List<SimNPC>            npcs           = new ArrayList<>();
    private final List<SimPickup>         pickups        = new ArrayList<>();
    private final List<SimShuriken>       shurikens       = new ArrayList<>();
    private       int                     shurikenCounter = 0;
    private final List<SimMovingPlatform> movingPlatforms = new ArrayList<>();
    private final List<SimPortal>         portals         = new ArrayList<>();
    private final List<SimEcho>           echoes          = new ArrayList<>();
    private final PlayerInputController   inputController = new PlayerInputController();
    private final List<SimEvent>          eventQueue      = new ArrayList<>();
    private       CollisionWorld          collisionWorld  = null;

    // ── CollisionWorld injection ──────────────────────────────────────────────

    /** Inject tile geometry so tickPlayers() uses real AABB resolution instead of spawnY stub. */
    public void setCollisionWorld(CollisionWorld world) {
        this.collisionWorld = world;
    }

    // ── Entity registration ───────────────────────────────────────────────────

    public void addPlayer(String playerId, int slot, float x, float y) {
        SimPlayer p = new SimPlayer(playerId, slot, x, y);
        p.physics.onGround = true;
        players.put(playerId, p);
        emit("PLAYER_JOINED", playerId, Map.of("slot", slot, "x", x, "y", y));
    }

    public void removePlayer(String playerId) {
        if (players.remove(playerId) != null) {
            emit("PLAYER_LEFT", playerId);
        }
    }

    public void addEnemy(String id, String type, float x, float y) {
        EnemyDefaults d = ENEMY_DEFAULTS.getOrDefault(type, DEFAULT_ENEMY);
        SimEnemy e = new SimEnemy(id, type, x, y, d.w(), d.h(),
                d.maxHp(), d.dmg(), d.speed(), d.detect(), d.atkRange(),
                x - d.patrolHalfRange(), x + d.patrolHalfRange(), "bat".equals(type));
        enemies.add(e);
    }

    public void addBoss(String bossId, BossType type, float x, float y) {
        bosses.add(new SimBoss(bossId, type, x, y));
    }

    public void addNpc(String npcId, String npcType, float x, float y,
                       float patrolMin, float patrolMax) {
        npcs.add(new SimNPC(npcId, npcType, x, y,
                SimNPC.DEFAULT_WIDTH, SimNPC.DEFAULT_HEIGHT, patrolMin, patrolMax));
    }

    public void addPickup(String id, String pickupType, float x, float y) {
        pickups.add(new SimPickup(id, pickupType, x, y));
    }

    public void addMovingPlatform(String id, float x, float y,
                                   float width, float height,
                                   float leftBound, float rightBound, float speed) {
        movingPlatforms.add(new SimMovingPlatform(id, x, y, width, height,
                                                  leftBound, rightBound, speed));
    }

    public void addPortal(String id, String type, String destId,
                          float x, float y, String requiredAbility) {
        portals.add(new SimPortal(id, type, destId, x, y, requiredAbility));
    }

    public void fireShuriken(String playerId, float vx, float vy) {
        SimPlayer p = players.get(playerId);
        if (p == null || !p.isAlive()) return;
        String id = "shr_" + shurikenCounter++;
        float cx = p.physics.x + p.physics.width  * 0.5f;
        float cy = p.physics.y + p.physics.height * 0.5f;
        shurikens.add(new SimShuriken(id, p.slot, cx, cy, vx, vy));
        emit("SHURIKEN_FIRED", id, Map.of("playerId", playerId, "vx", (double) vx, "vy", (double) vy));
    }

    public void attackEnemy(String playerId, String enemyId, int damage) {
        SimPlayer attacker = players.get(playerId);
        if (attacker == null || !attacker.isAlive()) return;
        SimEnemy target = findEnemy(enemyId);
        if (target == null) return;
        boolean died = target.takeDamage(damage);
        emit("ENEMY_DAMAGED", enemyId, Map.of("source", "player_melee", "playerId", playerId, "dmg", damage));
        if (died) {
            emit("ENEMY_DEFEATED", enemyId, Map.of("killedBy", playerId));
        }
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    public void applyInput(String playerId, InputCommand cmd) {
        SimPlayer p = players.get(playerId);
        if (p == null) return;
        inputController.apply(p, cmd);
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    public void tick(float dt) {
        tickPlayers(dt);
        for (SimEnemy e : enemies) tickEnemy(e, dt);
        for (SimBoss b : bosses)   tickBoss(b, dt);
        float nearestX = nearestPlayerX();
        for (SimNPC n : npcs) n.step(nearestX, false);
        tickPickups();
        tickShurikens(dt);
        tickMovingPlatforms();
        tickPortals(dt);
        tickEchoes();
        tickCoopProximity();
        tickCoopCollisions();
    }

    private void tickPlayers(float dt) {
        for (SimPlayer p : players.values()) {
            if (p.invincibilityTicks > 0) p.invincibilityTicks--;
            p.echoRecorder.record(p.latestInput);
            if (p.respawnTimer > 0f) {
                p.respawnTimer -= dt;
                if (p.respawnTimer <= 0f) {
                    p.respawnTimer = -1f;
                    p.isDead = false;
                    p.health = p.maxHealth;
                    p.physics.x = p.spawnX;
                    p.physics.y = p.spawnY;
                    p.physics.vy = 0f;
                    p.physics.onGround = true;
                }
            }
            if (!p.isDead) {
                if (!p.physics.gravityFrozen) {
                    p.physics.vy += PhysicsConstants.GRAVITY;
                }
                float prevX = p.physics.x;
                p.physics.x += p.physics.vx;
                if (collisionWorld != null) {
                    collisionWorld.resolveX(p.physics, prevX);
                }
                float prevY = p.physics.y;
                p.physics.y += p.physics.vy;
                if (collisionWorld != null) {
                    collisionWorld.resolveY(p.physics, prevY);
                } else {
                    // Stub floor at spawn Y — replaced by CollisionWorld when injected
                    if (p.physics.y >= p.spawnY) {
                        p.physics.y        = p.spawnY;
                        p.physics.vy       = 0f;
                        p.physics.onGround = true;
                    } else {
                        p.physics.onGround = false;
                    }
                }
                resolvePlayerMeleeAttack(p);
            }
        }
    }

    private void resolvePlayerMeleeAttack(SimPlayer player) {
        if (!player.isAttacking || player.meleeHitConsumed) {
            return;
        }

        float hitboxX;
        float hitboxY;
        float hitboxWidth;
        float hitboxHeight;
        if (player.attackAimY < 0) {
            hitboxWidth = SimPlayer.MELEE_HEIGHT;
            hitboxHeight = SimPlayer.MELEE_REACH;
            hitboxX = player.physics.x + (player.physics.width - hitboxWidth) * 0.5f;
            hitboxY = player.physics.y - hitboxHeight;
        } else if (player.attackAimY > 0) {
            hitboxWidth = SimPlayer.MELEE_REACH;
            hitboxHeight = SimPlayer.MELEE_HEIGHT;
            hitboxX = player.attackAimX >= 0
                    ? player.physics.x + player.physics.width * 0.35f
                    : player.physics.x - hitboxWidth + player.physics.width * 0.65f;
            hitboxY = player.physics.y + player.physics.height - hitboxHeight * 0.75f;
        } else {
            hitboxWidth = SimPlayer.MELEE_REACH;
            hitboxHeight = SimPlayer.MELEE_HEIGHT;
            hitboxX = player.attackAimX >= 0
                    ? player.physics.x + player.physics.width
                    : player.physics.x - hitboxWidth;
            hitboxY = player.physics.y + Math.max(0f, (player.physics.height - hitboxHeight) * 0.5f);
        }
        int damage = SimPlayer.MELEE_DAMAGE + Math.max(0, player.inventory.totalAttackBonus());

        for (SimEnemy enemy : enemies) {
            if (!enemy.isAlive()) {
                continue;
            }
            if (!overlaps(hitboxX, hitboxY, hitboxWidth, hitboxHeight,
                    enemy.physics.x, enemy.physics.y, enemy.physics.width, enemy.physics.height)) {
                continue;
            }

            player.meleeHitConsumed = true;
            boolean died = enemy.takeDamage(damage);
            emit("ENEMY_DAMAGED", enemy.enemyId,
                    Map.of("source", "player_melee", "playerId", player.playerId, "dmg", damage));
            emit("PLAYER_MELEE_HIT", player.playerId,
                    Map.of("enemyId", enemy.enemyId, "dmg", damage, "comboStep", player.comboStep));
            if (died) {
                emit("ENEMY_DEFEATED", enemy.enemyId, Map.of("killedBy", player.playerId));
            }
            break;
        }
    }

    private void tickEnemy(SimEnemy e, float dt) {
        if (!e.isAlive()) return;

        // Stun/flee override
        if (e.stunTimer > 0f) {
            e.stunTimer = Math.max(0f, e.stunTimer - dt);
            if (e.stunTimer <= 0f) e.aiState = EnemyAIState.PATROL;
            return;
        }
        if (e.fleeTimer > 0f) {
            e.fleeTimer = Math.max(0f, e.fleeTimer - dt);
            e.aiState = EnemyAIState.FLEE;
            return;
        }

        // Find nearest alive player
        SimPlayer nearest = nearestAlivePlayer(e.physics.x, e.physics.y);
        if (nearest == null) {
            e.aiState = EnemyAIState.PATROL;
            return;
        }

        float dist = e.distanceTo(nearest.physics.x, nearest.physics.y);

        // Awareness escalation
        if (dist <= e.detectionRadius && e.awarenessState == EnemyAwarenessState.UNAWARE) {
            e.awarenessState = EnemyAwarenessState.ALERTED;
            emit("ENEMY_AGGRO", e.enemyId,
                    Map.of("target", nearest.playerId, "dist", (double) dist));
        }

        if (e.awarenessState != EnemyAwarenessState.UNAWARE) {
            if (dist <= e.attackRange) {
                e.aiState = EnemyAIState.ATTACK;
                e.attackTimer = Math.max(0f, e.attackTimer - dt);
                if (e.attackTimer <= 0f && nearest.isAlive()) {
                    int hpBefore = nearest.health;
                    nearest.takeDamage(e.baseDamage);
                    if (nearest.health < hpBefore) {
                        emit("PLAYER_DAMAGED", nearest.playerId,
                                Map.of("hp", nearest.health,
                                       "dmg", hpBefore - nearest.health,
                                       "byEnemy", e.enemyId));
                        if (nearest.isDead) {
                            nearest.respawnTimer = RESPAWN_DELAY;
                            emit("PLAYER_DIED", nearest.playerId,
                                    Map.of("byEnemy", e.enemyId));
                        }
                    }
                    e.attackTimer = ENEMY_ATTACK_COOLDOWN;
                }
            } else {
                e.aiState = EnemyAIState.CHASE;
                float dir = nearest.physics.x > e.physics.x ? 1f : -1f;
                e.physics.x += dir * e.moveSpeed * dt;
                e.facingRight = dir > 0;
            }
        } else {
            // Patrol oscillation
            e.aiState = EnemyAIState.PATROL;
            float cx = e.physics.x + e.physics.width * 0.5f;
            if (cx >= e.patrolMaxX) e.facingRight = false;
            else if (cx <= e.patrolMinX) e.facingRight = true;
            e.physics.x += (e.facingRight ? 1f : -1f) * e.moveSpeed * e.patrolSpeedMult * dt;
        }
    }

    private void tickBoss(SimBoss b, float dt) {
        if (!b.isAlive()) {
            if (!b.yielded) {
                b.yielded = true;
                emit("BOSS_DEFEATED", b.bossId, Map.of("type", b.type.name()));
            }
            return;
        }

        b.tickInvincibility();
        b.stateTimer -= dt;

        if (b.aiState == BossAIState.INTRO && b.stateTimer <= 0f) {
            b.aiState   = BossAIState.IDLE;
            b.stateTimer = 0f;
            emit("BOSS_INTRO_DONE", b.bossId, Map.of("type", b.type.name()));
        }

        // Phase transition — each threshold crossed once
        float ratio = b.hpRatio();
        if (b.phaseNumber == 1 && ratio <= SimBoss.PHASE2_RATIO) {
            b.phaseNumber = 2;
            emit("BOSS_PHASE_TRANSITION", b.bossId,
                    Map.of("phase", 2, "hpRatio", (double) ratio));
        } else if (b.phaseNumber == 2 && ratio <= SimBoss.PHASE3_RATIO) {
            b.phaseNumber = 3;
            emit("BOSS_PHASE_TRANSITION", b.bossId,
                    Map.of("phase", 3, "hpRatio", (double) ratio));
        } else if (b.phaseNumber == 3 && ratio <= SimBoss.PHASE4_RATIO) {
            b.phaseNumber = 4;
            emit("BOSS_PHASE_TRANSITION", b.bossId,
                    Map.of("phase", 4, "hpRatio", (double) ratio));
        }

        // Pattern dispatch — only when boss is past INTRO
        if (b.aiState != BossAIState.INTRO) {
            BossPatternLibrary.PatternContext ctx = buildBossPatternContext();
            BossPatternLibrary.ServerEvent ev = BossPatternLibrary.tick(b, ctx, dt);
            if (ev == BossPatternLibrary.ServerEvent.SCRIPTED_LOSS) {
                emit("BOSS_SCRIPTED_LOSS", b.bossId, Map.of("type", b.type.name()));
            }
        }
    }

    private BossPatternLibrary.PatternContext buildBossPatternContext() {
        Map<Integer, SimPlayer> bySlot = new LinkedHashMap<>();
        for (SimPlayer p : players.values()) bySlot.put(p.slot, p);
        return new BossPatternLibrary.PatternContext(bySlot, enemies, () -> {}, null, null);
    }

    private void tickPickups() {
        for (SimPickup pu : pickups) {
            if (!pu.alive) continue;
            pu.tick();
            if (!pu.alive) continue;
            for (SimPlayer p : players.values()) {
                if (p.isDead) continue;
                if (!pu.canBeCollectedBy(p.slot)) continue;
                if (pu.overlaps(p.physics.x, p.physics.y,
                                p.physics.width, p.physics.height)) {
                    pu.alive = false;
                    emit("PICKUP_COLLECTED", pu.pickupId,
                            Map.of("playerId", p.playerId, "type", pu.pickupType));
                    break;
                }
            }
        }
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public SimPlayer getPlayer(String playerId) {
        return players.get(playerId);
    }

    public Collection<SimPlayer> getPlayers() {
        return Collections.unmodifiableCollection(players.values());
    }

    public List<SimEnemy> getEnemies() {
        return Collections.unmodifiableList(enemies);
    }

    public List<SimBoss> getBosses() {
        return Collections.unmodifiableList(bosses);
    }

    public List<SimNPC> getNpcs() {
        return Collections.unmodifiableList(npcs);
    }

    public List<SimPickup> getPickups() {
        return Collections.unmodifiableList(pickups);
    }

    public List<SimShuriken> getShurikens() {
        return Collections.unmodifiableList(shurikens);
    }

    public List<SimMovingPlatform> getMovingPlatforms() {
        return Collections.unmodifiableList(movingPlatforms);
    }

    public List<SimPortal> getPortals() {
        return Collections.unmodifiableList(portals);
    }

    public void spawnEcho(String playerId, String echoId, boolean looping) {
        SimPlayer p = players.get(playerId);
        if (p == null) return;
        java.util.List<InputCommand> seq = p.echoRecorder.snapshot();
        if (seq.isEmpty()) return;
        ReplayPlayer replay = ReplayPlayer.fromInputSequence(0L, p.slot, seq);
        SimEcho echo = new SimEcho(echoId, p.slot, p.physics.x, p.physics.y,
                                   replay, true, p.weaponState, "player_echo");
        echo.looping = looping;
        echoes.add(echo);
        emit("ECHO_STARTED", echoId,
                Map.of("playerId", playerId, "frames", (long) seq.size()));
    }

    public List<SimEcho> getEchoes() {
        return Collections.unmodifiableList(echoes);
    }

    public void requestRevive(String reviverPlayerId, String targetPlayerId) {
        SimPlayer reviver = players.get(reviverPlayerId);
        SimPlayer target  = players.get(targetPlayerId);
        if (reviver == null || target == null) return;
        if (reviver.isDead || !target.isDead)  return;
        float dx   = reviver.physics.x - target.physics.x;
        float dy   = reviver.physics.y - target.physics.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist > REVIVE_RANGE) return;
        target.isDead             = false;
        target.respawnTimer       = -1f;
        target.health             = Math.max(1, target.maxHealth / 2);
        target.invincibilityTicks = SimPlayer.INVINCIBILITY_TICKS;
        emit("COOP_REVIVE", targetPlayerId,
                Map.of("reviverPlayerId", reviverPlayerId, "hp", target.health));
    }

    public int playerCount()     { return players.size(); }
    public int aliveEnemyCount() { return (int) enemies.stream().filter(SimEnemy::isAlive).count(); }
    public int aliveBossCount()  { return (int) bosses.stream().filter(SimBoss::isAlive).count(); }

    // ── Events ────────────────────────────────────────────────────────────────

    /** Returns all queued events and clears the queue. */
    public List<SimEvent> drainEvents() {
        if (eventQueue.isEmpty()) return List.of();
        List<SimEvent> drained = new ArrayList<>(eventQueue);
        eventQueue.clear();
        return drained;
    }

    // ── Diagnostics ───────────────────────────────────────────────────────────

    public Map<String, Object> snapshot() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("players",  playerCount());
        m.put("enemies",  enemies.size());
        m.put("alive_enemies", aliveEnemyCount());
        m.put("bosses",   bosses.size());
        m.put("alive_bosses", aliveBossCount());
        m.put("npcs",     npcs.size());
        m.put("pickups",  pickups.size());
        m.put("alive_pickups", pickups.stream().filter(p -> p.alive).count());
        return m;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private SimPlayer nearestAlivePlayer(float fromX, float fromY) {
        SimPlayer nearest = null;
        float bestDist = Float.MAX_VALUE;
        for (SimPlayer p : players.values()) {
            if (p.isDead) continue;
            float dx = p.physics.x - fromX;
            float dy = p.physics.y - fromY;
            float d = (float) Math.sqrt(dx * dx + dy * dy);
            if (d < bestDist) { bestDist = d; nearest = p; }
        }
        return nearest;
    }

    private float nearestPlayerX() {
        if (players.isEmpty()) return Float.NaN;
        float nearest = Float.NaN;
        float bestDist = Float.MAX_VALUE;
        for (SimPlayer p : players.values()) {
            if (p.isDead) continue;
            float cx = p.physics.x + p.physics.width * 0.5f;
            // Use 0 as NPC reference for simplicity when no NPC position provided
            float d = Math.abs(cx);
            if (d < bestDist) { bestDist = d; nearest = cx; }
        }
        return nearest;
    }

    private void tickMovingPlatforms() {
        for (SimMovingPlatform plat : movingPlatforms) {
            float prevX = plat.x;
            plat.step();
            float delta = plat.x - prevX;
            if (delta == 0f) continue;
            for (SimPlayer p : players.values()) {
                if (p.isDead) continue;
                if (plat.isStandingOn(p.physics.x, p.physics.y,
                                      p.physics.width, p.physics.height)) {
                    p.physics.x += delta;
                }
            }
        }
    }

    private void tickPortals(float dt) {
        for (SimPortal portal : portals) {
            portal.step(dt);
            if (!portal.isActive) continue;
            for (SimPlayer p : players.values()) {
                if (p.isDead) continue;
                float px = p.physics.x + p.physics.width  * 0.5f;
                float py = p.physics.y + p.physics.height * 0.5f;
                if (portal.canInteract(px, py) && portal.canPlayerEnter(p)) {
                    portal.isActive = false;
                    emit("PORTAL_ACTIVATED", portal.portalId,
                            Map.of("playerId", p.playerId, "dest", portal.destinationId));
                    break;
                }
            }
        }
    }

    private void tickShurikens(float dt) {
        java.util.Iterator<SimShuriken> it = shurikens.iterator();
        while (it.hasNext()) {
            SimShuriken s = it.next();
            if (!s.alive) { it.remove(); continue; }
            s.ttl -= dt;
            if (s.ttl <= 0f) { s.alive = false; it.remove(); continue; }
            s.x += s.vx * dt;
            s.y += s.vy * dt;
            for (SimEnemy e : enemies) {
                if (!e.isAlive()) continue;
                if (overlaps(s.x, s.y, SimShuriken.W, SimShuriken.H,
                             e.physics.x, e.physics.y, e.physics.width, e.physics.height)) {
                    boolean died = e.takeDamage(s.damage);
                    emit("SHURIKEN_HIT", s.shurikenId,
                            Map.of("enemyId", e.enemyId, "dmg", s.damage));
                    if (died) {
                        emit("ENEMY_DEFEATED", e.enemyId,
                                Map.of("killedBy", "shuriken_" + s.shurikenId));
                    }
                    s.alive = false;
                    break;
                }
            }
            if (!s.alive) it.remove();
        }
    }

    private void tickCoopProximity() {
        java.util.List<SimPlayer> alive = players.values().stream()
                .filter(p -> !p.isDead)
                .collect(java.util.stream.Collectors.toList());
        for (int i = 0; i < alive.size(); i++) {
            for (int j = i + 1; j < alive.size(); j++) {
                SimPlayer a = alive.get(i);
                SimPlayer b = alive.get(j);
                float dx   = a.physics.x - b.physics.x;
                float dy   = a.physics.y - b.physics.y;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist <= REVIVE_RANGE) {
                    emit("PLAYER_PROXIMITY", a.playerId,
                            Map.of("nearPlayerId", b.playerId, "dist", dist));
                }
            }
        }
    }

    private void tickCoopCollisions() {
        java.util.List<SimPlayer> alive = players.values().stream()
                .filter(p -> !p.isDead)
                .collect(java.util.stream.Collectors.toList());
        for (int i = 0; i < alive.size(); i++) {
            for (int j = i + 1; j < alive.size(); j++) {
                SimPlayer a = alive.get(i);
                SimPlayer b = alive.get(j);
                if (!overlaps(a.physics.x, a.physics.y, a.physics.width, a.physics.height,
                              b.physics.x, b.physics.y, b.physics.width, b.physics.height)) {
                    continue;
                }
                float overlapX = Math.min(a.physics.x + a.physics.width,  b.physics.x + b.physics.width)
                               - Math.max(a.physics.x, b.physics.x);
                float overlapY = Math.min(a.physics.y + a.physics.height, b.physics.y + b.physics.height)
                               - Math.max(a.physics.y, b.physics.y);
                float halfX = overlapX * 0.5f;
                float halfY = overlapY * 0.5f;
                if (overlapX <= overlapY) {
                    if (a.physics.x < b.physics.x) { a.physics.x -= halfX; b.physics.x += halfX; }
                    else                            { a.physics.x += halfX; b.physics.x -= halfX; }
                } else {
                    if (a.physics.y < b.physics.y) { a.physics.y -= halfY; b.physics.y += halfY; }
                    else                            { a.physics.y += halfY; b.physics.y -= halfY; }
                }
                emit("PLAYER_COLLISION", a.playerId, Map.of("otherPlayerId", b.playerId));
            }
        }
    }

    private void tickEchoes() {
        java.util.Iterator<SimEcho> it = echoes.iterator();
        while (it.hasNext()) {
            SimEcho echo = it.next();
            if (!echo.active && (echo.completed || echo.failed)) { it.remove(); continue; }
            if (!echo.active) continue;
            boolean wasDone   = echo.completed;
            boolean wasFailed = echo.failed;
            echo.step();
            if (echo.attackedThisTick) {
                float hitX = echo.facing >= 0
                        ? echo.x
                        : echo.x - SimEcho.ATTACK_REACH;
                for (SimEnemy e : enemies) {
                    if (!e.isAlive()) continue;
                    if (overlaps(hitX, echo.y, SimEcho.W + SimEcho.ATTACK_REACH, SimEcho.H,
                                 e.physics.x, e.physics.y, e.physics.width, e.physics.height)) {
                        boolean died = e.takeDamage(SimEcho.ECHO_DAMAGE);
                        emit("ECHO_COMBAT_HIT", echo.echoId,
                             Map.of("enemyId", e.enemyId, "ownerSlot", echo.ownerSlot));
                        if (died) {
                            echo.echoKillCount++;
                            emit("ENEMY_DAMAGED", e.enemyId,
                                 Map.of("source", "echo", "echoId", echo.echoId));
                        }
                        break;
                    }
                }
            }
            if (!wasDone && echo.completed) {
                emit("ECHO_COMPLETED", echo.echoId, Map.of("ownerSlot", echo.ownerSlot));
            }
            if (!wasFailed && echo.failed) {
                emit("ECHO_FAILED", echo.echoId, Map.of("ownerSlot", echo.ownerSlot));
            }
            if (!echo.active && (echo.completed || echo.failed)) it.remove();
        }
    }

    private static boolean overlaps(float ax, float ay, float aw, float ah,
                                     float bx, float by, float bw, float bh) {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }

    private SimEnemy findEnemy(String id) {
        for (SimEnemy e : enemies) {
            if (id.equals(e.enemyId)) return e;
        }
        return null;
    }

    private void emit(String type, String entityId) {
        eventQueue.add(new SimEvent(type, entityId));
    }

    private void emit(String type, String entityId, Map<String, Object> data) {
        eventQueue.add(new SimEvent(type, entityId, data));
    }
}
