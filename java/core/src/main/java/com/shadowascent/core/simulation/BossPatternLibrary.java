package com.shadowascent.core.simulation;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;

/**
 * Shadow Ascent M5 — Boss Psychological Pattern Library.
 * Imported from indie-ninja-adventures (com.indieniinja.sim.BossPatternLibrary).
 *
 * Patterns are stateless helpers: they operate on mutable SimBoss + context and
 * return an optional server-side event. HubStateMachine (never accessed by any
 * pattern) and slf4j (no dep in this project) are both dropped.
 *
 * Boss → Pattern:
 *   SIREN           → ScriptedLossPattern  (Act II — invincible, strips Yin/Yang)
 *   ECHO_WARDEN     → EchoMirrorPattern    (Act III — mirrors player 0.5 s delayed)
 *   TIME_LEECH_LORD → LanternDrainPattern  (Act IV — drains Lantern, spawns time leeches)
 *   MEMORY_EATER    → PhaseResetPattern    (Act VI — resets platforms per phase)
 *   VEIL_MAIDEN     → VeilMaidenPattern    (Act V — illusions, final form)
 */
public final class BossPatternLibrary {

    private BossPatternLibrary() {}

    // ── Public dispatch ───────────────────────────────────────────────────────

    public static ServerEvent tick(SimBoss boss, PatternContext ctx, float dt) {
        return switch (boss.type) {
            case SIREN           -> ScriptedLossPattern.tick(boss, ctx, dt);
            case ECHO_WARDEN     -> EchoMirrorPattern.tick(boss, ctx, dt);
            case TIME_LEECH_LORD -> LanternDrainPattern.tick(boss, ctx, dt);
            case MEMORY_EATER    -> PhaseResetPattern.tick(boss, ctx, dt);
            case VEIL_MAIDEN     -> VeilMaidenPattern.tick(boss, ctx, dt);
            default              -> null;
        };
    }

    // ── Context object ────────────────────────────────────────────────────────

    public static final class PatternContext {
        public final Map<Integer, SimPlayer> players;
        public final List<SimEnemy>          enemies;
        public final Runnable                broadcastScriptedLoss;
        public final SpawnRequest            spawnEnemy;
        public final ProjectileRequest       fireProjectile;

        public PatternContext(Map<Integer, SimPlayer> players,
                              List<SimEnemy> enemies,
                              Runnable broadcastScriptedLoss,
                              SpawnRequest spawnEnemy,
                              ProjectileRequest fireProjectile) {
            this.players              = players;
            this.enemies              = enemies;
            this.broadcastScriptedLoss = broadcastScriptedLoss;
            this.spawnEnemy           = spawnEnemy;
            this.fireProjectile       = fireProjectile;
        }
    }

    @FunctionalInterface
    public interface SpawnRequest {
        void spawn(String enemyType, float x, float y);
    }

    @FunctionalInterface
    public interface ProjectileRequest {
        void fire(SimBoss boss, float targetX, float targetY, float speed, int damage);
    }

    public enum ServerEvent { SCRIPTED_LOSS }

    // ─────────────────────────────────────────────────────────────────────────
    // Pattern 1 — Siren: Scripted Loss (Act II)
    // ─────────────────────────────────────────────────────────────────────────

    private static final class ScriptedLossPattern {

        private static final float PHASE1_MIN_RANGE  = 220f;
        private static final float PHASE1_MAX_RANGE  = 360f;
        private static final float PHASE2_MIN_RANGE  = 180f;
        private static final float PHASE2_MAX_RANGE  = 320f;
        private static final float PROJECTILE_SPEED  = SimPlayer.SHURIKEN_SPEED * 0.95f;
        private static final float TELEPORT_COOLDOWN = 3.0f;
        private static final float VOLLEY_INTERVAL   = 0.22f;

