package net.minecraft.network;

import co.aikar.timings.MinecraftTimings;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import io.github.crucible.CrucibleConfigs;
import io.netty.buffer.Unpooled;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.Callable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityMinecartCommandBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEditableBook;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C0CPacketInput;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import net.minecraft.network.play.client.C11PacketEnchantItem;
import net.minecraft.network.play.client.C12PacketUpdateSign;
import net.minecraft.network.play.client.C13PacketPlayerAbilities;
import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.network.play.client.C15PacketClientSettings;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S00PacketKeepAlive;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.network.play.server.S3APacketTabComplete;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.stats.AchievementList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.cauldron.CauldronUtils;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

// CraftBukkit start
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.HashSet;

import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S05PacketSpawnPosition;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.network.play.server.S1BPacketEntityAttach;
import net.minecraft.network.play.server.S33PacketUpdateSign;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.LazyPlayerSet;
import org.bukkit.craftbukkit.util.Waitable;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.util.NumberConversions;
// CraftBukkit end
// Cauldron start
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.inventory.InventoryType;
// Cauldron end

public class NetHandlerPlayServer implements INetHandlerPlayServer
{
    private static final Logger logger = LogManager.getLogger();
    public final NetworkManager netManager;
    private final MinecraftServer serverController;
    public EntityPlayerMP playerEntity;
    private int networkTickCount;
    private int floatingTickCount;
    private boolean field_147366_g;
    private int field_147378_h;
    private long field_147379_i;
    private static Random field_147376_j = new Random();
    private long field_147377_k;
    private volatile int chatSpamThresholdCount; // Cauldron - set to volatile to fix multithreaded issues
    private static final AtomicIntegerFieldUpdater chatSpamField = AtomicIntegerFieldUpdater.newUpdater(NetHandlerPlayServer.class, CauldronUtils.deobfuscatedEnvironment() ? "chatSpamThresholdCount" : "fiel" + "d_147374_l"); // CraftBukkit - multithreaded field
    private int field_147375_m;
    private IntHashMap field_147372_n = new IntHashMap();
    public boolean hasMoved = true; // CraftBukkit - private -> public
    private boolean processedDisconnect; // CraftBukkit - added
    private static final String __OBFID = "CL_00001452";

    public NetHandlerPlayServer(MinecraftServer p_i1530_1_, NetworkManager p_i1530_2_, EntityPlayerMP p_i1530_3_)
    {
        this.serverController = p_i1530_1_;
        this.netManager = p_i1530_2_;
        p_i1530_2_.setNetHandler(this);
        this.playerEntity = p_i1530_3_;
        p_i1530_3_.playerNetServerHandler = this;
        // CraftBukkit start
        this.server = p_i1530_1_ == null ? null : p_i1530_1_.server;
    }

    private final org.bukkit.craftbukkit.CraftServer server;
    private int lastTick = MinecraftServer.currentTick;
    private int lastDropTick = MinecraftServer.currentTick;
    private int dropCount = 0;
    private static final int SURVIVAL_PLACE_DISTANCE_SQUARED = 6 * 6;
    private static final int CREATIVE_PLACE_DISTANCE_SQUARED = 7 * 7;

    private double lastPosX = Double.MAX_VALUE;
    private double lastPosY = Double.MAX_VALUE;
    private double lastPosZ = Double.MAX_VALUE;
    private float lastPitch = Float.MAX_VALUE;
    private float lastYaw = Float.MAX_VALUE;
    private boolean justTeleported = false;

    // For the PacketPlayOutBlockPlace hack :(
    Long lastPacket;

    // Store the last block right clicked and what type it was
    private Item lastMaterial;

    // Cauldron - rename getPlayer -> getPlayerB() to disambiguate with FML's getPlayer() method of the same name (below)
    // Plugins calling this method will be remapped appropriately, but CraftBukkit code should be updated
    public CraftPlayer getPlayerB()
    {
        return (this.playerEntity == null) ? null : (CraftPlayer) this.playerEntity.getBukkitEntity();
    }

    private final static HashSet<Integer> invalidItems = new HashSet<Integer>(java.util.Arrays.asList(8, 9, 10, 11, 26, 34, 36, 43, 51, 52, 55, 59, 60, 62, 63,
            64, 68, 71, 74, 75, 83, 90, 92, 93, 94, 104, 105, 115, 117, 118, 119, 125, 127, 132, 140, 141, 142, 144)); // TODO: Check after every update.
    // CraftBukkit end

    public void onNetworkTick()
    {
        this.field_147366_g = false;
        ++this.networkTickCount;
        this.serverController.theProfiler.startSection("keepAlive");

        if ((long)this.networkTickCount - this.field_147377_k > 40L)
        {
            this.field_147377_k = (long)this.networkTickCount;
            this.field_147379_i = this.func_147363_d();
            this.field_147378_h = (int)this.field_147379_i;
            this.sendPacket(new S00PacketKeepAlive(this.field_147378_h));
        }

        // CraftBukkit start
        for (int spam; (spam = this.chatSpamThresholdCount) > 0 && !chatSpamField.compareAndSet(this, spam, spam - 1);) ;

        /* Use thread-safe field access instead
        if (this.chatSpamThresholdCount > 0)
        {
            --this.chatSpamThresholdCount;
        }
        */
        // CraftBukkit end

        if (this.field_147375_m > 0)
        {
            --this.field_147375_m;
        }

        if (this.playerEntity.func_154331_x() > 0L && this.serverController.func_143007_ar() > 0 && MinecraftServer.getSystemTimeMillis() - this.playerEntity.func_154331_x() > (long)(this.serverController.func_143007_ar() * 1000 * 60))
        {
            this.kickPlayerFromServer("You have been idle for too long!");
        }
    }

    public NetworkManager func_147362_b()
    {
        return this.netManager;
    }

    public void kickPlayerFromServer(String p_147360_1_)
    {
        // CraftBukkit start
        String leaveMessage = EnumChatFormatting.YELLOW + this.playerEntity.getCommandSenderName() + " left the game.";
        PlayerKickEvent event = new PlayerKickEvent(this.server.getPlayer(this.playerEntity), p_147360_1_, leaveMessage);

        if (this.server.getServer().isServerRunning())
        {
            this.server.getPluginManager().callEvent(event);
        }

        if (event.isCancelled())
        {
            // Do not kick the player
            return;
        }

        // Send the possibly modified leave message
        p_147360_1_ = event.getReason();
        // CraftBukkit end
        final ChatComponentText chatcomponenttext = new ChatComponentText(p_147360_1_);
        this.netManager.scheduleOutboundPacket(new S40PacketDisconnect(chatcomponenttext), new GenericFutureListener[] {new GenericFutureListener()
        {
            private static final String __OBFID = "CL_00001453";
            public void operationComplete(Future p_operationComplete_1_)
            {
                NetHandlerPlayServer.this.netManager.closeChannel(chatcomponenttext);
            }
        }});
        this.onDisconnect(chatcomponenttext); // CraftBukkit - Process quit immediately
        this.netManager.disableAutoRead();
    }

    public void processInput(C0CPacketInput p_147358_1_)
    {
        this.playerEntity.setEntityActionState(p_147358_1_.func_149620_c(), p_147358_1_.func_149616_d(), p_147358_1_.func_149618_e(), p_147358_1_.func_149617_f());
    }

