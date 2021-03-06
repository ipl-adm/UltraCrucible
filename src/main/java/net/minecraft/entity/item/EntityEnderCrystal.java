package net.minecraft.entity.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;

import org.bukkit.craftbukkit.event.CraftEventFactory; // CraftBukkit

public class EntityEnderCrystal extends Entity
{
    public int innerRotation;
    public int health;
    private static final String __OBFID = "CL_00001658";

    public EntityEnderCrystal(World p_i1698_1_)
    {
        super(p_i1698_1_);
        this.preventEntitySpawning = true;
        this.setSize(2.0F, 2.0F);
        this.yOffset = this.height / 2.0F;
        this.health = 5;
        this.innerRotation = this.rand.nextInt(100000);
    }

    @SideOnly(Side.CLIENT)
    public EntityEnderCrystal(World p_i1699_1_, double p_i1699_2_, double p_i1699_4_, double p_i1699_6_)
    {
        this(p_i1699_1_);
        this.setPosition(p_i1699_2_, p_i1699_4_, p_i1699_6_);
    }

    protected boolean canTriggerWalking()
    {
        return false;
    }

    protected void entityInit()
    {
        this.dataWatcher.addObject(8, Integer.valueOf(this.health));
    }

    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        ++this.innerRotation;
        this.dataWatcher.updateObject(8, Integer.valueOf(this.health));
        int i = MathHelper.floor_double(this.posX);
        int j = MathHelper.floor_double(this.posY);
        int k = MathHelper.floor_double(this.posZ);

        if (this.worldObj.provider instanceof WorldProviderEnd && this.worldObj.getBlock(i, j, k) != Blocks.fire)
        {
            // CraftBukkit start
            if (!CraftEventFactory.callBlockIgniteEvent(this.worldObj, i, j, k, this).isCancelled())
            {
                this.worldObj.setBlock(i, j, k, Blocks.fire);
            }

            // CraftBukkit end
        }
    }

    protected void writeEntityToNBT(NBTTagCompound p_70014_1_) {}

    protected void readEntityFromNBT(NBTTagCompound p_70037_1_) {}

    @SideOnly(Side.CLIENT)
    public float getShadowSize()
    {
        return 0.0F;
    }

    public boolean canBeCollidedWith()
    {
        return true;
    }

    public boolean attackEntityFrom(DamageSource p_70097_1_, float p_70097_2_)
    {
        if (this.isEntityInvulnerable())
        {
            return false;
        }
        else
        {
            if (!this.isDead && !this.worldObj.isRemote)
            {
                // CraftBukkit start - All non-living entities need this
                if (CraftEventFactory.handleNonLivingEntityDamageEvent(this, p_70097_1_, p_70097_2_))
                {
                    return false;
                }

                // CraftBukkit end
                this.health = 0;

                if (this.health <= 0)
                {
                    this.setDead();

                    if (!this.worldObj.isRemote)
                    {
                        this.worldObj.createExplosion(this, this.posX, this.posY, this.posZ, 6.0F, true); // CraftBukkit - (Entity) null -> this
                    }
                }
            }

            return true;
        }
    }

    // Cauldron start
    @Override
    public boolean entityProjectileHook() {
        return true;
    }
    // Cauldron end
}