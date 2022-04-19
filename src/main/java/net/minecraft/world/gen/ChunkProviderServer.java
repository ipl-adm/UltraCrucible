package net.minecraft.world.gen;

import com.google.common.collect.Lists;

import cpw.mods.fml.common.registry.GameRegistry;
import gnu.trove.impl.sync.TSynchronizedLongObjectMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.github.crucible.CrucibleConfigs;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.LongHashMap;
import net.minecraft.util.ReportedException;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.chunkio.ChunkIOExecutor;
import thermos.wrapper.ChunkBlockHashMap;
import thermos.wrapper.VanillaChunkHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;





// CraftBukkit start
import java.util.Random;

import net.minecraft.block.BlockSand;

import org.bukkit.Server;
import org.bukkit.craftbukkit.util.LongHash;
import org.bukkit.craftbukkit.util.LongHashSet;
import org.bukkit.craftbukkit.util.LongObjectHashMap;
import org.bukkit.event.world.ChunkUnloadEvent;





// CraftBukkit end
// Cauldron start
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.cauldron.configuration.CauldronConfig;
import net.minecraftforge.cauldron.CauldronHooks;
// Cauldron end

public class ChunkProviderServer implements IChunkProvider
{

/*
    private Set chunksToUnload = Collections.newSetFromMap(new ConcurrentHashMap());
    private Chunk defaultEmptyChunk;
    public IChunkProvider currentChunkProvider;
    public IChunkLoader currentChunkLoader;
    public boolean loadChunkOnProvideRequest = true;
    public LongHashMap loadedChunkHashMap = new LongHashMap();
    public List loadedChunks = new ArrayList();
    public WorldServer worldObj;
    private Set<Long> loadingChunks = com.google.common.collect.Sets.newHashSet();

*/

    private static final Logger logger = LogManager.getLogger();
    public LongHashSet chunksToUnload = new LongHashSet(); // LongHashSet
    public Chunk defaultEmptyChunk;
    public IChunkProvider currentChunkProvider;
    public IChunkLoader currentChunkLoader;
    public boolean loadChunkOnProvideRequest = CrucibleConfigs.configs.cauldron_settings_loadChunkOnRequest; // Cauldron - if true, allows mods to force load chunks. to disable, set load-chunk-on-request in cauldron.yml to false
    public int initialTick; // Cauldron counter to keep track of when this loader was created
    public List loadedChunks = new ArrayList(); // Cauldron - vanilla compatibility
    public WorldServer worldObj;
    private Set<Long> loadingChunks = com.google.common.collect.Sets.newHashSet();
    public VanillaChunkHashMap loadedChunkHashMap_KC = new thermos.wrapper.VanillaChunkHashMap(new ChunkBlockHashMap());// Mobiuscraft KC compat
    public LongHashMap loadedChunkHashMap = loadedChunkHashMap_KC.thisIsNotMyRealFace(); // KCauldron - vanilla/mystcraft compatibility
    private static final String __OBFID = "CL_00001436";

    public ChunkProviderServer(WorldServer p_i1520_1_, IChunkLoader p_i1520_2_, IChunkProvider p_i1520_3_)
    {
        this.initialTick = MinecraftServer.currentTick; // Cauldron keep track of when the loader was created
        this.defaultEmptyChunk = new EmptyChunk(p_i1520_1_, 0, 0);
        this.worldObj = p_i1520_1_;
        this.currentChunkLoader = p_i1520_2_;
        this.currentChunkProvider = p_i1520_3_;
    }

    public boolean chunkExists(int p_73149_1_, int p_73149_2_)
    {
        return this.loadedChunkHashMap_KC.rawThermos().get(p_73149_1_, p_73149_2_) != null; //Thermos Replacement
    }

    public List func_152380_a() // Vanilla compatibility
    {
        return this.loadedChunks;
    }

