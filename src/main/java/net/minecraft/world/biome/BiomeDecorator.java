package net.minecraft.world.biome;

import java.util.*;

import net.minecraft.block.BlockFlower;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;
import net.minecraft.world.gen.feature.WorldGenCactus;
import net.minecraft.world.gen.feature.WorldGenClay;
import net.minecraft.world.gen.feature.WorldGenDeadBush;
import net.minecraft.world.gen.feature.WorldGenFlowers;
import net.minecraft.world.gen.feature.WorldGenLiquids;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenPumpkin;
import net.minecraft.world.gen.feature.WorldGenReed;
import net.minecraft.world.gen.feature.WorldGenSand;
import net.minecraft.world.gen.feature.WorldGenWaterlily;
import net.minecraft.world.gen.feature.WorldGenerator;

import static net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType.*;
import static net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.*;
import net.minecraftforge.common.*;
import net.minecraftforge.event.terraingen.*;

// Spigot Start

import net.minecraft.world.chunk.Chunk;
// Spigot End

public class BiomeDecorator
{
    public World currentWorld;
    public Random randomGenerator;
    public int chunk_X;
    public int chunk_Z;
    public WorldGenerator clayGen = new WorldGenClay(4);
    public WorldGenerator sandGen;
    public WorldGenerator gravelAsSandGen;
    public WorldGenerator dirtGen;
    public WorldGenerator gravelGen;
    public WorldGenerator coalGen;
    public WorldGenerator ironGen;
    public WorldGenerator goldGen;
    public WorldGenerator redstoneGen;
    public WorldGenerator diamondGen;
    public WorldGenerator lapisGen;
    public WorldGenFlowers yellowFlowerGen;
    public WorldGenerator mushroomBrownGen;
    public WorldGenerator mushroomRedGen;
    public WorldGenerator bigMushroomGen;
    public WorldGenerator reedGen;
    public WorldGenerator cactusGen;
    public WorldGenerator waterlilyGen;
    public int waterlilyPerChunk;
    public int treesPerChunk;
    public int flowersPerChunk;
    public int grassPerChunk;
    public int deadBushPerChunk;
    public int mushroomsPerChunk;
    public int reedsPerChunk;
    public int cactiPerChunk;
    public int sandPerChunk;
    public int sandPerChunk2;
    public int clayPerChunk;
    public int bigMushroomsPerChunk;
    public boolean generateLakes;
    private final List<Chunk> chunksToUnload = new ArrayList<Chunk>(); // Spigot
    private final java.util.concurrent.locks.ReentrantLock decorateLock = new java.util.concurrent.locks.ReentrantLock(); // Thermos - prevent decorator crashes
    private static final String __OBFID = "CL_00000164";

    public BiomeDecorator()
    {
        this.sandGen = new WorldGenSand(Blocks.sand, 7);
        this.gravelAsSandGen = new WorldGenSand(Blocks.gravel, 6);
        this.dirtGen = new WorldGenMinable(Blocks.dirt, 32);
        this.gravelGen = new WorldGenMinable(Blocks.gravel, 32);
        this.coalGen = new WorldGenMinable(Blocks.coal_ore, 16);
        this.ironGen = new WorldGenMinable(Blocks.iron_ore, 8);
        this.goldGen = new WorldGenMinable(Blocks.gold_ore, 8);
        this.redstoneGen = new WorldGenMinable(Blocks.redstone_ore, 7);
        this.diamondGen = new WorldGenMinable(Blocks.diamond_ore, 7);
        this.lapisGen = new WorldGenMinable(Blocks.lapis_ore, 6);
        this.yellowFlowerGen = new WorldGenFlowers(Blocks.yellow_flower);
        this.mushroomBrownGen = new WorldGenFlowers(Blocks.brown_mushroom);
        this.mushroomRedGen = new WorldGenFlowers(Blocks.red_mushroom);
        this.bigMushroomGen = new WorldGenBigMushroom();
        this.reedGen = new WorldGenReed();
        this.cactusGen = new WorldGenCactus();
        this.waterlilyGen = new WorldGenWaterlily();
        this.flowersPerChunk = 2;
        this.grassPerChunk = 1;
        this.sandPerChunk = 1;
        this.sandPerChunk2 = 3;
        this.clayPerChunk = 1;
        this.generateLakes = true;
    }

