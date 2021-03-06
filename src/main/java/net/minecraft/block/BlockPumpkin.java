package net.minecraft.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

// CraftBukkit start
import org.bukkit.craftbukkit.util.BlockStateListPopulator;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
// CraftBukkit end

public class BlockPumpkin extends BlockDirectional
{
    private boolean field_149985_a;
    @SideOnly(Side.CLIENT)
    private IIcon field_149984_b;
    @SideOnly(Side.CLIENT)
    private IIcon field_149986_M;
    private static final String __OBFID = "CL_00000291";

    protected BlockPumpkin(boolean p_i45419_1_)
    {
        super(Material.gourd);
        this.setTickRandomly(true);
        this.field_149985_a = p_i45419_1_;
        this.setCreativeTab(CreativeTabs.tabBlock);
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int p_149691_1_, int p_149691_2_)
    {
        return p_149691_1_ == 1 ? this.field_149984_b : (p_149691_1_ == 0 ? this.field_149984_b : (p_149691_2_ == 2 && p_149691_1_ == 2 ? this.field_149986_M : (p_149691_2_ == 3 && p_149691_1_ == 5 ? this.field_149986_M : (p_149691_2_ == 0 && p_149691_1_ == 3 ? this.field_149986_M : (p_149691_2_ == 1 && p_149691_1_ == 4 ? this.field_149986_M : this.blockIcon)))));
    }

    public void onBlockAdded(World p_149726_1_, int p_149726_2_, int p_149726_3_, int p_149726_4_)
    {
        super.onBlockAdded(p_149726_1_, p_149726_2_, p_149726_3_, p_149726_4_);

        // CHECK FOR SNOWMEN AHH
        if (p_149726_1_.getBlock(p_149726_2_, p_149726_3_ - 1, p_149726_4_) == Blocks.snow && p_149726_1_.getBlock(p_149726_2_, p_149726_3_ - 2, p_149726_4_) == Blocks.snow)
        {
            if (!p_149726_1_.isRemote)
            {
                // CraftBukkit start - Use BlockStateListPopulator
                BlockStateListPopulator blockList = new BlockStateListPopulator(p_149726_1_.getWorld());
                blockList.setTypeId(p_149726_2_, p_149726_3_, p_149726_4_, 0);
                blockList.setTypeId(p_149726_2_, p_149726_3_ - 1, p_149726_4_, 0);
                blockList.setTypeId(p_149726_2_, p_149726_3_ - 2, p_149726_4_, 0);
                EntitySnowman entitysnowman = new EntitySnowman(p_149726_1_);
                entitysnowman.setLocationAndAngles((double) p_149726_2_ + 0.5D, (double) p_149726_3_ - 1.95D, (double) p_149726_4_ + 0.5D, 0.0F, 0.0F);

                if (p_149726_1_.addEntity(entitysnowman, SpawnReason.BUILD_SNOWMAN))
                {
                    blockList.updateList();
                    // Check if everything is still there for dat snowman
                    if (p_149726_1_.getBlock(p_149726_2_, p_149726_3_ - 1, p_149726_4_) == Blocks.snow && p_149726_1_.getBlock(p_149726_2_, p_149726_3_ - 2, p_149726_4_) == Blocks.snow)
                    {
                    	entitysnowman.isDead = true;
                    	p_149726_1_.removeEntity(entitysnowman);
                    }
                }
                
            }
            
            for (int i1 = 0; i1 < 120; ++i1)
            {
                p_149726_1_.spawnParticle("snowshovel", (double)p_149726_2_ + p_149726_1_.rand.nextDouble(), (double)(p_149726_3_ - 2) + p_149726_1_.rand.nextDouble() * 2.5D, (double)p_149726_4_ + p_149726_1_.rand.nextDouble(), 0.0D, 0.0D, 0.0D);
            }
        }
        // Check below middle pumpkin for 2 iron blocks
        else if (p_149726_1_.getBlock(p_149726_2_, p_149726_3_ - 1, p_149726_4_) == Blocks.iron_block && p_149726_1_.getBlock(p_149726_2_, p_149726_3_ - 2, p_149726_4_) == Blocks.iron_block)
        {
        	//Check in the X direction for iron block left and right 1 block below pumpkin
            boolean flag = p_149726_1_.getBlock(p_149726_2_ - 1, p_149726_3_ - 1, p_149726_4_) == Blocks.iron_block && p_149726_1_.getBlock(p_149726_2_ + 1, p_149726_3_ - 1, p_149726_4_) == Blocks.iron_block;
            //Check in the Z direction for iron block left and right 1 block below pumpkin
            boolean flag1 = p_149726_1_.getBlock(p_149726_2_, p_149726_3_ - 1, p_149726_4_ - 1) == Blocks.iron_block && p_149726_1_.getBlock(p_149726_2_, p_149726_3_ - 1, p_149726_4_ + 1) == Blocks.iron_block;

            if (flag || flag1)
            {
                // CraftBukkit start - Use BlockStateListPopulator
                BlockStateListPopulator blockList = new BlockStateListPopulator(p_149726_1_.getWorld());
                blockList.setTypeId(p_149726_2_, p_149726_3_, p_149726_4_, 0);
                blockList.setTypeId(p_149726_2_, p_149726_3_ - 1, p_149726_4_, 0);
                blockList.setTypeId(p_149726_2_, p_149726_3_ - 2, p_149726_4_, 0);

                // If the iron blocks were in the X direction
                if (flag)
                {
                    blockList.setTypeId(p_149726_2_ - 1, p_149726_3_ - 1, p_149726_4_, 0);
                    blockList.setTypeId(p_149726_2_ + 1, p_149726_3_ - 1, p_149726_4_, 0);
                }
                // If the iron blocks were in the Z direction
                else
                {
                    blockList.setTypeId(p_149726_2_, p_149726_3_ - 1, p_149726_4_ - 1, 0);
                    blockList.setTypeId(p_149726_2_, p_149726_3_ - 1, p_149726_4_ + 1, 0);
                }

                EntityIronGolem entityirongolem = new EntityIronGolem(p_149726_1_);
                entityirongolem.setPlayerCreated(true);
                entityirongolem.setLocationAndAngles((double) p_149726_2_ + 0.5D, (double) p_149726_3_ - 1.95D, (double) p_149726_4_ + 0.5D, 0.0F, 0.0F);

                if (p_149726_1_.addEntity(entityirongolem, SpawnReason.BUILD_IRONGOLEM))
                {
                    for (int i1 = 0; i1 < 120; ++i1)
                    {
                        p_149726_1_.spawnParticle("snowballpoof", (double) p_149726_2_ + p_149726_1_.rand.nextDouble(), (double)(p_149726_3_ - 2) + p_149726_1_.rand.nextDouble() * 3.9D, (double) p_149726_4_ + p_149726_1_.rand.nextDouble(), 0.0D, 0.0D, 0.0D);
                    }

                    blockList.updateList();
                    //Recheck if the stuff is still there and the iron golem failed to delete its blocks - then delete the golem quickly
                    if (p_149726_1_.getBlock(p_149726_2_, p_149726_3_ - 1, p_149726_4_) == Blocks.iron_block && p_149726_1_.getBlock(p_149726_2_, p_149726_3_ - 2, p_149726_4_) == Blocks.iron_block)
                    {
                    	//Check in the X direction for iron block left and right 1 block below pumpkin
                        boolean xdir = p_149726_1_.getBlock(p_149726_2_ - 1, p_149726_3_ - 1, p_149726_4_) == Blocks.iron_block && p_149726_1_.getBlock(p_149726_2_ + 1, p_149726_3_ - 1, p_149726_4_) == Blocks.iron_block;
                        //Check in the Z direction for iron block left and right 1 block below pumpkin
                        boolean zdir = p_149726_1_.getBlock(p_149726_2_, p_149726_3_ - 1, p_149726_4_ - 1) == Blocks.iron_block && p_149726_1_.getBlock(p_149726_2_, p_149726_3_ - 1, p_149726_4_ + 1) == Blocks.iron_block;
                        if (xdir || zdir)
                        {
                        	entityirongolem.isDead = true;
                        	p_149726_1_.removeEntity(entityirongolem);

                        }
                    }
                }

                // CraftBukkit end
            }
        }
    }

