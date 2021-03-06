package net.minecraft.block;

import java.util.Random;

import io.github.crucible.CrucibleConfigs;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

// CraftBukkit start
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockFromToEvent;
// CraftBukkit end

public class BlockDynamicLiquid extends BlockLiquid
{
    int field_149815_a;
    boolean[] field_149814_b = new boolean[4];
    int[] field_149816_M = new int[4];
    private static final String __OBFID = "CL_00000234";

    protected BlockDynamicLiquid(Material p_i45403_1_)
    {
        super(p_i45403_1_);
    }

    private void func_149811_n(World p_149811_1_, int p_149811_2_, int p_149811_3_, int p_149811_4_)
    {
        int l = p_149811_1_.getBlockMetadata(p_149811_2_, p_149811_3_, p_149811_4_);
        p_149811_1_.setBlock(p_149811_2_, p_149811_3_, p_149811_4_, Block.getBlockById(Block.getIdFromBlock(this) + 1), l, 2);
    }

    public void updateTick(World p_149674_1_, int p_149674_2_, int p_149674_3_, int p_149674_4_, Random p_149674_5_)
    {
        int l = this.func_149804_e(p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_);
        byte b0 = 1;

        if (this.blockMaterial == Material.lava && !p_149674_1_.provider.isHellWorld)
        {
            b0 = 2;
        }

        boolean flag = true;
        int i1 = this.tickRate(p_149674_1_);
        int j1;

        // Cauldron - move CB edit to after variable initialization for coremod compatibility
        // CraftBukkit start
        org.bukkit.World bworld = p_149674_1_.getWorld();
        org.bukkit.Server server = p_149674_1_.getServer();
        org.bukkit.block.Block source = bworld == null ? null : bworld.getBlockAt(p_149674_2_, p_149674_3_, p_149674_4_);
        // CraftBukkit end
        if (l > 0)
        {
            byte b1 = -100;
            this.field_149815_a = 0;
            int l1 = this.func_149810_a(p_149674_1_, p_149674_2_ - 1, p_149674_3_, p_149674_4_, b1);
            l1 = this.func_149810_a(p_149674_1_, p_149674_2_ + 1, p_149674_3_, p_149674_4_, l1);
            l1 = this.func_149810_a(p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_ - 1, l1);
            l1 = this.func_149810_a(p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_ + 1, l1);
            j1 = l1 + b0;

            if (j1 >= 8 || l1 < 0)
            {
                j1 = -1;
            }

            if (this.func_149804_e(p_149674_1_, p_149674_2_, p_149674_3_ + 1, p_149674_4_) >= 0)
            {
                int k1 = this.func_149804_e(p_149674_1_, p_149674_2_, p_149674_3_ + 1, p_149674_4_);

                if (k1 >= 8)
                {
                    j1 = k1;
                }
                else
                {
                    j1 = k1 + 8;
                }
            }

            // Cauldron start - allow disabling infinite water sources
            if(CrucibleConfigs.configs.cauldron_settings_infiniteWaterSource)
            {
                if (this.field_149815_a >= 2 && this.blockMaterial == Material.water)
                {
                    if (p_149674_1_.getBlock(p_149674_2_, p_149674_3_ - 1, p_149674_4_).getMaterial().isSolid())
                    {
                        j1 = 0;
                    }
                    else if (p_149674_1_.getBlock(p_149674_2_, p_149674_3_ - 1, p_149674_4_).getMaterial() == this.blockMaterial && p_149674_1_.getBlockMetadata(p_149674_2_, p_149674_3_ - 1, p_149674_4_) == 0)
                    {
                        j1 = 0;
                    }
                }
            }
            // Cauldron end

            if (this.blockMaterial == Material.lava && l < 8 && j1 < 8 && j1 > l && p_149674_5_.nextInt(4) != 0)
            {
                i1 *= 4;
            }

            if (j1 == l)
            {
                if (flag)
                {
                    this.func_149811_n(p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_);
                }
                // Cauldron start - allow lava decaying at a 'natural' rate - see https://mojang.atlassian.net/browse/MC-4631 Lava decay fails to schedule block update
                else if (CrucibleConfigs.configs.cauldron_settings_flowingLavaDecay)
                {
                    // Ensure that even if the flow decay was skipped, it will retry at the material's natural flow period.
                    p_149674_1_.scheduleBlockUpdate(p_149674_2_, p_149674_3_, p_149674_4_, this, this.tickRate(p_149674_1_));
                }
                // Cauldron end
            }
            else
            {
                l = j1;

                if (j1 < 0)
                {
                    p_149674_1_.setBlockToAir(p_149674_2_, p_149674_3_, p_149674_4_);
                }
                else
                {
                    p_149674_1_.setBlockMetadataWithNotify(p_149674_2_, p_149674_3_, p_149674_4_, j1, 2);
                    p_149674_1_.scheduleBlockUpdate(p_149674_2_, p_149674_3_, p_149674_4_, this, i1);
                    
                    // Thermos follow PaperSpigot style and do this right here
                    p_149674_1_.notifyBlockOfNeighborChange(p_149674_2_ - 1, p_149674_3_, p_149674_4_, this);
                    p_149674_1_.notifyBlockOfNeighborChange(p_149674_2_ + 1, p_149674_3_, p_149674_4_, this);
                    
                    // DON'T UPDATE THIS ONE PAPER SPIGOT SAYS DON'T DO IT
                    //p_149674_1_.notifyBlockOfNeighborChange(p_149674_2_, p_149674_3_ - 1, p_149674_4_, this);
                    
                    p_149674_1_.notifyBlockOfNeighborChange(p_149674_2_, p_149674_3_ + 1, p_149674_4_, this);
                    p_149674_1_.notifyBlockOfNeighborChange(p_149674_2_, p_149674_3_, p_149674_4_ - 1, this);
                    p_149674_1_.notifyBlockOfNeighborChange(p_149674_2_, p_149674_3_, p_149674_4_ + 1, this);
                    // p_149674_1_.notifyBlocksOfNeighborChange(p_149674_2_, p_149674_3_, p_149674_4_, this);
                }
            }
        }
        else
        {
            this.func_149811_n(p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_);
        }

        if (this.func_149809_q(p_149674_1_, p_149674_2_, p_149674_3_ - 1, p_149674_4_))
        {
            if (p_149674_1_.getType(p_149674_2_, p_149674_3_, p_149674_4_).getMaterial() != this.blockMaterial) { return; } // PaperSpigot - Stop updating flowing block if material has changed

            // CraftBukkit start - Send "down" to the server
            BlockFromToEvent event = new BlockFromToEvent(source, BlockFace.DOWN);

            if (server != null && source != null)
            {
                server.getPluginManager().callEvent(event);
            }

            if (!event.isCancelled())
            {
                if (this.blockMaterial == Material.lava && p_149674_1_.getBlock(p_149674_2_, p_149674_3_ - 1, p_149674_4_).getMaterial() == Material.water)
                {
                    p_149674_1_.setBlock(p_149674_2_, p_149674_3_ - 1, p_149674_4_, Blocks.stone);
                    this.func_149799_m(p_149674_1_, p_149674_2_, p_149674_3_ - 1, p_149674_4_);
                    return;
                }

                if (l >= 8)
                {
                    this.func_149813_h(p_149674_1_, p_149674_2_, p_149674_3_ - 1, p_149674_4_, l);
                }
                else
                {
                    this.func_149813_h(p_149674_1_, p_149674_2_, p_149674_3_ - 1, p_149674_4_, l + 8);
                }
            }

            // CraftBukkit end
        }
        else if (l >= 0 && (l == 0 || this.func_149807_p(p_149674_1_, p_149674_2_, p_149674_3_ - 1, p_149674_4_)))
        {
            boolean[] aboolean = this.func_149808_o(p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_);
            j1 = l + b0;

            if (l >= 8)
            {
                j1 = 1;
            }

            if (j1 >= 8)
            {
                return;
            }

            // CraftBukkit start - All four cardinal directions. Do not change the order!
            BlockFace[] faces = new BlockFace[] { BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH };
            int index = 0;

            for (BlockFace currentFace : faces)
            {
                if (aboolean[index])
                {
                    BlockFromToEvent event = new BlockFromToEvent(source, currentFace);

                    if (server != null && source != null)
                    {
                        server.getPluginManager().callEvent(event);
                    }

                    if (!event.isCancelled())
                    {
                        this.func_149813_h(p_149674_1_, p_149674_2_ + currentFace.getModX(), p_149674_3_, p_149674_4_ + currentFace.getModZ(), j1);
                    }
                }

                index++;
            }

            // CraftBukkit end
        }
    }