    public void decorateChunk(World p_150512_1_, Random p_150512_2_, BiomeGenBase p_150512_3_, int p_150512_4_, int p_150512_5_)
    {
      int ct = 0;
      while(decorateLock.isLocked() && !decorateLock.isHeldByCurrentThread()) {
          try {
              Thread.sleep(100L);
          } catch(Exception ignored) {
          } finally {
              ct++;
          }
          if(ct > 100 ) {
              throw new RuntimeException("Generating a chunk in world " + this.currentWorld.getWorldInfo().getWorldName() +" took longer than 10 seconds, Thermos needs to crash so the server doesn't time out...sorry!");
          } // Thermos - Decoration took too long, we'll have to crash the server at this point so it doesn't hang.
      }
      decorateLock.lock();
      
      try {
            //throw new RuntimeException("Already decorating!!");
            // TODO Runtime with Timeout https://stackoverflow.com/questions/2275443/how-to-timeout-a-thread
            /*System.out.println("[Thermos] Already Decorating a Chunk! Waiting...");
            for(int i = 0; i < 10; i++)
            {
                try{ Thread.sleep(100L); } catch(Exception e){}
            }
            if(this.currentWorld != null)
                decorateChunk(p_150512_1_, p_150512_2_, p_150512_3_, p_150512_4_, p_150512_5_);
            else
                System.out.println("[Thermos] Decoration failure @ X:" + (p_150512_4_ << 4) + ", Z:" + (p_150512_5_ << 4) );
*/
            this.currentWorld = p_150512_1_;
            this.randomGenerator = p_150512_2_;
            this.chunk_X = p_150512_4_;
            this.chunk_Z = p_150512_5_;
            this.genDecorations(p_150512_3_);
            // this.currentWorld = null; //Crucible note - Thaumcraft causes an NPE, commenting this seems to fix the issue
            // this.randomGenerator = null; //Crucible note - does not setting this to null causes any consequences?
      } finally {
          decorateLock.unlock();
      }
    }

    protected void genDecorations(BiomeGenBase p_150513_1_)
    {
        Objects.requireNonNull(p_150513_1_, "[Crucible FATAL] field p_150513_1_ (BiomeGenBase) is null.");
        Objects.requireNonNull(currentWorld, "[Crucible FATAL] field currentWorld (World) is null.");
        Objects.requireNonNull(randomGenerator, "[Crucible FATAL] field randomGenerator (Random) is null.");
        MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Pre(currentWorld, randomGenerator, chunk_X, chunk_Z));
        this.generateOres();
        int i;
        int j;
        int k;

        boolean doGen = TerrainGen.decorate(currentWorld, randomGenerator, chunk_X, chunk_Z, SAND);
        for (i = 0; doGen && i < this.sandPerChunk2; ++i)
        {
            j = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
            k = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
            this.sandGen.generate(this.currentWorld, this.randomGenerator, j, this.currentWorld.getTopSolidOrLiquidBlock(j, k), k);
        }

        doGen = TerrainGen.decorate(currentWorld, randomGenerator, chunk_X, chunk_Z, CLAY);
        for (i = 0; doGen && i < this.clayPerChunk; ++i)
        {
            j = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
            k = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
            this.clayGen.generate(this.currentWorld, this.randomGenerator, j, this.currentWorld.getTopSolidOrLiquidBlock(j, k), k);
        }

        doGen = TerrainGen.decorate(currentWorld, randomGenerator, chunk_X, chunk_Z, SAND_PASS2);
        for (i = 0; doGen && i < this.sandPerChunk; ++i)
        {
            j = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
            k = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
            this.gravelAsSandGen.generate(this.currentWorld, this.randomGenerator, j, this.currentWorld.getTopSolidOrLiquidBlock(j, k), k);
        }