    boolean trigger = false;
    public void processPlayer(C03PacketPlayer p_147347_1_)
    {
        // CraftBukkit start - Check for NaN
        if (Double.isNaN(p_147347_1_.field_149479_a) || Double.isNaN(p_147347_1_.field_149477_b) || Double.isNaN(p_147347_1_.field_149478_c)
                || Double.isNaN(p_147347_1_.field_149475_d))
        {
            logger.warn(playerEntity.getCommandSenderName() + " was caught trying to crash the server with an invalid position.");
            getPlayerB().kickPlayer("Nope!");
            return;
        }
        // CraftBukkit end
        WorldServer worldserver = this.serverController.worldServerForDimension(this.playerEntity.dimension);
        this.field_147366_g = true;

        if (!this.playerEntity.playerConqueredTheEnd)
        {
            double d0;

            if (!this.hasMoved)
            {
                d0 = p_147347_1_.func_149467_d() - this.lastPosY;

                if (p_147347_1_.func_149464_c() == this.lastPosX && d0 * d0 < 0.01D && p_147347_1_.func_149472_e() == this.lastPosZ)
                {
                    this.hasMoved = true;
                }
            }

            // CraftBukkit start
            Player player = this.getPlayerB();
            Location from = new Location(player.getWorld(), lastPosX, lastPosY, lastPosZ, lastYaw, lastPitch); // Get the Players previous Event location.
            Location to = player.getLocation().clone(); // Start off the To location as the Players current location.

            // If the packet contains movement information then we update the To location with the correct XYZ.
            if (p_147347_1_.field_149480_h && !(p_147347_1_.field_149480_h && p_147347_1_.field_149477_b == -999.0D && p_147347_1_.field_149475_d == -999.0D))
            {
                to.setX(p_147347_1_.field_149479_a);
                to.setY(p_147347_1_.field_149477_b);
                to.setZ(p_147347_1_.field_149478_c);
            }

            // If the packet contains look information then we update the To location with the correct Yaw & Pitch.
            if (p_147347_1_.field_149481_i)
            {
                to.setYaw(p_147347_1_.field_149476_e);
                to.setPitch(p_147347_1_.field_149473_f);
            }

            // Prevent 40 event-calls for less than a single pixel of movement >.>
            double delta = Math.pow(this.lastPosX - to.getX(), 2) + Math.pow(this.lastPosY - to.getY(), 2) + Math.pow(this.lastPosZ - to.getZ(), 2);
            float deltaAngle = Math.abs(this.lastYaw - to.getYaw()) + Math.abs(this.lastPitch - to.getPitch());

            if ((delta > 2f / 256 || deltaAngle > 10f) && (this.hasMoved && !this.playerEntity.isDead))
            {
                if(this.lastPosX == to.getX() && this.lastPosY == to.getY() && this.lastPosZ == to.getZ() && this.lastYaw == to.getYaw() && this.lastPitch == to.getPitch()) return;
                this.lastPosX = to.getX();
                this.lastPosY = to.getY();
                this.lastPosZ = to.getZ();
                this.lastYaw = to.getYaw();
                this.lastPitch = to.getPitch();

                Location oldTo = to.clone();
                PlayerMoveEvent event = new PlayerMoveEvent(player, from, to);

                trigger = !trigger;
                if(trigger)
                	this.server.getPluginManager().callEvent(event);
                
                // If the event is cancelled we move the player back to their old location.
                if (event.isCancelled())
                {
                    this.playerEntity.playerNetServerHandler.sendPacket(new S08PacketPlayerPosLook(from.getX(), from.getY() + 1.6200000047683716D, from
                            .getZ(), from.getYaw(), from.getPitch(), false));
                    return;
                }

                /* If a Plugin has changed the To destination then we teleport the Player
                there to avoid any 'Moved wrongly' or 'Moved too quickly' errors.
                We only do this if the Event was not cancelled. */
                if (!oldTo.equals(event.getTo()) && !event.isCancelled())
                {
                    this.playerEntity.getBukkitEntity().teleport(event.getTo(), PlayerTeleportEvent.TeleportCause.UNKNOWN);
                    return;
                }

                /* Check to see if the Players Location has some how changed during the call of the event.
                This can happen due to a plugin teleporting the player instead of using .setTo() */
                if (!from.equals(this.getPlayerB().getLocation()) && this.justTeleported)
                {
                    this.justTeleported = false;
                    return;
                }
            }

            if (this.hasMoved && !this.playerEntity.isDead)
            {
                // CraftBukkit end
                double d1;
                double d2;
                double d3;

                if (this.playerEntity.ridingEntity != null)
                {
                    float f4 = this.playerEntity.rotationYaw;
                    float f = this.playerEntity.rotationPitch;
                    this.playerEntity.ridingEntity.updateRiderPosition();
                    d1 = this.playerEntity.posX;
                    d2 = this.playerEntity.posY;
                    d3 = this.playerEntity.posZ;

                    if (p_147347_1_.func_149463_k())
                    {
                        f4 = p_147347_1_.func_149462_g();
                        f = p_147347_1_.func_149470_h();
                    }

                    this.playerEntity.onGround = p_147347_1_.func_149465_i();
                    this.playerEntity.onUpdateEntity();
                    this.playerEntity.ySize = 0.0F;
                    this.playerEntity.setPositionAndRotation(d1, d2, d3, f4, f);

                    if (this.playerEntity.ridingEntity != null)
                    {
                        this.playerEntity.ridingEntity.updateRiderPosition();
                    }

                    if (!this.hasMoved) //Fixes teleportation kick while riding entities
                    {
                        return;
                    }

                    this.serverController.getConfigurationManager().updatePlayerPertinentChunks(this.playerEntity);

                    if (this.hasMoved)
                    {
                        this.lastPosX = this.playerEntity.posX;
                        this.lastPosY = this.playerEntity.posY;
                        this.lastPosZ = this.playerEntity.posZ;
                    }

                    worldserver.updateEntity(this.playerEntity);
                    return;
                }

                if (this.playerEntity.isPlayerSleeping())
                {
                    this.playerEntity.onUpdateEntity();
                    this.playerEntity.setPositionAndRotation(this.lastPosX, this.lastPosY, this.lastPosZ, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
                    worldserver.updateEntity(this.playerEntity);
                    return;
                }

                d0 = this.playerEntity.posY;
                this.lastPosX = this.playerEntity.posX;
                this.lastPosY = this.playerEntity.posY;
                this.lastPosZ = this.playerEntity.posZ;
                d1 = this.playerEntity.posX;
                d2 = this.playerEntity.posY;
                d3 = this.playerEntity.posZ;
                float f1 = this.playerEntity.rotationYaw;
                float f2 = this.playerEntity.rotationPitch;

                if (p_147347_1_.func_149466_j() && p_147347_1_.func_149467_d() == -999.0D && p_147347_1_.func_149471_f() == -999.0D)
                {
                    p_147347_1_.func_149469_a(false);
                }

                double d4;

                if (p_147347_1_.func_149466_j())
                {
                    d1 = p_147347_1_.func_149464_c();
                    d2 = p_147347_1_.func_149467_d();
                    d3 = p_147347_1_.func_149472_e();
                    d4 = p_147347_1_.func_149471_f() - p_147347_1_.func_149467_d();

                    if (Math.abs(p_147347_1_.func_149464_c()) > 3.2E7D || Math.abs(p_147347_1_.func_149472_e()) > 3.2E7D)
                    {
                        this.kickPlayerFromServer("Illegal position");
                        return;
                    }
                }

                if (p_147347_1_.func_149463_k())
                {
                    f1 = p_147347_1_.func_149462_g();
                    f2 = p_147347_1_.func_149470_h();
                }

                this.playerEntity.onUpdateEntity();
                this.playerEntity.ySize = 0.0F;
                this.playerEntity.setPositionAndRotation(this.lastPosX, this.lastPosY, this.lastPosZ, f1, f2);

                if (!this.hasMoved)
                {
                    return;
                }

                d4 = d1 - this.playerEntity.posX;
                double d5 = d2 - this.playerEntity.posY;
                double d6 = d3 - this.playerEntity.posZ;
                //BUGFIX: min -> max, grabs the highest distance
                double d7 = Math.max(Math.abs(d4), Math.abs(this.playerEntity.motionX));
                double d8 = Math.max(Math.abs(d5), Math.abs(this.playerEntity.motionY));
                boolean downMovement = d5 < 0 || this.playerEntity.motionY < 0;
                double d9 = Math.max(Math.abs(d6), Math.abs(this.playerEntity.motionZ));
                double d10 = d7 * d7 + d8 * d8 + d9 * d9;
                // 3D distance traversed, squared
                //if (!this.serverController.isFlightAllowed() && !this.playerEntity.theItemInWorldManager.isCreative() && !worldserver.checkBlockCollision(axisalignedbb) && !this.playerEntity.capabilities.allowFlying)

                // Thermos, allow bypass of moved too quickly if accelerating straight down
                if (d10 > 100.0D && this.hasMoved && (!this.serverController.isSinglePlayer()) && !(downMovement && d8 * d8 / 100.0D > .96))   // CraftBukkit - Added this.checkMovement condition to solve this check being triggered by teleports
                {
                    logger.warn(this.playerEntity.getCommandSenderName() + " moved too quickly! " + d4 + "," + d5 + "," + d6 + " (" + d7 + ", " + d8 + ", " + d9 + ")");
                    this.setPlayerLocation(this.lastPosX, this.lastPosY, this.lastPosZ, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
                    return;
                }

                float f3 = 0.0625F;
                boolean flag = worldserver.getCollidingBoundingBoxes(this.playerEntity, this.playerEntity.boundingBox.copy().contract((double)f3, (double)f3, (double)f3)).isEmpty();

                if (this.playerEntity.onGround && !p_147347_1_.func_149465_i() && d5 > 0.0D)
                {
                    this.playerEntity.jump();
                }

                if (!this.hasMoved) //Fixes "Moved Too Fast" kick when being teleported while moving
                {
                    return;
                }

                this.playerEntity.moveEntity(d4, d5, d6);
                this.playerEntity.onGround = p_147347_1_.func_149465_i();
                this.playerEntity.addMovementStat(d4, d5, d6);
                double d11 = d5;
                d4 = d1 - this.playerEntity.posX;
                d5 = d2 - this.playerEntity.posY;

                if (d5 > -0.5D || d5 < 0.5D)
                {
                    d5 = 0.0D;
                }

                d6 = d3 - this.playerEntity.posZ;
                d10 = d4 * d4 + d5 * d5 + d6 * d6;
                boolean flag1 = false;

                if (d10 > 0.0625D && !this.playerEntity.isPlayerSleeping() && !this.playerEntity.theItemInWorldManager.isCreative())
                {
                    flag1 = true;
                    logger.warn(this.playerEntity.getCommandSenderName() + " moved wrongly!");
                }

                if (!this.hasMoved) //Fixes "Moved Too Fast" kick when being teleported while moving
                {
                    return;
                }

                this.playerEntity.setPositionAndRotation(d1, d2, d3, f1, f2);
                boolean flag2 = worldserver.getCollidingBoundingBoxes(this.playerEntity, this.playerEntity.boundingBox.copy().contract((double)f3, (double)f3, (double)f3)).isEmpty();

                if (flag && (flag1 || !flag2) && !this.playerEntity.isPlayerSleeping() && !this.playerEntity.noClip)
                {
                    this.setPlayerLocation(this.lastPosX, this.lastPosY, this.lastPosZ, f1, f2);
                    return;
                }

                AxisAlignedBB axisalignedbb = this.playerEntity.boundingBox.copy().expand((double)f3, (double)f3, (double)f3).addCoord(0.0D, -0.55D, 0.0D);

                if (!this.serverController.isFlightAllowed() && !this.playerEntity.theItemInWorldManager.isCreative() && !worldserver.checkBlockCollision(axisalignedbb) && !this.playerEntity.capabilities.allowFlying)
                {
                    if (d11 >= -0.03125D)
                    {
                        ++this.floatingTickCount;

                        if (this.floatingTickCount > 80)
                        {
                            logger.warn(this.playerEntity.getCommandSenderName() + " was kicked for floating too long!");
                            this.kickPlayerFromServer("Flying is not enabled on this server");
                            return;
                        }
                    }
                }
                else
                {
                    this.floatingTickCount = 0;
                }

                if (!this.hasMoved) //Fixes "Moved Too Fast" kick when being teleported while moving
                {
                    return;
                }

                this.playerEntity.onGround = p_147347_1_.func_149465_i();
                this.serverController.getConfigurationManager().updatePlayerPertinentChunks(this.playerEntity);
                this.playerEntity.handleFalling(this.playerEntity.posY - d0, p_147347_1_.func_149465_i());
            }
            else if (this.networkTickCount % 20 == 0)
            {
                this.setPlayerLocation(this.lastPosX, this.lastPosY, this.lastPosZ, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
            }
        }
    }

    public void setPlayerLocation(double p_147364_1_, double p_147364_3_, double p_147364_5_, float p_147364_7_, float p_147364_8_)
    {
        // CraftBukkit start - Delegate to teleport(Location)
        Player player = this.getPlayerB();
        Location from = player.getLocation();
        Location to = new Location(this.getPlayerB().getWorld(), p_147364_1_, p_147364_3_, p_147364_5_, p_147364_7_, p_147364_8_);
        PlayerTeleportEvent event = new PlayerTeleportEvent(player, from, to, PlayerTeleportEvent.TeleportCause.UNKNOWN);
        this.server.getPluginManager().callEvent(event);
        from = event.getFrom();
        to = event.isCancelled() ? from : event.getTo();
        this.teleport(to);
    }

    public void teleport(Location dest)
    {
        double d0, d1, d2;
        float f, f1;
        d0 = dest.getX();
        d1 = dest.getY();
        d2 = dest.getZ();
        f = dest.getYaw();
        f1 = dest.getPitch();

        // TODO: make sure this is the best way to address this.
        if (Float.isNaN(f) || Float.isInfinite(f))
        {
            f = 0;
        }

        if (Float.isNaN(f1) || Float.isInfinite(f1))
        {
            f1 = 0;
        }

        this.lastPosX = d0;
        this.lastPosY = d1;
        this.lastPosZ = d2;
        this.lastYaw = f;
        this.lastPitch = f1;
        this.justTeleported = true;
        // CraftBukkit end
        this.hasMoved = false;
        this.lastPosX = d0;
        this.lastPosY = d1;
        this.lastPosZ = d2;
        this.playerEntity.setPositionAndRotation(d0, d1, d2, f, f1);
        this.playerEntity.playerNetServerHandler.sendPacket(new S08PacketPlayerPosLook(d0, d1 + 1.6200000047683716D, d2, f, f1, false));
    }

    public void processPlayerDigging(C07PacketPlayerDigging p_147345_1_)
    {
        if (this.playerEntity.isDead)
        {
            return; // CraftBukkit
        }

        WorldServer worldserver = this.serverController.worldServerForDimension(this.playerEntity.dimension);
        this.playerEntity.func_143004_u();

        if (p_147345_1_.func_149506_g() == 4)
        {
            // CraftBukkit start
            // If the ticks aren't the same then the count starts from 0 and we update the lastDropTick.
            if (this.lastDropTick != MinecraftServer.currentTick)
            {
                this.dropCount = 0;
                this.lastDropTick = MinecraftServer.currentTick;
            }
            else
            {
                // Else we increment the drop count and check the amount.
                this.dropCount++;

                if (this.dropCount >= 20)
                {
                    this.logger.warn(this.playerEntity.getCommandSenderName() + " dropped their items too quickly!");
                    this.kickPlayerFromServer("You dropped your items too quickly (Hacking?)");
                    return;
                }
            }
            // CraftBukkit end
            this.playerEntity.dropOneItem(false);
        }
        else if (p_147345_1_.func_149506_g() == 3)
        {
            this.playerEntity.dropOneItem(true);
        }
        else if (p_147345_1_.func_149506_g() == 5)
        {
            this.playerEntity.stopUsingItem();
        }
        else
        {
            boolean flag = false;

            if (p_147345_1_.func_149506_g() == 0)
            {
                flag = true;
            }

            if (p_147345_1_.func_149506_g() == 1)
            {
                flag = true;
            }

            if (p_147345_1_.func_149506_g() == 2)
            {
                flag = true;
            }

            int i = p_147345_1_.func_149505_c();
            int j = p_147345_1_.func_149503_d();
            int k = p_147345_1_.func_149502_e();

            if (flag)
            {
                double d0 = this.playerEntity.posX - ((double)i + 0.5D);
                double d1 = this.playerEntity.posY - ((double)j + 0.5D) + 1.5D;
                double d2 = this.playerEntity.posZ - ((double)k + 0.5D);
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                double dist = playerEntity.theItemInWorldManager.getBlockReachDistance() + 1;
                dist *= dist;

                if (d3 > dist)
                {
                    return;
                }

                if (j >= this.serverController.getBuildLimit())
                {
                    return;
                }
            }

            if (p_147345_1_.func_149506_g() == 0)
            {
                if (!this.serverController.isBlockProtected(worldserver, i, j, k, this.playerEntity))
                {
                    this.playerEntity.theItemInWorldManager.onBlockClicked(i, j, k, p_147345_1_.func_149501_f());
                }
                else
                {
                    // CraftBukkit start
                    CraftEventFactory.callPlayerInteractEvent(this.playerEntity, Action.LEFT_CLICK_BLOCK, i, j, k, p_147345_1_.func_149501_f(), this.playerEntity.inventory.getCurrentItem());
                    this.playerEntity.playerNetServerHandler.sendPacket(new S23PacketBlockChange(i, j, k, worldserver));
                    // Update any tile entity data for this block
                    TileEntity tileentity = worldserver.getTileEntity(i, j, k);

                    if (tileentity != null)
                    {
                        this.playerEntity.playerNetServerHandler.sendPacket(tileentity.getDescriptionPacket());
                    }
                    // CraftBukkit end
                }
            }
            else if (p_147345_1_.func_149506_g() == 2)
            {
                this.playerEntity.theItemInWorldManager.uncheckedTryHarvestBlock(i, j, k);

                if (worldserver.getBlock(i, j, k).getMaterial() != Material.air)
                {
                    this.playerEntity.playerNetServerHandler.sendPacket(new S23PacketBlockChange(i, j, k, worldserver));
                }
            }
            else if (p_147345_1_.func_149506_g() == 1)
            {
                this.playerEntity.theItemInWorldManager.cancelDestroyingBlock(i, j, k);

                if (worldserver.getBlock(i, j, k).getMaterial() != Material.air)
                {
                    this.playerEntity.playerNetServerHandler.sendPacket(new S23PacketBlockChange(i, j, k, worldserver));
                }
            }
        }
    }

    public void processPlayerBlockPlacement(C08PacketPlayerBlockPlacement p_147346_1_)
    {
        WorldServer worldserver = this.serverController.worldServerForDimension(this.playerEntity.dimension);
        // CraftBukkit start
        if (this.playerEntity.isDead)
        {
            return;
        }

        // This is a horrible hack needed because the client sends 2 packets on 'right mouse click'
        // aimed at a block. We shouldn't need to get the second packet if the data is handled
        // but we cannot know what the client will do, so we might still get it
        //
        // If the time between packets is small enough, and the 'signature' similar, we discard the
        // second one. This sadly has to remain until Mojang makes their packets saner. :(
        //  -- Grum
        if (p_147346_1_.func_149568_f() == 255)
        {
            if (p_147346_1_.func_149574_g() != null && p_147346_1_.func_149574_g().getItem() == this.lastMaterial && this.lastPacket != null
                    && p_147346_1_.timestamp - this.lastPacket < 100)
            {
                this.lastPacket = null;
                return;
            }
        }
        else
        {
            this.lastMaterial = p_147346_1_.func_149574_g() == null ? null : p_147346_1_.func_149574_g().getItem();
            this.lastPacket = p_147346_1_.timestamp;
        }

        ItemStack itemstack = this.playerEntity.inventory.getCurrentItem();
        boolean flag = false;
        boolean placeResult = true;
        int i = p_147346_1_.func_149576_c();
        int j = p_147346_1_.func_149571_d();
        int k = p_147346_1_.func_149570_e();
        int l = p_147346_1_.func_149568_f();
        this.playerEntity.func_143004_u();

        if (p_147346_1_.func_149568_f() == 255)
        {
            if (itemstack == null)
            {
                return;
            }
            org.bukkit.event.player.PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(this.playerEntity, Action.RIGHT_CLICK_AIR, itemstack);
            PlayerInteractEvent forgeEvent = ForgeEventFactory.onPlayerBukkitInteract(playerEntity, PlayerInteractEvent.Action.RIGHT_CLICK_AIR, 0, 0, 0, -1, worldserver, null); // Cauldron - rename event
            // CraftBukkit start
            int itemstackAmount = itemstack.stackSize;

            if (forgeEvent.useItem != cpw.mods.fml.common.eventhandler.Event.Result.DENY && event.useItemInHand() != Event.Result.DENY)
            {
                this.playerEntity.theItemInWorldManager.tryUseItem(this.playerEntity, worldserver, itemstack);
            }
            // CraftBukkit - notch decrements the counter by 1 in the above method with food,
            // snowballs and so forth, but he does it in a place that doesn't cause the
            // inventory update packet to get sent
            placeResult = itemstack.stackSize != itemstackAmount;
            // CraftBukkit end
        }
        else if (p_147346_1_.func_149571_d() >= this.serverController.getBuildLimit() - 1 && (p_147346_1_.func_149568_f() == 1 || p_147346_1_.func_149571_d() >= this.serverController.getBuildLimit()))
        {
            ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("build.tooHigh", new Object[] {Integer.valueOf(this.serverController.getBuildLimit())});
            chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.RED);
            this.playerEntity.playerNetServerHandler.sendPacket(new S02PacketChat(chatcomponenttranslation));
            flag = true;
        }
        else
        {
            // CraftBukkit start - Check if we can actually do something over this large a distance
            Location eyeLoc = this.getPlayerB().getEyeLocation();
            double reachDistance = NumberConversions.square(eyeLoc.getX() - i) + NumberConversions.square(eyeLoc.getY() - j)
                    + NumberConversions.square(eyeLoc.getZ() - k);

            if (reachDistance > (this.getPlayerB().getGameMode() == org.bukkit.GameMode.CREATIVE ? CREATIVE_PLACE_DISTANCE_SQUARED : SURVIVAL_PLACE_DISTANCE_SQUARED))
            {
                return;
            }

            // Cauldron start - record place result so we can update client inventory slot if place event is cancelled. Fixes stacksize client-side bug
            if (!this.playerEntity.theItemInWorldManager.activateBlockOrUseItem(this.playerEntity, worldserver, itemstack, i, j, k, l,
                    p_147346_1_.func_149573_h(), p_147346_1_.func_149569_i(), p_147346_1_.func_149575_j()))
            {
                placeResult = true;
            }
            // Cauldron end
            // CraftBukkit end
            flag = true;
        }

        if (flag)
        {
            this.playerEntity.playerNetServerHandler.sendPacket(new S23PacketBlockChange(i, j, k, worldserver));

            if (l == 0)
            {
                --j;
            }

            if (l == 1)
            {
                ++j;
            }

            if (l == 2)
            {
                --k;
            }

            if (l == 3)
            {
                ++k;
            }

            if (l == 4)
            {
                --i;
            }

            if (l == 5)
            {
                ++i;
            }

            this.playerEntity.playerNetServerHandler.sendPacket(new S23PacketBlockChange(i, j, k, worldserver));
        }

        itemstack = this.playerEntity.inventory.getCurrentItem();

        if (itemstack != null && itemstack.stackSize == 0)
        {
            this.playerEntity.inventory.mainInventory[this.playerEntity.inventory.currentItem] = null;
            itemstack = null;
        }

        if (itemstack == null || itemstack.getMaxItemUseDuration() == 0)
        {
            this.playerEntity.isChangingQuantityOnly = true;
            this.playerEntity.inventory.mainInventory[this.playerEntity.inventory.currentItem] = ItemStack.copyItemStack(this.playerEntity.inventory.mainInventory[this.playerEntity.inventory.currentItem]);
            Slot slot = this.playerEntity.openContainer.getSlotFromInventory(this.playerEntity.inventory, this.playerEntity.inventory.currentItem);
            this.playerEntity.openContainer.detectAndSendChanges();
            this.playerEntity.isChangingQuantityOnly = false;

            if (slot != null && (!ItemStack.areItemStacksEqual(this.playerEntity.inventory.getCurrentItem(), p_147346_1_.func_149574_g()) || !placeResult)) // Cauldron - always is needed to update client itemstack if placement is cancelled
            {
                this.sendPacket(new S2FPacketSetSlot(this.playerEntity.openContainer.windowId, slot.slotNumber, this.playerEntity.inventory.getCurrentItem()));
            }
        }
    }

    public void onDisconnect(IChatComponent p_147231_1_)
    {
        // CraftBukkit start - Rarely it would send a disconnect line twice
        if (this.processedDisconnect)
        {
            return;
        }
        else
        {
            this.processedDisconnect = true;
        }
        // CraftBukkit end
        logger.info(this.playerEntity.getCommandSenderName() + " lost connection: " + p_147231_1_.getUnformattedText()); // CraftBukkit - Don't toString the component
        this.serverController.func_147132_au();
        // CraftBukkit start - Replace vanilla quit message handling with our own.
        /*
        ChatMessage chatcomponenttranslation = new ChatMessage("multiplayer.player.left", new Object[] { this.player.getScoreboardDisplayName()});
        
        chatcomponenttranslation.b().setColor(EnumChatFormat.YELLOW);
        this.minecraftServer.getPlayerList().sendMessage(chatcomponenttranslation);
        */
        this.playerEntity.mountEntityAndWakeUp();
        String quitMessage = this.serverController.getConfigurationManager().disconnect(this.playerEntity);

        if ((quitMessage != null) && (quitMessage.length() > 0))
        {
            this.serverController.getConfigurationManager().sendMessage(CraftChatMessage.fromString(quitMessage));
        }
        // CraftBukkit end

        if (this.serverController.isSinglePlayer() && this.playerEntity.getCommandSenderName().equals(this.serverController.getServerOwner()))
        {
            logger.info("Stopping singleplayer server as player logged out");
            this.serverController.initiateShutdown();
        }
    }

    public void sendPacket(final Packet p_147359_1_)
    {
        if (p_147359_1_ instanceof S02PacketChat)
        {
            S02PacketChat s02packetchat = (S02PacketChat)p_147359_1_;
            EntityPlayer.EnumChatVisibility enumchatvisibility = this.playerEntity.func_147096_v();

            if (enumchatvisibility == EntityPlayer.EnumChatVisibility.HIDDEN)
            {
                return;
            }

            if (enumchatvisibility == EntityPlayer.EnumChatVisibility.SYSTEM && !s02packetchat.func_148916_d())
            {
                return;
            }
        }

        // CraftBukkit start
        if (p_147359_1_ == null)
        {
            return;
        }
        else if (p_147359_1_ instanceof S05PacketSpawnPosition)
        {
            S05PacketSpawnPosition packet6 = (S05PacketSpawnPosition) p_147359_1_;
            this.playerEntity.compassTarget = new Location(this.getPlayerB().getWorld(), packet6.field_149364_a, packet6.field_149362_b, packet6.field_149363_c);
        }
        // CraftBukkit end

        try
        {
            this.netManager.scheduleOutboundPacket(p_147359_1_, new GenericFutureListener[0]);
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Sending packet");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Packet being sent");
            crashreportcategory.addCrashSectionCallable("Packet class", new Callable()
            {
                private static final String __OBFID = "CL_00001454";
                public String call()
                {
                    return p_147359_1_.getClass().getCanonicalName();
                }
            });
            throw new ReportedException(crashreport);
        }
    }

    public void processHeldItemChange(C09PacketHeldItemChange p_147355_1_)
    {
        // CraftBukkit start
        if (this.playerEntity.isDead)
        {
            return;
        }

        if (p_147355_1_.func_149614_c() >= 0 && p_147355_1_.func_149614_c() < InventoryPlayer.getHotbarSize())
        {
            PlayerItemHeldEvent event = new PlayerItemHeldEvent(this.getPlayerB(), this.playerEntity.inventory.currentItem, p_147355_1_.func_149614_c());
            this.server.getPluginManager().callEvent(event);

            if (event.isCancelled())
            {
                this.sendPacket(new S09PacketHeldItemChange(this.playerEntity.inventory.currentItem));
                this.playerEntity.func_143004_u();
                return;
            }
            // CraftBukkit end
            this.playerEntity.inventory.currentItem = p_147355_1_.func_149614_c();
            this.playerEntity.func_143004_u();
        }
        else
        {
            logger.warn(this.playerEntity.getCommandSenderName() + " tried to set an invalid carried item");
            this.kickPlayerFromServer("Nope!"); // CraftBukkit
        }
    }

    public void processChatMessage(C01PacketChatMessage p_147354_1_)
    {
        if (this.playerEntity.isDead || this.playerEntity.func_147096_v() == EntityPlayer.EnumChatVisibility.HIDDEN) // CraftBukkit - dead men tell no tales
        {
            ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("chat.cannotSend", new Object[0]);
            chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.RED);
            this.sendPacket(new S02PacketChat(chatcomponenttranslation));
        }
        else
        {
            this.playerEntity.func_143004_u();
            String s = p_147354_1_.func_149439_c();
            s = StringUtils.normalizeSpace(s);

            for (int i = 0; i < s.length(); ++i)
            {
                if (!ChatAllowedCharacters.isAllowedCharacter(s.charAt(i)))
                {
                    // CraftBukkit start - threadsafety
                    if (p_147354_1_.hasPriority())
                    {
                        Waitable waitable = new Waitable() {
                            @Override
                            protected Object evaluate()
                            {
                                NetHandlerPlayServer.this.kickPlayerFromServer("Illegal characters in chat");
                                return null;
                            }
                        };
                        this.serverController.processQueue.add(waitable);

                        try
                        {
                            waitable.get();
                        }
                        catch (InterruptedException e)
                        {
                            Thread.currentThread().interrupt();
                        }
                        catch (ExecutionException e)
                        {
                            throw new RuntimeException(e);
                        }
                    }
                    else
                    {
                        this.kickPlayerFromServer("Illegal characters in chat");
                    }
                    // CraftBukkit end
                    return;
                }
            }

            // CraftBukkit start
            if (!p_147354_1_.hasPriority())
            {
                try
                {
                    this.serverController.server.playerCommandState = true;
                    this.handleSlashCommand(s);
                }
                finally
                {
                    this.serverController.server.playerCommandState = false;
                }
            }
            else if (s.isEmpty())
            {
                logger.warn(this.playerEntity.getCommandSenderName() + " tried to send an empty message");
            }
            else if (getPlayerB().isConversing())
            {
                getPlayerB().acceptConversationInput(s);
            }
            else if (this.playerEntity.func_147096_v() == EntityPlayer.EnumChatVisibility.SYSTEM) // Re-add "Command Only" flag check
            {
                ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("chat.cannotSend", new Object[0]);
                chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.RED);
                this.sendPacket(new S02PacketChat(chatcomponenttranslation));
            }
            else if (true)
            {
            	String[] bits = s.split(" ");
            	
            	HashSet<String> possibilities = new HashSet<String>(); // No duplicates allowed
            	for (String str: bits)
            	{
            		if (str.length() <= 17 && str.length() >= 4)
            		{
            			if(str.charAt(0) != '@')
            				continue;
            			possibilities.add(str.substring(1));

            		}
            	}
            	
    			for (Object o : MinecraftServer.getServer().getConfigurationManager().playerEntityList)
    			{
    				if (! (o instanceof EntityPlayerMP))
    				{
    					continue;
    				}
    				EntityPlayerMP ep = (EntityPlayerMP)o;
    				if (possibilities.contains(ep.getCommandSenderName()))
    				{
    					ep.worldObj.playSoundAtEntity(ep, "random.orb", 4.0F, 4.0F);
    				}
    				
    			}
            	
                this.chat(s, true);
                // CraftBukkit end - the below is for reference. :)
            }

            // CraftBukkit start - replaced with thread safe throttle
            // this.chatSpamThresholdCount += 20;
            if (chatSpamField.addAndGet(this, 20) > 200 && !this.serverController.getConfigurationManager().func_152596_g(this.playerEntity.getGameProfile()))
            {
                if (p_147354_1_.hasPriority())
                {
                    Waitable waitable = new Waitable() {
                        @Override
                        protected Object evaluate()
                        {
                            NetHandlerPlayServer.this.kickPlayerFromServer("disconnect.spam");
                            return null;
                        }
                    };
                    this.serverController.processQueue.add(waitable);

                    try
                    {
                        waitable.get();
                    }
                    catch (InterruptedException e)
                    {
                        Thread.currentThread().interrupt();
                    }
                    catch (ExecutionException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
                else
                {
                    this.kickPlayerFromServer("disconnect.spam");
                }

                // CraftBukkit end
            }
        }
    }

    // CraftBukkit start
    public void chat(String s, boolean async)
    {
        if (s.isEmpty() || this.playerEntity.func_147096_v() == EntityPlayer.EnumChatVisibility.HIDDEN)
        {
            return;
        }

        if (!async && s.startsWith("/"))
        {
            this.handleSlashCommand(s);
        }
        else if (this.playerEntity.func_147096_v() == EntityPlayer.EnumChatVisibility.SYSTEM)
        {
            // Do nothing, this is coming from a plugin
        }
        else
        {
            // Cauldron start - handle Forge event
            ChatComponentTranslation chatcomponenttranslation1 = new ChatComponentTranslation("chat.type.text", new Object[] {
                    this.playerEntity.func_145748_c_(), s });
            chatcomponenttranslation1 = ForgeHooks.onServerChatEvent(this, s, chatcomponenttranslation1);

            if (chatcomponenttranslation1 != null
                    && chatcomponenttranslation1.getFormatArgs()[chatcomponenttranslation1.getFormatArgs().length - 1] instanceof String)
            {
                // use event message from Forge
                s = (String) chatcomponenttranslation1.getFormatArgs()[chatcomponenttranslation1.getFormatArgs().length - 1];
            }
            // Cauldron end
            Player player = this.getPlayerB();
            AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(async, player, s, new LazyPlayerSet()); // Cauldron - pass changed message if any from Forge
            event.setCancelled(chatcomponenttranslation1 == null); // Cauldron - pre-cancel event if forge event was cancelled
            this.server.getPluginManager().callEvent(event);
            if (PlayerChatEvent.getHandlerList().getRegisteredListeners().length != 0)
            {
                // Evil plugins still listening to deprecated event
                final PlayerChatEvent queueEvent = new PlayerChatEvent(player, event.getMessage(), event.getFormat(), event.getRecipients());
                queueEvent.setCancelled(event.isCancelled());
                Waitable waitable = new Waitable() {
                    @Override
                    protected Object evaluate()
                    {
                        org.bukkit.Bukkit.getPluginManager().callEvent(queueEvent);

                        if (queueEvent.isCancelled())
                        {
                            return null;
                        }

                        String message = String.format(queueEvent.getFormat(), queueEvent.getPlayer().getDisplayName(), queueEvent.getMessage());
                        NetHandlerPlayServer.this.serverController.console.sendMessage(message);
                        if (((LazyPlayerSet) queueEvent.getRecipients()).isLazy())
                        {
                            for (Object recipient : serverController.getConfigurationManager().playerEntityList)
                            {
                                ((EntityPlayerMP) recipient).sendMessage(CraftChatMessage.fromString(message));
                            }
                        }
                        else
                        {
                            for (Player player : queueEvent.getRecipients())
                            {
                                player.sendMessage(message);
                            }
                        }

                        return null;
                    }
                };

                if (async)
                {
                    serverController.processQueue.add(waitable);
                }
                else
                {
                    waitable.run();
                }

                try
                {
                    waitable.get();
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt(); // This is proper habit for java. If we aren't handling it, pass it on!
                }
                catch (ExecutionException e)
                {
                    throw new RuntimeException("Exception processing chat event", e.getCause());
                }
            }
            else
            {
                if (event.isCancelled())
                {
                    return;
                }

                s = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
                serverController.console.sendMessage(s);
                if (((LazyPlayerSet) event.getRecipients()).isLazy())
                {
                    for (Object recipient : serverController.getConfigurationManager().playerEntityList)
                    {
                        for (IChatComponent component : CraftChatMessage.fromString(s))
                        {
                            ((EntityPlayerMP) recipient).sendMessage(CraftChatMessage.fromString(s));
                        }
                    }
                }
                else
                {
                    for (Player recipient : event.getRecipients())
                    {
                        recipient.sendMessage(s);
                    }
                }
            }
        }
    }
    // CraftBukkit end

    private void handleSlashCommand(String p_147361_1_)
    {
        MinecraftTimings.playerCommandTimer.startTiming(); // Spigot
        // CraftBukkit start
        CraftPlayer player = this.getPlayerB();
        
        // Thermos - block specified commands completely - no one will ever believe you.
        if(CrucibleConfigs.configs.cauldron_protection_blockedCommands.contains(p_147361_1_))
        {
            MinecraftTimings.playerCommandTimer.stopTiming(); // Spigot
            return;
        }
        
        
        if (CrucibleConfigs.configs.cauldron_protection_noFallbackAlias)
        {
        	int ind = p_147361_1_.indexOf(':');
        	int spc = p_147361_1_.indexOf(' ');
        	if (ind != -1 && ( ind < spc || spc == -1))
        		p_147361_1_ = "/" + p_147361_1_.substring(ind);
        }        
        
        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, p_147361_1_, new LazyPlayerSet());
        this.server.getPluginManager().callEvent(event);

        if (event.isCancelled())
        {
            MinecraftTimings.playerCommandTimer.stopTiming(); // Spigot
            return;
        }

        try
        {
            // Spigot Start
            if (org.spigotmc.SpigotConfig.logCommands)
            {
                this.logger.info(event.getPlayer().getName() + " issued server command: " + event.getMessage()); // CraftBukkit
            }

            // Spigot end
            // Cauldron start - handle bukkit/vanilla commands
            int space = event.getMessage().indexOf(" ");
            // if bukkit command exists then execute it over vanilla
            if (this.server.getCommandMap().getCommand(event.getMessage().substring(1, space != -1 ? space : event.getMessage().length())) != null)
            {
                this.server.dispatchCommand(event.getPlayer(), event.getMessage().substring(1));
                MinecraftTimings.playerCommandTimer.stopTiming(); // Spigot
                return;
            }
            else
            // process vanilla command
            {
                this.server.dispatchVanillaCommand(event.getPlayer(), event.getMessage().substring(1));
                MinecraftTimings.playerCommandTimer.stopTiming(); // Spigot
                return;
            }
        }
        catch (org.bukkit.command.CommandException ex)
        {
            player.sendMessage(org.bukkit.ChatColor.RED + "An internal error occurred while attempting to perform this command");
            java.util.logging.Logger.getLogger(NetHandlerPlayServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            MinecraftTimings.playerCommandTimer.stopTiming(); // Spigot
            return;
        }

        // this.serverController.getCommandManager().executeCommand(this.playerEntity, p_147361_1_);
        // CraftBukkit end
    }

    public void processAnimation(C0APacketAnimation p_147350_1_)
    {
        if (this.playerEntity.isDead)
        {
            return; // CraftBukkit
        }

        this.playerEntity.func_143004_u();

        if (p_147350_1_.func_149421_d() == 1)
        {
            // CraftBukkit start - Raytrace to look for 'rogue armswings'
            float f = 1.0F;
            float f1 = this.playerEntity.prevRotationPitch + (this.playerEntity.rotationPitch - this.playerEntity.prevRotationPitch) * f;
            float f2 = this.playerEntity.prevRotationYaw + (this.playerEntity.rotationYaw - this.playerEntity.prevRotationYaw) * f;
            double d0 = this.playerEntity.prevPosX + (this.playerEntity.posX - this.playerEntity.prevPosX) * (double) f;
            double d1 = this.playerEntity.prevPosY + (this.playerEntity.posY - this.playerEntity.prevPosY) * (double) f + 1.62D
                    - (double) this.playerEntity.yOffset;
            double d2 = this.playerEntity.prevPosZ + (this.playerEntity.posZ - this.playerEntity.prevPosZ) * (double) f;
            Vec3 vec3 = Vec3.createVectorHelper(d0, d1, d2);
            float f3 = MathHelper.cos(-f2 * 0.017453292F - (float) Math.PI);
            float f4 = MathHelper.sin(-f2 * 0.017453292F - (float) Math.PI);
            float f5 = -MathHelper.cos(-f1 * 0.017453292F);
            float f6 = MathHelper.sin(-f1 * 0.017453292F);
            float f7 = f4 * f5;
            float f8 = f3 * f5;
            double d3 = this.playerEntity.capabilities.isCreativeMode ? 5.0D : 4.5D;
            Vec3 vec31 = vec3.addVector((double) f7 * d3, (double) f6 * d3, (double) f8 * d3);
            MovingObjectPosition movingobjectposition = this.playerEntity.worldObj.rayTraceBlocks(vec3, vec31, true);
            boolean valid = false;

            if (movingobjectposition == null || movingobjectposition.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
            {
                valid = true;
            }
            else
            {
                Block block = this.playerEntity.worldObj.getBlock(movingobjectposition.blockX, movingobjectposition.blockY, movingobjectposition.blockZ);

                if (!block.isOpaqueCube()) // Should be isBreakable?
                {
                    valid = true;
                }
            }

            if (valid)
            {
                CraftEventFactory.callPlayerInteractEvent(this.playerEntity, Action.LEFT_CLICK_AIR, this.playerEntity.inventory.getCurrentItem());
            }

            // Arm swing animation
            PlayerAnimationEvent event = new PlayerAnimationEvent(this.getPlayerB());
            this.server.getPluginManager().callEvent(event);

            if (event.isCancelled())
            {
                return;
            }
            // CraftBukkit end
            this.playerEntity.swingItem();
        }
    }

    public void processEntityAction(C0BPacketEntityAction p_147357_1_)
    {
        // CraftBukkit start
        if (this.playerEntity.isDead)
        {
            return;
        }

        this.playerEntity.func_143004_u();

        if (p_147357_1_.func_149513_d() == 1 || p_147357_1_.func_149513_d() == 2)
        {
            PlayerToggleSneakEvent event = new PlayerToggleSneakEvent(this.getPlayerB(), p_147357_1_.func_149513_d() == 1);
            this.server.getPluginManager().callEvent(event);

            if (event.isCancelled())
            {
                return;
            }
        }

        if (p_147357_1_.func_149513_d() == 4 || p_147357_1_.func_149513_d() == 5)
        {
            PlayerToggleSprintEvent event = new PlayerToggleSprintEvent(this.getPlayerB(), p_147357_1_.func_149513_d() == 4);
            this.server.getPluginManager().callEvent(event);

            if (event.isCancelled())
            {
                return;
            }
        }
        // CraftBukkit end

        if (p_147357_1_.func_149513_d() == 1)
        {
            this.playerEntity.setSneaking(true);
        }
        else if (p_147357_1_.func_149513_d() == 2)
        {
            this.playerEntity.setSneaking(false);
        }
        else if (p_147357_1_.func_149513_d() == 4)
        {
            this.playerEntity.setSprinting(true);
        }
        else if (p_147357_1_.func_149513_d() == 5)
        {
            this.playerEntity.setSprinting(false);
        }
        else if (p_147357_1_.func_149513_d() == 3)
        {
            this.playerEntity.wakeUpPlayer(false, true, true);
            // this.hasMoved = false; // CraftBukkit - this is handled in teleport
        }
        else if (p_147357_1_.func_149513_d() == 6)
        {
            if (this.playerEntity.ridingEntity != null && this.playerEntity.ridingEntity instanceof EntityHorse)
            {
                ((EntityHorse)this.playerEntity.ridingEntity).setJumpPower(p_147357_1_.func_149512_e());
            }
        }
        else if (p_147357_1_.func_149513_d() == 7 && this.playerEntity.ridingEntity != null && this.playerEntity.ridingEntity instanceof EntityHorse)
        {
            ((EntityHorse)this.playerEntity.ridingEntity).openGUI(this.playerEntity);
        }
    }

    public void processUseEntity(C02PacketUseEntity p_147340_1_)
    {
        if (this.playerEntity.isDead)
        {
            return; // CraftBukkit
        }

        WorldServer worldserver = this.serverController.worldServerForDimension(this.playerEntity.dimension);
        Entity entity = p_147340_1_.func_149564_a((World) worldserver);
        // Spigot Start
        if (entity == playerEntity)
        {
            kickPlayerFromServer("Cannot interact with self!");
            return;
        }
        // Spigot End
        this.playerEntity.func_143004_u();

        if (entity != null)
        {
            boolean flag = this.playerEntity.canEntityBeSeen(entity);
            double d0 = 36.0D;

            if (!flag)
            {
                d0 = 9.0D;
            }

            if (this.playerEntity.getDistanceSqToEntity(entity) < d0)
            {
                ItemStack itemInHand = this.playerEntity.inventory.getCurrentItem(); // CraftBukkit

                if (p_147340_1_.func_149565_c() == C02PacketUseEntity.Action.INTERACT)
                {
                    // CraftBukkit start
                    boolean triggerTagUpdate = itemInHand != null && itemInHand.getItem() == Items.name_tag && entity instanceof EntityLiving;
                    boolean triggerChestUpdate = itemInHand != null && itemInHand.getItem() == Item.getItemFromBlock(Blocks.chest)
                            && entity instanceof EntityHorse;
                    boolean triggerLeashUpdate = itemInHand != null && itemInHand.getItem() == Items.lead && entity instanceof EntityLiving;
                    PlayerInteractEntityEvent event = new PlayerInteractEntityEvent((Player) this.getPlayerB(), entity.getBukkitEntity());
                    this.server.getPluginManager().callEvent(event);

                    if (triggerLeashUpdate
                            && (event.isCancelled() || this.playerEntity.inventory.getCurrentItem() == null || this.playerEntity.inventory.getCurrentItem()
                                    .getItem() != Items.lead))
                    {
                        // Refresh the current leash state
                        this.sendPacket(new S1BPacketEntityAttach(1, entity, ((EntityLiving) entity).getLeashedToEntity()));
                    }

                    if (triggerTagUpdate
                            && (event.isCancelled() || this.playerEntity.inventory.getCurrentItem() == null || this.playerEntity.inventory.getCurrentItem()
                                    .getItem() != Items.name_tag))
                    {
                        // Refresh the current entity metadata
                        this.sendPacket(new S1CPacketEntityMetadata(entity.getEntityId(), entity.dataWatcher, true));
                    }

                    if (triggerChestUpdate
                            && (event.isCancelled() || this.playerEntity.inventory.getCurrentItem() == null || this.playerEntity.inventory.getCurrentItem()
                                    .getItem() != Item.getItemFromBlock(Blocks.chest)))
                    {
                        this.sendPacket(new S1CPacketEntityMetadata(entity.getEntityId(), entity.dataWatcher, true));
                    }

                    if (event.isCancelled())
                    {
                        return;
                    }
                    // CraftBukkit end
                    this.playerEntity.interactWith(entity);
                    // CraftBukkit start
                    if (itemInHand != null && itemInHand.stackSize <= -1)
                    {
                        this.playerEntity.sendContainerToPlayer(this.playerEntity.openContainer);
                    }
                    // CraftBukkit end
                }
                else if (p_147340_1_.func_149565_c() == C02PacketUseEntity.Action.ATTACK)
                {
                    if (entity instanceof EntityItem || entity instanceof EntityXPOrb || entity instanceof EntityArrow || entity == this.playerEntity)
                    {
                        this.kickPlayerFromServer("Attempting to attack an invalid entity");
                        this.serverController.logWarning("Player " + this.playerEntity.getCommandSenderName() + " tried to attack an invalid entity");
                        return;
                    }

                    this.playerEntity.attackTargetEntityWithCurrentItem(entity);

                    // CraftBukkit start
                    if (itemInHand != null && itemInHand.stackSize <= -1)
                    {
                        this.playerEntity.sendContainerToPlayer(this.playerEntity.openContainer);
                    }
                    // CraftBukkit end
                }
            }
        }
    }

    public void processClientStatus(C16PacketClientStatus p_147342_1_)
    {
        this.playerEntity.func_143004_u();
        C16PacketClientStatus.EnumState enumstate = p_147342_1_.func_149435_c();
        
        final boolean isDead = this.playerEntity.getHealth() <= 0.0F;
        switch (NetHandlerPlayServer.SwitchEnumState.field_151290_a[enumstate.ordinal()])
        {
            case 1:
                if (this.playerEntity.playerConqueredTheEnd)
                {
                // Cauldron start
                if (this.playerEntity.dimension == 1) // coming from end
                {
                    // We really should be calling transferPlayerToDimension since the player is coming in contact with a portal.
                    this.serverController.getConfigurationManager().respawnPlayer(this.playerEntity, 0, true, null, isDead); // set flag to indicate player is leaving end.
                }
                else
                // not coming from end
                {
                	this.playerEntity = this.serverController.getConfigurationManager().respawnPlayer(this.playerEntity, 0, false, null, isDead);
                }
                // Cauldron end
                }
                else if (this.playerEntity.getServerForPlayer().getWorldInfo().isHardcoreModeEnabled())
                {
                    if (this.serverController.isSinglePlayer() && this.playerEntity.getCommandSenderName().equals(this.serverController.getServerOwner()))
                    {
                        this.playerEntity.playerNetServerHandler.kickPlayerFromServer("You have died. Game over, man, it\'s game over!");
                        this.serverController.deleteWorldAndStopServer();
                    }
                    else
                    {
                        UserListBansEntry userlistbansentry = new UserListBansEntry(this.playerEntity.getGameProfile(), (Date)null, "(You just lost the game)", (Date)null, "Death in Hardcore");
                        this.serverController.getConfigurationManager().func_152608_h().func_152687_a(userlistbansentry);
                        this.playerEntity.playerNetServerHandler.kickPlayerFromServer("You have died. Game over, man, it\'s game over!");
                    }
                }
                else
                {
                    if (this.playerEntity.getHealth() > 0.0F)
                    {
                        return;
                    }

                    this.playerEntity = this.serverController.getConfigurationManager().respawnPlayer(this.playerEntity, playerEntity.dimension, false, null, isDead);
                }

                break;
            case 2:
                this.playerEntity.func_147099_x().func_150876_a(this.playerEntity);
                break;
            case 3:
                this.playerEntity.triggerAchievement(AchievementList.openInventory);
        }
    }

    public void processCloseWindow(C0DPacketCloseWindow p_147356_1_)
    {
        if (this.playerEntity.isDead)
        {
            return; // CraftBukkit
        }

        // Cauldron start - vanilla compatibility
        try
        {
            if (this.playerEntity.openContainer.getBukkitView() != null)
            {
                CraftEventFactory.handleInventoryCloseEvent(this.playerEntity); // CraftBukkit
            }
        }
        catch (AbstractMethodError e)
        {
            // do nothing
        }
        // Cauldron end
        this.playerEntity.closeContainer();
    }

    public void processClickWindow(C0EPacketClickWindow p_147351_1_)
    {
        if (this.playerEntity.isDead)
        {
            return; // CraftBukkit
        }

        this.playerEntity.func_143004_u();

        if (this.playerEntity.openContainer.windowId == p_147351_1_.func_149548_c() && this.playerEntity.openContainer.isPlayerNotUsingContainer(this.playerEntity))
        {
            // CraftBukkit start - Call InventoryClickEvent
            if (p_147351_1_.func_149544_d() < -1 && p_147351_1_.func_149544_d() != -999)
            {
                return;
            }

            InventoryView inventory = this.playerEntity.openContainer.getBukkitView();
            SlotType type = CraftInventoryView.getSlotType(inventory, p_147351_1_.func_149544_d());
            InventoryClickEvent event = null;
            ClickType click = ClickType.UNKNOWN;
            InventoryAction action = InventoryAction.UNKNOWN;
            ItemStack itemstack = null;

            // Cauldron start - some containers such as NEI's Creative Container does not have a view at this point so we need to create one
            if (inventory == null)
            {
                inventory = new CraftInventoryView(this.playerEntity.getBukkitEntity(), MinecraftServer.getServer().server.createInventory(
                        this.playerEntity.getBukkitEntity(), InventoryType.CHEST), this.playerEntity.openContainer);
                this.playerEntity.openContainer.bukkitView = inventory;
            }
            // Cauldron end

            if (p_147351_1_.func_149544_d() == -1)
            {
                type = SlotType.OUTSIDE; // override
                click = p_147351_1_.func_149543_e() == 0 ? ClickType.WINDOW_BORDER_LEFT : ClickType.WINDOW_BORDER_RIGHT;
                action = InventoryAction.NOTHING;
            }
            else if (p_147351_1_.func_149542_h() == 0)
            {
                if (p_147351_1_.func_149543_e() == 0)
                {
                    click = ClickType.LEFT;
                }
                else if (p_147351_1_.func_149543_e() == 1)
                {
                    click = ClickType.RIGHT;
                }

                if (p_147351_1_.func_149543_e() == 0 || p_147351_1_.func_149543_e() == 1)
                {
                    action = InventoryAction.NOTHING; // Don't want to repeat ourselves

                    if (p_147351_1_.func_149544_d() == -999)
                    {
                        if (playerEntity.inventory.getItemStack() != null)
                        {
                            action = p_147351_1_.func_149543_e() == 0 ? InventoryAction.DROP_ALL_CURSOR : InventoryAction.DROP_ONE_CURSOR;
                        }
                    }
                    else
                    {
                        Slot slot = this.playerEntity.openContainer.getSlot(p_147351_1_.func_149544_d());

                        if (slot != null)
                        {
                            ItemStack clickedItem = slot.getStack();
                            ItemStack cursor = playerEntity.inventory.getItemStack();

                            if (clickedItem == null)
                            {
                                if (cursor != null)
                                {
                                    action = p_147351_1_.func_149543_e() == 0 ? InventoryAction.PLACE_ALL : InventoryAction.PLACE_ONE;
                                }
                            }
                            else if (slot.canTakeStack(playerEntity)) // Should be Slot.isPlayerAllowed
                            {
                                if (cursor == null)
                                {
                                    action = p_147351_1_.func_149543_e() == 0 ? InventoryAction.PICKUP_ALL : InventoryAction.PICKUP_HALF;
                                }
                                else if (slot.isItemValid(cursor)) // Should be Slot.isItemAllowed
                                {
                                    if (clickedItem.isItemEqual(cursor) && ItemStack.areItemStackTagsEqual(clickedItem, cursor))
                                    {
                                        int toPlace = p_147351_1_.func_149543_e() == 0 ? cursor.stackSize : 1;
                                        toPlace = Math.min(toPlace, clickedItem.getMaxStackSize() - clickedItem.stackSize);
                                        toPlace = Math.min(toPlace, slot.inventory.getInventoryStackLimit() - clickedItem.stackSize);

                                        if (toPlace == 1)
                                        {
                                            action = InventoryAction.PLACE_ONE;
                                        }
                                        else if (toPlace == cursor.stackSize)
                                        {
                                            action = InventoryAction.PLACE_ALL;
                                        }
                                        else if (toPlace < 0)
                                        {
                                            action = toPlace != -1 ? InventoryAction.PICKUP_SOME : InventoryAction.PICKUP_ONE; // this happens with oversized stacks
                                        }
                                        else if (toPlace != 0)
                                        {
                                            action = InventoryAction.PLACE_SOME;
                                        }
                                    }
                                    else if (cursor.stackSize <= slot.getSlotStackLimit()) // Should be Slot.getMaxStackSize()
                                    {
                                        action = InventoryAction.SWAP_WITH_CURSOR;
                                    }
                                }
                                else if (cursor.getItem() == clickedItem.getItem()
                                        && (!cursor.getHasSubtypes() || cursor.getItemDamage() == clickedItem.getItemDamage())
                                        && ItemStack.areItemStackTagsEqual(cursor, clickedItem))
                                {
                                    if (clickedItem.stackSize >= 0)
                                    {
                                        if (clickedItem.stackSize + cursor.stackSize <= cursor.getMaxStackSize())
                                        {
                                            // As of 1.5, this is result slots only
                                            action = InventoryAction.PICKUP_ALL;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else if (p_147351_1_.func_149542_h() == 1)
            {
                if (p_147351_1_.func_149543_e() == 0)
                {
                    click = ClickType.SHIFT_LEFT;
                }
                else if (p_147351_1_.func_149543_e() == 1)
                {
                    click = ClickType.SHIFT_RIGHT;
                }

                if (p_147351_1_.func_149543_e() == 0 || p_147351_1_.func_149543_e() == 1)
                {
                    if (p_147351_1_.func_149544_d() < 0)
                    {
                        action = InventoryAction.NOTHING;
                    }
                    else
                    {
                        Slot slot = this.playerEntity.openContainer.getSlot(p_147351_1_.func_149544_d());

                        if (slot != null && slot.canTakeStack(this.playerEntity) && slot.getHasStack()) // Should be Slot.hasItem()
                        {
                            action = InventoryAction.MOVE_TO_OTHER_INVENTORY;
                        }
                        else
                        {
                            action = InventoryAction.NOTHING;
                        }
                    }
                }
            }
            else if (p_147351_1_.func_149542_h() == 2)
            {
                if (p_147351_1_.func_149543_e() >= 0 && p_147351_1_.func_149543_e() < 9)
                {
                    click = ClickType.NUMBER_KEY;
                    Slot clickedSlot = this.playerEntity.openContainer.getSlot(p_147351_1_.func_149544_d());

                    if (clickedSlot.canTakeStack(playerEntity))
                    {
                        ItemStack hotbar = this.playerEntity.inventory.getStackInSlot(p_147351_1_.func_149543_e());
                        boolean canCleanSwap = hotbar == null || (clickedSlot.inventory == playerEntity.inventory && clickedSlot.isItemValid(hotbar)); // the slot will accept the hotbar item

                        if (clickedSlot.getHasStack())
                        {
                            if (canCleanSwap)
                            {
                                action = InventoryAction.HOTBAR_SWAP;
                            }
                            else
                            {
                                int firstEmptySlot = playerEntity.inventory.getFirstEmptyStack(); // Should be Inventory.firstEmpty()

                                if (firstEmptySlot > -1)
                                {
                                    action = InventoryAction.HOTBAR_MOVE_AND_READD;
                                }
                                else
                                {
                                    action = InventoryAction.NOTHING; // This is not sane! Mojang: You should test for other slots of same type
                                }
                            }
                        }
                        else if (!clickedSlot.getHasStack() && hotbar != null && clickedSlot.isItemValid(hotbar))
                        {
                            action = InventoryAction.HOTBAR_SWAP;
                        }
                        else
                        {
                            action = InventoryAction.NOTHING;
                        }
                    }
                    else
                    {
                        action = InventoryAction.NOTHING;
                    }

                    // Special constructor for number key
                    event = new InventoryClickEvent(inventory, type, p_147351_1_.func_149544_d(), click, action, p_147351_1_.func_149543_e());
                }
            }
            else if (p_147351_1_.func_149542_h() == 3)
            {
                if (p_147351_1_.func_149543_e() == 2)
                {
                    click = ClickType.MIDDLE;

                    if (p_147351_1_.func_149544_d() == -999)
                    {
                        action = InventoryAction.NOTHING;
                    }
                    else
                    {
                        Slot slot = this.playerEntity.openContainer.getSlot(p_147351_1_.func_149544_d());

                        if (slot != null && slot.getHasStack() && playerEntity.capabilities.isCreativeMode && playerEntity.inventory.getItemStack() == null)
                        {
                            action = InventoryAction.CLONE_STACK;
                        }
                        else
                        {
                            action = InventoryAction.NOTHING;
                        }
                    }
                }
                else
                {
                    click = ClickType.UNKNOWN;
                    action = InventoryAction.UNKNOWN;
                }
            }
            else if (p_147351_1_.func_149542_h() == 4)
            {
                if (p_147351_1_.func_149544_d() >= 0)
                {
                    if (p_147351_1_.func_149543_e() == 0)
                    {
                        click = ClickType.DROP;
                        Slot slot = this.playerEntity.openContainer.getSlot(p_147351_1_.func_149544_d());

                        if (slot != null && slot.getHasStack() && slot.canTakeStack(playerEntity) && slot.getStack() != null
                                && slot.getStack().getItem() != Item.getItemFromBlock(Blocks.air))
                        {
                            action = InventoryAction.DROP_ONE_SLOT;
                        }
                        else
                        {
                            action = InventoryAction.NOTHING;
                        }
                    }
                    else if (p_147351_1_.func_149543_e() == 1)
                    {
                        click = ClickType.CONTROL_DROP;
                        Slot slot = this.playerEntity.openContainer.getSlot(p_147351_1_.func_149544_d());

                        if (slot != null && slot.getHasStack() && slot.canTakeStack(playerEntity) && slot.getStack() != null
                                && slot.getStack().getItem() != Item.getItemFromBlock(Blocks.air))
                        {
                            action = InventoryAction.DROP_ALL_SLOT;
                        }
                        else
                        {
                            action = InventoryAction.NOTHING;
                        }
                    }
                }
                else
                {
                    // Sane default (because this happens when they are holding nothing. Don't ask why.)
                    click = ClickType.LEFT;

                    if (p_147351_1_.func_149543_e() == 1)
                    {
                        click = ClickType.RIGHT;
                    }

                    action = InventoryAction.NOTHING;
                }
            }
            else if (p_147351_1_.func_149542_h() == 5)
            {
                itemstack = this.playerEntity.openContainer.slotClick(p_147351_1_.func_149544_d(), p_147351_1_.func_149543_e(), 5, this.playerEntity);
            }
            else if (p_147351_1_.func_149542_h() == 6)
            {
                click = ClickType.DOUBLE_CLICK;
                action = InventoryAction.NOTHING;

                if (p_147351_1_.func_149544_d() >= 0 && this.playerEntity.inventory.getItemStack() != null)
                {
                    ItemStack cursor = this.playerEntity.inventory.getItemStack();
                    action = InventoryAction.NOTHING;

                    // Quick check for if we have any of the item
                    // Cauldron start - can't call getContents() on modded IInventory; CB-added method
                    try
                    {
                        if (inventory.getTopInventory().contains(org.bukkit.Material.getMaterial(Item.getIdFromItem(cursor.getItem())))
                                || inventory.getBottomInventory().contains(org.bukkit.Material.getMaterial(Item.getIdFromItem(cursor.getItem()))))
                        {
                            action = InventoryAction.COLLECT_TO_CURSOR;
                        }
                    }
                    catch (AbstractMethodError ex)
                    {
                        // nothing we can do
                    }
                    // Cauldron end
                }
            }

            // TODO check on updates

            if (p_147351_1_.func_149542_h() != 5)
            {
                if (click == ClickType.NUMBER_KEY)
                {
                    event = new InventoryClickEvent(inventory, type, p_147351_1_.func_149544_d(), click, action, p_147351_1_.func_149543_e());
                }
                else
                {
                    event = new InventoryClickEvent(inventory, type, p_147351_1_.func_149544_d(), click, action);
                }

                org.bukkit.inventory.Inventory top = inventory.getTopInventory();

                if (p_147351_1_.func_149544_d() == 0 && top instanceof CraftingInventory)
                {
                    // Cauldron start - vanilla compatibility (mod recipes)
                    org.bukkit.inventory.Recipe recipe = null;
                    try
                    {
                        recipe = ((CraftingInventory) top).getRecipe();
                    }
                    catch (AbstractMethodError e)
                    {
                        // do nothing
                    }
                    // Cauldron end

                    if (recipe != null)
                    {
                        if (click == ClickType.NUMBER_KEY)
                        {
                            event = new CraftItemEvent(recipe, inventory, type, p_147351_1_.func_149544_d(), click, action, p_147351_1_.func_149543_e());
                        }
                        else
                        {
                            event = new CraftItemEvent(recipe, inventory, type, p_147351_1_.func_149544_d(), click, action);
                        }
                    }
                }

                server.getPluginManager().callEvent(event);

                switch (event.getResult())
                {
                case ALLOW:
                case DEFAULT:
                    itemstack = this.playerEntity.openContainer.slotClick(p_147351_1_.func_149544_d(), p_147351_1_.func_149543_e(),
                            p_147351_1_.func_149542_h(), this.playerEntity);
                    break;
                case DENY:
                    /* Needs enum constructor in InventoryAction
                    if (action.modifiesOtherSlots()) {
                    } else {
                        if (action.modifiesCursor()) {
                            this.player.playerConnection.sendPacket(new Packet103SetSlot(-1, -1, this.player.inventory.getCarried()));
                        }
                        if (action.modifiesClicked()) {
                            this.player.playerConnection.sendPacket(new Packet103SetSlot(this.player.activeContainer.windowId, packet102windowclick.slot, this.player.activeContainer.getSlot(packet102windowclick.slot).getItem()));
                        }
                    }*/
                    switch (action)
                    {
                    // Modified other slots
                    case PICKUP_ALL:
                    case MOVE_TO_OTHER_INVENTORY:
                    case HOTBAR_MOVE_AND_READD:
                    case HOTBAR_SWAP:
                    case COLLECT_TO_CURSOR:
                    case UNKNOWN:
                        this.playerEntity.sendContainerToPlayer(this.playerEntity.openContainer);
                        break;

                    // Modified cursor and clicked
                    case PICKUP_SOME:
                    case PICKUP_HALF:
                    case PICKUP_ONE:
                    case PLACE_ALL:
                    case PLACE_SOME:
                    case PLACE_ONE:
                    case SWAP_WITH_CURSOR:
                        this.playerEntity.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, this.playerEntity.inventory.getItemStack()));
                        this.playerEntity.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(this.playerEntity.openContainer.windowId, p_147351_1_
                                .func_149544_d(), this.playerEntity.openContainer.getSlot(p_147351_1_.func_149544_d()).getStack()));
                        break;

                    // Modified clicked only
                    case DROP_ALL_SLOT:
                    case DROP_ONE_SLOT:
                        this.playerEntity.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(this.playerEntity.openContainer.windowId, p_147351_1_
                                .func_149544_d(), this.playerEntity.openContainer.getSlot(p_147351_1_.func_149544_d()).getStack()));
                        break;

                    // Modified cursor only
                    case DROP_ALL_CURSOR:
                    case DROP_ONE_CURSOR:
                    case CLONE_STACK:
                        this.playerEntity.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, this.playerEntity.inventory.getItemStack()));
                        break;

                    // Nothing
                    case NOTHING:
                        break;
                    }

                    return;
                }
            }
            // CraftBukkit end

            if (ItemStack.areItemStacksEqual(p_147351_1_.func_149546_g(), itemstack))
            {
                this.playerEntity.playerNetServerHandler.sendPacket(new S32PacketConfirmTransaction(p_147351_1_.func_149548_c(), p_147351_1_.func_149547_f(), true));
                this.playerEntity.isChangingQuantityOnly = true;
                this.playerEntity.openContainer.detectAndSendChanges();
                this.playerEntity.updateHeldItem();
                this.playerEntity.isChangingQuantityOnly = false;
            }
            else
            {
                this.field_147372_n.addKey(this.playerEntity.openContainer.windowId, Short.valueOf(p_147351_1_.func_149547_f()));
                this.playerEntity.playerNetServerHandler.sendPacket(new S32PacketConfirmTransaction(p_147351_1_.func_149548_c(), p_147351_1_.func_149547_f(), false));
                this.playerEntity.openContainer.setPlayerIsPresent(this.playerEntity, false);
                ArrayList arraylist = new ArrayList();

                for (int i = 0; i < this.playerEntity.openContainer.inventorySlots.size(); ++i)
                {
                    arraylist.add(((Slot)this.playerEntity.openContainer.inventorySlots.get(i)).getStack());
                }

                this.playerEntity.sendContainerAndContentsToPlayer(this.playerEntity.openContainer, arraylist);
                // CraftBukkit start - Send a Set Slot to update the crafting result slot
                if (type == SlotType.RESULT && itemstack != null)
                {
                    this.playerEntity.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(this.playerEntity.openContainer.windowId, 0, itemstack));
                }
                // CraftBukkit end
            }
        }
    }

    public void processEnchantItem(C11PacketEnchantItem p_147338_1_)
    {
        this.playerEntity.func_143004_u();

        if (this.playerEntity.openContainer.windowId == p_147338_1_.func_149539_c() && this.playerEntity.openContainer.isPlayerNotUsingContainer(this.playerEntity))
        {
            this.playerEntity.openContainer.enchantItem(this.playerEntity, p_147338_1_.func_149537_d());
            this.playerEntity.openContainer.detectAndSendChanges();
        }
    }

    public void processCreativeInventoryAction(C10PacketCreativeInventoryAction p_147344_1_)
    {
        if (this.playerEntity.theItemInWorldManager.isCreative())
        {
            boolean flag = p_147344_1_.func_149627_c() < 0;
            ItemStack itemstack = p_147344_1_.func_149625_d();
            boolean flag1 = p_147344_1_.func_149627_c() >= 1 && p_147344_1_.func_149627_c() < 36 + InventoryPlayer.getHotbarSize();
            // CraftBukkit - Add invalidItems check
            boolean flag2 = itemstack == null || itemstack.getItem() != null && !invalidItems.contains(Item.getIdFromItem(itemstack.getItem()));
            boolean flag3 = itemstack == null || itemstack.getItemDamage() >= 0 && itemstack.stackSize <= 64 && itemstack.stackSize > 0;
            // CraftBukkit start - Call click event
            if (flag
                    || (flag1 && !ItemStack.areItemStacksEqual(this.playerEntity.inventoryContainer.getSlot(p_147344_1_.func_149627_c()).getStack(),
                            p_147344_1_.func_149625_d()))) // Insist on valid slot
            {
                org.bukkit.entity.HumanEntity player = this.playerEntity.getBukkitEntity();
                InventoryView inventory = new CraftInventoryView(player, player.getInventory(), this.playerEntity.inventoryContainer);
                org.bukkit.inventory.ItemStack item = CraftItemStack.asBukkitCopy(p_147344_1_.func_149625_d()); // Should be packet107setcreativeslot.newitem
                SlotType type = SlotType.QUICKBAR;

                if (flag)
                {
                    type = SlotType.OUTSIDE;
                }
                else if (p_147344_1_.func_149627_c() < 36)
                {
                    if (p_147344_1_.func_149627_c() >= 5 && p_147344_1_.func_149627_c() < 9)
                    {
                        type = SlotType.ARMOR;
                    }
                    else
                    {
                        type = SlotType.CONTAINER;
                    }
                }

                InventoryCreativeEvent event = new InventoryCreativeEvent(inventory, type, flag ? -999 : p_147344_1_.func_149627_c(), item);
                server.getPluginManager().callEvent(event);
                itemstack = CraftItemStack.asNMSCopy(event.getCursor());

                switch (event.getResult())
                {
                case ALLOW:
                    // Plugin cleared the id / stacksize checks
                    flag2 = flag3 = true;
                    break;
                case DEFAULT:
                    break;
                case DENY:
                    // Reset the slot
                    if (p_147344_1_.func_149627_c() >= 0)
                    {
                        this.playerEntity.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(this.playerEntity.inventoryContainer.windowId, p_147344_1_
                                .func_149627_c(), this.playerEntity.inventoryContainer.getSlot(p_147344_1_.func_149627_c()).getStack()));
                        this.playerEntity.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, null));
                    }

                    return;
                }
            }
            // CraftBukkit end

            if (flag1 && flag2 && flag3)
            {
                if (itemstack == null)
                {
                    this.playerEntity.inventoryContainer.putStackInSlot(p_147344_1_.func_149627_c(), (ItemStack)null);
                }
                else
                {
                    this.playerEntity.inventoryContainer.putStackInSlot(p_147344_1_.func_149627_c(), itemstack);
                }

                this.playerEntity.inventoryContainer.setPlayerIsPresent(this.playerEntity, true);
            }
            else if (flag && flag2 && flag3 && this.field_147375_m < 200)
            {
                this.field_147375_m += 20;
                EntityItem entityitem = this.playerEntity.dropPlayerItemWithRandomChoice(itemstack, true);

                if (entityitem != null)
                {
                    entityitem.setAgeToCreativeDespawnTime();
                }
            }
        }
    }

