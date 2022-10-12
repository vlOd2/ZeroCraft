package net.fieme.zerocraft.game;

public class AliveEntity extends Entity {
	public byte yaw;
	public byte pitch;
	protected double health;
	
	/**
	 * Gets the health of this entity
	 * 
	 * @return the health
	 */
	public double getHealth() {
		return this.health;
	}
	
	/**
	 * Sets the health of this entity
	 * 
	 * @param value the new health
	 */
	public void setHealth(double value) {
		this.health = value;
	}
	
	/**
	 * Damages this entity
	 * 
	 * @param amount the damage amount
	 */
	public void damage(double amount) {
		this.health -= amount;
		if (this.health < 0)
			this.health = 0;
	}
	
	/**
	 * Kills this entity
	 */
	public void kill() {
		this.health = 0;
	}
}