        i = this.treesPerChunk;

        if (this.randomGenerator.nextInt(10) == 0)
        {
            ++i;
        }

        int l;
        int i1;

        doGen = TerrainGen.decorate(currentWorld, randomGenerator, chunk_X, chunk_Z, TREE);
        for (j = 0; doGen && j < i; ++j)
        {
            k = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
            l = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
            i1 = this.currentWorld.getHeightValue(k, l);
            WorldGenAbstractTree worldgenabstracttree = p_150513_1_.func_150567_a(this.randomGenerator);
            worldgenabstracttree.setScale(1.0D, 1.0D, 1.0D);

            if (worldgenabstracttree.generate(this.currentWorld, this.randomGenerator, k, i1, l))
            {
                worldgenabstracttree.func_150524_b(this.currentWorld, this.randomGenerator, k, i1, l);
            }
        }

        doGen = TerrainGen.decorate(currentWorld, randomGenerator, chunk_X, chunk_Z, BIG_SHROOM);
        for (j = 0; doGen && j < this.bigMushroomsPerChunk; ++j)
        {
            k = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
            l = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
            this.bigMushroomGen.generate(this.currentWorld, this.randomGenerator, k, this.currentWorld.getHeightValue(k, l), l);
        }

        doGen = TerrainGen.decorate(currentWorld, randomGenerator, chunk_X, chunk_Z, FLOWERS);
        for (j = 0; doGen && j < this.flowersPerChunk; ++j)
        {
            k = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
            l = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
            i1 = nextInt(this.currentWorld.getHeightValue(k, l) + 32);
            String s = p_150513_1_.func_150572_a(this.randomGenerator, k, i1, l);
            BlockFlower blockflower = BlockFlower.func_149857_e(s);

            if (blockflower.getMaterial() != Material.air)
            {
                this.yellowFlowerGen.func_150550_a(blockflower, BlockFlower.func_149856_f(s));
                this.yellowFlowerGen.generate(this.currentWorld, this.randomGenerator, k, i1, l);
            }
        }

        doGen = TerrainGen.decorate(currentWorld, randomGenerator, chunk_X, chunk_Z, GRASS);
        for (j = 0; doGen && j < this.grassPerChunk; ++j)
        {
            k = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
            l = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
            i1 = nextInt(this.getHighestBlockYAt(k, l) * 2); // Spigot
            WorldGenerator worldgenerator = p_150513_1_.getRandomWorldGenForGrass(this.randomGenerator);
            worldgenerator.generate(this.currentWorld, this.randomGenerator, k, i1, l);
        }

        doGen = TerrainGen.decorate(currentWorld, randomGenerator, chunk_X, chunk_Z, DEAD_BUSH);
        for (j = 0; doGen && j < this.deadBushPerChunk; ++j)
        {
            k = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
            l = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
            i1 = nextInt(this.getHighestBlockYAt(k, l) * 2); // Spigot
            (new WorldGenDeadBush(Blocks.deadbush)).generate(this.currentWorld, this.randomGenerator, k, i1, l);
        }

        doGen = TerrainGen.decorate(currentWorld, randomGenerator, chunk_X, chunk_Z, LILYPAD);
        for (j = 0; doGen && j < this.waterlilyPerChunk; ++j)
        {
            k = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
            l = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;

            for (i1 = nextInt(this.getHighestBlockYAt(k, l) * 2); i1 > 0 && this.currentWorld.isAirBlock(k, i1 - 1, l); --i1)  // Spigot
            {
                ;
            }

            this.waterlilyGen.generate(this.currentWorld, this.randomGenerator, k, i1, l);
        }