    public void processConfirmTransaction(C0FPacketConfirmTransaction p_147339_1_)
    {
        if (this.playerEntity.isDead)
        {
            return; // CraftBukkit
        }

        Short oshort = (Short)this.field_147372_n.lookup(this.playerEntity.openContainer.windowId);

        if (oshort != null && p_147339_1_.func_149533_d() == oshort.shortValue() && this.playerEntity.openContainer.windowId == p_147339_1_.func_149532_c() && !this.playerEntity.openContainer.isPlayerNotUsingContainer(this.playerEntity))
        {
            this.playerEntity.openContainer.setPlayerIsPresent(this.playerEntity, true);
        }
    }

    public void processUpdateSign(C12PacketUpdateSign p_147343_1_)
    {
        if (this.playerEntity.isDead)
        {
            return; // CraftBukkit
        }

        this.playerEntity.func_143004_u();
        WorldServer worldserver = this.serverController.worldServerForDimension(this.playerEntity.dimension);

        if (worldserver.blockExists(p_147343_1_.func_149588_c(), p_147343_1_.func_149586_d(), p_147343_1_.func_149585_e()))
        {
            TileEntity tileentity = worldserver.getTileEntity(p_147343_1_.func_149588_c(), p_147343_1_.func_149586_d(), p_147343_1_.func_149585_e());

            if (tileentity instanceof TileEntitySign)
            {
                TileEntitySign tileentitysign = (TileEntitySign)tileentity;

                if (!tileentitysign.func_145914_a() || tileentitysign.func_145911_b() != this.playerEntity)
                {
                    this.serverController.logWarning("Player " + this.playerEntity.getCommandSenderName() + " just tried to change non-editable sign");
                    this.sendPacket(new S33PacketUpdateSign(p_147343_1_.func_149588_c(), p_147343_1_.func_149586_d(), p_147343_1_.func_149585_e(), tileentitysign.signText)); // CraftBukkit
                    return;
                }
            }

            int i;
            int j;

            for (j = 0; j < 4; ++j)
            {
                boolean flag = true;
                p_147343_1_.func_149589_f()[j] = p_147343_1_.func_149589_f()[j].replaceAll("\uF700", "").replaceAll("\uF701", ""); // Spigot - Mac OSX sends weird chars

                if (p_147343_1_.func_149589_f()[j].length() > 15)
                {
                    flag = false;
                }
                else
                {
                    for (i = 0; i < p_147343_1_.func_149589_f()[j].length(); ++i)
                    {
                        if (!ChatAllowedCharacters.isAllowedCharacter(p_147343_1_.func_149589_f()[j].charAt(i)))
                        {
                            flag = false;
                        }
                    }
                }

                if (!flag)
                {
                    p_147343_1_.func_149589_f()[j] = "!?";
                }
            }

            if (tileentity instanceof TileEntitySign)
            {
                j = p_147343_1_.func_149588_c();
                int k = p_147343_1_.func_149586_d();
                i = p_147343_1_.func_149585_e();
                TileEntitySign tileentitysign1 = (TileEntitySign)tileentity;
                // CraftBukkit start
                Player player = this.server.getPlayer(this.playerEntity);
                SignChangeEvent event = new SignChangeEvent((org.bukkit.craftbukkit.block.CraftBlock) player.getWorld().getBlockAt(j, k, i),
                        this.server.getPlayer(this.playerEntity), p_147343_1_.func_149589_f());
                this.server.getPluginManager().callEvent(event);

                if (!event.isCancelled())
                {
                    for (int l = 0; l < 4; ++l)
                    {
                        tileentitysign1.signText[l] = event.getLine(l);

                        if (tileentitysign1.signText[l] == null)
                        {
                            tileentitysign1.signText[l] = "";
                        }
                    }

                    tileentitysign1.field_145916_j = false;
                }

                // System.arraycopy(p_147343_1_.func_149589_f(), 0, tileentitysign1.signText, 0, 4);
                // CraftBukkit end
                tileentitysign1.markDirty();
                worldserver.markBlockForUpdate(j, k, i);
            }
        }
    }

