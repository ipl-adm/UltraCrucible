package net.minecraft.entity.item;

import java.util.Iterator;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event.Result;

// CraftBukkit start
import org.bukkit.event.player.PlayerPickupItemEvent;
import net.minecraft.server.MinecraftServer;
// CraftBukkit end

public class EntityItem extends Entity
{
    private static final Logger logger = LogManager.getLogger();
    public int age;
    public int delayBeforeCanPickup;
    private int health;
    private String field_145801_f;
    private String field_145802_g;
    public float hoverStart;
    private int lastTick = MinecraftServer.currentTick; // CraftBukkit
    private static final String __OBFID = "CL_00001669";

    /**
     * The maximum age of this EntityItem.  The item is expired once this is reached.
     */
    public int lifespan = 6000;

    public EntityItem(World p_i1709_1_, double p_i1709_2_, double p_i1709_4_, double p_i1709_6_)
    {
        super(p_i1709_1_);
        this.health = 5;
        this.hoverStart = (float)(Math.random() * Math.PI * 2.0D);
        this.setSize(0.25F, 0.25F);
        this.yOffset = this.height / 2.0F;
        this.setPosition(p_i1709_2_, p_i1709_4_, p_i1709_6_);
        this.rotationYaw = (float)(Math.random() * 360.0D);
        this.motionX = (double)((float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D));
        this.motionY = 0.20000000298023224D;
        this.motionZ = (double)((float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D));
    }

    public EntityItem(World p_i1710_1_, double p_i1710_2_, double p_i1710_4_, double p_i1710_6_, ItemStack p_i1710_8_)
    {
        this(p_i1710_1_, p_i1710_2_, p_i1710_4_, p_i1710_6_);        

        // CraftBukkit start - Can't set null items in the datawatcher
        if (p_i1710_8_ == null || p_i1710_8_.getItem() == null)
        {
            return;
        }

        // CraftBukkit end

        this.setEntityItemStack(p_i1710_8_);
        this.lifespan = (p_i1710_8_.getItem() == null ? 6000 : p_i1710_8_.getItem().getEntityLifespan(p_i1710_8_, p_i1710_1_));
    }

    protected boolean canTriggerWalking()
    {
        return false;
    }

    public EntityItem(World p_i1711_1_)
    {
        super(p_i1711_1_);
        this.health = 5;
        this.hoverStart = (float)(Math.random() * Math.PI * 2.0D);
        this.setSize(0.25F, 0.25F);
        this.yOffset = this.height / 2.0F;
    }

    protected void entityInit()
    {
        this.getDataWatcher().addObjectByDataType(10, 5);
    }

    public void onUpdate()
    {
        ItemStack stack = this.getDataWatcher().getWatchableObjectItemStack(10);
        if (stack == null || stack.stackSize == 0) {
            setDead();
            return;
        }
        if (stack != null && stack.getItem() != null)
        {
            if (stack.getItem().onEntityItemUpdate(this))
            {
                return;
            }
        }

        super.onUpdate();
        // CraftBukkit start - Use wall time for pickup and despawn timers
        int elapsedTicks = MinecraftServer.currentTick - this.lastTick;
        this.delayBeforeCanPickup -= elapsedTicks;
        if (this.delayBeforeCanPickup < 0) this.delayBeforeCanPickup = 0; // Cauldron
        this.age += elapsedTicks;
        this.lastTick = MinecraftServer.currentTick;
        // CraftBukkit end

        boolean forceUpdate = this.ticksExisted > 0 && this.ticksExisted % 25 == 0; // Cauldron - optimize item tick updates
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.motionY -= 0.03999999910593033D;
        // Cauldron start - if forced
        if (forceUpdate || noClip) {
            this.noClip = this.func_145771_j(this.posX, (this.boundingBox.minY + this.boundingBox.maxY) / 2.0D, this.posZ);
        }
        // Cauldron end
        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        boolean flag = (int)this.prevPosX != (int)this.posX || (int)this.prevPosY != (int)this.posY || (int)this.prevPosZ != (int)this.posZ;

        if ((flag && this.ticksExisted % 5 == 0) || forceUpdate) // Cauldron - if forced
        {
            if (this.worldObj.getBlock(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ)).getMaterial() == Material.lava)
            {
                this.motionY = 0.20000000298023224D;
                this.motionX = (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
                this.motionZ = (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
                this.playSound("random.fizz", 0.4F, 2.0F + this.rand.nextFloat() * 0.4F);
            }

            if (forceUpdate && !this.worldObj.isRemote) // Cauldron - if forced
            {
                this.searchForOtherItemsNearby();
            }
        }

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
            this.motionY *= -0.5D;
        }

        // ++this.age; // CraftBukkit - Moved up (base age on wall time)

        ItemStack item = getDataWatcher().getWatchableObjectItemStack(10);
        
        if (!this.worldObj.isRemote && this.age >= lifespan - 1) // Cauldron adjust for age being off by one when it is first dropped
        {
            // CraftBukkit start
            if (org.bukkit.craftbukkit.event.CraftEventFactory.callItemDespawnEvent(this).isCancelled())
            {
                this.age = 0;
                return;
            }
            // CraftBukkit end
            if (item != null)
            {   
                ItemExpireEvent event = new ItemExpireEvent(this, (item.getItem() == null ? this.worldObj.getSpigotConfig().itemDespawnRate : item.getItem().getEntityLifespan(item, worldObj))); // Spigot // Cauldron
                if (MinecraftForge.EVENT_BUS.post(event))
                {
                    lifespan += event.extraLife;
                }
                else
                {
                    this.setDead();
                }
            }
            else
            {
                this.setDead();
            }
        }

        if (item != null && item.stackSize <= 0)
        {
            this.setDead();
        }
    }

