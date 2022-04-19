package net.minecraftforge.event;

import java.io.File;

import io.github.crucible.CrucibleConfigs;
import net.minecraft.util.ChatComponentText;
import net.minecraft.item.Item;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraftforge.cauldron.command.*;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.eventhandler.Event.Result;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.brewing.PotionBrewEvent;
import net.minecraftforge.event.brewing.PotionBrewedEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingPackSizeEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.AllowDespawn;
import net.minecraftforge.event.entity.living.ZombieEvent.SummonAidEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.MultiPlaceEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent;

//TODO: Crucible-There are a few hidden and weird cauldron/thermos protections, let's add a config file for those protections!
public class ForgeEventFactory
{

    public static MultiPlaceEvent onPlayerMultiBlockPlace(EntityPlayer player, List<BlockSnapshot> blockSnapshots, ForgeDirection direction)
    {
        Block placedAgainst = blockSnapshots.get(0).world.getBlock(blockSnapshots.get(0).x + direction.getOpposite().offsetX, blockSnapshots.get(0).y + direction.getOpposite().offsetY, blockSnapshots.get(0).z + direction.getOpposite().offsetZ);

        MultiPlaceEvent event = new MultiPlaceEvent(blockSnapshots, placedAgainst, player);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }

    public static PlaceEvent onPlayerBlockPlace(EntityPlayer player, BlockSnapshot blockSnapshot, ForgeDirection direction)
    {
        Block placedAgainst = blockSnapshot.world.getBlock(blockSnapshot.x + direction.getOpposite().offsetX, blockSnapshot.y + direction.getOpposite().offsetY, blockSnapshot.z + direction.getOpposite().offsetZ);

        PlaceEvent event = new PlaceEvent(blockSnapshot, placedAgainst, player);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }

        public static boolean isSpawn(EntityPlayer ep)
        {
                //return MinecraftServer.getServer().cauldronConfig.protectSP.getValue() && ep.worldObj.worldInfo.getWorldName().toLowerCase().contains("spawn");
            String playerWorld = ep.worldObj.worldInfo.getWorldName();
            for (String world : CrucibleConfigs.configs.crucible_protectedWorld) {
                if (world.equalsIgnoreCase(playerWorld))
                    return true;
            }
            return false;
        }

    public static boolean doPlayerHarvestCheck(EntityPlayer player, Block block, boolean success)
    {
                if (isSpawn(player) && nonVanilla(player))
                {
                        if (CauldronCommand.debug)
                                System.out.println("Told player no harvest");
                        return false;
                }

        PlayerEvent.HarvestCheck event = new PlayerEvent.HarvestCheck(player, block, success);
        MinecraftForge.EVENT_BUS.post(event);
        return event.success;
    }

    @Deprecated // Location version below
    public static float getBreakSpeed(EntityPlayer player, Block block, int metadata, float original)
    {
                if (isSpawn(player) && nonVanilla(player))
                {
                        if (CauldronCommand.debug)
                                System.out.println("Told user breakspeed = 0");
                        return 0F;
                }

        return getBreakSpeed(player, block, metadata, original, 0, -1, 0);
    }

    public static float getBreakSpeed(EntityPlayer player, Block block, int metadata, float original, int x, int y, int z)
    {
                if (isSpawn(player) && nonVanilla(player))
                {
                        if (CauldronCommand.debug)
                                System.out.println("Told user breakspeed = 0");
                        return 0F;
                }

        PlayerEvent.BreakSpeed event = new PlayerEvent.BreakSpeed(player, block, metadata, original, x, y, z);
        return (MinecraftForge.EVENT_BUS.post(event) ? -1 : event.newSpeed);
    }

        private static boolean outsideOfVanillaRange(int id)
        {
                if (CauldronCommand.debug)
                {
                        System.out.println("Testing item ID " + id);
                }
                return (id > 197 && !(id >= 256 && id <= 431) && !(id >= 2256 && id <= 2267))
                                && !( id == 628 || id == 5374 || id == 4568 || id == 4354 || id == 4355 || id == 4356
                                || (id >= 4324 && id <= 4329) || id == 4338 || id == 4688 || id == 4916 || (id <= 4987 && id>= 4980)
                                || id == 5386 || id == 753 || id == 5497 || id == 4096 || id == 550 || id == 4791 || id == 4790
                                || id == 564 || id == 562 || id == 555 || id == 4787 || id == 569 || id == 572 || id == 4749 || id == 4984
                                || id == 4568 || id == 220 || (id >= 4548 && id <= 4561) || (id >= 5360 && id <= 5365));
        }


