package net.minecraftforge.event.world;

import java.util.List;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.*;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.*;

import net.minecraftforge.common.util.*;

import com.mojang.authlib.GameProfile;

/** ExplosionEvent triggers when an explosion happens in the world.<br>
 * <br>
 * ExplosionEvent.Start is fired before the explosion actually occurs.<br>
 * ExplosionEvent.Detonate is fired once the explosion has a list of affected blocks and entities.<br>
 * <br>
 * ExplosionEvent.Start is {@link Cancelable}.<br>
 * ExplosionEvent.Detonate can modify the affected blocks and entities.<br>
 * Children do not use {@link HasResult}.<br>
 * Children of this event are fired on the {@link MinecraftForge#EVENT_BUS}.<br>
 */
public class ExplosionEvent extends Event
{
    public final World world;
    public final Explosion explosion;
    public static FakePlayer exploder_fake = null;
    public static final GameProfile exploder_profile = new GameProfile(null, "[Explosive]");

    public ExplosionEvent(World world, Explosion explosion)
    {
        if(exploder_fake == null || !exploder_fake.worldObj.equals(world)) { exploder_fake = FakePlayerFactory.get( (WorldServer) world, exploder_profile); }
        this.world = world;
        this.explosion = explosion;
    }

    /** ExplosionEvent.Start is fired before the explosion actually occurs.  Canceling this event will stop the explosion.<br>
     * <br>
     * This event is {@link Cancelable}.<br>
     * This event does not use {@link HasResult}.<br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.<br>
     */
    @Cancelable
    public static class Start extends ExplosionEvent
    {
        private ExplosionPrimeEvent event;
        public Start(World world, Explosion explosion)
        {
            super(world, explosion);
            // CraftBukkit start
            // float f = 4.0F;
            org.bukkit.craftbukkit.CraftServer server = world.getServer();
            org.bukkit.craftbukkit.entity.CraftEntity ce = null;
            if(explosion.exploder != null && explosion.exploder instanceof EntityLivingBase)
            {
                ce = new org.bukkit.craftbukkit.entity.CraftTNTPrimed(server, new EntityTNTPrimed(world, explosion.explosionX, explosion.explosionY, explosion.explosionZ, (EntityLivingBase) explosion.exploder ));
            }
	    if(ce == null)
            {
                ce = new org.bukkit.craftbukkit.entity.CraftTNTPrimed(server, new EntityTNTPrimed(world, explosion.explosionX, explosion.explosionY, explosion.explosionZ, exploder_fake ));
            }
            event = new ExplosionPrimeEvent(ce, 8.0F, true);
            server.getPluginManager().callEvent(event);
        }
        @Override
        public boolean isCanceled()
        {
	Entity p_72885_1_ = explosion.exploder;
            return super.isCanceled() || this.event.isCancelled();
        }
        @Override
        public void setCanceled(boolean cancel)
        {
            if (!isCancelable())
            {
                throw new IllegalArgumentException("Attempted to cancel a uncancelable event");
            }
            super.setCanceled(cancel); this.event.setCancelled(cancel);
        }

    }

    /** ExplosionEvent.Detonate is fired once the explosion has a list of affected blocks and entities.  These lists can be modified to change the outcome.<br>
     * <br>
     * This event is not {@link Cancelable}.<br>
     * This event does not use {@link HasResult}.<br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.<br>
     */
    public static class Detonate extends ExplosionEvent
    {
        private final List<Entity> entityList;

        public Detonate(World world, Explosion explosion, List<Entity> entityList)
        {
            super(world, explosion);
            this.entityList = entityList;
        }

        /** return the list of blocks affected by the explosion. */
        public List<ChunkPosition> getAffectedBlocks()
        {
            return explosion.affectedBlockPositions;
        }

        /** return the list of entities affected by the explosion. */
        public List<Entity> getAffectedEntities()
        {
            return entityList;
        }
    }
}