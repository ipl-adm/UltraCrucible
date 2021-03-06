package net.minecraft.entity.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;


// CraftBukkit start
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent;
// CraftBukkit end

public class EntityXPOrb extends Entity
{
    public int xpColor;
    public int xpOrbAge;
    public int field_70532_c;
    private int xpOrbHealth = 5;
    public int xpValue; // CraftBukkit - private -> public
    private EntityPlayer closestPlayer;
    private int xpTargetColor;
    private static final String __OBFID = "CL_00001544";

    public EntityXPOrb(World p_i1585_1_, double p_i1585_2_, double p_i1585_4_, double p_i1585_6_, int p_i1585_8_)
    {
        super(p_i1585_1_);
        this.setSize(0.5F, 0.5F);
        this.yOffset = this.height / 2.0F;
        this.setPosition(p_i1585_2_, p_i1585_4_, p_i1585_6_);
        this.rotationYaw = (float)(Math.random() * 360.0D);
        this.motionX = (double)((float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D) * 2.0F);
        this.motionY = (double)((float)(Math.random() * 0.2D) * 2.0F);
        this.motionZ = (double)((float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D) * 2.0F);
        this.xpValue = p_i1585_8_;
    }

    protected boolean canTriggerWalking()
    {
        return false;
    }

    public EntityXPOrb(World p_i1586_1_)
    {
        super(p_i1586_1_);
        this.setSize(0.25F, 0.25F);
        this.yOffset = this.height / 2.0F;
    }

    protected void entityInit() {}

    @SideOnly(Side.CLIENT)
    public int getBrightnessForRender(float p_70070_1_)
    {
        float f1 = 0.5F;

        if (f1 < 0.0F)
        {
            f1 = 0.0F;
        }

        if (f1 > 1.0F)
        {
            f1 = 1.0F;
        }

        int i = super.getBrightnessForRender(p_70070_1_);
        int j = i & 255;
        int k = i >> 16 & 255;
        j += (int)(f1 * 15.0F * 16.0F);

        if (j > 240)
        {
            j = 240;
        }

        return j | k << 16;
    }

    public void onUpdate()
    {
        super.onUpdate();

        if (this.field_70532_c > 0)
        {
            --this.field_70532_c;
        }

        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.motionY -= 0.029999999329447746D;

        if (this.worldObj.getBlock(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ)).getMaterial() == Material.lava)
        {
            this.motionY = 0.20000000298023224D;
            this.motionX = (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
            this.motionZ = (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
            this.playSound("random.fizz", 0.4F, 2.0F + this.rand.nextFloat() * 0.4F);
        }

        this.func_145771_j(this.posX, (this.boundingBox.minY + this.boundingBox.maxY) / 2.0D, this.posZ);
        double d0 = 8.0D;

        if (this.xpTargetColor < this.xpColor - 20 + this.getEntityId() % 100)
        {
            if (this.closestPlayer == null || this.closestPlayer.getDistanceSqToEntity(this) > d0 * d0)
            {
                this.closestPlayer = this.worldObj.getClosestPlayerToEntity(this, d0);
            }

            this.xpTargetColor = this.xpColor;
        }

        if (this.closestPlayer != null)
        {
            // CraftBukkit start
            EntityTargetEvent event = CraftEventFactory.callEntityTargetEvent(this, closestPlayer, EntityTargetEvent.TargetReason.CLOSEST_PLAYER);
            Entity target = event.getTarget() == null ? null : ((org.bukkit.craftbukkit.entity.CraftEntity) event.getTarget()).getHandle();

            if (!event.isCancelled() && target != null)
            {
                double d1 = (target.posX - this.posX) / d0;
                double d2 = (target.posY + (double) target.getEyeHeight() - this.posY) / d0;
                double d3 = (target.posZ - this.posZ) / d0;
                double d4 = Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
                double d5 = 1.0D - d4;

                if (d5 > 0.0D)
                {
                    d5 *= d5;
                    this.motionX += d1 / d4 * d5 * 0.1D;
                    this.motionY += d2 / d4 * d5 * 0.1D;
                    this.motionZ += d3 / d4 * d5 * 0.1D;
                }

                // CraftBukkit end
            }
        }

        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        float f = 0.98F;

        if (this.onGround)
        {
            f = this.worldObj.getBlock(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.boundingBox.minY) - 1, MathHelper.floor_double(this.posZ)).slipperiness * 0.98F;
        }

        this.motionX *= (double)f;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= (double)f;

        if (this.onGround)
        {
            this.motionY *= -0.8999999761581421D;
        }

        ++this.xpColor;
        ++this.xpOrbAge;

        if (this.xpOrbAge >= 6000)
        {
            this.setDead();
        }
    }

    public boolean handleWaterMovement()
    {
        return this.worldObj.handleMaterialAcceleration(this.boundingBox, Material.water, this);
    }

