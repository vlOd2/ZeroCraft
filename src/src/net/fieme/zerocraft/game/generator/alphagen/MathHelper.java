package net.fieme.zerocraft.game.generator.alphagen;

/**
 * net.minecraft.src.MathHelper<br>
 * Taken from Alpha 1.0.6_03<br>
 * Mapped with RetroMCP 1.0<br>
 * <br>
 * NOTE: This might be removed in a future build<br>
 * NOTE: This version of the implementation has been trimmed
 */
public class MathHelper {
	private static float[] SIN_TABLE = new float[65536];

	public static final float sin(float value) {
		return SIN_TABLE[(int)(value * 10430.378F) & 65535];
	}

	public static final float cos(float value) {
		return SIN_TABLE[(int)(value * 10430.378F + 16384.0F) & 65535];
	}

	public static int floor_double(double value) {
		int i2 = (int)value;
		return value < (double)i2 ? i2 - 1 : i2;
	}
	
	static {
		for (int i0 = 0; i0 < 65536; ++i0) {
			SIN_TABLE[i0] = (float)Math.sin((double)i0 * Math.PI * 2.0D / 65536.0D);
		}
	}
}