        public static boolean isOp(EntityPlayer ep)
        {
                 return MinecraftServer.getServer().getConfigurationManager().func_152596_g(ep.getGameProfile());
        }
        
        public static boolean isItemIdAllowed(int itemId) {
                return CrucibleConfigs.configs.crucible_protectedWorldWhitelistInvert ^ CrucibleConfigs.configs.crucible_protectedWorldWhitelist.contains(itemId);
        }

        public static boolean nonVanilla(EntityPlayer ep)
        {
                if (ep == null)
                {
                if (CauldronCommand.debug)System.out.println("EntityPlayer is null");
                        return false;
                }
                if (isOp(ep)) return false;
                if (ep.getHeldItem() == null)
                {
                        if (CauldronCommand.debug)
                                System.out.println("Held item is null");
                        return false;
                }
                if (ep.getHeldItem().getItem() == null)
                {
                if (CauldronCommand.debug)System.out.println("Item getItem() is null");
                        return false;
                }
                int itemId = Item.getIdFromItem(ep.getHeldItem().getItem());
                if (outsideOfVanillaRange(itemId) && !isItemIdAllowed(itemId)) {
                        if(ep instanceof EntityPlayerMP) {
                                EntityPlayerMP mp = (EntityPlayerMP)ep;
                                mp.addChatComponentMessage(new ChatComponentText("You cannot use that item here."));
                        }
                        return true;
                }
                return false;
        }

    @Deprecated
    public static PlayerInteractEvent onPlayerInteract(EntityPlayer player, Action action, int x, int y, int z, int face)
    {
    	if(player == null)
        {
            	return onPlayerBukkitInteract( player, action, x, y, z, face, null);
        }
        org.bukkit.event.block.Action aktor = null;
        switch (action)
        {
            case RIGHT_CLICK_AIR:
                aktor = org.bukkit.event.block.Action.RIGHT_CLICK_AIR;
                break;
            case RIGHT_CLICK_BLOCK:
                aktor = org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;
                break;
            case LEFT_CLICK_BLOCK:
                aktor = org.bukkit.event.block.Action.LEFT_CLICK_BLOCK;
            default:
                aktor = org.bukkit.event.block.Action.LEFT_CLICK_AIR;
                break;
        }
        return onPlayerBukkitInteract( player, action, x, y, z, face, org.bukkit.craftbukkit.event.CraftEventFactory.callPlayerInteractEvent(player, aktor, x, y, z, face, player.inventory.getCurrentItem()));
    }

    @Deprecated
    public static PlayerInteractEvent onPlayerBukkitInteract(EntityPlayer player, Action action, int x, int y, int z, int face, org.bukkit.event.player.PlayerInteractEvent eve)
    {
                PlayerInteractEvent event = new PlayerInteractEvent(player, action, x, y, z, face, null);
                
                //if(eve != null && !(player == null || isOp(player) || isCoFHFakePlayer(player,x,y,z,face)))
                 if(eve != null && !(player == null || isCoFHFakePlayer(player,x,y,z,face))) //Crucible fix op skip.
                {
                	event.cb = eve;
                	if(eve.isCancelled())
                	{
                		event.setCanceled(true);
                		return event;
                	}
                }
                
                if (isSpawn(player) && nonVanilla(player))
                {
                        event.setCanceled(true);
                        if (CauldronCommand.debug)
                                System.out.println("Canceled onPlayerInteract()");
                } else
                        MinecraftForge.EVENT_BUS.post(event);
                return event;

    }
    