    public void processKeepAlive(C00PacketKeepAlive p_147353_1_)
    {
        if (p_147353_1_.func_149460_c() == this.field_147378_h)
        {
            int i = (int)(this.func_147363_d() - this.field_147379_i);
            this.playerEntity.ping = (this.playerEntity.ping * 3 + i) / 4;
        }
    }

    private long func_147363_d()
    {
        return System.nanoTime() / 1000000L;
    }

    public void processPlayerAbilities(C13PacketPlayerAbilities p_147348_1_)
    {
        // CraftBukkit start - d() should be isFlying()
        if (this.playerEntity.capabilities.allowFlying && this.playerEntity.capabilities.isFlying != p_147348_1_.func_149488_d())
        {
            PlayerToggleFlightEvent event = new PlayerToggleFlightEvent(this.server.getPlayer(this.playerEntity), p_147348_1_.func_149488_d());
            this.server.getPluginManager().callEvent(event);

            if (!event.isCancelled())
            {
                this.playerEntity.capabilities.isFlying = p_147348_1_.func_149488_d(); // Actually set the player's flying status
            }
            else
            {
                this.playerEntity.sendPlayerAbilities(); // Tell the player their ability was reverted
            }
        }
        // CraftBukkit end
    }

    public void processTabComplete(C14PacketTabComplete p_147341_1_)
    {
        ArrayList arraylist = Lists.newArrayList();
        Iterator iterator = this.serverController.getPossibleCompletions(this.playerEntity, p_147341_1_.func_149419_c()).iterator();

        while (iterator.hasNext())
        {
            String s = (String)iterator.next();
            arraylist.add(s);
        }

        this.playerEntity.playerNetServerHandler.sendPacket(new S3APacketTabComplete((String[])arraylist.toArray(new String[arraylist.size()])));
    }