    public void unloadChunksIfNotNearSpawn(int p_73241_1_, int p_73241_2_)
    {
        // PaperSpigot start - Asynchronous lighting updates
        Chunk chunk = this.loadedChunkHashMap_KC.rawThermos().get(p_73241_1_, p_73241_2_); //Thermos replacement
        // Thermos don't light modded chunks
        boolean modFlag = false;
        try { if (chunk.worldObj.isModded == null) chunk.worldObj.isModded = false; modFlag = chunk.worldObj.isModded;} catch(Exception e){}
        
        if (chunk != null && (chunk.worldObj.spigotConfig.useAsyncLighting && !modFlag) && (chunk.pendingLightUpdates.get() > 0 || chunk.worldObj.getTotalWorldTime() - chunk.lightUpdateTime < 20)) {
            return;
        }
        // PaperSpigot end
        if (this.worldObj.provider.canRespawnHere() && DimensionManager.shouldLoadSpawn(this.worldObj.provider.dimensionId))
        {
            ChunkCoordinates chunkcoordinates = this.worldObj.getSpawnPoint();
            int k = p_73241_1_ * 16 + 8 - chunkcoordinates.posX;
            int l = p_73241_2_ * 16 + 8 - chunkcoordinates.posZ;
            short short1 = 128;

            // CraftBukkit start
            if (k < -short1 || k > short1 || l < -short1 || l > short1)
            {
                this.chunksToUnload.add(p_73241_1_, p_73241_2_);
                Chunk c = (this.loadedChunkHashMap_KC.rawThermos().get(p_73241_1_, p_73241_2_)); //Thermos replacement

                if (c != null)
                {
                    c.mustSave = true;
                }
                CauldronHooks.logChunkUnload(this, p_73241_1_, p_73241_2_, "Chunk added to unload queue");
            }

            // CraftBukkit end
        }
        else
        {
            // CraftBukkit start
            this.chunksToUnload.add(p_73241_1_, p_73241_2_);
            Chunk c = (this.loadedChunkHashMap_KC.rawThermos().get(p_73241_1_, p_73241_2_)); //KCauldron replacement

            if (c != null)
            {
                c.mustSave = true;
            }
            CauldronHooks.logChunkUnload(this, p_73241_1_, p_73241_2_, "Chunk added to unload queue");
            // CraftBukkit end
        }
    }

    public void unloadAllChunks()
    {
    	for(Chunk chunk : this.loadedChunkHashMap_KC.rawVanilla().values())
    	{
    		unloadChunksIfNotNearSpawn(chunk.xPosition, chunk.zPosition);
    	}
    }

    public Chunk getChunkIfLoaded(int x, int z) {
    	return this.loadedChunkHashMap_KC.rawThermos().get(x,z); //Thermos replacement
    }

    public Chunk loadChunk(int p_73158_1_, int p_73158_2_)
    {
        return loadChunk(p_73158_1_, p_73158_2_, null);
    }

    public Chunk loadChunk(int par1, int par2, Runnable runnable)
    {
        this.chunksToUnload.remove(par1, par2);
        Chunk chunk = this.loadedChunkHashMap_KC.rawThermos().get(par1,par2); //Thermos replacement
        boolean newChunk = false;
        AnvilChunkLoader loader = null;

        if (this.currentChunkLoader instanceof AnvilChunkLoader)
        {
            loader = (AnvilChunkLoader) this.currentChunkLoader;
        }

        CauldronHooks.logChunkLoad(this, "Get", par1, par2, true);

        // We can only use the queue for already generated chunks
        if (chunk == null && loader != null && loader.chunkExists(this.worldObj, par1, par2))
        {
            if (runnable != null)
            {
                ChunkIOExecutor.queueChunkLoad(this.worldObj, loader, this, par1, par2, runnable);
                return null;
            }
            else
            {
                chunk = ChunkIOExecutor.syncChunkLoad(this.worldObj, loader, this, par1, par2);
            }
        }
        else if (chunk == null)
        {
            chunk = this.originalLoadChunk(par1, par2);
        }

        // If we didn't load the chunk async and have a callback run it now
        if (runnable != null)
        {
            runnable.run();
        }

        return chunk;
    }

