package net.minecraft.server.network;

import cpw.mods.fml.common.FMLCommonHandler;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.INetHandlerHandshakeServer;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.server.S00PacketDisconnect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;


// CraftBukkit start
import java.net.InetAddress;
import java.util.HashMap;
// CraftBukkit end
// Spigot start
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
// Spigot end

public class NetHandlerHandshakeTCP implements INetHandlerHandshakeServer
{
    private static final com.google.gson.Gson gson = new com.google.gson.Gson(); // Spigot
    // CraftBukkit start
    private static final HashMap<InetAddress, Long> throttleTracker = new HashMap<InetAddress, Long>();
    private static int throttleCounter = 0;
    // CraftBukkit end

    private final MinecraftServer field_147387_a;
    private final NetworkManager field_147386_b;
    private static final String __OBFID = "CL_00001456";

    public NetHandlerHandshakeTCP(MinecraftServer p_i45295_1_, NetworkManager p_i45295_2_)
    {
        this.field_147387_a = p_i45295_1_;
        this.field_147386_b = p_i45295_2_;
    }

    public void processHandshake(C00Handshake p_147383_1_)
    {
        if (!FMLCommonHandler.instance().shouldAllowPlayerLogins())
        {
            ChatComponentText chatcomponenttext = new ChatComponentText("Server is still starting! Please wait before reconnecting.");
            this.field_147386_b.scheduleOutboundPacket(new S00PacketDisconnect(chatcomponenttext), new GenericFutureListener[0]);
            this.field_147386_b.closeChannel(chatcomponenttext);
            return;
        }

        switch (NetHandlerHandshakeTCP.SwitchEnumConnectionState.field_151291_a[p_147383_1_.func_149594_c().ordinal()])
        {
            case 1:
                this.field_147386_b.setConnectionState(EnumConnectionState.LOGIN);
                ChatComponentText chatcomponenttext;

                // CraftBukkit start
                try
                {
                    long currentTime = System.currentTimeMillis();
                    long connectionThrottle = MinecraftServer.getServer().server.getConnectionThrottle();
                    InetAddress address = ((java.net.InetSocketAddress) this.field_147386_b.getSocketAddress()).getAddress();

                    synchronized (throttleTracker)
                    {
                        if (throttleTracker.containsKey(address) && !"127.0.0.1".equals(address.getHostAddress()) && currentTime - throttleTracker.get(address) < connectionThrottle)
                        {
                            throttleTracker.put(address, currentTime);
                            chatcomponenttext = new ChatComponentText("Connection throttled! Please wait before reconnecting.");
                            this.field_147386_b.scheduleOutboundPacket(new S00PacketDisconnect(chatcomponenttext), new GenericFutureListener[0]);
                            this.field_147386_b.closeChannel(chatcomponenttext); // Should be close
                            return;
                        }

                        throttleTracker.put(address, currentTime);
                        throttleCounter++;

                        if (throttleCounter > 200)
                        {
                            throttleCounter = 0;
                            // Cleanup stale entries
                            java.util.Iterator iter = throttleTracker.entrySet().iterator();

                            while (iter.hasNext())
                            {
                                java.util.Map.Entry<InetAddress, Long> entry = (java.util.Map.Entry) iter.next();

                                if (entry.getValue() > connectionThrottle)
                                {
                                    iter.remove();
                                }
                            }
                        }
                    }
                }
                catch (Throwable t)
                {
                    org.apache.logging.log4j.LogManager.getLogger().debug("Failed to check connection throttle", t);
                }

                // CraftBukkit end

                if (p_147383_1_.func_149595_d() > 5)
                {
                    chatcomponenttext = new ChatComponentText(java.text.MessageFormat.format(org.spigotmc.SpigotConfig.outdatedServerMessage, "1.7.10"));
                    this.field_147386_b.scheduleOutboundPacket(new S00PacketDisconnect(chatcomponenttext), new GenericFutureListener[0]);
                    this.field_147386_b.closeChannel(chatcomponenttext);
                }
                else if (p_147383_1_.func_149595_d() < 5)
                {
                    chatcomponenttext = new ChatComponentText(java.text.MessageFormat.format(org.spigotmc.SpigotConfig.outdatedClientMessage, "1.7.10"));
                    this.field_147386_b.scheduleOutboundPacket(new S00PacketDisconnect(chatcomponenttext), new GenericFutureListener[0]);
                    this.field_147386_b.closeChannel(chatcomponenttext);
                }
                else
                {
                    this.field_147386_b.setNetHandler(new NetHandlerLoginServer(this.field_147387_a, this.field_147386_b));

                    // Spigot Start
                    if (org.spigotmc.SpigotConfig.bungee)
                    {
                        String[] split = p_147383_1_.field_149598_b.split("\00");

                        if (split.length == 3 || split.length == 4)
                        {
                            p_147383_1_.field_149598_b = split[0];
                            field_147386_b.socketAddress = new java.net.InetSocketAddress(split[1], ((java.net.InetSocketAddress) field_147386_b.getSocketAddress()).getPort());
                            field_147386_b.spoofedUUID = UUIDTypeAdapter.fromString( split[2] );
                        }
                        else
                        {
                            chatcomponenttext = new ChatComponentText("If you wish to use IP forwarding, please enable it in your BungeeCord config as well!");
                            this.field_147386_b.scheduleOutboundPacket(new S00PacketDisconnect(chatcomponenttext), new GenericFutureListener[0]);
                            this.field_147386_b.closeChannel(chatcomponenttext);
                            return;
                        }

                        if (split.length == 4)
                        {
                            field_147386_b.spoofedProfile = gson.fromJson(split[3], Property[].class);
                        }
                    }
                    // Spigot End
                    ((NetHandlerLoginServer) this.field_147386_b.getNetHandler()).hostname = p_147383_1_.field_149598_b + ":" + p_147383_1_.field_149599_c; // CraftBukkit - set hostname
                }

                break;
            case 2:
                this.field_147386_b.setConnectionState(EnumConnectionState.STATUS);
                this.field_147386_b.setNetHandler(new NetHandlerStatusServer(this.field_147387_a, this.field_147386_b));
                break;
            default:
                throw new UnsupportedOperationException("Invalid intention " + p_147383_1_.func_149594_c());
        }
    }

    public void onDisconnect(IChatComponent p_147231_1_) {}

    public void onConnectionStateTransition(EnumConnectionState p_147232_1_, EnumConnectionState p_147232_2_)
    {
        if (p_147232_2_ != EnumConnectionState.LOGIN && p_147232_2_ != EnumConnectionState.STATUS)
        {
            throw new UnsupportedOperationException("Invalid state " + p_147232_2_);
        }
    }

    public void onNetworkTick() {}

    static final class SwitchEnumConnectionState
        {
            static final int[] field_151291_a = new int[EnumConnectionState.values().length];
            private static final String __OBFID = "CL_00001457";

            static
            {
                try
                {
                    field_151291_a[EnumConnectionState.LOGIN.ordinal()] = 1;
                }
                catch (NoSuchFieldError var2)
                {
                    ;
                }

                try
                {
                    field_151291_a[EnumConnectionState.STATUS.ordinal()] = 2;
                }
                catch (NoSuchFieldError var1)
                {
                    ;
                }
            }
        }
}