    public static PlayerInteractEvent onPlayerInteract(EntityPlayer player, Action action, int x, int y, int z, int face, World world)
    {
    	//if(player == null || isOp(player) || isCoFHFakePlayer(player,x,y,z,face)) //Crucible fix op skip
        if(player == null || isCoFHFakePlayer(player,x,y,z,face)) //Crucible fix op skip
    	{
        	if(CauldronCommand.debug)
        	{
        		System.out.println("[Thermos] CoFH Fake Player / Null player detected...refusing to create Bukkit event");
        	}
        	onPlayerBukkitInteract( player, action, x, y, z, face, world, null);
        }
    	else
    	{
    		if(CauldronCommand.debug)
    		{
        		System.out.println("[Thermos] Creating & calling bukkit event!");
    		}
    	}
        org.bukkit.event.block.Action aktor = null;
        switch (action)
        {
            case RIGHT_CLICK_AIR:
                aktor = org.bukkit.event.block.Action.RIGHT_CLICK_AIR;
                break;
            case RIGHT_CLICK_BLOCK:
                aktor = org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;
                break;
            case LEFT_CLICK_BLOCK:
                aktor = org.bukkit.event.block.Action.LEFT_CLICK_BLOCK;
            default:
                aktor = org.bukkit.event.block.Action.LEFT_CLICK_AIR;
                break;
        }    	
    	return onPlayerBukkitInteract( player, action, x, y, z, face, world, org.bukkit.craftbukkit.event.CraftEventFactory.callPlayerInteractEvent(player, aktor, x, y, z, face, player.inventory.getCurrentItem()));
    }
    
    private static boolean isCoFHFakePlayer(EntityPlayer player, int x, int y, int z, int face)
    {
    	if(CauldronCommand.debug)
    	{
    		System.out.println("[Thermos] Checking if it is a CoFH Fake Player...");
    	}
    	return player instanceof FakePlayer && player.getGameProfile().getName().equals("[CoFH]");
    }
    
    public static PlayerInteractEvent onPlayerBukkitInteract(EntityPlayer player, Action action, int x, int y, int z, int face, World world, org.bukkit.event.player.PlayerInteractEvent eve)
    {
                PlayerInteractEvent event = new PlayerInteractEvent(player, action, x, y, z, face, world);
                
                //if(eve != null && !(player == null || isOp(player) || isCoFHFakePlayer(player,x,y,z,face)))
                if(eve != null && !(player == null || isCoFHFakePlayer(player,x,y,z,face))) //Crucible fix op skip
                {
                	if(CauldronCommand.debug)
                	{
                		System.out.println("[Thermos] Using Bukkit Event for this action...");
                	}
                	event.cb = eve;
                	if(eve.isCancelled()) 
                	{ 
                		event.setCanceled(true); 
                		return event; 
                	}
                }
                else if(eve == null)
                {
                	if(CauldronCommand.debug)
                	{
                		System.out.println("[Thermos] Refusing to call Bukkit event for that interaction");
                	}
                }
                else
                {
                	if(CauldronCommand.debug)
                	{
                		System.out.println("[Thermos] Bukkit event was never considered...single tear");
                	}                	
                }
                
                if (isSpawn(player) && nonVanilla(player))
                {
                        event.setCanceled(true);
                        if (CauldronCommand.debug)
                                System.out.println("Canceled onPlayerInteract()");
                } else
                        MinecraftForge.EVENT_BUS.post(event);
                return event;

    }