    private void searchForOtherItemsNearby()
    {
        // Spigot start
        double radius = worldObj.getSpigotConfig().itemMerge; // Cauldron
        Iterator iterator = this.worldObj.getEntitiesWithinAABB(EntityItem.class, this.boundingBox.expand(radius, radius, radius)).iterator();
        // Spigot end

        while (iterator.hasNext())
        {
            EntityItem entityitem = (EntityItem)iterator.next();
            this.combineItems(entityitem);
        }
    }

    public boolean combineItems(EntityItem p_70289_1_)
    {
        if (p_70289_1_ == this)
        {
            return false;
        }
        else if (p_70289_1_.isEntityAlive() && this.isEntityAlive())
        {
            ItemStack itemstack = this.getEntityItem();
            ItemStack itemstack1 = p_70289_1_.getEntityItem();

            if (itemstack1.getItem() != itemstack.getItem())
            {
                return false;
            }
            else if (itemstack1.hasTagCompound() ^ itemstack.hasTagCompound())
            {
                return false;
            }
            else if (itemstack1.hasTagCompound() && !itemstack1.getTagCompound().equals(itemstack.getTagCompound()))
            {
                return false;
            }
            else if (itemstack1.getItem() == null)
            {
                return false;
            }
            else if (itemstack1.getItem().getHasSubtypes() && itemstack1.getItemDamage() != itemstack.getItemDamage())
            {
                return false;
            }
            else if (itemstack1.stackSize < itemstack.stackSize)
            {
                return p_70289_1_.combineItems(this);
            }
            else if (itemstack1.stackSize + itemstack.stackSize > itemstack1.getMaxStackSize())
            {
                return false;
            }
            else
            {
                // Spigot start
                itemstack.stackSize += itemstack1.stackSize;
                this.delayBeforeCanPickup = Math.max(p_70289_1_.delayBeforeCanPickup, this.delayBeforeCanPickup);
                this.age = Math.min(p_70289_1_.age, this.age);
                this.setEntityItemStack(itemstack);
                p_70289_1_.setDead();
                // Spigot end
                return true;
            }
        }
        else
        {
            return false;
        }
    }

    public void setAgeToCreativeDespawnTime()
    {
        this.age = 4800;
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
        else if (this.getEntityItem() != null && this.getEntityItem().getItem() == Items.nether_star && p_70097_1_.isExplosion())
        {
            return false;
        }
        else
        {
            this.setBeenAttacked();
            this.health = (int)((float)this.health - p_70097_2_);

            if (this.health <= 0)
            {
                this.setDead();
            }

            return false;
        }
    }

    public void writeEntityToNBT(NBTTagCompound p_70014_1_)
    {
        p_70014_1_.setShort("Health", (short)((byte)this.health));
        p_70014_1_.setShort("Age", (short)this.age);
        p_70014_1_.setInteger("Lifespan", lifespan);

        if (this.func_145800_j() != null)
        {
            p_70014_1_.setString("Thrower", this.field_145801_f);
        }

        if (this.func_145798_i() != null)
        {
            p_70014_1_.setString("Owner", this.field_145802_g);
        }

        if (this.getEntityItem() != null)
        {
            p_70014_1_.setTag("Item", this.getEntityItem().writeToNBT(new NBTTagCompound()));
        }
    }

    public void readEntityFromNBT(NBTTagCompound p_70037_1_)
    {
        this.health = p_70037_1_.getShort("Health") & 255;
        this.age = p_70037_1_.getShort("Age");

        if (p_70037_1_.hasKey("Owner"))
        {
            this.field_145802_g = p_70037_1_.getString("Owner");
        }

        if (p_70037_1_.hasKey("Thrower"))
        {
            this.field_145801_f = p_70037_1_.getString("Thrower");
        }

        NBTTagCompound nbttagcompound1 = p_70037_1_.getCompoundTag("Item");

        // CraftBukkit start
        if (nbttagcompound1 != null)
        {
            ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbttagcompound1);

            if (itemstack != null)
            {
                this.setEntityItemStack(itemstack);
            }
            else
            {
                this.setDead();
            }
        }
        else
        {
            this.setDead();
        }

        // CraftBukkit end
        ItemStack item = getDataWatcher().getWatchableObjectItemStack(10);

        if (item == null || item.stackSize <= 0)
        {
            this.setDead();
        }

