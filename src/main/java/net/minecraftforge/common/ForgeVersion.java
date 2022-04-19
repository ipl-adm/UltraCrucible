/**
 * This software is provided under the terms of the Minecraft Forge Public
 * License v1.0.
 */

package net.minecraftforge.common;
import static net.minecraftforge.common.ForgeVersion.Status.*;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;

import cpw.mods.fml.common.versioning.ArtifactVersion;
import cpw.mods.fml.common.versioning.DefaultArtifactVersion;
import io.github.crucible.Crucible;

public class ForgeVersion
{
    //This number is incremented every time we remove deprecated code/major API changes, never reset
    public static final int majorVersion    = 10;
    //This number is incremented every minecraft release, never reset
    public static final int minorVersion    = 13;
    //This number is incremented every time a interface changes or new major feature is added, and reset every Minecraft version
    public static final int revisionVersion = 4;
    //This number is incremented every time Jenkins builds Forge, and never reset. Should always be 0 in the repo code.
    public static final int buildVersion    = Crucible.FORGE_BUILD_VERSION;

    private static Status status = PENDING;
    private static String target = null;

    public static int getMajorVersion()
    {
        return majorVersion;
    }

    public static int getMinorVersion()
    {
        return minorVersion;
    }

    public static int getRevisionVersion()
    {
        return revisionVersion;
    }

    public static int getBuildVersion()
    {
        return buildVersion;
    }

    public static Status getStatus()
    {
        return status;
    }

    public static String getTarget()
    {
        return target;
    }

    public static String getVersion()
    {
        return String.format("%d.%d.%d.%d", majorVersion, minorVersion, revisionVersion, buildVersion);
    }

    public static enum Status
    {
        PENDING,
        FAILED,
        UP_TO_DATE,
        OUTDATED,
        AHEAD,
        BETA,
        BETA_OUTDATED
    }

    public static void startVersionCheck()
    {
        status = UP_TO_DATE; // Crucible - Forge changed how the version endpoint works, and there's no real need for checking the version, maybe reuse this for crucible's own version check.
    }
}