    public Chunk originalLoadChunk(int p_73158_1_, int p_73158_2_)
    {
        this.chunksToUnload.remove(p_73158_1_, p_73158_2_);
        Chunk chunk = this.loadedChunkHashMap_KC.rawThermos().get(p_73158_1_, p_73158_2_); //Thermos replacement
        boolean newChunk = false; // CraftBukkit

        if (chunk == null)
        {
            worldObj.timings.syncChunkLoadTimer.startTiming(); // Spigot
            boolean added = loadingChunks.add(LongHash.toLong(p_73158_1_, p_73158_2_));
            if (!added)
            {
                cpw.mods.fml.common.FMLLog.bigWarning("There is an attempt to load a chunk (%d,%d) in dimension %d that is already being loaded. This will cause weird chunk breakages.", p_73158_1_, p_73158_2_, worldObj.provider.dimensionId);
            }
            chunk = ForgeChunkManager.fetchDormantChunk(LongHash.toLong(p_73158_1_, p_73158_2_), this.worldObj);
            if (chunk == null)
            {
                chunk = this.safeLoadChunk(p_73158_1_, p_73158_2_);
            }

            if (chunk == null)
            {
                if (this.currentChunkProvider == null)
                {
                    chunk = this.defaultEmptyChunk;
                }
                else
                {
                    try
                    {
                        chunk = this.currentChunkProvider.provideChunk(p_73158_1_, p_73158_2_);
                    }
                    catch (Throwable throwable)
                    {
                        CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception generating new chunk");
                        CrashReportCategory crashreportcategory = crashreport.makeCategory("Chunk to be generated");
                        crashreportcategory.addCrashSection("Location", String.format("%d,%d", new Object[] {Integer.valueOf(p_73158_1_), Integer.valueOf(p_73158_2_)}));
                        crashreportcategory.addCrashSection("Position hash", LongHash.toLong(p_73158_1_, p_73158_2_));
                        crashreportcategory.addCrashSection("Generator", this.currentChunkProvider.makeString());
                        throw new ReportedException(crashreport);
                    }
                }

                newChunk = true; // CraftBukkit
            }

            this.loadedChunkHashMap_KC.add(LongHash.toLong(p_73158_1_, p_73158_2_), chunk); // Will pass on to chunkt_TH
            this.loadedChunks.add(chunk); // Cauldron - vanilla compatibility
            loadingChunks.remove(LongHash.toLong(p_73158_1_, p_73158_2_)); // Cauldron - LongHash

            if (chunk != null)
            {
                chunk.onChunkLoad();
            }
            // CraftBukkit start
            Server server = this.worldObj.getServer();

            if (server != null)
            {
                /*
                 * If it's a new world, the first few chunks are generated inside
                 * the World constructor. We can't reliably alter that, so we have
                 * no way of creating a CraftWorld/CraftServer at that point.
                 */
                server.getPluginManager().callEvent(new org.bukkit.event.world.ChunkLoadEvent(chunk.bukkitChunk, newChunk));
            }

            // Update neighbor counts
            for (int x = -2; x < 3; x++) {
                for (int z = -2; z < 3; z++) {
                    if (x == 0 && z == 0) {
                        continue;
                    }

                    Chunk neighbor = this.getChunkIfLoaded(chunk.xPosition + x, chunk.zPosition + z);
                    if (neighbor != null) {
                        neighbor.setNeighborLoaded(-x, -z);
                        chunk.setNeighborLoaded(x, z);
                    }
                }
            }
            // CraftBukkit end
            chunk.populateChunk(this, this, p_73158_1_, p_73158_2_);
            worldObj.timings.syncChunkLoadTimer.stopTiming(); // Spigot
        }

        return chunk;
    }

