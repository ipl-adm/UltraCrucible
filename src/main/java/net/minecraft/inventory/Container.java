package net.minecraft.inventory;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;


// CraftBukkit start
import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayerMP;

import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
// CraftBukkit end
import org.spigotmc.SpigotConfig;

public abstract class Container
{
    public List inventoryItemStacks = new ArrayList();
    public List<Slot> inventorySlots = new ArrayList<Slot>();
    public int windowId;
    @SideOnly(Side.CLIENT)
    private short transactionID;
    private int field_94535_f = -1;
    public int field_94536_g; // CraftBukkit - private -> public
    private final Set field_94537_h = new HashSet();
    public InventoryView bukkitView = null; // Cauldron
    private int tickCount = 0; // Spigot
    private final int fullMatchRate = SpigotConfig.fullMatchRate; // Spigot
    protected List crafters = new ArrayList();
    private Set playerList = new HashSet();
    private static final String __OBFID = "CL_00001730";

    // CraftBukkit start
    public boolean checkReachable = true;
    public InventoryView getBukkitView() { return bukkitView; } // Cauldron
    public void transferTo(Container other, org.bukkit.craftbukkit.entity.CraftHumanEntity player)
    {
        InventoryView source = this.getBukkitView(), destination = other.getBukkitView();
        // Cauldron start - add null checks to skip modded inventories with no Bukkit wrappers, and
        // catch AbstractMethodErrors for modded IInventory's with no onClose()
        if (source != null) {
            try {
                ((CraftInventory) source.getTopInventory()).getInventory().onClose(player);
            } catch (AbstractMethodError ex) {
                // modded
            }

            try {
                ((CraftInventory) source.getBottomInventory()).getInventory().onClose(player);
            } catch (AbstractMethodError ex) {
                // modded
            }
        }
        if (destination != null) {
            try {
                ((CraftInventory) destination.getTopInventory()).getInventory().onOpen(player);
            } catch (AbstractMethodError ex) {
                // modded
            }

            try {
                ((CraftInventory) destination.getBottomInventory()).getInventory().onOpen(player);
            } catch (AbstractMethodError ex) {
                // modded
            }
        }
        // Cauldron end
    }
    // CraftBukkit end

    protected Slot addSlotToContainer(Slot p_75146_1_)
    {
        p_75146_1_.slotNumber = this.inventorySlots.size();
        this.inventorySlots.add(p_75146_1_);
        this.inventoryItemStacks.add(null);
        return p_75146_1_;
    }

    public void addCraftingToCrafters(ICrafting p_75132_1_)
    {
        if (this.crafters.contains(p_75132_1_))
        {
            // Cauldron start - As we do not create a new player object on respawn, we need to update the client with changes if listener already exists
            //throw new IllegalArgumentException("Listener already listening");
            p_75132_1_.sendContainerAndContentsToPlayer(this, this.getInventory());
            this.detectAndSendChanges();
            // Cauldron end
        }
        else
        {
            this.crafters.add(p_75132_1_);
            p_75132_1_.sendContainerAndContentsToPlayer(this, this.getInventory());
            this.detectAndSendChanges();
        }
    }

    public List getInventory()
    {
        ArrayList<ItemStack> arraylist = new ArrayList<ItemStack>();

        for (int i = 0; i < this.inventorySlots.size(); ++i)
        {
            arraylist.add(((Slot)this.inventorySlots.get(i)).getStack());
        }

        return arraylist;
    }

    @SideOnly(Side.CLIENT)
    public void removeCraftingFromCrafters(ICrafting p_82847_1_)
    {
        this.crafters.remove(p_82847_1_);
    }

    public void detectAndSendChanges()
    {
        for (int i = 0; i < this.inventorySlots.size(); ++i)
        {
        	Slot slotty = (Slot)this.inventorySlots.get(i);
        	slotty.slotNumber = i; // Thermos ensure correct indexing
            ItemStack itemstack = (slotty).getStack();
            ItemStack itemstack1 = (ItemStack)this.inventoryItemStacks.get(i);

            if (!areItemStacksEqual(itemstack1, itemstack))
            {
                itemstack1 = itemstack == null ? null : itemstack.copy();
                this.inventoryItemStacks.set(i, itemstack1);

                for (int j = 0; j < this.crafters.size(); ++j)
                {
                    ((ICrafting)this.crafters.get(j)).sendSlotContents(this, i, itemstack1);
                }
            }
        }
        tickCount++;
    }