    public void processClientSettings(C15PacketClientSettings p_147352_1_)
    {
        this.playerEntity.func_147100_a(p_147352_1_);
    }

    public void processVanilla250Packet(C17PacketCustomPayload p_147349_1_)
    {
        PacketBuffer packetbuffer;
        ItemStack push;
        ItemStack nouveau;
        ItemStack stack;

        if ("MC|BEdit".equals(p_147349_1_.func_149559_c()))
        {
            packetbuffer = new PacketBuffer(Unpooled.wrappedBuffer(p_147349_1_.func_149558_e()));

            try
            {
                push = packetbuffer.readItemStackFromBuffer();

                if (push == null)
                {
                    return;
                }

                if (!ItemWritableBook.func_150930_a(push.getTagCompound()))
                {
                    throw new IOException("Invalid book tag!");
                }

                stack = this.playerEntity.inventory.getCurrentItem();

                if (stack != null)
                {
                    if (push.getItem() == Items.writable_book && push.getItem() == stack.getItem())
                    {
                        //nouveau = stack.copy();
                        //ItemWritableBook.shadowUpdate(nouveau, push, false);
                        //CraftEventFactory.handleEditBookEvent(playerEntity, nouveau); // CraftBukkit
                        //this.playerEntity.inventory.setInventorySlotContents(this.playerEntity.inventory.currentItem, nouveau);
                        ItemWritableBook.shadowUpdate(stack, push, false);
                        CraftEventFactory.handleEditBookEvent(playerEntity, stack); // CraftBukkit
                    }
                    else
                    {
                        throw new RuntimeException("Bogey book-user! Stop using AllUWant");
                    }

                    return;
                }
            }
            // CraftBukkit start
            catch (Throwable throwable)
            {
                logger.error("Couldn\'t handle book info", throwable);
                if (throwable instanceof RuntimeException && !(throwable instanceof IOException))
                {
                    this.kickPlayerFromServer(throwable.getMessage());
                }
                else
                {
                    this.kickPlayerFromServer("Invalid book data!");
                }
                // CraftBukkit end
            }
            finally
            {
                packetbuffer.release();
            }

            return;
        }
        else if ("MC|BSign".equals(p_147349_1_.func_149559_c()))
        {
            packetbuffer = new PacketBuffer(Unpooled.wrappedBuffer(p_147349_1_.func_149558_e()));

            try
            {
                push = packetbuffer.readItemStackFromBuffer();

                if (push != null)
                {
                    if (!ItemEditableBook.validBookTagContents(push.getTagCompound()))
                    {
                        throw new IOException("Invalid book tag!");
                    }

                    stack = this.playerEntity.inventory.getCurrentItem();

                    if (stack == null)
                    {
                        return;
                    }

                    if (push.getItem() == Items.written_book && stack.getItem() == Items.writable_book)
                    {
                        nouveau = stack.copy();
                        nouveau.func_150996_a(Items.written_book);
                        nouveau.setTagInfo("author", new NBTTagString(this.playerEntity.getCommandSenderName()));
                        ItemWritableBook.shadowUpdate(nouveau, push, true);
                        this.playerEntity.inventory.setInventorySlotContents(this.playerEntity.inventory.currentItem, nouveau);
                        CraftEventFactory.handleEditBookEvent(playerEntity,nouveau);
                    }

                    return;
                }
            }
            // CraftBukkit start
            catch (Throwable throwable)
            {
                logger.error("Couldn\'t sign book", throwable);
                if (throwable instanceof RuntimeException && !(throwable instanceof IOException))
                {
                    this.kickPlayerFromServer(throwable.getMessage());
                }
                else
                {
                    this.kickPlayerFromServer("Invalid book data!");
                }
                // CraftBukkit end
            }
            finally
            {
                packetbuffer.release();
            }

            return;
        }
        else
        {
            DataInputStream datainputstream;
            int i;

            if ("MC|TrSel".equals(p_147349_1_.func_149559_c()))
            {
                try
                {
                    datainputstream = new DataInputStream(new ByteArrayInputStream(p_147349_1_.func_149558_e()));
                    i = datainputstream.readInt();
                    Container container = this.playerEntity.openContainer;

                    if (container instanceof ContainerMerchant)
                    {
                        ((ContainerMerchant)container).setCurrentRecipeIndex(i);
                    }
                }
                // CraftBukkit start
                catch (Throwable exception2)
                {
                    logger.error("Couldn\'t select trade", exception2);
                    this.kickPlayerFromServer("Invalid trade data!");
                    // CraftBukkit end
                }
            }
            else if ("MC|AdvCdm".equals(p_147349_1_.func_149559_c()))
            {
                if (!this.serverController.isCommandBlockEnabled())
                {
                    this.playerEntity.addChatMessage(new ChatComponentTranslation("advMode.notEnabled", new Object[0]));
                }
                else if (this.playerEntity.canCommandSenderUseCommand(2, "") && this.playerEntity.capabilities.isCreativeMode)
                {
                    packetbuffer = new PacketBuffer(Unpooled.wrappedBuffer(p_147349_1_.func_149558_e()));

                    try
                    {
                        byte b0 = packetbuffer.readByte();
                        CommandBlockLogic commandblocklogic = null;

                        if (b0 == 0)
                        {
                            TileEntity tileentity = this.playerEntity.worldObj.getTileEntity(packetbuffer.readInt(), packetbuffer.readInt(), packetbuffer.readInt());

                            if (tileentity instanceof TileEntityCommandBlock)
                            {
                                commandblocklogic = ((TileEntityCommandBlock)tileentity).func_145993_a();
                            }
                        }
                        else if (b0 == 1)
                        {
                            Entity entity = this.playerEntity.worldObj.getEntityByID(packetbuffer.readInt());

                            if (entity instanceof EntityMinecartCommandBlock)
                            {
                                commandblocklogic = ((EntityMinecartCommandBlock)entity).func_145822_e();
                            }
                        }

                        String s1 = packetbuffer.readStringFromBuffer(packetbuffer.readableBytes());

                        if (commandblocklogic != null)
                        {
                            commandblocklogic.func_145752_a(s1);
                            commandblocklogic.func_145756_e();
                            this.playerEntity.addChatMessage(new ChatComponentTranslation("advMode.setCommand.success", new Object[] {s1}));
                        }
                    }
                    // CraftBukkit start
                    catch (Throwable exception3)
                    {
                        logger.error("Couldn\'t set command block", exception3);
                        this.kickPlayerFromServer("Invalid CommandBlock data!");
                        // CraftBukkit end
                    }
                    finally
                    {
                        packetbuffer.release();
                    }
                }
                else
                {
                    this.playerEntity.addChatMessage(new ChatComponentTranslation("advMode.notAllowed", new Object[0]));
                }
            }
            else if ("MC|Beacon".equals(p_147349_1_.func_149559_c()))
            {
                if (this.playerEntity.openContainer instanceof ContainerBeacon)
                {
                    try
                    {
                        datainputstream = new DataInputStream(new ByteArrayInputStream(p_147349_1_.func_149558_e()));
                        i = datainputstream.readInt();
                        int j = datainputstream.readInt();
                        ContainerBeacon containerbeacon = (ContainerBeacon)this.playerEntity.openContainer;
                        Slot slot = containerbeacon.getSlot(0);

                        if (slot.getHasStack())
                        {
                            slot.decrStackSize(1);
                            TileEntityBeacon tileentitybeacon = containerbeacon.func_148327_e();
                            tileentitybeacon.setPrimaryEffect(i);
                            tileentitybeacon.setSecondaryEffect(j);
                            tileentitybeacon.markDirty();
                        }
                    }
                    // CraftBukkit start
                    catch (Throwable exception4)
                    {
                        logger.error("Couldn\'t set beacon", exception4);
                        this.kickPlayerFromServer("Invalid beacon data!");
                        // CraftBukkit end
                    }
                }
            }
            else if ("MC|ItemName".equals(p_147349_1_.func_149559_c()) && this.playerEntity.openContainer instanceof ContainerRepair)
            {
                ContainerRepair containerrepair = (ContainerRepair)this.playerEntity.openContainer;

                if (p_147349_1_.func_149558_e() != null && p_147349_1_.func_149558_e().length >= 1)
                {
                    String s = ChatAllowedCharacters.filerAllowedCharacters(new String(p_147349_1_.func_149558_e(), Charsets.UTF_8));

                    if (s.length() <= 30)
                    {
                        containerrepair.updateItemName(s);
                    }
                }
                else
                {
                    containerrepair.updateItemName("");
                }
            }
            // CraftBukkit start
            // Cauldron - bukkit registration moved to FML's ChannelRegistrationHandler
            else
            {
                server.getMessenger().dispatchIncomingMessage(playerEntity.getBukkitEntity(), p_147349_1_.func_149559_c(), p_147349_1_.func_149558_e());
            }
            // CraftBukkit end
        }
    }