    public Chunk provideChunk(int p_73154_1_, int p_73154_2_)
    {

        // CraftBukkit start
        Chunk chunk = (Chunk) this.loadedChunkHashMap_KC.rawThermos().get(p_73154_1_, p_73154_2_);
        chunk = chunk == null ? (shouldLoadChunk() ? this.loadChunk(p_73154_1_, p_73154_2_) : this.defaultEmptyChunk) : chunk; // Cauldron handle forge server tick events and load the chunk within 5 seconds of the world being loaded (for chunk loaders)

        if (chunk == this.defaultEmptyChunk)
        {
            return chunk;
        }
        
        if (chunk == null)
        {
        	logger.error("Provided chunk is null for (" +p_73154_1_ + ", " + p_73154_2_+") !");
        	return null;
        }
        
        try
        {
        	worldObj.isProfilingWorld();
        }
        catch (Throwable t)
        {
        	return chunk;
        }
        
        if ((p_73154_1_ != chunk.xPosition || p_73154_2_ != chunk.zPosition) && !worldObj.isProfilingWorld())
        {
            logger.error("Chunk (" + chunk.xPosition + ", " + chunk.zPosition + ") stored at  (" + p_73154_1_ + ", " + p_73154_2_ + ") in world '" + worldObj.getWorld().getName() + "'");
            logger.error(chunk.getClass().getName());
            Throwable ex = new Throwable();
            ex.fillInStackTrace();
            ex.printStackTrace();
        }
        chunk.lastAccessedTick = MinecraftServer.getServer().getTickCounter(); // Cauldron
        return chunk;
        // CraftBukkit end
    }

    public Chunk safeLoadChunk(int p_73239_1_, int p_73239_2_) // CraftBukkit - private -> public
    {
        if (this.currentChunkLoader == null)
        {
            return null;
        }
        else
        {
            try
            {
                CauldronHooks.logChunkLoad(this, "Safe Load", p_73239_1_, p_73239_2_, false); // Cauldron
                Chunk chunk = this.currentChunkLoader.loadChunk(this.worldObj, p_73239_1_, p_73239_2_);

                if (chunk != null)
                {
                    chunk.lastSaveTime = this.worldObj.getTotalWorldTime();

                    if (this.currentChunkProvider != null)
                    {
                        worldObj.timings.syncChunkLoadStructuresTimer.startTiming(); // Spigot
                        this.currentChunkProvider.recreateStructures(p_73239_1_, p_73239_2_);
                        worldObj.timings.syncChunkLoadStructuresTimer.stopTiming(); // Spigot
                    }
                    chunk.lastAccessedTick = MinecraftServer.getServer().getTickCounter(); // Cauldron
                }

                return chunk;
            }
            catch (Exception exception)
            {
                logger.error("Couldn\'t load chunk", exception);
                return null;
            }
        }
    }

    public void safeSaveExtraChunkData(Chunk p_73243_1_)   // CraftBukkit - private -> public
    {
        if (this.currentChunkLoader != null)
        {
            try
            {
                this.currentChunkLoader.saveExtraChunkData(this.worldObj, p_73243_1_);
            }
            catch (Exception exception)
            {
                logger.error("Couldn\'t save entities", exception);
            }
        }
    }

    public void safeSaveChunk(Chunk p_73242_1_)   // CraftBukkit - private -> public
    {
        if (this.currentChunkLoader != null)
        {
            try
            {
                p_73242_1_.lastSaveTime = this.worldObj.getTotalWorldTime();
                this.currentChunkLoader.saveChunk(this.worldObj, p_73242_1_);
                // CraftBukkit start - IOException to Exception
            }
            catch (Exception ioexception)
            {
                logger.error("Couldn\'t save chunk", ioexception);
            }
            /* Remove extra exception
            catch (MinecraftException minecraftexception)
            {
                logger.error("Couldn\'t save chunk; already in use by another instance of Minecraft?", minecraftexception);
            }
            // CraftBukkit end */
        }
    }

