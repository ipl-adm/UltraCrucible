package net.minecraft.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.AxisAlignedBB;

// CraftBukkit start
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import io.github.crucible.event.BeaconEffectEvent;
// CraftBukkit end

public class TileEntityBeacon extends TileEntity implements IInventory
{
    public static final Potion[][] effectsList = new Potion[][] {{Potion.moveSpeed, Potion.digSpeed}, {Potion.resistance, Potion.jump}, {Potion.damageBoost}, {Potion.regeneration}};
    @SideOnly(Side.CLIENT)
    private long field_146016_i;
    @SideOnly(Side.CLIENT)
    private float field_146014_j;
    private boolean field_146015_k;
    private int levels = -1;
    private int primaryEffect;
    private int secondaryEffect;
    private ItemStack payment;
    private String field_146008_p;
    // CraftBukkit start
    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();
    private int maxStack = MAX_STACK;

    public ItemStack[] getContents()
    {
        return new ItemStack[] { this.payment };
    }

    public void onOpen(CraftHumanEntity who)
    {
        transaction.add(who);
    }

    public void onClose(CraftHumanEntity who)
    {
        transaction.remove(who);
    }

    public List<HumanEntity> getViewers()
    {
        return transaction;
    }

    public void setMaxStackSize(int size)
    {
        maxStack = size;
    }
    // CraftBukkit end
    private static final String __OBFID = "CL_00000339";

    public void updateEntity()
    {
        if (this.worldObj.getTotalWorldTime() % 80L == 0L)
        {
            this.func_146003_y();
            this.func_146000_x();
        }
    }

    private void func_146000_x()
    {
        if (this.field_146015_k && this.levels > 0 && !this.worldObj.isRemote && this.primaryEffect > 0)
        {
            double d0 = (double)(this.levels * 10 + 10);
            byte b0 = 0;

            if (this.levels >= 4 && this.primaryEffect == this.secondaryEffect)
            {
                b0 = 1;
            }

            AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox((double)this.xCoord, (double)this.yCoord, (double)this.zCoord, (double)(this.xCoord + 1), (double)(this.yCoord + 1), (double)(this.zCoord + 1)).expand(d0, d0, d0);
            axisalignedbb.maxY = (double)this.worldObj.getHeight();
            List list = this.worldObj.getEntitiesWithinAABB(EntityPlayer.class, axisalignedbb);
            Iterator iterator = list.iterator();
            EntityPlayer entityplayer;

            //PaperSpigot start
            org.bukkit.block.Block block = worldObj.getWorld().getBlockAt(this.xCoord, this.yCoord, this.zCoord);
            org.bukkit.potion.PotionEffect primaryEffect = new org.bukkit.potion.PotionEffect(PotionEffectType.getById(this.getPrimaryEffect()), 180, b0, true);
            //PaperSpigot stop

            while (iterator.hasNext()) {
                entityplayer = (EntityPlayer) iterator.next();
                //PaperSpigot start
                BeaconEffectEvent event = new BeaconEffectEvent(block, primaryEffect, (Player) entityplayer.getBukkitEntity(), true);
                if (CraftEventFactory.callEvent(event).isCancelled()) continue;

                org.bukkit.potion.PotionEffect effect = event.getEffect();
                entityplayer.addPotionEffect(new PotionEffect(effect.getType().getId(), effect.getDuration(), effect.getAmplifier(), effect.isAmbient()));
                //PaperSpigot end
            }

            if (this.levels >= 4 && this.primaryEffect != this.secondaryEffect && this.secondaryEffect > 0)
            {
                iterator = list.iterator();
                org.bukkit.potion.PotionEffect secondaryEffect;
                if (this.secondaryEffect == 10) {
                    secondaryEffect = new org.bukkit.potion.PotionEffect(PotionEffectType.getById(10), 180, b0, true); // PaperSpigot
                } else {
                    secondaryEffect = new org.bukkit.potion.PotionEffect(PotionEffectType.getById(this.getPrimaryEffect()), 180, b0, true); // PaperSpigot
                }

                while (iterator.hasNext())
                {
                    entityplayer = (EntityPlayer)iterator.next();
                    //PaperSpigot Start - BeaconEffectEvent
                    BeaconEffectEvent event = new BeaconEffectEvent(block, secondaryEffect, (Player) entityplayer.getBukkitEntity(), false);
                    if (CraftEventFactory.callEvent(event).isCancelled()) continue;

                    org.bukkit.potion.PotionEffect effect = event.getEffect();
                    entityplayer.addPotionEffect(new PotionEffect(effect.getType().getId(), effect.getDuration(), effect.getAmplifier(), effect.isAmbient()));
                    //PaperSpigot end
                }
            }
        }
    }