    public boolean canPlaceBlockAt(World p_149742_1_, int p_149742_2_, int p_149742_3_, int p_149742_4_)
    {
        return  p_149742_1_.getBlock(p_149742_2_, p_149742_3_, p_149742_4_).isReplaceable(p_149742_1_, p_149742_2_, p_149742_3_, p_149742_4_) && World.doesBlockHaveSolidTopSurface(p_149742_1_, p_149742_2_, p_149742_3_ - 1, p_149742_4_);
    }

    public void onBlockPlacedBy(World p_149689_1_, int p_149689_2_, int p_149689_3_, int p_149689_4_, EntityLivingBase p_149689_5_, ItemStack p_149689_6_)
    {
        int l = MathHelper.floor_double((double)(p_149689_5_.rotationYaw * 4.0F / 360.0F) + 2.5D) & 3;
        p_149689_1_.setBlockMetadataWithNotify(p_149689_2_, p_149689_3_, p_149689_4_, l, 2);
    }

    // CraftBukkit start
    public void onNeighborBlockChange(World world, int i, int j, int k, Block block)
    {
        if (block != null && block.canProvidePower())
        {
            org.bukkit.block.Block bukkitBlock = world.getWorld().getBlockAt(i, j, k);
            int power = bukkitBlock.getBlockPower();
            BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(bukkitBlock, power, power);
            world.getServer().getPluginManager().callEvent(eventRedstone);
        }
    }
    // CraftBukkit end

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister p_149651_1_)
    {
        this.field_149986_M = p_149651_1_.registerIcon(this.getTextureName() + "_face_" + (this.field_149985_a ? "on" : "off"));
        this.field_149984_b = p_149651_1_.registerIcon(this.getTextureName() + "_top");
        this.blockIcon = p_149651_1_.registerIcon(this.getTextureName() + "_side");
    }
}