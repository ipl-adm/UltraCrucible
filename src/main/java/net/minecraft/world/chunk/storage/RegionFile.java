package net.minecraft.world.chunk.storage;

import cpw.mods.fml.common.FMLLog;
import io.github.crucible.CrucibleConfigs;
import io.github.crucible.CrucibleModContainer;
import net.minecraft.server.MinecraftServer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class RegionFile {
    // Minecraft is limited to 256 sections per chunk. So 1MB. This can easily be override.
    // So we extend this to use the REAL size when the count is maxed by seeking to that section and reading the length.
    private static final boolean FORGE_ENABLE_EXTENDED_SAVE = CrucibleConfigs.configs.crucible_enableOversizedChunk;
    private static final long SECTOR_LENGTH = 4096L;
    private static final byte[] EMPTY_SECTOR = new byte[(int) SECTOR_LENGTH];
    private final File fileName;
    private final int[] offsets = new int[1024];
    private final int[] chunkTimestamps = new int[1024];
    private List<Boolean> sectorFree;
    private int sizeDelta;
    private long lastModified;
    private static final String __OBFID = "CL_00000381";

    private RandomAccessFile dataFile = null;

    public RegionFile(File fileNameIn) {
        this.fileName = fileNameIn;
        this.sizeDelta = 0;

        try {
            if (fileNameIn.exists()) {
                this.lastModified = fileNameIn.lastModified();
            }

            RandomAccessFile dataFile = new RandomAccessFile(fileNameIn, "rw");

            this.dataFile = dataFile;
            //int i;
            if (this.dataFile.length() < SECTOR_LENGTH) {
                // Spigot - more efficient chunk zero'ing
                this.dataFile.write(RegionFile.EMPTY_SECTOR); // Spigot // Crucible - info:this sector is the chunk offset table
                this.dataFile.write(RegionFile.EMPTY_SECTOR); // Spigot // Crucible - info:this sector is the timestamp info

                this.sizeDelta += SECTOR_LENGTH * 2;
            }

            if ((this.dataFile.length() & 4095L) != 0L) {
                for (int i = 0; (long) i < (this.dataFile.length() & 4095L); ++i) {
                    this.dataFile.write(0);
                }
            }

            int freeSectors = (int) this.dataFile.length() / 4096;
            this.sectorFree = new ArrayList<>(freeSectors);
            //int j;

            for (int i = 0; i < freeSectors; ++i) {
                this.sectorFree.add(true);
            }

            //Sections already used by the offset table and timestamp
            this.sectorFree.set(0, false);
            this.sectorFree.set(1, false);

            this.dataFile.seek(0L);
            for (int i = 0; i < 1024; ++i) {

                int offset = this.dataFile.readInt();
                this.offsets[i] = offset;
                // Spigot start
                int length = offset & 255;
                if (length == 255) {

                    // We're maxed out, so we need to read the proper length from the section
                    if ((offset >> 8) <= this.sectorFree.size()) {
                        this.dataFile.seek((offset >> 8) * 4096L);
                        length = (this.dataFile.readInt() + 4) / 4096 + 1;
                        this.dataFile.seek(i * 4 + 4); //Go back to where we were
                    }
                }
                if (offset != 0 && (offset >> 8) + length <= this.sectorFree.size()) {
                    for (int l = 0; l < length; ++l) {
                        // Spigot end
                        this.sectorFree.set((offset >> 8) + l, false);
                    }
                } else if (length > 0)
                    FMLLog.warning("Invalid chunk: (%s, %s) Offset: %s Length: %s runs off end file. %s", i % 32, i / 32, offset >> 8, length, fileNameIn);
            }
            for (int i = 0; i < 1024; ++i) {
                int timestamp = this.dataFile.readInt();
                this.chunkTimestamps[i] = timestamp;
            }


        } catch (IOException ioexception) {
            ioexception.printStackTrace();
        }
    }

    // This is a copy (sort of) of the method below it, make sure they stay in sync
    public synchronized boolean chunkExists(int x, int z) {
        return isChunkSaved(x, z);
    }

    public synchronized DataInputStream getChunkDataInputStream(int x, int z) {
        if (this.outOfBounds(x, z)) {
            return null;
        } else {
            try {
                int offset = this.getOffset(x, z);

                if (offset == 0) {
                    return null;
                } else {
                    int sector = offset >> 8;
                    int sectorCount = offset & 255;
                    // Spigot start
                    if (sectorCount == 255) {
                        this.dataFile.seek(sector * SECTOR_LENGTH);
                        sectorCount = (this.dataFile.readInt() + 4) / 4096 + 1;
                    } else {
					}
                    // Spigot end
                    if (sector + sectorCount > this.sectorFree.size()) {
                        return null;
                    } else {
                        this.dataFile.seek(sector * 4096L);
                        int length = this.dataFile.readInt();

                        if (length > 4096 * sectorCount) {
                            return null;
                        } else if (length <= 0) {
                            return null;
                        } else {
                            byte compressionType = this.dataFile.readByte();
                            byte[] compressedData;

                            if (compressionType == 1) {
                                compressedData = new byte[length - 1];
                                this.dataFile.read(compressedData);
                                return new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(compressedData))));
                            } else if (compressionType == 2) {
                                compressedData = new byte[length - 1];
                                this.dataFile.read(compressedData);
                                return new DataInputStream(new BufferedInputStream(new InflaterInputStream(new ByteArrayInputStream(compressedData))));
                            } else {
                                return null;
                            }
                        }
                    }
                }
            } catch (IOException ioexception) {
            	ioexception.printStackTrace();
                return null;
            }
        }
    }

    public DataOutputStream getChunkDataOutputStream(int x, int z) {
        return this.outOfBounds(x, z) ? null : new DataOutputStream(new java.io.BufferedOutputStream(new DeflaterOutputStream(new RegionFile.ChunkBuffer(x, z)))); // Spigot - use a BufferedOutputStream to greatly improve file write performance
    }

    protected synchronized void write(int x, int z, byte[] data, int length) {
        try {
            int offset = this.getOffset(x, z);
            int sector = offset >> 8;
            int sectorCount = offset & 255;
            // Spigot start
            if (sectorCount == 255) {
                this.dataFile.seek(sector * SECTOR_LENGTH);
                sectorCount = (this.dataFile.readInt() + 4) / 4096 + 1;
            }
            // Spigot end
            int sectorsNeeded = (length + 5) / 4096 + 1;

            if (sectorsNeeded >= 256) { //crucible - info: chunk has a limit of 255 sectors
                CrucibleModContainer.logger.warn("[Crucible] Oversized Chunk at ({}, {})", x, z);
                if (!FORGE_ENABLE_EXTENDED_SAVE) {
                    return;
                }
            }

            if (sector != 0 && sectorCount == sectorsNeeded) {
            	//crucible - info: this part just overwrite the current old sectors.
                this.write(sector, data, length);
            } else {

                for (int i = 0; i < sectorCount; ++i) {
                    this.sectorFree.set(sector + i, true);
                }

                int sectorStart = this.sectorFree.indexOf(true);
                int sectorLength = 0;

				//crucible - info: search for an area with enough free space
                if (sectorStart != -1) {
                    for (int i = sectorStart; i < this.sectorFree.size(); ++i) {
                        if (sectorLength != 0) {
                            if (this.sectorFree.get(i)) {
                                ++sectorLength;
                            } else {
                                sectorLength = 0;
                            }
                        } else if (this.sectorFree.get(i)) {
                            sectorStart = i;
                            sectorLength = 1;
                        }

                        if (sectorLength >= sectorsNeeded) {
                            break;
                        }
                    }
                }

                if (sectorLength >= sectorsNeeded) {
					//crucible - info: space found.
                    sector = sectorStart;
                    this.setOffset(x, z, sector << 8 | (sectorsNeeded > 255 ? 255 : sectorsNeeded)); // Spigot

                    for (int i = 0; i < sectorsNeeded; ++i) {
                        this.sectorFree.set(sector + i, false);
                    }

                    this.write(sector, data, length);
                } else {
					//crucible - info: space nof found, grow the file.
                    this.dataFile.seek(this.dataFile.length());
                    sector = this.sectorFree.size();

                    for (int i = 0; i < sectorsNeeded; ++i) {
                        this.dataFile.write(EMPTY_SECTOR);
                        this.sectorFree.add(false);
                    }

                    this.sizeDelta += 4096 * sectorsNeeded;
                    this.write(sector, data, length);
                    this.setOffset(x, z, sector << 8 | (sectorsNeeded > 255 ? 255 : sectorsNeeded)); // Spigot
                }
            }

            this.setChunkTimestamp(x, z, (int) (MinecraftServer.getSystemTimeMillis() / 1000L));
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
        }
    }

    private void write(int sectorNumber, byte[] data, int length) throws IOException {
        this.dataFile.seek(sectorNumber * SECTOR_LENGTH);
        this.dataFile.writeInt(length + 1);
        this.dataFile.writeByte(2);
        this.dataFile.write(data, 0, length);
    }

    public boolean outOfBounds(int x, int z) {
        return x < 0 || x >= 32 || z < 0 || z >= 32;
    }

    public int getOffset(int x, int z) {
        return this.offsets[x + z * 32];
    }

    public boolean isChunkSaved(int x, int z) {
        return this.getOffset(x, z) != 0;
    }

    private void setOffset(int x, int z, int offset) throws IOException {
        this.offsets[x + z * 32] = offset;
        this.dataFile.seek((x + z * 32) * 4);
        this.dataFile.writeInt(offset);
    }

    private void setChunkTimestamp(int x, int z, int timestamp) throws IOException {
        this.chunkTimestamps[x + z * 32] = timestamp;
        this.dataFile.seek(4096 + (x + z * 32) * 4);
        this.dataFile.writeInt(timestamp);
    }

    public void close() throws IOException {
        if (this.dataFile != null) {
            this.dataFile.close();
        }
    }

    class ChunkBuffer extends ByteArrayOutputStream {
        private final int chunkX;
        private final int chunkZ;
        private static final String __OBFID = "CL_00000382";

        public ChunkBuffer(int x, int z) {
            super(8096);
            this.chunkX = x;
            this.chunkZ = z;
        }

        public void close() throws IOException {
            RegionFile.this.write(this.chunkX, this.chunkZ, this.buf, this.count);
        }
    }
}