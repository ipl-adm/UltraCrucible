package net.minecraft.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.network.Packet;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.ReportedException;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.server.MinecraftServer; // Spigot

import cpw.mods.fml.common.registry.EntityRegistry;

public class EntityTracker
{
	private static final Logger logger = LogManager.getLogger();
	private final WorldServer theWorld;
	private Set<EntityTrackerEntry> trackedEntities = new HashSet<EntityTrackerEntry>(); 
	private final java.util.concurrent.locks.ReentrantLock trackerLock = new java.util.concurrent.locks.ReentrantLock(); // Thermos - prevent entity tracking crashes

	public IntHashMap trackedEntityIDs = new IntHashMap(); // CraftBukkit - private -> public
	private int entityViewDistance;
	private static final String __OBFID = "CL_00001431";

	public EntityTracker(WorldServer p_i1516_1_)
	{
		this.theWorld = p_i1516_1_;
		this.entityViewDistance = p_i1516_1_.func_73046_m().getConfigurationManager().getEntityViewDistance();
	}

	public void waitForLock()
	{
		int counter = 0;
		while(this.trackerLock.isLocked())
		{
			try
			{
				counter += 1;
				if(counter >= 100)
				{
					unlock();
					break;
				}
				Thread.sleep(100L);
			}
			catch(Exception e)
			{

			}
		}
	}
	public void lock()
	{
		this.trackerLock.lock();
	}
	public void unlock()
	{
		if(!this.trackerLock.isLocked()) return;
		this.trackerLock.unlock();
	}

	public void addEntityToTracker(Entity p_72786_1_)
	{
		if (EntityRegistry.instance().tryTrackingEntity(this, p_72786_1_))
		{
			return;
		}

		if (p_72786_1_ instanceof EntityPlayerMP)
		{
			this.addEntityToTracker(p_72786_1_, 512, 2);
			EntityPlayerMP entityplayermp = (EntityPlayerMP)p_72786_1_;

			waitForLock(); // Themrmos Tracker Crash Prevention
			lock(); // TTCP

			for(EntityTrackerEntry entitytrackerentry  : this.trackedEntities)
			{
				if (entitytrackerentry != null && entitytrackerentry.myEntity != entityplayermp)
				{
					entitytrackerentry.tryStartWachingThis(entityplayermp);
				}
			}

			unlock();// TTCP
		}
		else if (p_72786_1_ instanceof EntityFishHook)
		{
			this.addEntityToTracker(p_72786_1_, 64, 5, true);
		}
		else if (p_72786_1_ instanceof EntityArrow)
		{
			this.addEntityToTracker(p_72786_1_, 64, 20, false);
		}
		else if (p_72786_1_ instanceof EntitySmallFireball)
		{
			this.addEntityToTracker(p_72786_1_, 64, 10, false);
		}
		else if (p_72786_1_ instanceof EntityFireball)
		{
			this.addEntityToTracker(p_72786_1_, 64, 10, false);
		}
		else if (p_72786_1_ instanceof EntitySnowball)
		{
			this.addEntityToTracker(p_72786_1_, 64, 10, true);
		}
		else if (p_72786_1_ instanceof EntityEnderPearl)
		{
			this.addEntityToTracker(p_72786_1_, 64, 10, true);
		}
		else if (p_72786_1_ instanceof EntityEnderEye)
		{
			this.addEntityToTracker(p_72786_1_, 64, 4, true);
		}
		else if (p_72786_1_ instanceof EntityEgg)
		{
			this.addEntityToTracker(p_72786_1_, 64, 10, true);
		}
		else if (p_72786_1_ instanceof EntityPotion)
		{
			this.addEntityToTracker(p_72786_1_, 64, 10, true);
		}
		else if (p_72786_1_ instanceof EntityExpBottle)
		{
			this.addEntityToTracker(p_72786_1_, 64, 10, true);
		}
		else if (p_72786_1_ instanceof EntityFireworkRocket)
		{
			this.addEntityToTracker(p_72786_1_, 64, 10, true);
		}
		else if (p_72786_1_ instanceof EntityItem)
		{
			this.addEntityToTracker(p_72786_1_, 64, 20, true);
		}
		else if (p_72786_1_ instanceof EntityMinecart)
		{
			this.addEntityToTracker(p_72786_1_, 80, 3, true);
		}
		else if (p_72786_1_ instanceof EntityBoat)
		{
			this.addEntityToTracker(p_72786_1_, 80, 3, true);
		}
		else if (p_72786_1_ instanceof EntitySquid)
		{
			this.addEntityToTracker(p_72786_1_, 64, 3, true);
		}
		else if (p_72786_1_ instanceof EntityWither)
		{
			this.addEntityToTracker(p_72786_1_, 80, 3, false);
		}
		else if (p_72786_1_ instanceof EntityBat)
		{
			this.addEntityToTracker(p_72786_1_, 80, 3, false);
		}
		else if (p_72786_1_ instanceof IAnimals)
		{
			this.addEntityToTracker(p_72786_1_, 80, 3, true);
		}
		else if (p_72786_1_ instanceof EntityDragon)
		{
			this.addEntityToTracker(p_72786_1_, 160, 3, true);
		}
		else if (p_72786_1_ instanceof EntityTNTPrimed)
		{
			this.addEntityToTracker(p_72786_1_, 160, 10, true);
		}
		else if (p_72786_1_ instanceof EntityFallingBlock)
		{
			this.addEntityToTracker(p_72786_1_, 160, 20, true);
		}
		else if (p_72786_1_ instanceof EntityHanging)
		{
			this.addEntityToTracker(p_72786_1_, 160, Integer.MAX_VALUE, false);
		}
		else if (p_72786_1_ instanceof EntityXPOrb)
		{
			this.addEntityToTracker(p_72786_1_, 160, 20, true);
		}
		else if (p_72786_1_ instanceof EntityEnderCrystal)
		{
			this.addEntityToTracker(p_72786_1_, 256, Integer.MAX_VALUE, false);
		}
	}