    public static void onPlayerDestroyItem(EntityPlayer player, ItemStack stack)
    {
        MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, stack));
    }

    public static Result canEntitySpawn(EntityLiving entity, World world, float x, float y, float z)
    {
        LivingSpawnEvent.CheckSpawn event = new LivingSpawnEvent.CheckSpawn(entity, world, x, y, z);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResult();
    }

    public static boolean doSpecialSpawn(EntityLiving entity, World world, float x, float y, float z)
    {
        return MinecraftForge.EVENT_BUS.post(new LivingSpawnEvent.SpecialSpawn(entity, world, x, y, z));
    }

    public static Result canEntityDespawn(EntityLiving entity)
    {
        AllowDespawn event = new AllowDespawn(entity);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResult();
    }

    public static List<BiomeGenBase.SpawnListEntry> getPotentialSpawns(WorldServer world, EnumCreatureType type, int x, int y, int z, List<BiomeGenBase.SpawnListEntry> oldList)
    {
        WorldEvent.PotentialSpawns event = new WorldEvent.PotentialSpawns(world, type, x, y, z, oldList);
        if (MinecraftForge.EVENT_BUS.post(event))
        {
            return null;
        }
        return event.list;
    }

    public static int getFuelBurnTime(ItemStack fuel)
    {
        FuelBurnTimeEvent event = new FuelBurnTimeEvent(fuel);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResult() == Result.DEFAULT ? -1 : event.burnTime;
    }

    public static int getMaxSpawnPackSize(EntityLiving entity)
    {
        LivingPackSizeEvent maxCanSpawnEvent = new LivingPackSizeEvent(entity);
        MinecraftForge.EVENT_BUS.post(maxCanSpawnEvent);
        return maxCanSpawnEvent.getResult() == Result.ALLOW ? maxCanSpawnEvent.maxPackSize : entity.getMaxSpawnedInChunk();
    }

    public static String getPlayerDisplayName(EntityPlayer player, String username)
    {
        PlayerEvent.NameFormat event = new PlayerEvent.NameFormat(player, username);
        MinecraftForge.EVENT_BUS.post(event);
        return event.displayname;
    }

    public static float fireBlockHarvesting(ArrayList<ItemStack> drops, World world, Block block, int x, int y, int z, int meta, int fortune, float dropChance, boolean silkTouch, EntityPlayer player)
    {
        BlockEvent.HarvestDropsEvent event = new BlockEvent.HarvestDropsEvent(x, y, z, world, block, meta, fortune, dropChance, drops, player, silkTouch);
        MinecraftForge.EVENT_BUS.post(event);
        return event.dropChance;
    }

    public static ItemTooltipEvent onItemTooltip(ItemStack itemStack, EntityPlayer entityPlayer, List<String> toolTip, boolean showAdvancedItemTooltips)
    {
        ItemTooltipEvent event = new ItemTooltipEvent(itemStack, entityPlayer, toolTip, showAdvancedItemTooltips);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }

    public static SummonAidEvent fireZombieSummonAid(EntityZombie zombie, World world, int x, int y, int z, EntityLivingBase attacker, double summonChance)
    {
        SummonAidEvent summonEvent = new SummonAidEvent(zombie, world, x, y, z, attacker, summonChance);
        MinecraftForge.EVENT_BUS.post(summonEvent);
        return summonEvent;
    }

    public static boolean onEntityStruckByLightning(Entity entity, EntityLightningBolt bolt)
    {
        return MinecraftForge.EVENT_BUS.post(new EntityStruckByLightningEvent(entity, bolt));
    }

        public static int onItemUseStart(EntityPlayer player, ItemStack item, int duration)
        {
                PlayerUseItemEvent event;
                if (isSpawn(player) && nonVanilla(player))
                {
                        event = new PlayerUseItemEvent.Start(player, null, duration);
                        if (CauldronCommand.debug)
                                System.out.println("Canceled onItemuseStart()");
                } else
                {
                        event = new PlayerUseItemEvent.Start(player, item, duration);
                }
                return MinecraftForge.EVENT_BUS.post(event) ? -1 : event.duration;
        }

        public static int onItemUseTick(EntityPlayer player, ItemStack item, int duration)
        {
                PlayerUseItemEvent event;
                if (isSpawn(player) && nonVanilla(player))
                {
                        event = new PlayerUseItemEvent.Tick(player, null, duration);
                        if (CauldronCommand.debug)
                                System.out.println("Canceled onItemuseTick()");
                } else
                {
                        event = new PlayerUseItemEvent.Tick(player, item, duration);
                }
                return MinecraftForge.EVENT_BUS.post(event) ? -1 : event.duration;
        }
        public static boolean onUseItemStop(EntityPlayer player, ItemStack item, int duration)
        {
                PlayerUseItemEvent event;
                if (isSpawn(player) && nonVanilla(player))
                {
                        event = new PlayerUseItemEvent.Start(player, null, duration);
                        if (CauldronCommand.debug)
                                System.out.println("Canceled onItemuseStop()");
                } else
                {
                        event = new PlayerUseItemEvent.Start(player, item, duration);
                }
                return MinecraftForge.EVENT_BUS.post(event);
        }

        public static ItemStack onItemUseFinish(EntityPlayer player, ItemStack item, int duration, ItemStack result)
        {
                PlayerUseItemEvent.Finish event;
                if (isSpawn(player) && nonVanilla(player))
                {
                        event = new PlayerUseItemEvent.Finish(player, null, duration, null);
                        if (CauldronCommand.debug)
                                System.out.println("Canceled onItemuseFinish()");
                } else
                        event = new PlayerUseItemEvent.Finish(player, item, duration, result);
                MinecraftForge.EVENT_BUS.post(event);
                return event.result;
        }

    public static void onStartEntityTracking(Entity entity, EntityPlayer player)
    {
        MinecraftForge.EVENT_BUS.post(new PlayerEvent.StartTracking(player, entity));
    }

    public static void onStopEntityTracking(Entity entity, EntityPlayer player)
    {
        MinecraftForge.EVENT_BUS.post(new PlayerEvent.StopTracking(player, entity));
    }

    public static void firePlayerLoadingEvent(EntityPlayer player, File playerDirectory, String uuidString)
    {
        MinecraftForge.EVENT_BUS.post(new PlayerEvent.LoadFromFile(player, playerDirectory, uuidString));
    }

    public static void firePlayerSavingEvent(EntityPlayer player, File playerDirectory, String uuidString)
    {
        MinecraftForge.EVENT_BUS.post(new PlayerEvent.SaveToFile(player, playerDirectory, uuidString));
    }

    public static void firePlayerLoadingEvent(EntityPlayer player, IPlayerFileData playerFileData, String uuidString)
    {
        SaveHandler sh = (SaveHandler) playerFileData;
        File dir = ObfuscationReflectionHelper.getPrivateValue(SaveHandler.class, sh, "playersDirectory", "field_"+"75771_c");
        MinecraftForge.EVENT_BUS.post(new PlayerEvent.LoadFromFile(player, dir, uuidString));
    }

    public static boolean onExplosionStart(World world, Explosion explosion)
    {
        return MinecraftForge.EVENT_BUS.post(new ExplosionEvent.Start(world, explosion));
    }

    public static void onExplosionDetonate(World world, Explosion explosion, List<Entity> list, double diameter)
    {
        //Filter entities to only those who are effected, to prevent modders from seeing more then will be hurt.
        /* Enable this if we get issues with modders looping to much.
        Iterator<Entity> itr = list.iterator();
        while (itr.hasNext())
        {
            Entity e = itr.next();
            double dist = e.getDistance(explosion.explosionX, explosion.explosionY, explosion.explosionZ) / diameter;
            if (dist > 1.0F) itr.remove();
        }
        */
        MinecraftForge.EVENT_BUS.post(new ExplosionEvent.Detonate(world, explosion, list));
    }

    public static boolean onCreateWorldSpawn(World world, WorldSettings settings)
    {
        return MinecraftForge.EVENT_BUS.post(new WorldEvent.CreateSpawnPosition(world, settings));
    }

    public static float onLivingHeal(EntityLivingBase entity, float amount)
    {
        LivingHealEvent event = new LivingHealEvent(entity, amount);
        return (MinecraftForge.EVENT_BUS.post(event) ? 0 : event.amount);
    }

    public static boolean onPotionAttemptBreaw(ItemStack[] stacks)
    {
        ItemStack[] tmp = new ItemStack[stacks.length];
        for (int x = 0; x < tmp.length; x++)
            tmp[x] = ItemStack.copyItemStack(stacks[x]);

        PotionBrewEvent.Pre event = new PotionBrewEvent.Pre(tmp);
        if (MinecraftForge.EVENT_BUS.post(event))
        {
            boolean changed = false;
            for (int x = 0; x < stacks.length; x++)
            {
                changed |= ItemStack.areItemStacksEqual(tmp[x], stacks[x]);
                stacks[x] = event.getItem(x);
            }
            if (changed)
                onPotionBrewed(stacks);
            return true;
        }
        return false;
    }

    public static void onPotionBrewed(ItemStack[] brewingItemStacks)
    {
        MinecraftForge.EVENT_BUS.post(new PotionBrewEvent.Post(brewingItemStacks));
    }
}