        if (p_70037_1_.hasKey("Lifespan"))
        {
            lifespan = p_70037_1_.getInteger("Lifespan");
        }
    }

    public void onCollideWithPlayer(EntityPlayer p_70100_1_)
    {
        if (!this.worldObj.isRemote)
        {
            if (this.delayBeforeCanPickup > 0)
            {
                return;
            }

            EntityItemPickupEvent event = new EntityItemPickupEvent(p_70100_1_, this);

            if (MinecraftForge.EVENT_BUS.post(event))
            {
                return;
            }

            ItemStack itemstack = this.getEntityItem();
            int i = itemstack.stackSize;

            // CraftBukkit start
            int canHold = p_70100_1_.inventory.canHold(itemstack);
            int remaining = itemstack.stackSize - canHold;

            if (this.delayBeforeCanPickup <= 0 && canHold > 0)
            {
                itemstack.stackSize = canHold;
                // Cauldron start - rename to cbEvent to fix naming collision
                PlayerPickupItemEvent cbEvent = new PlayerPickupItemEvent((org.bukkit.entity.Player) p_70100_1_.getBukkitEntity(), (org.bukkit.entity.Item) this.getBukkitEntity(), remaining);
                //cbEvent.setCancelled(!par1EntityPlayer.canPickUpLoot); TODO
                this.worldObj.getServer().getPluginManager().callEvent(cbEvent);
                itemstack.stackSize = canHold + remaining;

                if (cbEvent.isCancelled())
                {
                    return;
                }
                // Cauldron end

                // Possibly < 0; fix here so we do not have to modify code below
                this.delayBeforeCanPickup = 0;
            }

            // CraftBukkit end

            if (this.delayBeforeCanPickup <= 0 && (this.field_145802_g == null || lifespan - this.age <= 200 || this.field_145802_g.equals(p_70100_1_.getCommandSenderName())) && (event.getResult() == Result.ALLOW || i <= 0 || p_70100_1_.inventory.addItemStackToInventory(itemstack)))
            {
                if (itemstack.getItem() == Item.getItemFromBlock(Blocks.log))
                {
                    p_70100_1_.triggerAchievement(AchievementList.mineWood);
                }

                if (itemstack.getItem() == Item.getItemFromBlock(Blocks.log2))
                {
                    p_70100_1_.triggerAchievement(AchievementList.mineWood);
                }

                if (itemstack.getItem() == Items.leather)
                {
                    p_70100_1_.triggerAchievement(AchievementList.killCow);
                }

                if (itemstack.getItem() == Items.diamond)
                {
                    p_70100_1_.triggerAchievement(AchievementList.diamonds);
                }

                if (itemstack.getItem() == Items.blaze_rod)
                {
                    p_70100_1_.triggerAchievement(AchievementList.blazeRod);
                }

                if (itemstack.getItem() == Items.diamond && this.func_145800_j() != null)
                {
                    EntityPlayer entityplayer1 = this.worldObj.getPlayerEntityByName(this.func_145800_j());

                    if (entityplayer1 != null && entityplayer1 != p_70100_1_)
                    {
                        entityplayer1.triggerAchievement(AchievementList.field_150966_x);
                    }
                }

                FMLCommonHandler.instance().firePlayerItemPickupEvent(p_70100_1_, this);

                this.worldObj.playSoundAtEntity(p_70100_1_, "random.pop", 0.2F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                p_70100_1_.onItemPickup(this, i);

                if (itemstack.stackSize <= 0)
                {
                    this.setDead();
                }
            }
        }
    }

    public String getCommandSenderName()
    {
        return StatCollector.translateToLocal("item." + this.getEntityItem().getUnlocalizedName());
    }

    public boolean canAttackWithItem()
    {
        return false;
    }

    public void travelToDimension(int p_71027_1_)
    {
        super.travelToDimension(p_71027_1_);

        if (!this.worldObj.isRemote)
        {
            this.searchForOtherItemsNearby();
        }
    }

    public ItemStack getEntityItem()
    {
        ItemStack itemstack = this.getDataWatcher().getWatchableObjectItemStack(10);
        return itemstack == null ? new ItemStack(Blocks.air, 0) : itemstack;
    }

    public void setEntityItemStack(ItemStack p_92058_1_)
    {
        this.getDataWatcher().updateObject(10, p_92058_1_);
        this.getDataWatcher().setObjectWatched(10);
    }

    public String func_145798_i()
    {
        return this.field_145802_g;
    }

    public void func_145797_a(String p_145797_1_)
    {
        this.field_145802_g = p_145797_1_;
    }

    public String func_145800_j()
    {
        return this.field_145801_f;
    }

    public void func_145799_b(String p_145799_1_)
    {
        this.field_145801_f = p_145799_1_;
    }
    
    @Override
    public boolean entityAllowedToSpawn() {
        ItemStack stack = getDataWatcher().getWatchableObjectItemStack(10);
        return stack != null && stack.getItem() != null && stack.stackSize > 0;
    }
}