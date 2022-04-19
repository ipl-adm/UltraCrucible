package net.minecraft.world.chunk.storage;

import io.github.crucible.CrucibleConfigs;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class RegionFileCache
{
	public static final Map<File, RegionFile> REGIONS_BY_FILE = new LinkedHashMap<File, RegionFile>(CrucibleConfigs.configs.crucible_chunkCacheSize, 0.75f, true); // Spigot - private -> public, Paper - HashMap -> LinkedHashMap
	private static final String __OBFID = "CL_00000383";

	public static synchronized RegionFile createOrLoadRegionFile(File worldDir, int chunkX, int chunkZ)
	{
		return createOrLoadRegionFile(worldDir,  chunkX,  chunkZ, true);
	}
	public static synchronized RegionFile createOrLoadRegionFile(File worldDir, int chunkX, int chunkZ, boolean create)
	{
		File file2 = new File(worldDir, "region");
		File file3 = new File(file2, "r." + (chunkX >> 5) + "." + (chunkZ >> 5) + ".mca");
		RegionFile regionfile = (RegionFile)REGIONS_BY_FILE.get(file3);

		if (regionfile != null)
		{
			return regionfile;
		}
		else
		{
			if (!create && !file2.exists()) { return null; } // PaperSpigot
			if (!file2.exists())
			{
				file2.mkdirs();
			}

			if (REGIONS_BY_FILE.size() >= 256)
			{
				deleteOldestReference();
			}

			RegionFile regionfile1 = new RegionFile(file3);
			REGIONS_BY_FILE.put(file3, regionfile1);
			return regionfile1;
		}
	}


	// Thermos a kinda-copy of the method below, except here we delete the oldest stream rather than deleting all filestreams
	// This is a key part of ensuring that the chunk existence thread can complete its necessary tasks
	public static synchronized void deleteOldestReference()
	{
		long oldest = System.currentTimeMillis();
		File toJunk = null;
		for (Map.Entry<File,RegionFile> entry :REGIONS_BY_FILE.entrySet())
		{
			if(entry.getKey().lastModified() < oldest)
			{
				oldest = entry.getKey().lastModified();
				toJunk = entry.getKey();
			}
		}
		if(toJunk != null)
		{
			try
			{
				REGIONS_BY_FILE.remove(toJunk).close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		else // Default to usual behavior if this fails
		{
			clearRegionFileReferences();
		}
	}

	public static synchronized void clearRegionFileReferences()
	{
		Iterator iterator = REGIONS_BY_FILE.values().iterator();

		while (iterator.hasNext())
		{
			RegionFile regionfile = (RegionFile)iterator.next();

			try
			{
				if (regionfile != null)
				{
					regionfile.close();
				}
			}
			catch (IOException ioexception)
			{
				ioexception.printStackTrace();
			}
		}

		REGIONS_BY_FILE.clear();
	}

	public static synchronized RegionFile getRegionFileIfExists(File worldDir, int chunkX, int chunkZ)
	{
		File file1 = new File(worldDir, "region");
		File file2 = new File(file1, "r." + (chunkX >> 5) + "." + (chunkZ >> 5) + ".mca");
		RegionFile regionfile = REGIONS_BY_FILE.get(file2);

		if (regionfile != null)
		{
			return regionfile;
		}
		else if (file1.exists() && file2.exists())
		{
			if (REGIONS_BY_FILE.size() >= 256)
			{
				clearRegionFileReferences();
			}

			RegionFile regionfile1 = new RegionFile(file2);
			REGIONS_BY_FILE.put(file2, regionfile1);
			return regionfile1;
		}
		else
		{
			return null;
		}
	}

	public static DataInputStream getChunkInputStream(File p_76549_0_, int p_76549_1_, int p_76549_2_)
	{
		RegionFile regionfile = createOrLoadRegionFile(p_76549_0_, p_76549_1_, p_76549_2_);
		return regionfile.getChunkDataInputStream(p_76549_1_ & 31, p_76549_2_ & 31);
	}

	public static DataOutputStream getChunkOutputStream(File worldDir, int chunkX, int chunkZ)
	{
		RegionFile regionfile = createOrLoadRegionFile(worldDir, chunkX, chunkZ);
		return regionfile.getChunkDataOutputStream(chunkX & 31, chunkZ & 31);
	}

	public static synchronized void getChunkOutputStream(File worldDir, int chunkX, int chunkZ, NBTTagCompound nbttagcompound) throws IOException
	{
		RegionFile regionfile = createOrLoadRegionFile(worldDir, chunkX, chunkZ);
		DataOutputStream dataoutputstream = regionfile.getChunkDataOutputStream(chunkX & 31, chunkZ & 31);
		CompressedStreamTools.write(nbttagcompound, dataoutputstream);
		dataoutputstream.close();
	}

	public static boolean chunkExists(File worldDir, int chunkX, int chunkZ)
	{
		RegionFile regionfile = getRegionFileIfExists(worldDir, chunkX, chunkZ);
		return regionfile != null ? regionfile.isChunkSaved(chunkX & 31, chunkZ & 31) : false;
	}
}