    public void populate(IChunkProvider p_73153_1_, int p_73153_2_, int p_73153_3_)
    {
        Chunk chunk = this.provideChunk(p_73153_2_, p_73153_3_);

        if (!chunk.isTerrainPopulated)
        {
            chunk.func_150809_p();

            if (this.currentChunkProvider != null)
            {
                try (co.aikar.timings.Timing ignored = this.worldObj.timings.syncChunkLoadPopulateTimer.startTiming()) { // Paper //Crucible - Is this right? I think so
                this.currentChunkProvider.populate(p_73153_1_, p_73153_2_, p_73153_3_);
                // CraftBukkit start
                BlockSand.fallInstantly = true;
                Random random = new Random();
                random.setSeed(worldObj.getSeed());
                long xRand = random.nextLong() / 2L * 2L + 1L;
                long zRand = random.nextLong() / 2L * 2L + 1L;
                random.setSeed((long) p_73153_2_ * xRand + (long) p_73153_3_ * zRand ^ worldObj.getSeed());
                org.bukkit.World world = this.worldObj.getWorld();

                if (world != null)
                {
                    this.worldObj.populating = true;

                    try
                    {
                        for (org.bukkit.generator.BlockPopulator populator : world.getPopulators())
                        {
                            populator.populate(world, random, chunk.bukkitChunk);
                        }
                    }
                    finally
                    {
                        this.worldObj.populating = false;
                    }
                }

                BlockSand.fallInstantly = false;
                this.worldObj.getServer().getPluginManager().callEvent(new org.bukkit.event.world.ChunkPopulateEvent(chunk.bukkitChunk));
                // CraftBukkit end
                GameRegistry.generateWorld(p_73153_2_, p_73153_3_, worldObj, currentChunkProvider, p_73153_1_);
                chunk.setChunkModified();
                } // Paper
            }
        }
    }

    public boolean saveChunks(boolean p_73151_1_, IProgressUpdate p_73151_2_)
    {
        try (co.aikar.timings.Timing timed = worldObj.timings.chunkSaveData.startTiming()) { // Paper - Timings //Crucible - Is this right? I think so
        int i = 0;
        // Cauldron start - use thread-safe method for iterating loaded chunks
        Object[] chunks = this.loadedChunks.toArray();

        for (int j = 0; j < chunks.length; ++j)
        {
            if (chunks[j] == null) { continue; }
            Chunk chunk = (Chunk)chunks[j];
            //Cauldron end

            if (p_73151_1_)
            {
                this.safeSaveExtraChunkData(chunk);
            }

            if (chunk.needsSaving(p_73151_1_))
            {
                this.safeSaveChunk(chunk);
                chunk.isModified = false;
                ++i;

                if (i == 24 && !p_73151_1_)
                {
                    return false;
                }
            }
        }
        } // Paper - Timings
        return true;
    }

    public void saveExtraData()
    {
        if (this.currentChunkLoader != null)
        {
            this.currentChunkLoader.saveExtraData();
        }
    }