        doGen = TerrainGen.decorate(currentWorld, randomGenerator, chunk_X, chunk_Z, SHROOM);
        for (j = 0; doGen && j < this.mushroomsPerChunk; ++j)
        {
            if (this.randomGenerator.nextInt(4) == 0)
            {
                k = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
                l = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
                i1 = this.getHighestBlockYAt(k, l); // Spigot
                this.mushroomBrownGen.generate(this.currentWorld, this.randomGenerator, k, i1, l);
            }

            if (this.randomGenerator.nextInt(8) == 0)
            {
                k = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
                l = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
                i1 = nextInt(this.getHighestBlockYAt(k, l) * 2); // Spigot
                this.mushroomRedGen.generate(this.currentWorld, this.randomGenerator, k, i1, l);
            }
        }

        if (doGen && this.randomGenerator.nextInt(4) == 0)
        {
            j = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
            k = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
            l = nextInt(this.getHighestBlockYAt(j, k) * 2); // Spigot
            this.mushroomBrownGen.generate(this.currentWorld, this.randomGenerator, j, l, k);
        }

        if (doGen && this.randomGenerator.nextInt(8) == 0)
        {
            j = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
            k = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
            l = nextInt(this.getHighestBlockYAt(j, k) * 2); // Spigot
            this.mushroomRedGen.generate(this.currentWorld, this.randomGenerator, j, l, k);
        }

        doGen = TerrainGen.decorate(currentWorld, randomGenerator, chunk_X, chunk_Z, REED);
        for (j = 0; doGen && j < this.reedsPerChunk; ++j)
        {
            k = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
            l = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
            i1 = nextInt(this.getHighestBlockYAt(k, l) * 2); // Spigot
            this.reedGen.generate(this.currentWorld, this.randomGenerator, k, i1, l);
        }

        for (j = 0; doGen && j < 10; ++j)
        {
            k = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
            l = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
            i1 = nextInt(this.getHighestBlockYAt(k, l) * 2); // Spigot
            this.reedGen.generate(this.currentWorld, this.randomGenerator, k, i1, l);
        }

        doGen = TerrainGen.decorate(currentWorld, randomGenerator, chunk_X, chunk_Z, PUMPKIN);
        if (doGen && this.randomGenerator.nextInt(32) == 0)
        {
            j = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
            k = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
            l = nextInt(this.getHighestBlockYAt(j, k) * 2); // Spigot
            (new WorldGenPumpkin()).generate(this.currentWorld, this.randomGenerator, j, l, k);
        }

        doGen = TerrainGen.decorate(currentWorld, randomGenerator, chunk_X, chunk_Z, CACTUS);
        for (j = 0; doGen && j < this.cactiPerChunk; ++j)
        {
            k = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
            l = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
            i1 = nextInt(this.getHighestBlockYAt(k, l) * 2); // Spigot
            this.cactusGen.generate(this.currentWorld, this.randomGenerator, k, i1, l);
        }

        doGen = TerrainGen.decorate(currentWorld, randomGenerator, chunk_X, chunk_Z, LAKE);
        if (doGen && this.generateLakes)
        {
            for (j = 0; j < 50; ++j)
            {
                k = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
                l = this.randomGenerator.nextInt(this.randomGenerator.nextInt(248) + 8);
                i1 = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
                (new WorldGenLiquids(Blocks.flowing_water)).generate(this.currentWorld, this.randomGenerator, k, l, i1);
            }

            for (j = 0; j < 20; ++j)
            {
                k = this.chunk_X + this.randomGenerator.nextInt(16) + 8;
                l = this.randomGenerator.nextInt(this.randomGenerator.nextInt(this.randomGenerator.nextInt(240) + 8) + 8);
                i1 = this.chunk_Z + this.randomGenerator.nextInt(16) + 8;
                (new WorldGenLiquids(Blocks.flowing_lava)).generate(this.currentWorld, this.randomGenerator, k, l, i1);
            }
        }