    public boolean enchantItem(EntityPlayer p_75140_1_, int p_75140_2_)
    {
        return false;
    }

    public Slot getSlotFromInventory(IInventory p_75147_1_, int p_75147_2_)
    {
        for (int j = 0; j < this.inventorySlots.size(); ++j)
        {
            Slot slot = (Slot)this.inventorySlots.get(j);

            if (slot.isSlotInInventory(p_75147_1_, p_75147_2_))
            {
                return slot;
            }
        }

        return null;
    }

    public Slot getSlot(int p_75139_1_)
    {
        // Cauldron start - vanilla compatibility. fixes NPE with ProjectRed's Item Stock Keeper
        if (p_75139_1_ < 0 || p_75139_1_ > this.inventorySlots.size())
            return null;
        // Cauldron end
        return (Slot)this.inventorySlots.get(p_75139_1_);
    }

    public ItemStack transferStackInSlot(EntityPlayer p_82846_1_, int p_82846_2_)
    {
        Slot slot = (Slot)this.inventorySlots.get(p_82846_2_);
        return slot != null ? slot.getStack() : null;
    }

    public ItemStack slotClick(int p_75144_1_, int p_75144_2_, int p_75144_3_, EntityPlayer p_75144_4_)
    {
        ItemStack itemstack = null;
        InventoryPlayer inventoryplayer = p_75144_4_.inventory;
        int i1;
        ItemStack itemstack3;

        if (p_75144_3_ == 5)
        {
            int l = this.field_94536_g;
            this.field_94536_g = func_94532_c(p_75144_2_);

            if ((l != 1 || this.field_94536_g != 2) && l != this.field_94536_g)
            {
                this.func_94533_d();
            }
            else if (inventoryplayer.getItemStack() == null)
            {
                this.func_94533_d();
            }
            else if (this.field_94536_g == 0)
            {
                this.field_94535_f = func_94529_b(p_75144_2_);

                if (func_94528_d(this.field_94535_f))
                {
                    this.field_94536_g = 1;
                    this.field_94537_h.clear();
                }
                else
                {
                    this.func_94533_d();
                }
            }
            else if (this.field_94536_g == 1)
            {
                Slot slot = (Slot)this.inventorySlots.get(p_75144_1_);

                if (slot != null && func_94527_a(slot, inventoryplayer.getItemStack(), true) && slot.isItemValid(inventoryplayer.getItemStack()) && inventoryplayer.getItemStack().stackSize > this.field_94537_h.size() && this.canDragIntoSlot(slot))
                {
                    this.field_94537_h.add(slot);
                }
            }
            else if (this.field_94536_g == 2)
            {
                if (!this.field_94537_h.isEmpty())
                {
                    itemstack3 = inventoryplayer.getItemStack().copy();
                    i1 = inventoryplayer.getItemStack().stackSize;
                    Iterator iterator = this.field_94537_h.iterator();
                    Map<Integer, ItemStack> draggedSlots = new HashMap<Integer, ItemStack>(); // CraftBukkit - Store slots from drag in map (raw slot id -> new stack)

                    while (iterator.hasNext())
                    {
                        Slot slot1 = (Slot)iterator.next();

                        if (slot1 != null && func_94527_a(slot1, inventoryplayer.getItemStack(), true) && slot1.isItemValid(inventoryplayer.getItemStack()) && inventoryplayer.getItemStack().stackSize >= this.field_94537_h.size() && this.canDragIntoSlot(slot1))
                        {
                            ItemStack itemstack1 = itemstack3.copy();
                            int j1 = slot1.getHasStack() ? slot1.getStack().stackSize : 0;
                            func_94525_a(this.field_94537_h, this.field_94535_f, itemstack1, j1);

                            if (itemstack1.stackSize > itemstack1.getMaxStackSize())
                            {
                                itemstack1.stackSize = itemstack1.getMaxStackSize();
                            }

                            if (itemstack1.stackSize > slot1.getSlotStackLimit())
                            {
                                itemstack1.stackSize = slot1.getSlotStackLimit();
                            }

                            i1 -= itemstack1.stackSize - j1;
                            draggedSlots.put(slot1.slotNumber, itemstack1); // CraftBukkit - Put in map instead of setting
                        }
                    }

                    // CraftBukkit start - InventoryDragEvent
                    InventoryView view = getBukkitView();
                    org.bukkit.inventory.ItemStack newcursor = CraftItemStack.asCraftMirror(itemstack3);
                    newcursor.setAmount(i1);
                    Map<Integer, org.bukkit.inventory.ItemStack> eventmap = new HashMap<Integer, org.bukkit.inventory.ItemStack>();

                    for (Map.Entry<Integer, ItemStack> ditem : draggedSlots.entrySet())
                    {
                        eventmap.put(ditem.getKey(), CraftItemStack.asBukkitCopy(ditem.getValue()));
                    }

                    // It's essential that we set the cursor to the new value here to prevent item duplication if a plugin closes the inventory.
                    ItemStack oldCursor = inventoryplayer.getItemStack();
                    inventoryplayer.setItemStack(CraftItemStack.asNMSCopy(newcursor));
                    InventoryDragEvent event = new InventoryDragEvent(view, (newcursor.getType() != org.bukkit.Material.AIR ? newcursor : null), CraftItemStack.asBukkitCopy(oldCursor), this.field_94535_f == i1, eventmap); // Should be dragButton
                    p_75144_4_.worldObj.getServer().getPluginManager().callEvent(event);
                    // Whether or not a change was made to the inventory that requires an update.
                    boolean needsUpdate = event.getResult() != Result.DEFAULT;

                    if (event.getResult() != Result.DENY)
                    {
                        for (Map.Entry<Integer, ItemStack> dslot : draggedSlots.entrySet())
                        {
                            view.setItem(dslot.getKey(), CraftItemStack.asBukkitCopy(dslot.getValue()));
                        }

                        // The only time the carried item will be set to null is if the inventory is closed by the server.
                        // If the inventory is closed by the server, then the cursor items are dropped.  This is why we change the cursor early.
                        if (inventoryplayer.getItemStack() != null)
                        {
                            inventoryplayer.setItemStack(CraftItemStack.asNMSCopy(event.getCursor()));
                            needsUpdate = true;
                        }
                    }
                    else
                    {
                        inventoryplayer.setItemStack(oldCursor);
                    }

                    if (needsUpdate && p_75144_4_ instanceof EntityPlayerMP)
                    {
                        ((EntityPlayerMP) p_75144_4_).sendContainerToPlayer(this);
                    }

                    // CraftBukkit end
                }

                this.func_94533_d();
            }
            else
            {
                this.func_94533_d();
            }
        }
        else if (this.field_94536_g != 0)
        {
            this.func_94533_d();
        }
        else
        {
            Slot slot2;
            int l1;
            ItemStack itemstack5;

            if ((p_75144_3_ == 0 || p_75144_3_ == 1) && (p_75144_2_ == 0 || p_75144_2_ == 1))
            {
                if (p_75144_1_ == -999)
                {
                    if (inventoryplayer.getItemStack() != null && p_75144_1_ == -999)
                    {
                        if (p_75144_2_ == 0)
                        {
                            p_75144_4_.dropPlayerItemWithRandomChoice(inventoryplayer.getItemStack(), true);
                            inventoryplayer.setItemStack((ItemStack)null);
                        }

                        if (p_75144_2_ == 1)
                        {
                            // CraftBukkit start - Store a reference
                            ItemStack itemstack4 = inventoryplayer.getItemStack();

                            if (inventoryplayer.getItemStack().stackSize == 0)
                            {
                                p_75144_4_.dropPlayerItemWithRandomChoice(inventoryplayer.getItemStack().splitStack(1), true);
                            }

                            if (itemstack4.stackSize == 0)
                            {
                                // CraftBukkit end
                                inventoryplayer.setItemStack((ItemStack)null);
                            }
                        }
                    }
                }
                else if (p_75144_3_ == 1)
                {
                    if (p_75144_1_ < 0)
                    {
                        return null;
                    }

                    slot2 = (Slot)this.inventorySlots.get(p_75144_1_);

                    if (slot2 != null && slot2.canTakeStack(p_75144_4_))
                    {
                        itemstack3 = this.transferStackInSlot(p_75144_4_, p_75144_1_);

                        if (itemstack3 != null)
                        {
                            Item item = itemstack3.getItem();
                            itemstack = itemstack3.copy();

                            if (slot2.getStack() != null && slot2.getStack().getItem() == item)
                            {
                                this.retrySlotClick(p_75144_1_, p_75144_2_, true, p_75144_4_);
                            }
                        }
                    }
                }
                else
                {
                    if (p_75144_1_ < 0)
                    {
                        return null;
                    }

                    slot2 = (Slot)this.inventorySlots.get(p_75144_1_);

                    if (slot2 != null)
                    {
                        itemstack3 = slot2.getStack();
                        ItemStack itemstack4 = inventoryplayer.getItemStack();

                        if (itemstack3 != null)
                        {
                            itemstack = itemstack3.copy();
                        }

                        if (itemstack3 == null)
                        {
                            if (itemstack4 != null && slot2.isItemValid(itemstack4))
                            {
                                l1 = p_75144_2_ == 0 ? itemstack4.stackSize : 1;

                                if (l1 > slot2.getSlotStackLimit())
                                {
                                    l1 = slot2.getSlotStackLimit();
                                }

                                if (itemstack4.stackSize >= l1)
                                {
                                    slot2.putStack(itemstack4.splitStack(l1));
                                }

                                if (itemstack4.stackSize == 0)
                                {
                                    inventoryplayer.setItemStack((ItemStack)null);
                                }
                            }
                        }
                        else if (slot2.canTakeStack(p_75144_4_))
                        {
                            if (itemstack4 == null)
                            {
                                l1 = p_75144_2_ == 0 ? itemstack3.stackSize : (itemstack3.stackSize + 1) / 2;
                                itemstack5 = slot2.decrStackSize(l1);
                                inventoryplayer.setItemStack(itemstack5);

                                if (itemstack3.stackSize == 0)
                                {
                                    slot2.putStack((ItemStack)null);
                                }

                                slot2.onPickupFromSlot(p_75144_4_, inventoryplayer.getItemStack());
                            }
                            else if (slot2.isItemValid(itemstack4))
                            {
                                if (itemstack3.getItem() == itemstack4.getItem() && itemstack3.getItemDamage() == itemstack4.getItemDamage() && ItemStack.areItemStackTagsEqual(itemstack3, itemstack4))
                                {
                                    l1 = p_75144_2_ == 0 ? itemstack4.stackSize : 1;

                                    if (l1 > slot2.getSlotStackLimit() - itemstack3.stackSize)
                                    {
                                        l1 = slot2.getSlotStackLimit() - itemstack3.stackSize;
                                    }

                                    if (l1 > itemstack4.getMaxStackSize() - itemstack3.stackSize)
                                    {
                                        l1 = itemstack4.getMaxStackSize() - itemstack3.stackSize;
                                    }

                                    itemstack4.splitStack(l1);

                                    if (itemstack4.stackSize == 0)
                                    {
                                        inventoryplayer.setItemStack((ItemStack)null);
                                    }

                                    itemstack3.stackSize += l1;
                                }
                                else if (itemstack4.stackSize <= slot2.getSlotStackLimit())
                                {
                                    slot2.putStack(itemstack4);
                                    inventoryplayer.setItemStack(itemstack3);
                                }
                            }
                            else if (itemstack3.getItem() == itemstack4.getItem() && itemstack4.getMaxStackSize() > 1 && (!itemstack3.getHasSubtypes() || itemstack3.getItemDamage() == itemstack4.getItemDamage()) && ItemStack.areItemStackTagsEqual(itemstack3, itemstack4))
                            {
                                l1 = itemstack3.stackSize;

                                if (l1 > 0 && l1 + itemstack4.stackSize <= itemstack4.getMaxStackSize())
                                {
                                    itemstack4.stackSize += l1;
                                    itemstack3 = slot2.decrStackSize(l1);

                                    if (itemstack3.stackSize == 0)
                                    {
                                        slot2.putStack((ItemStack)null);
                                    }

                                    slot2.onPickupFromSlot(p_75144_4_, inventoryplayer.getItemStack());
                                }
                            }
                        }

                        slot2.onSlotChanged();
                    }
                }
            }
            else if (p_75144_3_ == 2 && p_75144_2_ >= 0 && p_75144_2_ < 9)
            {
                slot2 = (Slot)this.inventorySlots.get(p_75144_1_);

                if (slot2.canTakeStack(p_75144_4_))
                {
                    itemstack3 = inventoryplayer.getStackInSlot(p_75144_2_);
                    boolean flag = itemstack3 == null || slot2.inventory == inventoryplayer && slot2.isItemValid(itemstack3);
                    l1 = -1;

                    if (!flag)
                    {
                        l1 = inventoryplayer.getFirstEmptyStack();
                        flag |= l1 > -1;
                    }

                    if (slot2.getHasStack() && flag)
                    {
                        itemstack5 = slot2.getStack();
                        inventoryplayer.setInventorySlotContents(p_75144_2_, itemstack5.copy());

                        if ((slot2.inventory != inventoryplayer || !slot2.isItemValid(itemstack3)) && itemstack3 != null)
                        {
                            if (l1 > -1)
                            {
                                inventoryplayer.addItemStackToInventory(itemstack3);
                                slot2.decrStackSize(itemstack5.stackSize);
                                slot2.putStack((ItemStack)null);
                                slot2.onPickupFromSlot(p_75144_4_, itemstack5);
                            }
                        }
                        else
                        {
                            slot2.decrStackSize(itemstack5.stackSize);
                            slot2.putStack(itemstack3);
                            slot2.onPickupFromSlot(p_75144_4_, itemstack5);
                        }
                    }
                    else if (!slot2.getHasStack() && itemstack3 != null && slot2.isItemValid(itemstack3))
                    {
                        inventoryplayer.setInventorySlotContents(p_75144_2_, (ItemStack)null);
                        slot2.putStack(itemstack3);
                    }
                }
            }
            else if (p_75144_3_ == 3 && p_75144_4_.capabilities.isCreativeMode && inventoryplayer.getItemStack() == null && p_75144_1_ >= 0)
            {
                slot2 = (Slot)this.inventorySlots.get(p_75144_1_);

                if (slot2 != null && slot2.getHasStack())
                {
                    itemstack3 = slot2.getStack().copy();
                    itemstack3.stackSize = itemstack3.getMaxStackSize();
                    inventoryplayer.setItemStack(itemstack3);
                }
            }
            else if (p_75144_3_ == 4 && inventoryplayer.getItemStack() == null && p_75144_1_ >= 0)
            {
                slot2 = (Slot)this.inventorySlots.get(p_75144_1_);

                if (slot2 != null && slot2.getHasStack() && slot2.canTakeStack(p_75144_4_))
                {
                    itemstack3 = slot2.decrStackSize(p_75144_2_ == 0 ? 1 : slot2.getStack().stackSize);
                    slot2.onPickupFromSlot(p_75144_4_, itemstack3);
                    p_75144_4_.dropPlayerItemWithRandomChoice(itemstack3, true);
                }
            }
            else if (p_75144_3_ == 6 && p_75144_1_ >= 0)
            {
                slot2 = (Slot)this.inventorySlots.get(p_75144_1_);
                itemstack3 = inventoryplayer.getItemStack();

                if (itemstack3 != null && (slot2 == null || !slot2.getHasStack() || !slot2.canTakeStack(p_75144_4_)))
                {
                    i1 = p_75144_2_ == 0 ? 0 : this.inventorySlots.size() - 1;
                    l1 = p_75144_2_ == 0 ? 1 : -1;

                    for (int i2 = 0; i2 < 2; ++i2)
                    {
                        for (int j2 = i1; j2 >= 0 && j2 < this.inventorySlots.size() && itemstack3.stackSize < itemstack3.getMaxStackSize(); j2 += l1)
                        {
                            Slot slot3 = (Slot)this.inventorySlots.get(j2);

                            if (slot3.getHasStack() && func_94527_a(slot3, itemstack3, true) && slot3.canTakeStack(p_75144_4_) && this.func_94530_a(itemstack3, slot3) && (i2 != 0 || slot3.getStack().stackSize != slot3.getStack().getMaxStackSize()))
                            {
                                int k1 = Math.min(itemstack3.getMaxStackSize() - itemstack3.stackSize, slot3.getStack().stackSize);
                                ItemStack itemstack2 = slot3.decrStackSize(k1);
                                itemstack3.stackSize += k1;

                                if (itemstack2.stackSize <= 0)
                                {
                                    slot3.putStack((ItemStack)null);
                                }

                                slot3.onPickupFromSlot(p_75144_4_, itemstack2);
                            }
                        }
                    }
                }

                this.detectAndSendChanges();
            }
        }

        return itemstack;
    }