    private void func_149813_h(World p_149813_1_, int p_149813_2_, int p_149813_3_, int p_149813_4_, int p_149813_5_)
    {
        if (this.func_149809_q(p_149813_1_, p_149813_2_, p_149813_3_, p_149813_4_))
        {
            Block block = p_149813_1_.getBlock(p_149813_2_, p_149813_3_, p_149813_4_);

            if (this.blockMaterial == Material.lava)
            {
                this.func_149799_m(p_149813_1_, p_149813_2_, p_149813_3_, p_149813_4_);
            }
            else
            {
                block.dropBlockAsItem(p_149813_1_, p_149813_2_, p_149813_3_, p_149813_4_, p_149813_1_.getBlockMetadata(p_149813_2_, p_149813_3_, p_149813_4_), 0);
            }

            p_149813_1_.setBlock(p_149813_2_, p_149813_3_, p_149813_4_, this, p_149813_5_, 3);
        }
    }

    private int func_149812_c(World p_149812_1_, int p_149812_2_, int p_149812_3_, int p_149812_4_, int p_149812_5_, int p_149812_6_)
    {
        int j1 = 1000;

        for (int k1 = 0; k1 < 4; ++k1)
        {
            if ((k1 != 0 || p_149812_6_ != 1) && (k1 != 1 || p_149812_6_ != 0) && (k1 != 2 || p_149812_6_ != 3) && (k1 != 3 || p_149812_6_ != 2))
            {
                int l1 = p_149812_2_;
                int i2 = p_149812_4_;

                if (k1 == 0)
                {
                    l1 = p_149812_2_ - 1;
                }

                if (k1 == 1)
                {
                    ++l1;
                }

                if (k1 == 2)
                {
                    i2 = p_149812_4_ - 1;
                }

                if (k1 == 3)
                {
                    ++i2;
                }

                if (!this.func_149807_p(p_149812_1_, l1, p_149812_3_, i2) && (p_149812_1_.getBlock(l1, p_149812_3_, i2).getMaterial() != this.blockMaterial || p_149812_1_.getBlockMetadata(l1, p_149812_3_, i2) != 0))
                {
                    if (!this.func_149807_p(p_149812_1_, l1, p_149812_3_ - 1, i2))
                    {
                        return p_149812_5_;
                    }

                    if (p_149812_5_ < 4)
                    {
                        int j2 = this.func_149812_c(p_149812_1_, l1, p_149812_3_, i2, p_149812_5_ + 1, k1);

                        if (j2 < j1)
                        {
                            j1 = j2;
                        }
                    }
                }
            }
        }

        return j1;
    }

