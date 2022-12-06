package net.fieme.zerocraft.game.generator.alphagen;

import java.util.Random;

import net.fieme.zerocraft.game.WorldTiles;

/**
 * net.minecraft.src.MathHelper<br>
 * Taken from Alpha 1.0.6_03<br>
 * Mapped with RetroMCP 1.0<br>
 * <br>
 * NOTE: This might be removed in a future build<br>
 * NOTE: This version of the implementation has been trimmed and modified
 */
public class ChunkBasedOctaveGenerator {
	private Random rand;
	private NoiseGeneratorOctaves noiseGen1;
	private NoiseGeneratorOctaves noiseGen2;
	private NoiseGeneratorOctaves noiseGen3;
	private NoiseGeneratorOctaves noiseGen4;
	private NoiseGeneratorOctaves noiseGen5;
	private NoiseGeneratorOctaves noiseGen6;
	private NoiseGeneratorOctaves noiseGen7;
	private long seed;
	private double[] noiseArray;
	private double[] sandNoise = new double[256];
	private double[] gravelNoise = new double[256];
	private double[] stoneNoise = new double[256];
	double[] noise3;
	double[] noise1;
	double[] noise2;
	double[] noise6;
	double[] noise7;
	int[][] unused = new int[32][32];

	public ChunkBasedOctaveGenerator(long seed) {
		this.seed = seed;
		this.rand = new Random(seed);
		this.noiseGen1 = new NoiseGeneratorOctaves(this.rand, 16);
		this.noiseGen2 = new NoiseGeneratorOctaves(this.rand, 16);
		this.noiseGen3 = new NoiseGeneratorOctaves(this.rand, 8);
		this.noiseGen4 = new NoiseGeneratorOctaves(this.rand, 4);
		this.noiseGen5 = new NoiseGeneratorOctaves(this.rand, 4);
		this.noiseGen6 = new NoiseGeneratorOctaves(this.rand, 10);
		this.noiseGen7 = new NoiseGeneratorOctaves(this.rand, 16);
	}

	private void generateTerrain(int chunkX, int chunkZ, byte[] blocks) {
		byte b4 = 4;
		byte b5 = 64;
		int i6 = b4 + 1;
		byte b7 = 17;
		int i8 = b4 + 1;
		this.noiseArray = this.initializeNoiseField(this.noiseArray, chunkX * b4, 0, chunkZ * b4, i6, b7, i8);

		for(int i9 = 0; i9 < b4; ++i9) {
			for(int i10 = 0; i10 < b4; ++i10) {
				for(int i11 = 0; i11 < 16; ++i11) {
					double d12 = 0.125D;
					double d14 = this.noiseArray[((i9 + 0) * i8 + i10 + 0) * b7 + i11 + 0];
					double d16 = this.noiseArray[((i9 + 0) * i8 + i10 + 1) * b7 + i11 + 0];
					double d18 = this.noiseArray[((i9 + 1) * i8 + i10 + 0) * b7 + i11 + 0];
					double d20 = this.noiseArray[((i9 + 1) * i8 + i10 + 1) * b7 + i11 + 0];
					double d22 = (this.noiseArray[((i9 + 0) * i8 + i10 + 0) * b7 + i11 + 1] - d14) * d12;
					double d24 = (this.noiseArray[((i9 + 0) * i8 + i10 + 1) * b7 + i11 + 1] - d16) * d12;
					double d26 = (this.noiseArray[((i9 + 1) * i8 + i10 + 0) * b7 + i11 + 1] - d18) * d12;
					double d28 = (this.noiseArray[((i9 + 1) * i8 + i10 + 1) * b7 + i11 + 1] - d20) * d12;

					for(int i30 = 0; i30 < 8; ++i30) {
						double d31 = 0.25D;
						double d33 = d14;
						double d35 = d16;
						double d37 = (d18 - d14) * d31;
						double d39 = (d20 - d16) * d31;

						for(int i41 = 0; i41 < 4; ++i41) {
							int i42 = i41 + i9 * 4 << 11 | 0 + i10 * 4 << 7 | i11 * 8 + i30;
							short s43 = 128;
							double d44 = 0.25D;
							double d46 = d33;
							double d48 = (d35 - d33) * d44;

							for(int i50 = 0; i50 < 4; ++i50) {
								int i51 = 0;
								if(i11 * 8 + i30 < b5) {
									i51 = WorldTiles.water.id;
								}

								if(d46 > 0.0D) {
									i51 = WorldTiles.stone.id;
								}

								blocks[i42] = (byte)i51;
								i42 += s43;
								d46 += d48;
							}

							d33 += d37;
							d35 += d39;
						}

						d14 += d22;
						d16 += d24;
						d18 += d26;
						d20 += d28;
					}
				}
			}
		}

	}

