package net.fieme.zerocraft.game.generator;

import net.fieme.zerocraft.game.World;

public interface WorldGenerator {
	public void generateWorld(World world) throws Exception;
}
