package net.minecraft.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.WeakHashMap;

import com.google.common.collect.Maps;
import io.github.crucible.CrucibleConfigs;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraftforge.common.util.ForgeDirection;
import static net.minecraftforge.common.util.ForgeDirection.*;
// CraftBukkit start
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockSpreadEvent;
// CraftBukkit end

public class BlockFire extends Block
{
    @Deprecated
    private int[] field_149849_a = new int[4096];
    @Deprecated
    private int[] field_149848_b = new int[4096];
    @SideOnly(Side.CLIENT)
    private IIcon[] field_149850_M;
    private static final String __OBFID = "CL_00000245";

    protected BlockFire()
    {
        super(Material.fire);
        this.setTickRandomly(true);
    }

    public static void func_149843_e()
    {
        Blocks.fire.func_149842_a(getIdFromBlock(Blocks.planks), 5, 20);
        Blocks.fire.func_149842_a(getIdFromBlock(Blocks.double_wooden_slab), 5, 20);
        Blocks.fire.func_149842_a(getIdFromBlock(Blocks.wooden_slab), 5, 20);
        Blocks.fire.func_149842_a(getIdFromBlock(Blocks.fence), 5, 20);
        Blocks.fire.func_149842_a(getIdFromBlock(Blocks.oak_stairs), 5, 20);
        Blocks.fire.func_149842_a(getIdFromBlock(Blocks.birch_stairs), 5, 20);
        Blocks.fire.func_149842_a(getIdFromBlock(Blocks.spruce_stairs), 5, 20);
        Blocks.fire.func_149842_a(getIdFromBlock(Blocks.jungle_stairs), 5, 20);
        Blocks.fire.func_149842_a(getIdFromBlock(Blocks.log), 5, 5);
        Blocks.fire.func_149842_a(getIdFromBlock(Blocks.log2), 5, 5);
        Blocks.fire.func_149842_a(getIdFromBlock(Blocks.leaves), 30, 60);
        Blocks.fire.func_149842_a(getIdFromBlock(Blocks.leaves2), 30, 60);
        Blocks.fire.func_149842_a(getIdFromBlock(Blocks.bookshelf), 30, 20);
        Blocks.fire.func_149842_a(getIdFromBlock(Blocks.tnt), 15, 100);
        Blocks.fire.func_149842_a(getIdFromBlock(Blocks.tallgrass), 60, 100);
        Blocks.fire.func_149842_a(getIdFromBlock(Blocks.double_plant), 60, 100);
        Blocks.fire.func_149842_a(getIdFromBlock(Blocks.yellow_flower), 60, 100);
        Blocks.fire.func_149842_a(getIdFromBlock(Blocks.red_flower), 60, 100);
        Blocks.fire.func_149842_a(getIdFromBlock(Blocks.wool), 30, 60);
        Blocks.fire.func_149842_a(getIdFromBlock(Blocks.vine), 15, 100);
        Blocks.fire.func_149842_a(getIdFromBlock(Blocks.coal_block), 5, 5);
        Blocks.fire.func_149842_a(getIdFromBlock(Blocks.hay_block), 60, 20);
        Blocks.fire.func_149842_a(getIdFromBlock(Blocks.carpet), 60, 20);
    }

    @Deprecated // Use setFireInfo
    public void func_149842_a(int p_149842_1_, int p_149842_2_, int p_149842_3_)
    {
        this.setFireInfo((Block)Block.blockRegistry.getObjectById(p_149842_1_), p_149842_2_, p_149842_3_);
    }

    public AxisAlignedBB getCollisionBoundingBoxFromPool(World p_149668_1_, int p_149668_2_, int p_149668_3_, int p_149668_4_)
    {
        return null;
    }

    public boolean isOpaqueCube()
    {
        return false;
    }

    public boolean renderAsNormalBlock()
    {
        return false;
    }

    public int getRenderType()
    {
        return 3;
    }

    public int quantityDropped(Random p_149745_1_)
    {
        return 0;
    }

    public int tickRate(World p_149738_1_)
    {
        return 30;
    }