        static ServerEvent tick(SimBoss boss, PatternContext ctx, float dt) {
            SimPlayer target = nearestAlivePlayer(boss, ctx);
            if (target == null) return null;

            boss.tickInvincibility();
            if (boss.attackCooldown > 0f)        boss.attackCooldown -= dt;
            if (boss.sirenTeleportCooldown > 0f) boss.sirenTeleportCooldown -= dt;
            if (boss.sirenVulnerableTimer > 0f)  boss.sirenVulnerableTimer -= dt;

            int phase = Math.max(1, Math.min(3, boss.phaseNumber));
            if (boss.sirenAddsPhaseSpawned < phase) {
                spawnSirenAddsForPhase(boss, ctx, phase);
                boss.sirenAddsPhaseSpawned = phase;
                boss.sirenVulnerableTimer  = 0f;
            }

            float bossCx = boss.physics.x + boss.physics.width  * 0.5f;
            float tx     = target.physics.x + target.physics.width  * 0.5f;
            float ty     = target.physics.y + target.physics.height * 0.5f;
            float dx     = tx - bossCx;
            float absDx  = Math.abs(dx);
            float speed  = boss.type.moveSpeed * dt;
            float minRange = phase >= 2 ? PHASE2_MIN_RANGE : PHASE1_MIN_RANGE;
            float maxRange = phase >= 2 ? PHASE2_MAX_RANGE : PHASE1_MAX_RANGE;

            if (phase >= 2 && boss.sirenTeleportCooldown <= 0f && absDx < minRange * 1.25f) {
                float teleportDir = dx >= 0f ? -1f : 1f;
                float jump = 140f + phase * 26f;
                float dstX = boss.physics.x + teleportDir * jump;
                boss.physics.x = Math.max(boss.arenaMinX, Math.min(boss.arenaMaxX, dstX));
                boss.sirenTeleportCooldown = TELEPORT_COOLDOWN;
            } else {
                if (absDx < minRange) {
                    boss.physics.x += (dx >= 0f ? -1f : 1f) * speed;
                } else if (absDx > maxRange) {
                    boss.physics.x += (dx >= 0f ? 1f : -1f) * speed;
                }
            }

            boss.facingRight = tx >= boss.physics.x + boss.physics.width * 0.5f;
            boss.clampToArena();

            if (boss.sirenVolleyShotsRemaining > 0) {
                boss.aiState = BossAIState.ATTACK_RANGED;
                boss.sirenVolleyTimer -= dt;
                if (boss.sirenVolleyTimer <= 0f) {
                    float spread = (boss.sirenVolleyShotsRemaining - 2) * 26f;
                    if (ctx.fireProjectile != null) {
                        ctx.fireProjectile.fire(boss, tx + spread, ty, PROJECTILE_SPEED, boss.type.baseDamage + 1);
                    }
                    boss.sirenVolleyShotsRemaining--;
                    boss.sirenVolleyTimer = VOLLEY_INTERVAL;
                }
                return null;
            }

            if (boss.attackCooldown <= 0f) {
                if (phase == 3) {
                    boss.sirenVolleyShotsRemaining = 3;
                    boss.sirenVolleyTimer = 0f;
                    boss.attackCooldown   = 1.95f;
                    boss.aiState          = BossAIState.ATTACK_RANGED;
                } else {
                    if (ctx.fireProjectile != null) {
                        ctx.fireProjectile.fire(boss, tx, ty, PROJECTILE_SPEED,
                                boss.type.baseDamage + (phase >= 2 ? 1 : 0));
                    }
                    boss.attackCooldown = (phase == 1) ? 1.70f : 1.35f;
                    boss.aiState        = BossAIState.ATTACK_RANGED;
                }
            } else {
                boss.aiState = BossAIState.MOVE;
            }
            return null;
        }