	private void replaceSurfaceBlocks(int chunkX, int chunkZ, byte[] blocks) {
		byte b4 = 64;
		double d5 = 8.0D / 256D;
		this.sandNoise = this.noiseGen4.generateNoiseOctaves(this.sandNoise, (double)(chunkX * 16), (double)(chunkZ * 16), 0.0D, 16, 16, 1, d5, d5, 1.0D);
		this.gravelNoise = this.noiseGen4.generateNoiseOctaves(this.gravelNoise, (double)(chunkZ * 16), 109.0134D, (double)(chunkX * 16), 16, 1, 16, d5, 1.0D, d5);
		this.stoneNoise = this.noiseGen5.generateNoiseOctaves(this.stoneNoise, (double)(chunkX * 16), (double)(chunkZ * 16), 0.0D, 16, 16, 1, d5 * 2.0D, d5 * 2.0D, d5 * 2.0D);

		for(int i7 = 0; i7 < 16; ++i7) {
			for(int i8 = 0; i8 < 16; ++i8) {
				boolean z9 = this.sandNoise[i7 + i8 * 16] + this.rand.nextDouble() * 0.2D > 0.0D;
				boolean z10 = this.gravelNoise[i7 + i8 * 16] + this.rand.nextDouble() * 0.2D > 3.0D;
				int i11 = (int)(this.stoneNoise[i7 + i8 * 16] / 3.0D + 3.0D + this.rand.nextDouble() * 0.25D);
				int i12 = -1;
				byte b13 = WorldTiles.grass.id;
				byte b14 = WorldTiles.dirt.id;

				for(int i15 = 127; i15 >= 0; --i15) {
					int i16 = (i7 * 16 + i8) * 128 + i15;
					if(i15 <= 0 + this.rand.nextInt(6) - 1) {
						blocks[i16] = WorldTiles.bedrock.id;
					} else {
						byte b17 = blocks[i16];
						if(b17 == 0) {
							i12 = -1;
						} else if(b17 == WorldTiles.stone.id) {
							if(i12 == -1) {
								if(i11 <= 0) {
									b13 = 0;
									b14 = WorldTiles.stone.id;
								} else if(i15 >= b4 - 4 && i15 <= b4 + 1) {
									b13 = WorldTiles.grass.id;
									b14 = WorldTiles.dirt.id;
									if(z10) {
										b13 = 0;
									}

									if(z10) {
										b14 = WorldTiles.gravel.id;
									}

									if(z9) {
										b13 = WorldTiles.sand.id;
									}

									if(z9) {
										b14 = WorldTiles.sand.id;
									}
								}

								if(i15 < b4 && b13 == 0) {
									b13 = WorldTiles.stillwater.id;
								}

								i12 = i11;
								if(i15 >= b4 - 1) {
									blocks[i16] = b13;
								} else {
									blocks[i16] = b14;
								}
							} else if(i12 > 0) {
								--i12;
								blocks[i16] = b14;
							}
						}
					}
				}
			}
		}

	}