    public void updateTick(World p_149674_1_, int p_149674_2_, int p_149674_3_, int p_149674_4_, Random p_149674_5_)
    {
        if (p_149674_1_.getGameRules().getGameRuleBooleanValue("doFireTick"))
        {
            boolean flag = p_149674_1_.getBlock(p_149674_2_, p_149674_3_ - 1, p_149674_4_).isFireSource(p_149674_1_, p_149674_2_, p_149674_3_ - 1, p_149674_4_, UP);

            if (!this.canPlaceBlockAt(p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_))
            {
                if (!CraftEventFactory.callBlockFadeEvent(p_149674_1_.getWorld().getBlockAt(p_149674_2_, p_149674_3_, p_149674_4_), Blocks.air).isCancelled()) { p_149674_1_.setBlockToAir(p_149674_2_, p_149674_3_, p_149674_4_); } // CraftBukkit - invalid place location
            }

            if (!flag && p_149674_1_.isRaining() && (p_149674_1_.canLightningStrikeAt(p_149674_2_, p_149674_3_, p_149674_4_) || p_149674_1_.canLightningStrikeAt(p_149674_2_ - 1, p_149674_3_, p_149674_4_) || p_149674_1_.canLightningStrikeAt(p_149674_2_ + 1, p_149674_3_, p_149674_4_) || p_149674_1_.canLightningStrikeAt(p_149674_2_, p_149674_3_, p_149674_4_ - 1) || p_149674_1_.canLightningStrikeAt(p_149674_2_, p_149674_3_, p_149674_4_ + 1)))
            {
                if (!CraftEventFactory.callBlockFadeEvent(p_149674_1_.getWorld().getBlockAt(p_149674_2_, p_149674_3_, p_149674_4_), Blocks.air).isCancelled()) { p_149674_1_.setBlockToAir(p_149674_2_, p_149674_3_, p_149674_4_); } // CraftBukkit - extinguished by rain
            }
            else
            {
                int l = p_149674_1_.getBlockMetadata(p_149674_2_, p_149674_3_, p_149674_4_);

                if (l < 15)
                {
                    p_149674_1_.setBlockMetadataWithNotify(p_149674_2_, p_149674_3_, p_149674_4_, l + p_149674_5_.nextInt(3) / 2, 4);
                }

                p_149674_1_.scheduleBlockUpdate(p_149674_2_, p_149674_3_, p_149674_4_, this, this.tickRate(p_149674_1_) + p_149674_5_.nextInt(10));

                if (!flag && !this.canNeighborBurn(p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_))
                {
                    if (!World.doesBlockHaveSolidTopSurface(p_149674_1_, p_149674_2_, p_149674_3_ - 1, p_149674_4_) || l > 3)
                    {
                        if (!CraftEventFactory.callBlockFadeEvent(p_149674_1_.getWorld().getBlockAt(p_149674_2_, p_149674_3_, p_149674_4_), Blocks.air).isCancelled()) { p_149674_1_.setBlockToAir(p_149674_2_, p_149674_3_, p_149674_4_); } // CraftBukkit - burn out of inflammable block
                    }
                }
                else if (!flag && !this.canCatchFire(p_149674_1_, p_149674_2_, p_149674_3_ - 1, p_149674_4_, UP) && l == 15 && p_149674_5_.nextInt(4) == 0)
                {
                    if (!CraftEventFactory.callBlockFadeEvent(p_149674_1_.getWorld().getBlockAt(p_149674_2_, p_149674_3_, p_149674_4_), Blocks.air).isCancelled()) { p_149674_1_.setBlockToAir(p_149674_2_, p_149674_3_, p_149674_4_); } // CraftBukkit - burn out
                }
                else
                {
                    boolean flag1 = p_149674_1_.isBlockHighHumidity(p_149674_2_, p_149674_3_, p_149674_4_);
                    byte b0 = 0;

                    if (flag1)
                    {
                        b0 = -50;
                    }

                    this.tryCatchFire(p_149674_1_, p_149674_2_ + 1, p_149674_3_, p_149674_4_, 300 + b0, p_149674_5_, l, WEST );
                    this.tryCatchFire(p_149674_1_, p_149674_2_ - 1, p_149674_3_, p_149674_4_, 300 + b0, p_149674_5_, l, EAST );
                    this.tryCatchFire(p_149674_1_, p_149674_2_, p_149674_3_ - 1, p_149674_4_, 250 + b0, p_149674_5_, l, UP   );
                    this.tryCatchFire(p_149674_1_, p_149674_2_, p_149674_3_ + 1, p_149674_4_, 250 + b0, p_149674_5_, l, DOWN );
                    this.tryCatchFire(p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_ - 1, 300 + b0, p_149674_5_, l, SOUTH);
                    this.tryCatchFire(p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_ + 1, 300 + b0, p_149674_5_, l, NORTH);

                    for (int i1 = p_149674_2_ - 1; i1 <= p_149674_2_ + 1; ++i1)
                    {
                        for (int j1 = p_149674_4_ - 1; j1 <= p_149674_4_ + 1; ++j1)
                        {
                            for (int k1 = p_149674_3_ - 1; k1 <= p_149674_3_ + 4; ++k1)
                            {
                                if (i1 != p_149674_2_ || k1 != p_149674_3_ || j1 != p_149674_4_)
                                {
                                    int l1 = 100;

                                    if (k1 > p_149674_3_ + 1)
                                    {
                                        l1 += (k1 - (p_149674_3_ + 1)) * 100;
                                    }

                                    int i2 = this.getChanceOfNeighborsEncouragingFire(p_149674_1_, i1, k1, j1);

                                    if (i2 > 0)
                                    {
                                        int j2 = (i2 + 40 + p_149674_1_.difficultySetting.getDifficultyId() * 7) / (l + 30);

                                        if (flag1)
                                        {
                                            j2 /= 2;
                                        }

                                        if (j2 > 0 && p_149674_5_.nextInt(l1) <= j2 && (!p_149674_1_.isRaining() || !p_149674_1_.canLightningStrikeAt(i1, k1, j1)) && !p_149674_1_.canLightningStrikeAt(i1 - 1, k1, p_149674_4_) && !p_149674_1_.canLightningStrikeAt(i1 + 1, k1, j1) && !p_149674_1_.canLightningStrikeAt(i1, k1, j1 - 1) && !p_149674_1_.canLightningStrikeAt(i1, k1, j1 + 1))
                                        {
                                            int k2 = l + p_149674_5_.nextInt(5) / 4;

                                            if (k2 > 15)
                                            {
                                                k2 = 15;
                                            }

                                            // CraftBukkit start - Call to stop spread of fire
                                            if (p_149674_1_.getBlock(i1, k1, j1) != Blocks.fire)
                                            {
                                                if (CraftEventFactory.callBlockIgniteEvent(p_149674_1_, i1, k1, j1, p_149674_2_, p_149674_3_, p_149674_4_).isCancelled())
                                                {
                                                    continue;
                                                }

                                                org.bukkit.Server server = p_149674_1_.getServer();
                                                org.bukkit.World bworld = p_149674_1_.getWorld();
                                                org.bukkit.block.BlockState blockState = bworld.getBlockAt(i1, k1, j1).getState();
                                                blockState.setTypeId(Block.getIdFromBlock(this));
                                                blockState.setData(new org.bukkit.material.MaterialData(Block.getIdFromBlock(this), (byte) k2));
                                                BlockSpreadEvent spreadEvent = new BlockSpreadEvent(blockState.getBlock(), bworld.getBlockAt(p_149674_2_, p_149674_3_, p_149674_4_), blockState);
                                                server.getPluginManager().callEvent(spreadEvent);

                                                if (!spreadEvent.isCancelled())
                                                {
                                                    p_149674_1_.setBlock(i1, k1, j1, ((org.bukkit.craftbukkit.block.CraftBlock)blockState.getBlock()).getNMSBlock(), blockState.getRawData(), 3); // KCauldron - DragonAPI uses this call
                                                    blockState.update(true);
                                                }
                                            }

                                            // CraftBukkit end
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean func_149698_L()
    {
        return false;
    }

    @Deprecated
    private void tryCatchFire(World p_149841_1_, int p_149841_2_, int p_149841_3_, int p_149841_4_, int p_149841_5_, Random p_149841_6_, int p_149841_7_)
    {
        this.tryCatchFire(p_149841_1_, p_149841_2_, p_149841_3_, p_149841_4_, p_149841_5_, p_149841_6_, p_149841_7_, UP);
    }

    private final class pXYZ { 
    	private int x, z;
    	private byte y; 
    	public pXYZ(int x, int y, int z){ this.x = x; this.y = (byte) (y - 128); this.z = z; }
        public int hashCode()
        {
        	return ((y * 31 + x) * 31 + z) * 17 + y;
        }
    }
    private static WeakHashMap<World,HashMap<pXYZ,Long>> failures = new WeakHashMap<World, HashMap<pXYZ,Long>>();
    private void tryCatchFire(World p_149841_1_, int p_149841_2_, int p_149841_3_, int p_149841_4_, int p_149841_5_, Random p_149841_6_, int p_149841_7_, ForgeDirection face)
    {
        int j1 = p_149841_1_.getBlock(p_149841_2_, p_149841_3_, p_149841_4_).getFlammability(p_149841_1_, p_149841_2_, p_149841_3_, p_149841_4_, face);
        
        if (!p_149841_1_.getGameRules().getGameRuleBooleanValue("doFireTick"))
        {
        	return; // Thermos don't let things catch fire if doFireTick is off
        }
        else if (p_149841_6_.nextInt(p_149841_5_) < j1)
        {
        	HashMap<pXYZ,Long> lavie = failures.get(p_149841_1_);
        	if (lavie == null) { lavie = new HashMap<pXYZ,Long>(); failures.put(p_149841_1_, lavie); }
        	pXYZ lapoint = new pXYZ(p_149841_2_, p_149841_3_, p_149841_4_);
        	Long last = lavie.get(lapoint);
        	long now = System.currentTimeMillis();
        	if (last != null && now - last < 1000)
        	{
        		return; //Thermos premature ending
        	}
        	
            boolean flag = p_149841_1_.getBlock(p_149841_2_, p_149841_3_, p_149841_4_) == Blocks.tnt;
            // CraftBukkit start
            org.bukkit.block.Block theBlock = p_149841_1_.getWorld().getBlockAt(p_149841_2_, p_149841_3_, p_149841_4_);
            BlockBurnEvent event = new BlockBurnEvent(theBlock);
            p_149841_1_.getServer().getPluginManager().callEvent(event);

            if (event.isCancelled())
            {
            	lavie.put(lapoint, System.currentTimeMillis());
                return;
            }

            // CraftBukkit end

            if (p_149841_6_.nextInt(p_149841_7_ + 10) < 5 && !p_149841_1_.canLightningStrikeAt(p_149841_2_, p_149841_3_, p_149841_4_))
            {
                int k1 = p_149841_7_ + p_149841_6_.nextInt(5) / 4;

                if (k1 > 15)
                {
                    k1 = 15;
                }

                p_149841_1_.setBlock(p_149841_2_, p_149841_3_, p_149841_4_, this, k1, 3);
            }
            else
            {
                p_149841_1_.setBlockToAir(p_149841_2_, p_149841_3_, p_149841_4_);
            }

            if (flag)
            {
                Blocks.tnt.onBlockDestroyedByPlayer(p_149841_1_, p_149841_2_, p_149841_3_, p_149841_4_, 1);
            }
        }
    }

    private boolean canNeighborBurn(World p_149847_1_, int p_149847_2_, int p_149847_3_, int p_149847_4_)
    {
        return this.canCatchFire(p_149847_1_, p_149847_2_ + 1, p_149847_3_, p_149847_4_, WEST ) ||
               this.canCatchFire(p_149847_1_, p_149847_2_ - 1, p_149847_3_, p_149847_4_, EAST ) ||
               this.canCatchFire(p_149847_1_, p_149847_2_, p_149847_3_ - 1, p_149847_4_, UP   ) ||
               this.canCatchFire(p_149847_1_, p_149847_2_, p_149847_3_ + 1, p_149847_4_, DOWN ) ||
               this.canCatchFire(p_149847_1_, p_149847_2_, p_149847_3_, p_149847_4_ - 1, SOUTH) ||
               this.canCatchFire(p_149847_1_, p_149847_2_, p_149847_3_, p_149847_4_ + 1, NORTH);
    }

    private int getChanceOfNeighborsEncouragingFire(World p_149845_1_, int p_149845_2_, int p_149845_3_, int p_149845_4_)
    {
        byte b0 = 0;

        if (!p_149845_1_.isAirBlock(p_149845_2_, p_149845_3_, p_149845_4_))
        {
            return 0;
        }
        else
        {
            int l = b0;
            l = this.getChanceToEncourageFire(p_149845_1_, p_149845_2_ + 1, p_149845_3_, p_149845_4_, l, WEST );
            l = this.getChanceToEncourageFire(p_149845_1_, p_149845_2_ - 1, p_149845_3_, p_149845_4_, l, EAST );
            l = this.getChanceToEncourageFire(p_149845_1_, p_149845_2_, p_149845_3_ - 1, p_149845_4_, l, UP   );
            l = this.getChanceToEncourageFire(p_149845_1_, p_149845_2_, p_149845_3_ + 1, p_149845_4_, l, DOWN );
            l = this.getChanceToEncourageFire(p_149845_1_, p_149845_2_, p_149845_3_, p_149845_4_ - 1, l, SOUTH);
            l = this.getChanceToEncourageFire(p_149845_1_, p_149845_2_, p_149845_3_, p_149845_4_ + 1, l, NORTH);
            return l;
        }
    }

    public boolean isCollidable()
    {
        return false;
    }

    @Deprecated
    public boolean canBlockCatchFire(IBlockAccess p_149844_1_, int p_149844_2_, int p_149844_3_, int p_149844_4_)
    {
        return canCatchFire(p_149844_1_, p_149844_2_, p_149844_3_, p_149844_4_, UP);
    }

    @Deprecated
    public int func_149846_a(World p_149846_1_, int p_149846_2_, int p_149846_3_, int p_149846_4_, int p_149846_5_)
    {
        return getChanceToEncourageFire(p_149846_1_, p_149846_2_, p_149846_3_, p_149846_4_, p_149846_5_, UP);
    }

    public boolean canPlaceBlockAt(World p_149742_1_, int p_149742_2_, int p_149742_3_, int p_149742_4_)
    {
        return World.doesBlockHaveSolidTopSurface(p_149742_1_, p_149742_2_, p_149742_3_ - 1, p_149742_4_) || this.canNeighborBurn(p_149742_1_, p_149742_2_, p_149742_3_, p_149742_4_);
    }

    public void onNeighborBlockChange(World p_149695_1_, int p_149695_2_, int p_149695_3_, int p_149695_4_, Block p_149695_5_)
    {
        if (!World.doesBlockHaveSolidTopSurface(p_149695_1_, p_149695_2_, p_149695_3_ - 1, p_149695_4_) && !this.canNeighborBurn(p_149695_1_, p_149695_2_, p_149695_3_, p_149695_4_))
        {
            if (!CraftEventFactory.callBlockFadeEvent(p_149695_1_.getWorld().getBlockAt(p_149695_2_, p_149695_3_, p_149695_4_), Blocks.air).isCancelled()) { p_149695_1_.setBlockToAir(p_149695_2_, p_149695_3_, p_149695_4_); } // CraftBukkit - fuel block gone
        }
    }

    public void onBlockAdded(World p_149726_1_, int p_149726_2_, int p_149726_3_, int p_149726_4_)
    {
        if (!CrucibleConfigs.configs.thermos_allowNetherPortal && p_149726_1_.provider.dimensionId > 0 || !Blocks.portal.func_150000_e(p_149726_1_, p_149726_2_, p_149726_3_, p_149726_4_))
        {
            if (!World.doesBlockHaveSolidTopSurface(p_149726_1_, p_149726_2_, p_149726_3_ - 1, p_149726_4_) && !this.canNeighborBurn(p_149726_1_, p_149726_2_, p_149726_3_, p_149726_4_))
            {
                if (!CraftEventFactory.callBlockFadeEvent(p_149726_1_.getWorld().getBlockAt(p_149726_2_, p_149726_3_, p_149726_4_), Blocks.air).isCancelled()) { p_149726_1_.setBlockToAir(p_149726_2_, p_149726_3_, p_149726_4_); } // CraftBukkit - fuel block broke
            }
            else
            {
                p_149726_1_.scheduleBlockUpdate(p_149726_2_, p_149726_3_, p_149726_4_, this, this.tickRate(p_149726_1_) + p_149726_1_.rand.nextInt(10));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World p_149734_1_, int p_149734_2_, int p_149734_3_, int p_149734_4_, Random p_149734_5_)
    {
        if (p_149734_5_.nextInt(24) == 0)
        {
            p_149734_1_.playSound((double)((float)p_149734_2_ + 0.5F), (double)((float)p_149734_3_ + 0.5F), (double)((float)p_149734_4_ + 0.5F), "fire.fire", 1.0F + p_149734_5_.nextFloat(), p_149734_5_.nextFloat() * 0.7F + 0.3F, false);
        }

        int l;
        float f;
        float f1;
        float f2;

        if (!World.doesBlockHaveSolidTopSurface(p_149734_1_, p_149734_2_, p_149734_3_ - 1, p_149734_4_) && !Blocks.fire.canCatchFire(p_149734_1_, p_149734_2_, p_149734_3_ - 1, p_149734_4_, UP))
        {
            if (Blocks.fire.canCatchFire(p_149734_1_, p_149734_2_ - 1, p_149734_3_, p_149734_4_, EAST))
            {
                for (l = 0; l < 2; ++l)
                {
                    f = (float)p_149734_2_ + p_149734_5_.nextFloat() * 0.1F;
                    f1 = (float)p_149734_3_ + p_149734_5_.nextFloat();
                    f2 = (float)p_149734_4_ + p_149734_5_.nextFloat();
                    p_149734_1_.spawnParticle("largesmoke", (double)f, (double)f1, (double)f2, 0.0D, 0.0D, 0.0D);
                }
            }

            if (Blocks.fire.canCatchFire(p_149734_1_, p_149734_2_ + 1, p_149734_3_, p_149734_4_, WEST))
            {
                for (l = 0; l < 2; ++l)
                {
                    f = (float)(p_149734_2_ + 1) - p_149734_5_.nextFloat() * 0.1F;
                    f1 = (float)p_149734_3_ + p_149734_5_.nextFloat();
                    f2 = (float)p_149734_4_ + p_149734_5_.nextFloat();
                    p_149734_1_.spawnParticle("largesmoke", (double)f, (double)f1, (double)f2, 0.0D, 0.0D, 0.0D);
                }
            }

            if (Blocks.fire.canCatchFire(p_149734_1_, p_149734_2_, p_149734_3_, p_149734_4_ - 1, SOUTH))
            {
                for (l = 0; l < 2; ++l)
                {
                    f = (float)p_149734_2_ + p_149734_5_.nextFloat();
                    f1 = (float)p_149734_3_ + p_149734_5_.nextFloat();
                    f2 = (float)p_149734_4_ + p_149734_5_.nextFloat() * 0.1F;
                    p_149734_1_.spawnParticle("largesmoke", (double)f, (double)f1, (double)f2, 0.0D, 0.0D, 0.0D);
                }
            }

            if (Blocks.fire.canCatchFire(p_149734_1_, p_149734_2_, p_149734_3_, p_149734_4_ + 1, NORTH))
            {
                for (l = 0; l < 2; ++l)
                {
                    f = (float)p_149734_2_ + p_149734_5_.nextFloat();
                    f1 = (float)p_149734_3_ + p_149734_5_.nextFloat();
                    f2 = (float)(p_149734_4_ + 1) - p_149734_5_.nextFloat() * 0.1F;
                    p_149734_1_.spawnParticle("largesmoke", (double)f, (double)f1, (double)f2, 0.0D, 0.0D, 0.0D);
                }
            }

            if (Blocks.fire.canCatchFire(p_149734_1_, p_149734_2_, p_149734_3_ + 1, p_149734_4_, DOWN))
            {
                for (l = 0; l < 2; ++l)
                {
                    f = (float)p_149734_2_ + p_149734_5_.nextFloat();
                    f1 = (float)(p_149734_3_ + 1) - p_149734_5_.nextFloat() * 0.1F;
                    f2 = (float)p_149734_4_ + p_149734_5_.nextFloat();
                    p_149734_1_.spawnParticle("largesmoke", (double)f, (double)f1, (double)f2, 0.0D, 0.0D, 0.0D);
                }
            }
        }
        else
        {
            for (l = 0; l < 3; ++l)
            {
                f = (float)p_149734_2_ + p_149734_5_.nextFloat();
                f1 = (float)p_149734_3_ + p_149734_5_.nextFloat() * 0.5F + 0.5F;
                f2 = (float)p_149734_4_ + p_149734_5_.nextFloat();
                p_149734_1_.spawnParticle("largesmoke", (double)f, (double)f1, (double)f2, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister p_149651_1_)
    {
        this.field_149850_M = new IIcon[] {p_149651_1_.registerIcon(this.getTextureName() + "_layer_0"), p_149651_1_.registerIcon(this.getTextureName() + "_layer_1")};
    }

    @SideOnly(Side.CLIENT)
    public IIcon getFireIcon(int p_149840_1_)
    {
        return this.field_149850_M[p_149840_1_];
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int p_149691_1_, int p_149691_2_)
    {
        return this.field_149850_M[0];
    }

    public MapColor getMapColor(int p_149728_1_)
    {
        return MapColor.tntColor;
    }

    /*================================= Forge Start ======================================*/
    private static class FireInfo
    {
        private int encouragement = 0;
        private int flammibility = 0;
    }
    private IdentityHashMap<Block, FireInfo> blockInfo = Maps.newIdentityHashMap();

    public void setFireInfo(Block block, int encouragement, int flammibility)
    {
        if (block == Blocks.air) throw new IllegalArgumentException("Tried to set air on fire... This is bad.");
        int id = Block.getIdFromBlock(block);
        this.field_149849_a[id] = encouragement;
        this.field_149848_b[id] = flammibility;

        FireInfo info = getInfo(block, true);
        info.encouragement = encouragement;
        info.flammibility = flammibility;
    }

    private FireInfo getInfo(Block block, boolean garentee)
    {
        FireInfo ret = blockInfo.get(block);
        if (ret == null && garentee)
        {
            ret = new FireInfo();
            blockInfo.put(block, ret);
        }
        return ret;
    }

    public void rebuildFireInfo()
    {
        for (int x = 0; x < 4096; x++)
        {
            //If we care.. we could detect changes in here and make sure we keep them, however 
            //it's my thinking that anyone who hacks into the private variables should DIAF and we don't care about them.
            field_149849_a[x] = 0;
            field_149848_b[x] = 0;
        }

        for (Entry<Block, FireInfo> e : blockInfo.entrySet())
        {
            int id = Block.getIdFromBlock(e.getKey());
            if (id >= 0 && id < 4096)
            {
                field_149849_a[id] = e.getValue().encouragement;
                field_149848_b[id] = e.getValue().flammibility;
            }
        }
    }

    public int getFlammability(Block block)
    {
        int id = Block.getIdFromBlock(block);
        return id >= 0 && id < 4096 ? field_149848_b[id] : 0;
    }

    public int getEncouragement(Block block)
    {
        int id = Block.getIdFromBlock(block);
        return id >= 0 && id < 4096 ? field_149849_a[id] : 0;
    }

    /**
     * Side sensitive version that calls the block function.
     * 
     * @param world The current world
     * @param x X Position
     * @param y Y Position
     * @param z Z Position
     * @param face The side the fire is coming from
     * @return True if the face can catch fire.
     */
    public boolean canCatchFire(IBlockAccess world, int x, int y, int z, ForgeDirection face)
    {
        return world.getBlock(x, y, z).isFlammable(world, x, y, z, face);
    }

    /**
     * Side sensitive version that calls the block function.
     * 
     * @param world The current world
     * @param x X Position
     * @param y Y Position
     * @param z Z Position
     * @param oldChance The previous maximum chance.
     * @param face The side the fire is coming from
     * @return The chance of the block catching fire, or oldChance if it is higher
     */
    public int getChanceToEncourageFire(IBlockAccess world, int x, int y, int z, int oldChance, ForgeDirection face)
    {
    	Block block = world.getBlock(x, y, z);
        int newChance = block != null ? getFireSpreadSpeed(world, x, y, z, face) : 0;
        return (newChance > oldChance ? newChance : oldChance);
    }
    /*================================= Forge Start ======================================*/
}