        this.unloadChunks(); // Spigot - unload chunks we force loaded
        MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Post(currentWorld, randomGenerator, chunk_X, chunk_Z));
    }

    protected void genStandardOre1(int p_76795_1_, WorldGenerator p_76795_2_, int p_76795_3_, int p_76795_4_)
    {
        for (int l = 0; l < p_76795_1_; ++l)
        {
            int i1 = this.chunk_X + this.randomGenerator.nextInt(16);
            int j1 = this.randomGenerator.nextInt(p_76795_4_ - p_76795_3_) + p_76795_3_;
            int k1 = this.chunk_Z + this.randomGenerator.nextInt(16);
            p_76795_2_.generate(this.currentWorld, this.randomGenerator, i1, j1, k1);
        }
    }

    protected void genStandardOre2(int p_76793_1_, WorldGenerator p_76793_2_, int p_76793_3_, int p_76793_4_)
    {
        for (int l = 0; l < p_76793_1_; ++l)
        {
            int i1 = this.chunk_X + this.randomGenerator.nextInt(16);
            int j1 = this.randomGenerator.nextInt(p_76793_4_) + this.randomGenerator.nextInt(p_76793_4_) + (p_76793_3_ - p_76793_4_);
            int k1 = this.chunk_Z + this.randomGenerator.nextInt(16);
            p_76793_2_.generate(this.currentWorld, this.randomGenerator, i1, j1, k1);
        }
    }

    protected void generateOres()
    {
        MinecraftForge.ORE_GEN_BUS.post(new OreGenEvent.Pre(currentWorld, randomGenerator, chunk_X, chunk_Z));
        if (TerrainGen.generateOre(currentWorld, randomGenerator, dirtGen, chunk_X, chunk_Z, DIRT))
        this.genStandardOre1(20, this.dirtGen, 0, 256);
        if (TerrainGen.generateOre(currentWorld, randomGenerator, gravelGen, chunk_X, chunk_Z, GRAVEL))
        this.genStandardOre1(10, this.gravelGen, 0, 256);
        if (TerrainGen.generateOre(currentWorld, randomGenerator, coalGen, chunk_X, chunk_Z, COAL))
        this.genStandardOre1(20, this.coalGen, 0, 128);
        if (TerrainGen.generateOre(currentWorld, randomGenerator, ironGen, chunk_X, chunk_Z, IRON))
        this.genStandardOre1(20, this.ironGen, 0, 64);
        if (TerrainGen.generateOre(currentWorld, randomGenerator, goldGen, chunk_X, chunk_Z, GOLD))
        this.genStandardOre1(2, this.goldGen, 0, 32);
        if (TerrainGen.generateOre(currentWorld, randomGenerator, redstoneGen, chunk_X, chunk_Z, REDSTONE))
        this.genStandardOre1(8, this.redstoneGen, 0, 16);
        if (TerrainGen.generateOre(currentWorld, randomGenerator, diamondGen, chunk_X, chunk_Z, DIAMOND))
        this.genStandardOre1(1, this.diamondGen, 0, 16);
        if (TerrainGen.generateOre(currentWorld, randomGenerator, lapisGen, chunk_X, chunk_Z, LAPIS))
        this.genStandardOre2(1, this.lapisGen, 16, 16);
        MinecraftForge.ORE_GEN_BUS.post(new OreGenEvent.Post(currentWorld, randomGenerator, chunk_X, chunk_Z));
    }

    // Spigot start - force load chunks
    private int getHighestBlockYAt(int i, int j)
    {
        // Make sure the chunk is loaded
        if (!this.currentWorld.chunkExists(i >> 4, j >> 4))
        {
            // If not, load it, then add it to our unload list
            this.chunksToUnload.add(this.currentWorld.getChunkFromChunkCoords(i >> 4, j >> 4));
        }

        return this.currentWorld.getHeightValue(i, j);
    }

    private void unloadChunks()
    {
        Iterator<Chunk> iter = this.chunksToUnload.iterator();

        while (iter.hasNext())
        {
            Chunk chunk = iter.next();
            if (chunk.bukkitChunk != null)
                this.currentWorld.getWorld().unloadChunk(chunk.bukkitChunk);
            iter.remove();
        }
    }
    // Spigot end

    private int nextInt(int i) {
        if (i <= 1)
            return 0;
        return this.randomGenerator.nextInt(i);
	}
}