    protected void dealFireDamage(int p_70081_1_)
    {
        this.attackEntityFrom(DamageSource.inFire, (float)p_70081_1_);
    }

    public boolean attackEntityFrom(DamageSource p_70097_1_, float p_70097_2_)
    {
        if (this.isEntityInvulnerable())
        {
            return false;
        }
        else
        {
            this.setBeenAttacked();
            this.xpOrbHealth = (int)((float)this.xpOrbHealth - p_70097_2_);

            if (this.xpOrbHealth <= 0)
            {
                this.setDead();
            }

            return false;
        }
    }

    public void writeEntityToNBT(NBTTagCompound p_70014_1_)
    {
        p_70014_1_.setShort("Health", (short)((byte)this.xpOrbHealth));
        p_70014_1_.setShort("Age", (short)this.xpOrbAge);
        p_70014_1_.setShort("Value", (short)this.xpValue);
    }

    public void readEntityFromNBT(NBTTagCompound p_70037_1_)
    {
        this.xpOrbHealth = p_70037_1_.getShort("Health") & 255;
        this.xpOrbAge = p_70037_1_.getShort("Age");
        this.xpValue = p_70037_1_.getShort("Value");
    }

    public void onCollideWithPlayer(EntityPlayer p_70100_1_)
    {
        if (!this.worldObj.isRemote)
        {
            if (this.field_70532_c == 0 && p_70100_1_.xpCooldown == 0)
            {
                if (MinecraftForge.EVENT_BUS.post(new PlayerPickupXpEvent(p_70100_1_, this))) return;
                p_70100_1_.xpCooldown = 2;
                this.worldObj.playSoundAtEntity(p_70100_1_, "random.orb", 0.1F, 0.5F * ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.8F));
                p_70100_1_.onItemPickup(this, 1);
                p_70100_1_.addExperience(CraftEventFactory.callPlayerExpChangeEvent(p_70100_1_, this.xpValue).getAmount());
                this.setDead();
            }
        }
    }

    public int getXpValue()
    {
        return this.xpValue;
    }

    @SideOnly(Side.CLIENT)
    public int getTextureByXP()
    {
        return this.xpValue >= 2477 ? 10 : (this.xpValue >= 1237 ? 9 : (this.xpValue >= 617 ? 8 : (this.xpValue >= 307 ? 7 : (this.xpValue >= 149 ? 6 : (this.xpValue >= 73 ? 5 : (this.xpValue >= 37 ? 4 : (this.xpValue >= 17 ? 3 : (this.xpValue >= 7 ? 2 : (this.xpValue >= 3 ? 1 : 0)))))))));
    }

    public static int getXPSplit(int p_70527_0_)
    {
        // CraftBukkit start
        if (p_70527_0_ > 162670129)
        {
            return p_70527_0_ - 100000;
        }

        if (p_70527_0_ > 81335063)
        {
            return 81335063;
        }

        if (p_70527_0_ > 40667527)
        {
            return 40667527;
        }

        if (p_70527_0_ > 20333759)
        {
            return 20333759;
        }

        if (p_70527_0_ > 10166857)
        {
            return 10166857;
        }

        if (p_70527_0_ > 5083423)
        {
            return 5083423;
        }

        if (p_70527_0_ > 2541701)
        {
            return 2541701;
        }

        if (p_70527_0_ > 1270849)
        {
            return 1270849;
        }

        if (p_70527_0_ > 635413)
        {
            return 635413;
        }

        if (p_70527_0_ > 317701)
        {
            return 317701;
        }

        if (p_70527_0_ > 158849)
        {
            return 158849;
        }

        if (p_70527_0_ > 79423)
        {
            return 79423;
        }

        if (p_70527_0_ > 39709)
        {
            return 39709;
        }

        if (p_70527_0_ > 19853)
        {
            return 19853;
        }

        if (p_70527_0_ > 9923)
        {
            return 9923;
        }

        if (p_70527_0_ > 4957)
        {
            return 4957;
        }

        // CraftBukkit end
        return p_70527_0_ >= 2477 ? 2477 : (p_70527_0_ >= 1237 ? 1237 : (p_70527_0_ >= 617 ? 617 : (p_70527_0_ >= 307 ? 307 : (p_70527_0_ >= 149 ? 149 : (p_70527_0_ >= 73 ? 73 : (p_70527_0_ >= 37 ? 37 : (p_70527_0_ >= 17 ? 17 : (p_70527_0_ >= 7 ? 7 : (p_70527_0_ >= 3 ? 3 : 1)))))))));
    }

    public boolean canAttackWithItem()
    {
        return false;
    }

    // Cauldron start
    @Override
    public boolean entityProjectileHook() {
        return true;
    }
    // Cauldron end
}