package com.shadowascent.core.ecs.components;

import com.shadowascent.core.ecs.Component;

/**
 * Health component: manages entity health points and damage state.
 */
public class HealthComponent implements Component {
    private int maxHealth;
    private int currentHealth;
    private boolean isDead;

    public HealthComponent(int maxHealth) {
        this.maxHealth = Math.max(1, maxHealth);
        this.currentHealth = this.maxHealth;
        this.isDead = false;
    }

    @Override
    public Class<? extends Component> componentType() {
        return HealthComponent.class;
    }

    // Getters
    public int maxHealth() { return maxHealth; }
    public int currentHealth() { return currentHealth; }
    public boolean isDead() { return isDead; }
    public float healthPercent() { return (float) currentHealth / maxHealth; }

    /**
     * Takes damage and updates dead state.
     * Returns true if the entity was killed by this damage.
     */
    public boolean takeDamage(int amount) {
        currentHealth = Math.max(0, currentHealth - amount);
        if (currentHealth <= 0) {
            isDead = true;
            return true;
        }
        return false;
    }

    /**
     * Restores health.
     */
    public void heal(int amount) {
        if (!isDead) {
            currentHealth = Math.min(maxHealth, currentHealth + amount);
        }
    }

    /**
     * Fully restores entity to full health (for respawn).
     */
    public void resurrect() {
        currentHealth = maxHealth;
        isDead = false;
    }
}