	public byte[] generateChunk(int i1, int i2) {
		this.rand.setSeed((long)i1 * 341873128712L + (long)i2 * 132897987541L);
		byte[] chunkBlocks = new byte[32768];
		
		this.generateTerrain(i1, i2, chunkBlocks);
		this.replaceSurfaceBlocks(i1, i2, chunkBlocks);
		this.c(i1, i2, chunkBlocks);
		
		return chunkBlocks;
	}

	protected void a(int i1, int i2, byte[] b3, double d4, double d6, double d8) {
		this.a(i1, i2, b3, d4, d6, d8, 1.0F + this.rand.nextFloat() * 6.0F, 0.0F, 0.0F, -1, -1, 0.5D);
	}

	protected void a(int i1, int i2, byte[] b3, double d4, double d6, double d8, float f10, float f11, float f12, int i13, int i14, double d15) {
		double d17 = (double)(i1 * 16 + 8);
		double d19 = (double)(i2 * 16 + 8);
		float f21 = 0.0F;
		float f22 = 0.0F;
		Random random23 = new Random(this.rand.nextLong());
		if(i14 <= 0) {
			byte b24 = 112;
			i14 = b24 - random23.nextInt(b24 / 4);
		}

		boolean z52 = false;
		if(i13 == -1) {
			i13 = i14 / 2;
			z52 = true;
		}

		int i25 = random23.nextInt(i14 / 2) + i14 / 4;

		for(boolean z26 = random23.nextInt(6) == 0; i13 < i14; ++i13) {
			double d27 = 1.5D + (double)(MathHelper.sin((float)i13 * (float)Math.PI / (float)i14) * f10 * 1.0F);
			double d29 = d27 * d15;
			float f31 = MathHelper.cos(f12);
			float f32 = MathHelper.sin(f12);
			d4 += (double)(MathHelper.cos(f11) * f31);
			d6 += (double)f32;
			d8 += (double)(MathHelper.sin(f11) * f31);
			if(z26) {
				f12 *= 0.92F;
			} else {
				f12 *= 0.7F;
			}

			f12 += f22 * 0.1F;
			f11 += f21 * 0.1F;
			f22 *= 0.9F;
			f21 *= 0.75F;
			f22 += (random23.nextFloat() - random23.nextFloat()) * random23.nextFloat() * 2.0F;
			f21 += (random23.nextFloat() - random23.nextFloat()) * random23.nextFloat() * 4.0F;
			if(!z52 && i13 == i25 && f10 > 1.0F) {
				this.a(i1, i2, b3, d4, d6, d8, random23.nextFloat() * 0.5F + 0.5F, f11 - (float)Math.PI / 2F, f12 / 3.0F, i13, i14, 1.0D);
				this.a(i1, i2, b3, d4, d6, d8, random23.nextFloat() * 0.5F + 0.5F, f11 + (float)Math.PI / 2F, f12 / 3.0F, i13, i14, 1.0D);
				return;
			}

			if(z52 || random23.nextInt(4) != 0) {
				double d33 = d4 - d17;
				double d35 = d8 - d19;
				double d37 = (double)(i14 - i13);
				double d39 = (double)(f10 + 2.0F + 16.0F);
				if(d33 * d33 + d35 * d35 - d37 * d37 > d39 * d39) {
					return;
				}

				if(d4 >= d17 - 16.0D - d27 * 2.0D && d8 >= d19 - 16.0D - d27 * 2.0D && d4 <= d17 + 16.0D + d27 * 2.0D && d8 <= d19 + 16.0D + d27 * 2.0D) {
					int i53 = MathHelper.floor_double(d4 - d27) - i1 * 16 - 1;
					int i34 = MathHelper.floor_double(d4 + d27) - i1 * 16 + 1;
					int i54 = MathHelper.floor_double(d6 - d29) - 1;
					int i36 = MathHelper.floor_double(d6 + d29) + 1;
					int i55 = MathHelper.floor_double(d8 - d27) - i2 * 16 - 1;
					int i38 = MathHelper.floor_double(d8 + d27) - i2 * 16 + 1;
					if(i53 < 0) {
						i53 = 0;
					}

					if(i34 > 16) {
						i34 = 16;
					}

					if(i54 < 1) {
						i54 = 1;
					}

					if(i36 > 120) {
						i36 = 120;
					}

					if(i55 < 0) {
						i55 = 0;
					}

					if(i38 > 16) {
						i38 = 16;
					}

					boolean z56 = false;

					int i40;
					int i43;
					for(i40 = i53; !z56 && i40 < i34; ++i40) {
						for(int i41 = i55; !z56 && i41 < i38; ++i41) {
							for(int i42 = i36 + 1; !z56 && i42 >= i54 - 1; --i42) {
								i43 = (i40 * 16 + i41) * 128 + i42;
								if(i42 >= 0 && i42 < 128) {
									if(b3[i43] == WorldTiles.water.id || b3[i43] == WorldTiles.water.id) {
										z56 = true;
									}

									if(i42 != i54 - 1 && i40 != i53 && i40 != i34 - 1 && i41 != i55 && i41 != i38 - 1) {
										i42 = i54;
									}
								}
							}
						}
					}

					if(!z56) {
						for(i40 = i53; i40 < i34; ++i40) {
							double d57 = ((double)(i40 + i1 * 16) + 0.5D - d4) / d27;

							for(i43 = i55; i43 < i38; ++i43) {
								double d44 = ((double)(i43 + i2 * 16) + 0.5D - d8) / d27;
								int i46 = (i40 * 16 + i43) * 128 + i36;
								boolean z47 = false;

								for(int i48 = i36 - 1; i48 >= i54; --i48) {
									double d49 = ((double)i48 + 0.5D - d6) / d29;
									if(d49 > -0.7D && d57 * d57 + d49 * d49 + d44 * d44 < 1.0D) {
										byte b51 = b3[i46];
										if(b51 == WorldTiles.grass.id) {
											z47 = true;
										}

										if(b51 == WorldTiles.stone.id || b51 == WorldTiles.dirt.id || b51 == WorldTiles.grass.id) {
											if(i48 < 10) {
												b3[i46] = WorldTiles.lava.id;
											} else {
												b3[i46] = 0;
												if(z47 && b3[i46 - 1] == WorldTiles.dirt.id) {
													b3[i46 - 1] = WorldTiles.grass.id;
												}
											}
										}
									}

									--i46;
								}
							}
						}

						if(z52) {
							break;
						}
					}
				}
			}
		}

	}

