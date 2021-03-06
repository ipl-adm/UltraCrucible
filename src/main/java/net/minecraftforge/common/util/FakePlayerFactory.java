package net.minecraftforge.common.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;

import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

//To be expanded for generic Mod fake players?
public class FakePlayerFactory
{
    private static GameProfile MINECRAFT = new GameProfile(UUID.fromString("41C82C87-7AfB-4024-BA57-13D2C99CAE77"), "[Minecraft]");
    // Map of all active fake player usernames to their entities
    public static Map<GameProfile, FakePlayer> fakePlayers = Maps.newHashMap();
    private static FakePlayer MINECRAFT_PLAYER = null;
    
    public static FakePlayer getMinecraft(WorldServer world)
    {
        if (MINECRAFT_PLAYER == null)
        {
            MINECRAFT_PLAYER = FakePlayerFactory.get(world,  MINECRAFT);
        }
        return MINECRAFT_PLAYER;
    }
    
    /**
     * Get a fake player with a given username,
     * Mods should either hold weak references to the return value, or listen for a 
     * WorldEvent.Unload and kill all references to prevent worlds staying in memory.
     */
    public static FakePlayer get(WorldServer world, GameProfile username)
    {
        // Cauldron start - Refactored below to avoid a hashCode check with a null GameProfile ID
        if (username == null || username.getName() == null) return null;

        for (Map.Entry<GameProfile, FakePlayer> mapEntry : fakePlayers.entrySet())
        {
            GameProfile gameprofile = mapEntry.getKey();
            if (gameprofile.getName().equals(username.getName()))
            {
                return mapEntry.getValue();
            }
        }
        FakePlayer fakePlayer = new FakePlayer(world, username);
        if (username.getId() == null) // GameProfile hashCode check will fail with a null ID
        {
            username = new GameProfile(UUID.randomUUID(), username.getName()); // Create new GameProfile with random UUID
        }
        // Cauldron end
        fakePlayers.put(username, fakePlayer);
        return fakePlayers.get(username);
    }

    public static void unloadWorld(WorldServer world)
    {
        Iterator<Entry<GameProfile, FakePlayer>> itr = fakePlayers.entrySet().iterator();
        while (itr.hasNext())
        {
            Entry<GameProfile, FakePlayer> entry = itr.next();
            if (entry.getValue().worldObj == world)
            {
                itr.remove();
            }
        }
    }
}