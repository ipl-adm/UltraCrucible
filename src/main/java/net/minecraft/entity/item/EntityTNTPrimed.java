package net.minecraft.entity.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import thermos.thermite.ThermiteRandom;

import java.util.Random;

import org.bukkit.event.entity.ExplosionPrimeEvent; // CraftBukkit

public class EntityTNTPrimed extends Entity
{
    public int fuse;
    private EntityLivingBase tntPlacedBy;
    private static final String __OBFID = "CL_00001681";
    public float yield = 4; // CraftBukkit
    public boolean isIncendiary = false; // CraftBukkit
    private static final Random random = new ThermiteRandom();

    public EntityTNTPrimed(World p_i1729_1_)
    {
        super(p_i1729_1_);
        this.preventEntitySpawning = true;
        this.setSize(0.98F, 0.98F);
        this.yOffset = this.height / 2.0F;
    }

    public EntityTNTPrimed(World p_i1730_1_, double p_i1730_2_, double p_i1730_4_, double p_i1730_6_, EntityLivingBase p_i1730_8_)
    {
        this(p_i1730_1_);
        this.setPosition(p_i1730_2_, p_i1730_4_, p_i1730_6_);
        float f = (float)(random.nextDouble() * Math.PI * 2.0D);
        this.motionX = (double)(-(MathHelper.sin(f)) * 0.02F);
        this.motionY = 0.20000000298023224D;
        this.motionZ = (double)(-(MathHelper.cos(f)) * 0.02F);
        this.fuse = 80;
        this.prevPosX = p_i1730_2_;
        this.prevPosY = p_i1730_4_;
        this.prevPosZ = p_i1730_6_;
        this.tntPlacedBy = p_i1730_8_;
    }

    protected void entityInit() {}

    protected boolean canTriggerWalking()
    {
        return false;
    }

    public boolean canBeCollidedWith()
    {
        return !this.isDead;
    }

    public void onUpdate()
    {
        if (worldObj.spigotConfig.currentPrimedTnt++ > worldObj.spigotConfig.maxTntTicksPerTick) { return; } // Spigot
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.motionY -= 0.03999999910593033D;
        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;

        if (this.onGround)
        {
            this.motionX *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
            this.motionY *= -0.5D;
        }

        if (this.fuse-- <= 0)
        {
            // CraftBukkit start - Need to reverse the order of the explosion and the entity death so we have a location for the event
            if (!this.worldObj.isRemote)
            {
                this.explode();
            }

            this.setDead();
            // CraftBukkit end
        }
        else
        {
            this.worldObj.spawnParticle("smoke", this.posX, this.posY + 0.5D, this.posZ, 0.0D, 0.0D, 0.0D);
        }
    }

    private void explode()
    {
        // CraftBukkit start
        // float f = 4.0F;
        org.bukkit.craftbukkit.CraftServer server = this.worldObj.getServer();
        ExplosionPrimeEvent event = new ExplosionPrimeEvent((org.bukkit.entity.Explosive) org.bukkit.craftbukkit.entity.CraftEntity.getEntity(server, this));
        server.getPluginManager().callEvent(event);

        if (!event.isCancelled())
        {
            // give 'this' instead of (Entity) null so we know what causes the damage
            this.worldObj.newExplosion(this, this.posX, this.posY, this.posZ, event.getRadius(), event.getFire(), true);
        }

        // CraftBukkit end
    }

    protected void writeEntityToNBT(NBTTagCompound p_70014_1_)
    {
        p_70014_1_.setByte("Fuse", (byte)this.fuse);
    }

    protected void readEntityFromNBT(NBTTagCompound p_70037_1_)
    {
        this.fuse = p_70037_1_.getByte("Fuse");
    }

    @SideOnly(Side.CLIENT)
    public float getShadowSize()
    {
        return 0.0F;
    }

    public EntityLivingBase getTntPlacedBy()
    {
        return this.tntPlacedBy;
    }

    // Cauldron start
    @Override
    public boolean entityProjectileHook() {
        return true;
    }
    // Cauldron end
}