    public void onConnectionStateTransition(EnumConnectionState p_147232_1_, EnumConnectionState p_147232_2_)
    {
        if (p_147232_2_ != EnumConnectionState.PLAY)
        {
            throw new IllegalStateException("Unexpected change in protocol!");
        }
    }

    // CraftBukkit start - Add "isDisconnected" method
    public final boolean isDisconnected()
    {
        return !this.netManager.channel().config().isAutoRead();
    }

    // CraftBukkit end

    // Cauldron start
    public CraftServer getCraftServer()
    {
        return this.server;
    }
    // Cauldron end

    static final class SwitchEnumState
        {
            static final int[] field_151290_a = new int[C16PacketClientStatus.EnumState.values().length];
            private static final String __OBFID = "CL_00001455";

            static
            {
                try
                {
                    field_151290_a[C16PacketClientStatus.EnumState.PERFORM_RESPAWN.ordinal()] = 1;
                }
                catch (NoSuchFieldError var3)
                {
                    ;
                }

                try
                {
                    field_151290_a[C16PacketClientStatus.EnumState.REQUEST_STATS.ordinal()] = 2;
                }
                catch (NoSuchFieldError var2)
                {
                    ;
                }

                try
                {
                    field_151290_a[C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT.ordinal()] = 3;
                }
                catch (NoSuchFieldError var1)
                {
                    ;
                }
            }
        }
}