    public boolean unloadQueuedChunks()
    {
        if (!this.worldObj.levelSaving)
        {
            // Cauldron start - remove any chunk that has a ticket associated with it
            if (!this.chunksToUnload.isEmpty())
            {
                for (ChunkCoordIntPair forcedChunk : this.worldObj.getPersistentChunks().keySet())
                {
                    this.chunksToUnload.remove(forcedChunk.chunkXPos, forcedChunk.chunkZPos);
                }
            }
            // Cauldron end
            // CraftBukkit start
            Server server = this.worldObj.getServer();

            for (int i = 0; i < 100 && !this.chunksToUnload.isEmpty(); i++)
            {
                long chunkcoordinates = this.chunksToUnload.popFirst();
                Chunk chunk = (Chunk) this.loadedChunkHashMap_KC.getValueByKey(chunkcoordinates);

                if (chunk == null)
                {
                    continue;
                }

                // Cauldron static - check if the chunk was accessed recently and keep it loaded if there are players in world
                if (!shouldUnloadChunk(chunk) && this.worldObj.playerEntities.size() > 0)
                {
                    CauldronHooks.logChunkUnload(this, chunk.xPosition, chunk.zPosition, "** Chunk kept from unloading due to recent activity");
                    continue;
                }
                // Cauldron end


                ChunkUnloadEvent event = new ChunkUnloadEvent(chunk.bukkitChunk);
                server.getPluginManager().callEvent(event);

                if (!event.isCancelled())
                {
                    CauldronHooks.logChunkUnload(this, chunk.xPosition, chunk.zPosition, "Unloading Chunk at");

                    chunk.onChunkUnload();
                    this.safeSaveChunk(chunk);
                    this.safeSaveExtraChunkData(chunk);
                    // Update neighbor counts
                    for (int x = -2; x < 3; x++) {
                        for (int z = -2; z < 3; z++) {
                            if (x == 0 && z == 0) {
                                continue;
                            }

                            Chunk neighbor = this.getChunkIfLoaded(chunk.xPosition + x, chunk.zPosition + z);
                            if (neighbor != null) {
                                neighbor.setNeighborUnloaded(-x, -z);
                                chunk.setNeighborUnloaded(x, z);
                            }
                        }
                    }
                    this.loadedChunkHashMap_KC.remove(chunkcoordinates); // CraftBukkit, will pass to chunkt_TH
                    this.loadedChunks.remove(chunk);

                    ForgeChunkManager.putDormantChunk(chunkcoordinates, chunk);
                    if(this.loadedChunkHashMap_KC.rawThermos().size() == 0 && ForgeChunkManager.getPersistentChunksFor(this.worldObj).size() == 0 && !DimensionManager.shouldLoadSpawn(this.worldObj.provider.dimensionId)){
                        DimensionManager.unloadWorld(this.worldObj.provider.dimensionId);
                        return currentChunkProvider.unloadQueuedChunks();
                    }
                }
            }

            // CraftBukkit end

            if (this.currentChunkLoader != null)
            {
                this.currentChunkLoader.chunkTick();
            }
        }

        return this.currentChunkProvider.unloadQueuedChunks();
    }

    public boolean canSave()
    {
        return !this.worldObj.levelSaving;
    }

    public String makeString()
    {
        return "ServerChunkCache: " + this.loadedChunkHashMap_KC.rawThermos().size() + " Drop: " + this.chunksToUnload.size(); // Cauldron
    }

    public List getPossibleCreatures(EnumCreatureType p_73155_1_, int p_73155_2_, int p_73155_3_, int p_73155_4_)
    {
        return this.currentChunkProvider.getPossibleCreatures(p_73155_1_, p_73155_2_, p_73155_3_, p_73155_4_);
    }

    public ChunkPosition func_147416_a(World p_147416_1_, String p_147416_2_, int p_147416_3_, int p_147416_4_, int p_147416_5_)
    {
        return this.currentChunkProvider.func_147416_a(p_147416_1_, p_147416_2_, p_147416_3_, p_147416_4_, p_147416_5_);
    }

    public int getLoadedChunkCount()
    {
        return this.loadedChunkHashMap_KC.rawThermos().size(); // Cauldron
    }

    public void recreateStructures(int p_82695_1_, int p_82695_2_) {}

    // Cauldron start
    private boolean shouldLoadChunk()
    {
        return this.worldObj.findingSpawnPoint ||
                this.loadChunkOnProvideRequest ||
                (MinecraftServer.callingForgeTick && CrucibleConfigs.configs.cauldron_settings_loadChunkOnForgeTick) ||
                (MinecraftServer.currentTick - initialTick <= 100);
    }

    public long lastAccessed(int x, int z)
    {
        Chunk c = this.loadedChunkHashMap_KC.rawThermos().get(x,z);
        if(c == null)return 0;
        else return c.lastAccessedTick;
    }

    private boolean shouldUnloadChunk(Chunk chunk)
    {
        if (chunk == null) return false;
        return MinecraftServer.getServer().getTickCounter() - chunk.lastAccessedTick > CrucibleConfigs.configs.cauldron_settings_chunkGCGracePeriod;
    }


    // Cauldron end
}