	public void addEntityToTracker(Entity p_72791_1_, int p_72791_2_, int p_72791_3_)
	{
		this.addEntityToTracker(p_72791_1_, p_72791_2_, p_72791_3_, false);
	}

	public synchronized void addEntityToTracker(Entity p_72785_1_, int p_72785_2_, final int p_72785_3_, boolean p_72785_4_)
	{
		p_72785_2_ = org.spigotmc.TrackingRange.getEntityTrackingRange(p_72785_1_, p_72785_2_); // Spigot
		if (p_72785_2_ > this.entityViewDistance)
		{
			p_72785_2_ = this.entityViewDistance;
		}

		try
		{
			if (this.trackedEntityIDs.containsItem(p_72785_1_.getEntityId()))
			{
				removeEntityFromAllTrackingPlayers(p_72785_1_);
				System.out.println("[Thermos] Entity tracking error corection code activated...re-adding");
				addEntityToTracker(p_72785_1_, p_72785_2_, p_72785_3_, p_72785_4_);
				return;
			}
			
			EntityTrackerEntry entitytrackerentry = new EntityTrackerEntry(p_72785_1_, p_72785_2_, p_72785_3_, p_72785_4_);

			waitForLock(); lock();// TTCP
			this.trackedEntities.add(entitytrackerentry);
			unlock();// TTCP

			this.trackedEntityIDs.addKey(p_72785_1_.getEntityId(), entitytrackerentry);
			entitytrackerentry.sendEventsToPlayers(this.theWorld.playerEntities);


		}
		catch (Throwable throwable)
		{
			unlock(); // TTCP

			CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Adding entity to track");
			CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity To Track");
			crashreportcategory.addCrashSection("Tracking range", p_72785_2_ + " blocks");
			crashreportcategory.addCrashSectionCallable("Update interval", new Callable()
			{
				private static final String __OBFID = "CL_00001432";
				public String call()
				{
					String s = "Once per " + p_72785_3_ + " ticks";

					if (p_72785_3_ == Integer.MAX_VALUE)
					{
						s = "Maximum (" + s + ")";
					}

					return s;
				}
			});
			p_72785_1_.addEntityCrashInfo(crashreportcategory);
			CrashReportCategory crashreportcategory1 = crashreport.makeCategory("Entity That Is Already Tracked");
			try {
				((EntityTrackerEntry)this.trackedEntityIDs.lookup(p_72785_1_.getEntityId())).myEntity.addEntityCrashInfo(crashreportcategory1);
			} catch(Exception ignored) {}

			try
			{
				throw new ReportedException(crashreport);
			}
			catch (ReportedException reportedexception)
			{
				logger.error("\"Silently\" catching entity tracking error.", reportedexception);
			}
		}
	}