    private void func_146003_y()
    {
        int i = this.levels;

        if (!this.worldObj.canBlockSeeTheSky(this.xCoord, this.yCoord + 1, this.zCoord))
        {
            this.field_146015_k = false;
            this.levels = 0;
        }
        else
        {
            this.field_146015_k = true;
            this.levels = 0;

            for (int j = 1; j <= 4; this.levels = j++)
            {
                int k = this.yCoord - j;

                if (k < 0)
                {
                    break;
                }

                boolean flag = true;

                for (int l = this.xCoord - j; l <= this.xCoord + j && flag; ++l)
                {
                    for (int i1 = this.zCoord - j; i1 <= this.zCoord + j; ++i1)
                    {
                        Block block = this.worldObj.getBlock(l, k, i1);

                        if (!block.isBeaconBase(this.worldObj, l, k, i1, xCoord, yCoord, zCoord))
                        {
                            flag = false;
                            break;
                        }
                    }
                }

                if (!flag)
                {
                    break;
                }
            }

            if (this.levels == 0)
            {
                this.field_146015_k = false;
            }
        }

        if (!this.worldObj.isRemote && this.levels == 4 && i < this.levels)
        {
            Iterator iterator = this.worldObj.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox((double)this.xCoord, (double)this.yCoord, (double)this.zCoord, (double)this.xCoord, (double)(this.yCoord - 4), (double)this.zCoord).expand(10.0D, 5.0D, 10.0D)).iterator();

            while (iterator.hasNext())
            {
                EntityPlayer entityplayer = (EntityPlayer)iterator.next();
                entityplayer.triggerAchievement(AchievementList.field_150965_K);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public float func_146002_i()
    {
        if (!this.field_146015_k)
        {
            return 0.0F;
        }
        else
        {
            int i = (int)(this.worldObj.getTotalWorldTime() - this.field_146016_i);
            this.field_146016_i = this.worldObj.getTotalWorldTime();

            if (i > 1)
            {
                this.field_146014_j -= (float)i / 40.0F;

                if (this.field_146014_j < 0.0F)
                {
                    this.field_146014_j = 0.0F;
                }
            }

            this.field_146014_j += 0.025F;

            if (this.field_146014_j > 1.0F)
            {
                this.field_146014_j = 1.0F;
            }

            return this.field_146014_j;
        }
    }

    public int getPrimaryEffect()
    {
        return this.primaryEffect;
    }

    public int getSecondaryEffect()
    {
        return this.secondaryEffect;
    }

    public int getLevels()
    {
        return this.levels;
    }

    @SideOnly(Side.CLIENT)
    public void func_146005_c(int p_146005_1_)
    {
        this.levels = p_146005_1_;
    }

    public void setPrimaryEffect(int p_146001_1_)
    {
        this.primaryEffect = 0;

        for (int j = 0; j < this.levels && j < 3; ++j)
        {
            Potion[] apotion = effectsList[j];
            int k = apotion.length;

            for (int l = 0; l < k; ++l)
            {
                Potion potion = apotion[l];

                if (potion.id == p_146001_1_)
                {
                    this.primaryEffect = p_146001_1_;
                    return;
                }
            }
        }
    }

    public void setSecondaryEffect(int p_146004_1_)
    {
        this.secondaryEffect = 0;

        if (this.levels >= 4)
        {
            for (int j = 0; j < 4; ++j)
            {
                Potion[] apotion = effectsList[j];
                int k = apotion.length;

                for (int l = 0; l < k; ++l)
                {
                    Potion potion = apotion[l];

                    if (potion.id == p_146004_1_)
                    {
                        this.secondaryEffect = p_146004_1_;
                        return;
                    }
                }
            }
        }
    }

    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.writeToNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 3, nbttagcompound);
    }

    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared()
    {
        return 65536.0D;
    }

    public void readFromNBT(NBTTagCompound p_145839_1_)
    {
        super.readFromNBT(p_145839_1_);
        this.primaryEffect = p_145839_1_.getInteger("Primary");
        this.secondaryEffect = p_145839_1_.getInteger("Secondary");
        this.levels = p_145839_1_.getInteger("Levels");
    }

    public void writeToNBT(NBTTagCompound p_145841_1_)
    {
        super.writeToNBT(p_145841_1_);
        p_145841_1_.setInteger("Primary", this.primaryEffect);
        p_145841_1_.setInteger("Secondary", this.secondaryEffect);
        p_145841_1_.setInteger("Levels", this.levels);
    }

    public int getSizeInventory()
    {
        return 1;
    }

    public ItemStack getStackInSlot(int p_70301_1_)
    {
        return p_70301_1_ == 0 ? this.payment : null;
    }

    public ItemStack decrStackSize(int p_70298_1_, int p_70298_2_)
    {
        if (p_70298_1_ == 0 && this.payment != null)
        {
            if (p_70298_2_ >= this.payment.stackSize)
            {
                ItemStack itemstack = this.payment;
                this.payment = null;
                return itemstack;
            }
            else
            {
                this.payment.stackSize -= p_70298_2_;
                return new ItemStack(this.payment.getItem(), p_70298_2_, this.payment.getItemDamage());
            }
        }
        else
        {
            return null;
        }
    }

    public ItemStack getStackInSlotOnClosing(int p_70304_1_)
    {
        if (p_70304_1_ == 0 && this.payment != null)
        {
            ItemStack itemstack = this.payment;
            this.payment = null;
            return itemstack;
        }
        else
        {
            return null;
        }
    }

    public void setInventorySlotContents(int p_70299_1_, ItemStack p_70299_2_)
    {
        if (p_70299_1_ == 0)
        {
            this.payment = p_70299_2_;
        }
    }

    public String getInventoryName()
    {
        return this.hasCustomInventoryName() ? this.field_146008_p : "container.beacon";
    }

    public boolean hasCustomInventoryName()
    {
        return this.field_146008_p != null && this.field_146008_p.length() > 0;
    }

    public void func_145999_a(String p_145999_1_)
    {
        this.field_146008_p = p_145999_1_;
    }

    public int getInventoryStackLimit()
    {
        return maxStack; // CraftBukkit
    }

    public boolean isUseableByPlayer(EntityPlayer p_70300_1_)
    {
        return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false : p_70300_1_.getDistanceSq((double)this.xCoord + 0.5D, (double)this.yCoord + 0.5D, (double)this.zCoord + 0.5D) <= 64.0D;
    }

    public void openInventory() {}

    public void closeInventory() {}

    public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_)
    {
        return p_94041_2_.getItem() != null && p_94041_2_.getItem().isBeaconPayment(p_94041_2_);
    }
}