    private boolean[] func_149808_o(World p_149808_1_, int p_149808_2_, int p_149808_3_, int p_149808_4_)
    {
        int l;
        int i1;

        for (l = 0; l < 4; ++l)
        {
            this.field_149816_M[l] = 1000;
            i1 = p_149808_2_;
            int j1 = p_149808_4_;

            if (l == 0)
            {
                i1 = p_149808_2_ - 1;
            }

            if (l == 1)
            {
                ++i1;
            }

            if (l == 2)
            {
                j1 = p_149808_4_ - 1;
            }

            if (l == 3)
            {
                ++j1;
            }

            if (!this.func_149807_p(p_149808_1_, i1, p_149808_3_, j1) && (p_149808_1_.getBlock(i1, p_149808_3_, j1).getMaterial() != this.blockMaterial || p_149808_1_.getBlockMetadata(i1, p_149808_3_, j1) != 0))
            {
                if (this.func_149807_p(p_149808_1_, i1, p_149808_3_ - 1, j1))
                {
                    this.field_149816_M[l] = this.func_149812_c(p_149808_1_, i1, p_149808_3_, j1, 1, l);
                }
                else
                {
                    this.field_149816_M[l] = 0;
                }
            }
        }

        l = this.field_149816_M[0];

        for (i1 = 1; i1 < 4; ++i1)
        {
            if (this.field_149816_M[i1] < l)
            {
                l = this.field_149816_M[i1];
            }
        }

        for (i1 = 0; i1 < 4; ++i1)
        {
            this.field_149814_b[i1] = this.field_149816_M[i1] == l;
        }

        return this.field_149814_b;
    }

    private boolean func_149807_p(World p_149807_1_, int p_149807_2_, int p_149807_3_, int p_149807_4_)
    {
        Block block = p_149807_1_.getBlock(p_149807_2_, p_149807_3_, p_149807_4_);
        return block != Blocks.wooden_door && block != Blocks.iron_door && block != Blocks.standing_sign && block != Blocks.ladder && block != Blocks.reeds ? (block.blockMaterial == Material.portal ? true : block.blockMaterial.blocksMovement()) : true;
    }

    protected int func_149810_a(World p_149810_1_, int p_149810_2_, int p_149810_3_, int p_149810_4_, int p_149810_5_)
    {
        int i1 = this.func_149804_e(p_149810_1_, p_149810_2_, p_149810_3_, p_149810_4_);

        if (i1 < 0)
        {
            return p_149810_5_;
        }
        else
        {
            if (i1 == 0)
            {
                ++this.field_149815_a;
            }

            if (i1 >= 8)
            {
                i1 = 0;
            }

            return p_149810_5_ >= 0 && i1 >= p_149810_5_ ? p_149810_5_ : i1;
        }
    }

    private boolean func_149809_q(World p_149809_1_, int p_149809_2_, int p_149809_3_, int p_149809_4_)
    {
        Material material = p_149809_1_.getBlock(p_149809_2_, p_149809_3_, p_149809_4_).getMaterial();
        return material == this.blockMaterial ? false : (material == Material.lava ? false : !this.func_149807_p(p_149809_1_, p_149809_2_, p_149809_3_, p_149809_4_));
    }

    public void onBlockAdded(World p_149726_1_, int p_149726_2_, int p_149726_3_, int p_149726_4_)
    {
        super.onBlockAdded(p_149726_1_, p_149726_2_, p_149726_3_, p_149726_4_);

        if (p_149726_1_.getBlock(p_149726_2_, p_149726_3_, p_149726_4_) == this)
        {
            p_149726_1_.scheduleBlockUpdate(p_149726_2_, p_149726_3_, p_149726_4_, this, this.tickRate(p_149726_1_));
        }
    }

    public boolean func_149698_L()
    {
        return true;
    }
}