	private void c(int i1, int i2, byte[] b3) {
		byte b4 = 8;
		this.rand.setSeed(this.seed);
		long j5 = this.rand.nextLong() / 2L * 2L + 1L;
		long j7 = this.rand.nextLong() / 2L * 2L + 1L;

		for(int i9 = i1 - b4; i9 <= i1 + b4; ++i9) {
			for(int i10 = i2 - b4; i10 <= i2 + b4; ++i10) {
				this.rand.setSeed((long)i9 * j5 + (long)i10 * j7 ^ this.seed);
				int i11 = this.rand.nextInt(this.rand.nextInt(this.rand.nextInt(40) + 1) + 1);
				if(this.rand.nextInt(15) != 0) {
					i11 = 0;
				}

				for(int i12 = 0; i12 < i11; ++i12) {
					double d13 = (double)(i9 * 16 + this.rand.nextInt(16));
					double d15 = (double)this.rand.nextInt(this.rand.nextInt(120) + 8);
					double d17 = (double)(i10 * 16 + this.rand.nextInt(16));
					int i19 = 1;
					if(this.rand.nextInt(4) == 0) {
						this.a(i1, i2, b3, d13, d15, d17);
						i19 += this.rand.nextInt(4);
					}

					for(int i20 = 0; i20 < i19; ++i20) {
						float f21 = this.rand.nextFloat() * (float)Math.PI * 2.0F;
						float f22 = (this.rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
						float f23 = this.rand.nextFloat() * 2.0F + this.rand.nextFloat();
						this.a(i1, i2, b3, d13, d15, d17, f23, f21, f22, 0, 0, 1.0D);
					}
				}
			}
		}

	}

	private double[] initializeNoiseField(double[] d1, int i2, int i3, int i4, int i5, int i6, int i7) {
		if(d1 == null) {
			d1 = new double[i5 * i6 * i7];
		}

		double d8 = 684.412D;
		double d10 = 684.412D;
		this.noise6 = this.noiseGen6.generateNoiseOctaves(this.noise6, (double)i2, (double)i3, (double)i4, i5, 1, i7, 1.0D, 0.0D, 1.0D);
		this.noise7 = this.noiseGen7.generateNoiseOctaves(this.noise7, (double)i2, (double)i3, (double)i4, i5, 1, i7, 100.0D, 0.0D, 100.0D);
		this.noise3 = this.noiseGen3.generateNoiseOctaves(this.noise3, (double)i2, (double)i3, (double)i4, i5, i6, i7, d8 / 80.0D, d10 / 160.0D, d8 / 80.0D);
		this.noise1 = this.noiseGen1.generateNoiseOctaves(this.noise1, (double)i2, (double)i3, (double)i4, i5, i6, i7, d8, d10, d8);
		this.noise2 = this.noiseGen2.generateNoiseOctaves(this.noise2, (double)i2, (double)i3, (double)i4, i5, i6, i7, d8, d10, d8);
		int i12 = 0;
		int i13 = 0;

		for(int i14 = 0; i14 < i5; ++i14) {
			for(int i15 = 0; i15 < i7; ++i15) {
				double d16 = (this.noise6[i13] + 256.0D) / 512.0D;
				if(d16 > 1.0D) {
					d16 = 1.0D;
				}

				double d18 = 0.0D;
				double d20 = this.noise7[i13] / 8000.0D;
				if(d20 < 0.0D) {
					d20 = -d20;
				}

				d20 = d20 * 3.0D - 3.0D;
				if(d20 < 0.0D) {
					d20 /= 2.0D;
					if(d20 < -1.0D) {
						d20 = -1.0D;
					}

					d20 /= 1.4D;
					d20 /= 2.0D;
					d16 = 0.0D;
				} else {
					if(d20 > 1.0D) {
						d20 = 1.0D;
					}

					d20 /= 6.0D;
				}

				d16 += 0.5D;
				d20 = d20 * (double)i6 / 16.0D;
				double d22 = (double)i6 / 2.0D + d20 * 4.0D;
				++i13;

				for(int i24 = 0; i24 < i6; ++i24) {
					double d25 = 0.0D;
					double d27 = ((double)i24 - d22) * 12.0D / d16;
					if(d27 < 0.0D) {
						d27 *= 4.0D;
					}

					double d29 = this.noise1[i12] / 512.0D;
					double d31 = this.noise2[i12] / 512.0D;
					double d33 = (this.noise3[i12] / 10.0D + 1.0D) / 2.0D;
					if(d33 < 0.0D) {
						d25 = d29;
					} else if(d33 > 1.0D) {
						d25 = d31;
					} else {
						d25 = d29 + (d31 - d29) * d33;
					}

					d25 -= d27;
					double d35;
					if(i24 > i6 - 4) {
						d35 = (double)((float)(i24 - (i6 - 4)) / 3.0F);
						d25 = d25 * (1.0D - d35) + -10.0D * d35;
					}

					if((double)i24 < d18) {
						d35 = (d18 - (double)i24) / 4.0D;
						if(d35 < 0.0D) {
							d35 = 0.0D;
						}

						if(d35 > 1.0D) {
							d35 = 1.0D;
						}

						d25 = d25 * (1.0D - d35) + -10.0D * d35;
					}

					d1[i12] = d25;
					++i12;
				}
			}
		}

		return d1;
	}
}