    public boolean func_94530_a(ItemStack p_94530_1_, Slot p_94530_2_)
    {
        return true;
    }

    protected void retrySlotClick(int p_75133_1_, int p_75133_2_, boolean p_75133_3_, EntityPlayer p_75133_4_)
    {
        this.slotClick(p_75133_1_, p_75133_2_, 1, p_75133_4_);
    }

    public void onContainerClosed(EntityPlayer p_75134_1_)
    {
        InventoryPlayer inventoryplayer = p_75134_1_.inventory;

        if (inventoryplayer.getItemStack() != null)
        {
            p_75134_1_.dropPlayerItemWithRandomChoice(inventoryplayer.getItemStack(), false);
            inventoryplayer.setItemStack((ItemStack)null);
        }
    }

    public void onCraftMatrixChanged(IInventory p_75130_1_)
    {
        this.detectAndSendChanges();
    }

    public void putStackInSlot(int p_75141_1_, ItemStack p_75141_2_)
    {
        this.getSlot(p_75141_1_).putStack(p_75141_2_);
    }

    @SideOnly(Side.CLIENT)
    public void putStacksInSlots(ItemStack[] p_75131_1_)
    {
        for (int i = 0; i < p_75131_1_.length; ++i)
        {
            this.getSlot(i).putStack(p_75131_1_[i]);
        }
    }

    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int p_75137_1_, int p_75137_2_) {}

    @SideOnly(Side.CLIENT)
    public short getNextTransactionID(InventoryPlayer p_75136_1_)
    {
        ++this.transactionID;
        return this.transactionID;
    }

    public boolean isPlayerNotUsingContainer(EntityPlayer p_75129_1_)
    {
        return !this.playerList.contains(p_75129_1_);
    }

    public void setPlayerIsPresent(EntityPlayer p_75128_1_, boolean p_75128_2_)
    {
        if (p_75128_2_)
        {
            this.playerList.remove(p_75128_1_);
        }
        else
        {
            this.playerList.add(p_75128_1_);
        }
    }

    public abstract boolean canInteractWith(EntityPlayer p_75145_1_);

    protected boolean mergeItemStack(ItemStack p_75135_1_, int p_75135_2_, int p_75135_3_, boolean p_75135_4_)
    {
        boolean flag1 = false;
        int k = p_75135_2_;

        if (p_75135_4_)
        {
            k = p_75135_3_ - 1;
        }

        Slot slot;
        ItemStack itemstack1;

        if (p_75135_1_.isStackable())
        {
            while (p_75135_1_.stackSize > 0 && (!p_75135_4_ && k < p_75135_3_ || p_75135_4_ && k >= p_75135_2_))
            {
                slot = (Slot)this.inventorySlots.get(k);
                itemstack1 = slot.getStack();

                if (itemstack1 != null && itemstack1.getItem() == p_75135_1_.getItem() && (!p_75135_1_.getHasSubtypes() || p_75135_1_.getItemDamage() == itemstack1.getItemDamage()) && ItemStack.areItemStackTagsEqual(p_75135_1_, itemstack1))
                {
                    int l = itemstack1.stackSize + p_75135_1_.stackSize;

                    if (l <= p_75135_1_.getMaxStackSize())
                    {
                        p_75135_1_.stackSize = 0;
                        itemstack1.stackSize = l;
                        slot.onSlotChanged();
                        flag1 = true;
                    }
                    else if (itemstack1.stackSize < p_75135_1_.getMaxStackSize())
                    {
                        p_75135_1_.stackSize -= p_75135_1_.getMaxStackSize() - itemstack1.stackSize;
                        itemstack1.stackSize = p_75135_1_.getMaxStackSize();
                        slot.onSlotChanged();
                        flag1 = true;
                    }
                }

                if (p_75135_4_)
                {
                    --k;
                }
                else
                {
                    ++k;
                }
            }
        }

        if (p_75135_1_.stackSize > 0)
        {
            if (p_75135_4_)
            {
                k = p_75135_3_ - 1;
            }
            else
            {
                k = p_75135_2_;
            }

            while (!p_75135_4_ && k < p_75135_3_ || p_75135_4_ && k >= p_75135_2_)
            {
                slot = (Slot)this.inventorySlots.get(k);
                itemstack1 = slot.getStack();

                if (itemstack1 == null)
                {
                    slot.putStack(p_75135_1_.copy());
                    slot.onSlotChanged();
                    p_75135_1_.stackSize = 0;
                    flag1 = true;
                    break;
                }

                if (p_75135_4_)
                {
                    --k;
                }
                else
                {
                    ++k;
                }
            }
        }

        return flag1;
    }

    public static int func_94529_b(int p_94529_0_)
    {
        return p_94529_0_ >> 2 & 3;
    }

    public static int func_94532_c(int p_94532_0_)
    {
        return p_94532_0_ & 3;
    }

    @SideOnly(Side.CLIENT)
    public static int func_94534_d(int p_94534_0_, int p_94534_1_)
    {
        return p_94534_0_ & 3 | (p_94534_1_ & 3) << 2;
    }

    public static boolean func_94528_d(int p_94528_0_)
    {
        return p_94528_0_ == 0 || p_94528_0_ == 1;
    }

    protected void func_94533_d()
    {
        this.field_94536_g = 0;
        this.field_94537_h.clear();
    }

    public static boolean func_94527_a(Slot p_94527_0_, ItemStack p_94527_1_, boolean p_94527_2_)
    {
        boolean flag1 = p_94527_0_ == null || !p_94527_0_.getHasStack();

        if (p_94527_0_ != null && p_94527_0_.getHasStack() && p_94527_1_ != null && p_94527_1_.isItemEqual(p_94527_0_.getStack()) && ItemStack.areItemStackTagsEqual(p_94527_0_.getStack(), p_94527_1_))
        {
            int i = p_94527_2_ ? 0 : p_94527_1_.stackSize;
            flag1 |= p_94527_0_.getStack().stackSize + i <= p_94527_1_.getMaxStackSize();
        }

        return flag1;
    }

    public static void func_94525_a(Set p_94525_0_, int p_94525_1_, ItemStack p_94525_2_, int p_94525_3_)
    {
        switch (p_94525_1_)
        {
            case 0:
                p_94525_2_.stackSize = MathHelper.floor_float((float)p_94525_2_.stackSize / (float)p_94525_0_.size());
                break;
            case 1:
                p_94525_2_.stackSize = 1;
        }

        p_94525_2_.stackSize += p_94525_3_;
    }

    public boolean canDragIntoSlot(Slot p_94531_1_)
    {
        return true;
    }

    public static int calcRedstoneFromInventory(IInventory p_94526_0_)
    {
        if (p_94526_0_ == null)
        {
            return 0;
        }
        else
        {
            int i = 0;
            float f = 0.0F;

            for (int j = 0; j < p_94526_0_.getSizeInventory(); ++j)
            {
                ItemStack itemstack = p_94526_0_.getStackInSlot(j);

                if (itemstack != null)
                {
                    f += (float)itemstack.stackSize / (float)Math.min(p_94526_0_.getInventoryStackLimit(), itemstack.getMaxStackSize());
                    ++i;
                }
            }

            f /= (float)p_94526_0_.getSizeInventory();
            return MathHelper.floor_float(f * 14.0F) + (i > 0 ? 1 : 0);
        }
    }
    
    // Spigot start
    public boolean areItemStacksEqual(ItemStack is1, ItemStack is2) {
        return tickCount % fullMatchRate == 0 ? ItemStack.areItemStacksEqual(is1, is2) : ItemStack.fastMatches(is1, is2);
    }
    // Spigot
}