	public void removeEntityFromAllTrackingPlayers(Entity p_72790_1_)
	{
		if (p_72790_1_ instanceof EntityPlayerMP)
		{
			EntityPlayerMP entityplayermp = (EntityPlayerMP)p_72790_1_;
			waitForLock();lock();// TTCP
			for(EntityTrackerEntry entitytrackerentry : this.trackedEntities )
			{
				if (entitytrackerentry != null)
					entitytrackerentry.removeFromWatchingList(entityplayermp);
			}
			unlock();// TTCP
		}

		EntityTrackerEntry entitytrackerentry1 = (EntityTrackerEntry)this.trackedEntityIDs.removeObject(p_72790_1_.getEntityId());

		if (entitytrackerentry1 != null)
		{
			waitForLock();lock();// TTCP
			this.trackedEntities.remove(entitytrackerentry1);
			unlock();// TTCP
			entitytrackerentry1.informAllAssociatedPlayersOfItemDestruction();
		}
	}

	public void updateTrackedEntities()
	{
		waitForLock();lock();// TTCP
		ArrayList arraylist = new ArrayList((int)(this.trackedEntities.size() *1.5) + 1);
		for (EntityTrackerEntry entitytrackerentry : this.trackedEntities)
		{
			if (entitytrackerentry != null) {
				entitytrackerentry.sendLocationToAllClients(this.theWorld.playerEntities);

				if (entitytrackerentry.playerEntitiesUpdated && entitytrackerentry.myEntity instanceof EntityPlayerMP)
				{
					arraylist.add(entitytrackerentry.myEntity);
				}
			}
		}
		unlock();

		for (int i = 0; i < arraylist.size(); ++i)
		{
			EntityPlayerMP entityplayermp = (EntityPlayerMP)arraylist.get(i);
			waitForLock(); lock();// TTCP
			for(EntityTrackerEntry entitytrackerentry1 : this.trackedEntities)
			{

				if (entitytrackerentry1 != null && entitytrackerentry1.myEntity != entityplayermp)
				{
					entitytrackerentry1.tryStartWachingThis(entityplayermp);
				}
			}
			unlock();// TTCP
		}
	}

	public void func_151247_a(Entity p_151247_1_, Packet p_151247_2_)
	{
		EntityTrackerEntry entitytrackerentry = (EntityTrackerEntry)this.trackedEntityIDs.lookup(p_151247_1_.getEntityId());

		if (entitytrackerentry != null)
		{
			entitytrackerentry.func_151259_a(p_151247_2_);
		}
	}

	public void func_151248_b(Entity p_151248_1_, Packet p_151248_2_)
	{
		EntityTrackerEntry entitytrackerentry = (EntityTrackerEntry)this.trackedEntityIDs.lookup(p_151248_1_.getEntityId());

		if (entitytrackerentry != null)
		{
			entitytrackerentry.func_151261_b(p_151248_2_);
		}
	}

	public void removePlayerFromTrackers(EntityPlayerMP p_72787_1_)
	{
		waitForLock(); lock(); // TTCP
		for (EntityTrackerEntry entitytrackerentry: this.trackedEntities)
		{
			if (entitytrackerentry != null)
				entitytrackerentry.removePlayerFromTracker(p_72787_1_);
		}
		unlock();// TTCP
	}

	public void func_85172_a(EntityPlayerMP p_85172_1_, Chunk p_85172_2_)
	{
		waitForLock();lock();// TTCP
		for (EntityTrackerEntry entitytrackerentry : this.trackedEntities)
		{
			if (entitytrackerentry != null && entitytrackerentry.myEntity != p_85172_1_ && entitytrackerentry.myEntity.chunkCoordX == p_85172_2_.xPosition && entitytrackerentry.myEntity.chunkCoordZ == p_85172_2_.zPosition)
			{
				entitytrackerentry.tryStartWachingThis(p_85172_1_);
			}
		}
		unlock();// TTCP
	}

	/* ======================================== FORGE START =====================================*/

	// don't expose the EntityTrackerEntry directly so mods can't mess with the data in there as easily
	/**
	 * Get all players tracking the given Entity. The Entity must be part of the World that this Tracker belongs to.
	 * @param entity the Entity
	 * @return all players tracking the Entity
	 */
	public Set<net.minecraft.entity.player.EntityPlayer> getTrackingPlayers(Entity entity)
	{
		EntityTrackerEntry entry = (EntityTrackerEntry) trackedEntityIDs.lookup(entity.getEntityId());
		if (entry == null)
			return java.util.Collections.emptySet();
		else
			return java.util.Collections.unmodifiableSet(entry.trackingPlayers);
	}

	/* ======================================== FORGE END   =====================================*/
}