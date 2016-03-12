/**
 * Copyright (c) 2006-2016 Julien Gouesse
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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Data model of the enemy (there is some temporary code duplication until a
 * larger code redesign, see {@link PlayerData}). It contains some operations
 * for these data too.
 * 
 * @author Julien Gouesse
 * 
 */
public class EnemyData {

    /** maximum health */
    private static final int maxHealth = 100;
    /** automatic increment used to compute the identifiers */
    private static final AtomicInteger autoIncrementalIndex = new AtomicInteger(0);
    /** unique identifier */
    private final int uid;
    /** current health */
    private int health;

    public EnemyData() {
        this.uid = autoIncrementalIndex.getAndIncrement();
        health = maxHealth;
    }

    /**
     * decreases the health
     * 
     * @param damage
     *            the suggested decrease of health
     * @return the real decrease of health
     */
    public int decreaseHealth(int damage) {
        int oldHealth = health;
        if (damage > 0)
            health = Math.max(0, health - damage);
        return (oldHealth - health);
    }

    /**
     * increases the health
     * 
     * @param amount
     *            the suggested increase of health
     * @return the real increase of health
     */
    public int increaseHealth(int amount) {
        final int oldHealth = health;
        if (amount > 0)
            health = Math.min(maxHealth, health + amount);
        return (health - oldHealth);
    }

    public boolean isAlive() {
        return (this.health > 0);
    }

    public int getHealth() {
        return (health);
    }

    @Override
    public String toString() {
        return (super.toString() + "#" + String.valueOf(uid));
    }
}
