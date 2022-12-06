package net.fieme.zerocraft.game.generator;

import net.fieme.zerocraft.game.World;

public class EmptyWorldGenerator implements WorldGenerator {
	@Override
	public void generateWorld(World world) {
		world.spawnX = (short) (world.width / 2);
		world.spawnY = (short) (world.height / 2);
		world.spawnZ = (short) (world.depth / 2);
	}
}