        private static void spawnSirenAddsForPhase(SimBoss boss, PatternContext ctx, int phase) {
            if (ctx.spawnEnemy == null) return;
            int extraMax  = phase * 2;
            int seed      = Math.abs(boss.bossId.hashCode() * 31 + phase * 97);
            int extra     = seed % (extraMax + 1);
            int addCount  = phase * 3 + extra;
            float cx = boss.physics.x + boss.physics.width  * 0.5f;
            float cy = boss.physics.y + boss.physics.height * 0.5f;
            for (int i = 0; i < addCount; i++) {
                double a  = (Math.PI * 2.0 * i) / Math.max(1, addCount);
                float  r  = 96f + (i % 4) * 28f;
                float  sx = cx + (float) Math.cos(a) * r;
                float  sy = cy + (float) Math.sin(a) * (r * 0.35f);
                sx = Math.max(boss.arenaMinX, Math.min(boss.arenaMaxX, sx));
                sy = Math.max(boss.arenaMinY, Math.min(boss.arenaMaxY, sy));
                ctx.spawnEnemy.spawn("slime_red", sx, sy);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Pattern 2 — Echo Warden: Mirror Movement (Act III)
    // ─────────────────────────────────────────────────────────────────────────

    private static final class EchoMirrorPattern {

        private static final int BUFFER_SIZE = 30; // 30 ticks @ 60 Hz = 0.5 s delay

        static ServerEvent tick(SimBoss boss, PatternContext ctx, float dt) {
            if (boss.echoBuffer == null) boss.echoBuffer = new ArrayDeque<>(BUFFER_SIZE + 1);

            SimPlayer nearest = nearestAlivePlayer(boss, ctx);
            if (nearest == null) return null;

            boss.echoBuffer.addLast(nearest.physics.x);
            while (boss.echoBuffer.size() > BUFFER_SIZE) boss.echoBuffer.removeFirst();

            if (boss.echoBuffer.size() == BUFFER_SIZE) {
                float targetX = boss.echoBuffer.peekFirst();
                float bossCx  = boss.physics.x + boss.physics.width * 0.5f;
                float speed   = boss.type.moveSpeed * dt;
                if (targetX > bossCx)      { boss.physics.x += speed; boss.facingRight = true;  }
                else if (targetX < bossCx) { boss.physics.x -= speed; boss.facingRight = false; }
            }

            float dist = boss.distanceTo(nearest.physics.x, nearest.physics.y);
            if (dist < SimBoss.MELEE_RANGE && boss.attackCooldown <= 0) {
                boss.aiState      = BossAIState.ATTACK_MELEE;
                boss.attackCooldown = SimBoss.BASE_ATTACK_COOLDOWN;
            } else {
                boss.aiState = BossAIState.MOVE;
            }
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Pattern 3 — Time Leech Lord: Lantern Drain (Act IV)
    // ─────────────────────────────────────────────────────────────────────────

    private static final class LanternDrainPattern {

        private static final float DRAIN_RATE         = 0.03f;
        private static final float SPAWN_INTERVAL     = 8.0f;
        private static final float SPEED_BURST_MULT   = 2.0f;
        private static final float BURST_HP_RATIO     = 0.30f;
        private static final int   MAX_ACTIVE_LEECHES = 5;

        static ServerEvent tick(SimBoss boss, PatternContext ctx, float dt) {
            for (SimPlayer p : ctx.players.values()) {
                if (p.lantern != null) p.lantern.decay(DRAIN_RATE * dt, true);
            }

            boss.spawnTimer -= dt;
            if (boss.spawnTimer <= 0 && ctx.spawnEnemy != null) {
                boss.spawnTimer = SPAWN_INTERVAL;
                int activeLeeches = 0;
                for (SimEnemy en : ctx.enemies) {
                    if (!en.isAlive()) continue;
                    if (!"time_leech".equals(en.enemyType)) continue;
                    float cx = en.physics.x + en.physics.width  * 0.5f;
                    float cy = en.physics.y + en.physics.height * 0.5f;
                    if (cx < boss.arenaMinX - 64f || cx > boss.arenaMaxX + 64f) continue;
                    if (cy < boss.arenaMinY - 64f || cy > boss.arenaMaxY + 64f) continue;
                    activeLeeches++;
                }
                if (activeLeeches < MAX_ACTIVE_LEECHES) {
                    float spawnX = boss.physics.x + (boss.facingRight ? 64 : -64);
                    float spawnY = boss.physics.y;
                    spawnX = Math.max(boss.arenaMinX, Math.min(boss.arenaMaxX, spawnX));
                    spawnY = Math.max(boss.arenaMinY, Math.min(boss.arenaMaxY, spawnY));
                    ctx.spawnEnemy.spawn("time_leech", spawnX, spawnY);
                }
            }

            if (!boss.speedBurstActive && (float) boss.hp / boss.maxHp <= BURST_HP_RATIO) {
                boss.speedBurstActive = true;
            }

            SimPlayer nearest = nearestAlivePlayer(boss, ctx);
            if (nearest == null) return null;

            float dist   = boss.distanceTo(nearest.physics.x, nearest.physics.y);
            float speed  = boss.type.moveSpeed * (boss.speedBurstActive ? SPEED_BURST_MULT : 1f) * dt;
            float bossCx = boss.physics.x + boss.physics.width * 0.5f;

            if (nearest.physics.x > bossCx) { boss.physics.x += speed; boss.facingRight = true;  }
            else                             { boss.physics.x -= speed; boss.facingRight = false; }

            if (dist < SimBoss.MELEE_RANGE && boss.attackCooldown <= 0) {
                boss.aiState      = BossAIState.ATTACK_MELEE;
                boss.attackCooldown = SimBoss.BASE_ATTACK_COOLDOWN;
            } else {
                boss.aiState = BossAIState.MOVE;
            }
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Pattern 4 — Memory Eater: Phase Reset (Act VI)
    // ─────────────────────────────────────────────────────────────────────────

    private static final class PhaseResetPattern {

        static ServerEvent tick(SimBoss boss, PatternContext ctx, float dt) {
            int prevPhase = boss.phaseNumber;

            SimPlayer nearest = nearestAlivePlayer(boss, ctx);
            if (nearest != null) {
                float dist   = boss.distanceTo(nearest.physics.x, nearest.physics.y);
                float speed  = boss.type.moveSpeed * dt;
                float bossCx = boss.physics.x + boss.physics.width * 0.5f;

                if (nearest.physics.x > bossCx) { boss.physics.x += speed; boss.facingRight = true;  }
                else                             { boss.physics.x -= speed; boss.facingRight = false; }

                if (dist < SimBoss.MELEE_RANGE && boss.attackCooldown <= 0) {
                    boss.aiState      = BossAIState.ATTACK_MELEE;
                    boss.attackCooldown = SimBoss.BASE_ATTACK_COOLDOWN * 0.7f;
                } else {
                    boss.aiState = BossAIState.MOVE;
                }
            }

            if (boss.phaseNumber > prevPhase) {
                boss.platformReset = true;
            }
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Pattern 5 — Veil Maiden: Illusion and Gaslighting (Act V / final boss)
    // ─────────────────────────────────────────────────────────────────────────

    private static final class VeilMaidenPattern {

        private static final float ILLUSION_SPAWN_INTERVAL = 6.0f;
        private static final float PROJECTILE_SPEED        = SimPlayer.SHURIKEN_SPEED * 0.85f;
        private static final float REPOSITION_JUMP         = 160f;
        private static final float REPOSITION_COOLDOWN     = 2.5f;
        private static final float FINAL_FORM_HP_RATIO     = 0.25f;

        static ServerEvent tick(SimBoss boss, PatternContext ctx, float dt) {
            boss.tickInvincibility();
            if (boss.attackCooldown > 0f)          boss.attackCooldown -= dt;
            if (boss.veilMaidenIllusionTimer > 0f) boss.veilMaidenIllusionTimer -= dt;
            if (boss.sirenTeleportCooldown > 0f)   boss.sirenTeleportCooldown -= dt;

            if (!boss.veilMaidenFinalForm
                    && (float) boss.hp / boss.maxHp <= FINAL_FORM_HP_RATIO) {
                boss.veilMaidenFinalForm = true;
            }

            SimPlayer target = nearestAlivePlayer(boss, ctx);
            if (target == null) return null;

            float tx     = target.physics.x + target.physics.width  * 0.5f;
            float ty     = target.physics.y + target.physics.height * 0.5f;
            float bossCx = boss.physics.x   + boss.physics.width    * 0.5f;

            if (boss.veilMaidenFinalForm) {
                float speed = boss.type.moveSpeed * 1.6f * dt;
                if (tx > bossCx) { boss.physics.x += speed; boss.facingRight = true;  }
                else             { boss.physics.x -= speed; boss.facingRight = false; }
                boss.clampToArena();
                if (boss.attackCooldown <= 0f && ctx.fireProjectile != null) {
                    ctx.fireProjectile.fire(boss, tx, ty, PROJECTILE_SPEED * 1.3f,
                                            boss.type.baseDamage + 3);
                    boss.attackCooldown = 0.9f;
                    boss.aiState = BossAIState.ATTACK_RANGED;
                } else {
                    boss.aiState = BossAIState.MOVE;
                }
                return null;
            }

            int liveIllusions = countArenaIllusions(boss, ctx);
            int phase = Math.max(1, Math.min(3, boss.phaseNumber));

            if (boss.veilMaidenIllusionPhaseSpawned < phase) {
                spawnIllusionWave(boss, ctx, phase);
                boss.veilMaidenIllusionPhaseSpawned = phase;
                boss.veilMaidenIllusionTimer = ILLUSION_SPAWN_INTERVAL;
                liveIllusions = phase + 1;
            }

            if (liveIllusions == 0 && boss.veilMaidenIllusionTimer <= 0f) {
                spawnIllusionWave(boss, ctx, phase);
                boss.veilMaidenIllusionTimer = ILLUSION_SPAWN_INTERVAL;
                liveIllusions = phase + 1;
            }

            if (liveIllusions > 0) {
                boss.forceInvincible(3);
                float driftSpeed = boss.type.moveSpeed * 0.4f * dt;
                if (tx > bossCx) { boss.physics.x -= driftSpeed; boss.facingRight = false; }
                else             { boss.physics.x += driftSpeed; boss.facingRight = true;  }
                boss.clampToArena();
                boss.aiState = BossAIState.MOVE;
                return null;
            }

            if (boss.attackCooldown <= 0f && ctx.fireProjectile != null) {
                ctx.fireProjectile.fire(boss, tx, ty, PROJECTILE_SPEED, boss.type.baseDamage + phase);
                if (phase >= 2) {
                    ctx.fireProjectile.fire(boss, tx + 40f, ty, PROJECTILE_SPEED, boss.type.baseDamage);
                    ctx.fireProjectile.fire(boss, tx - 40f, ty, PROJECTILE_SPEED, boss.type.baseDamage);
                }
                boss.attackCooldown = 1.4f - (phase * 0.2f);
                boss.aiState = BossAIState.ATTACK_RANGED;
            }

            if (boss.sirenTeleportCooldown <= 0f) {
                float dir = tx > bossCx ? -1f : 1f;
                float dst = boss.physics.x + dir * REPOSITION_JUMP * (1f + phase * 0.3f);
                boss.physics.x = Math.max(boss.arenaMinX, Math.min(boss.arenaMaxX, dst));
                boss.sirenTeleportCooldown = REPOSITION_COOLDOWN;
            }

            boss.clampToArena();
            if (boss.aiState != BossAIState.ATTACK_RANGED) boss.aiState = BossAIState.MOVE;
            return null;
        }

        private static void spawnIllusionWave(SimBoss boss, PatternContext ctx, int phase) {
            if (ctx.spawnEnemy == null) return;
            int   count = phase + 1;
            float cx = boss.physics.x + boss.physics.width  * 0.5f;
            float cy = boss.physics.y + boss.physics.height * 0.5f;
            for (int i = 0; i < count; i++) {
                double a  = (Math.PI * 2.0 * i) / Math.max(1, count);
                float  r  = 80f + i * 24f;
                float  sx = cx + (float) Math.cos(a) * r;
                float  sy = cy + (float) Math.sin(a) * (r * 0.3f);
                sx = Math.max(boss.arenaMinX, Math.min(boss.arenaMaxX, sx));
                sy = Math.max(boss.arenaMinY, Math.min(boss.arenaMaxY, sy));
                ctx.spawnEnemy.spawn("illusion_maiden", sx, sy);
            }
        }

        private static int countArenaIllusions(SimBoss boss, PatternContext ctx) {
            int count = 0;
            for (SimEnemy en : ctx.enemies) {
                if (!en.isAlive()) continue;
                if (!"illusion_maiden".equals(en.enemyType)) continue;
                float cx = en.physics.x + en.physics.width  * 0.5f;
                float cy = en.physics.y + en.physics.height * 0.5f;
                if (cx < boss.arenaMinX - 64f || cx > boss.arenaMaxX + 64f) continue;
                if (cy < boss.arenaMinY - 64f || cy > boss.arenaMaxY + 64f) continue;
                count++;
            }
            return count;
        }
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private static SimPlayer nearestAlivePlayer(SimBoss boss, PatternContext ctx) {
        SimPlayer nearest     = null;
        float     nearestDist = Float.MAX_VALUE;
        for (SimPlayer p : ctx.players.values()) {
            if (!p.isAlive()) continue;
            float d = boss.distanceTo(p.physics.x, p.physics.y);
            if (d < nearestDist) { nearestDist = d; nearest = p; }
        }
        return nearest;
    }
}
