package net.minecraft.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

// CraftBukkit start
import net.minecraft.entity.player.EntityPlayerMP;

import org.bukkit.entity.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEggThrowEvent;

import net.minecraft.entity.Entity;
// CraftBukkit end

public class EntityEgg extends EntityThrowable
{
    private static final String __OBFID = "CL_00001724";

    public EntityEgg(World p_i1779_1_)
    {
        super(p_i1779_1_);
    }

    public EntityEgg(World p_i1780_1_, EntityLivingBase p_i1780_2_)
    {
        super(p_i1780_1_, p_i1780_2_);
    }

    public EntityEgg(World p_i1781_1_, double p_i1781_2_, double p_i1781_4_, double p_i1781_6_)
    {
        super(p_i1781_1_, p_i1781_2_, p_i1781_4_, p_i1781_6_);
    }

    protected void onImpact(MovingObjectPosition p_70184_1_)
    {
        if (p_70184_1_.entityHit != null)
        {
            p_70184_1_.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), 0.0F);
        }

        // CraftBukkit start
        boolean hatching = !this.worldObj.isRemote && this.rand.nextInt(8) == 0;
        int numHatching = (this.rand.nextInt(32) == 0) ? 4 : 1;

        if (!hatching)
        {
            numHatching = 0;
        }

        EntityType hatchingType = EntityType.CHICKEN;
        Entity shooter = this.getThrower();

        if (shooter instanceof EntityPlayerMP)
        {
            Player player = (shooter == null) ? null : (Player) shooter.getBukkitEntity();
            PlayerEggThrowEvent event = new PlayerEggThrowEvent(player, (org.bukkit.entity.Egg) this.getBukkitEntity(), hatching, (byte) numHatching, hatchingType);
            this.worldObj.getServer().getPluginManager().callEvent(event);
            hatching = event.isHatching();
            numHatching = event.getNumHatches();
            hatchingType = event.getHatchingType();
        }

        if (hatching)
        {
            for (int k = 0; k < numHatching; k++)
            {
                org.bukkit.entity.Entity entity = worldObj.getWorld().spawn(new org.bukkit.Location(worldObj.getWorld(), this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F), hatchingType.getEntityClass(), org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.EGG);

                if (entity instanceof Ageable)
                {
                    ((Ageable) entity).setBaby();
                }
            }
        }

        // CraftBukkit end

        for (int j = 0; j < 8; ++j)
        {
            this.worldObj.spawnParticle("snowballpoof", this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
        }

        if (!this.worldObj.isRemote)
        {
            this.setDead();
        }
    }

    // Cauldron start
    @Override
    public boolean entityProjectileHook() {
        return true;
    }
    // Cauldron end
}