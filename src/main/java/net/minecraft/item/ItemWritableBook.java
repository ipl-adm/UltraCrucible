package net.minecraft.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;

public class ItemWritableBook extends Item
{
    private static final String __OBFID = "CL_00000076";

    public ItemWritableBook()
    {
        this.setMaxStackSize(1);
    }

    public ItemStack onItemRightClick(ItemStack p_77659_1_, World p_77659_2_, EntityPlayer p_77659_3_)
    {
        p_77659_3_.displayGUIBook(p_77659_1_);
        return p_77659_1_;
    }

    public boolean getShareTag()
    {
        return true;
    }

    public static boolean shadowUpdate(ItemStack stack, ItemStack  push, boolean isSigning)
    {
        if (isSigning)
        {
            //if(push.getTagCompound().hasKey("title"))
            if(push.getTagCompound().hasKey("title", 8))
                stack.setTagInfo("title", new NBTTagString(push.getTagCompound().getString("title")));
        }
        //if (push.getTagCompound().hasKey("pages",8))
        if (push.getTagCompound().hasKey("pages",9))
            stack.setTagInfo("pages", push.getTagCompound().getTagList("pages", 8));
        return true;
        /*if (this.hasTagCompound() && par1.hasTagCompound())
        {
            NBTTagCompound nbttagcompound = this.getTagCompound();
            NBTTagCompound nbttagcompound1 = par1.getTagCompound();
            if (nbttagcompound1.hasKey("pages"))
                nbttagcompound.setTag("pages", nbttagcompound1.getTag("pages"));
            if (isSigning)
            {
                if (nbttagcompound1.hasKey("title"))
                {
                    nbttagcompound.setTag("title", nbttagcompound1.getTag("title"));
                }
                if (nbttagcompound1.hasKey("author"))
                    nbttagcompound.setTag("author", nbttagcompound1.getTag("author"));
            }
        }
        else if(par1.hasTagCompound())
        {
            NBTTagCompound nbttagcompound1 = par1.getTagCompound();
            if (nbttagcompound1.hasKey("pages"))
                this.setTagInfo("pages", nbttagcompound1.getTag("pages"));
            if (isSigning)
            {
                if (nbttagcompound1.hasKey("title"))
                {
                    this.setTagInfo("title", nbttagcompound1.getTag("title"));
                }
                if (nbttagcompound1.hasKey("author"))
                    this.setTagInfo("author", nbttagcompound1.getTag("author"));
            }
        }*/
    }

    public static boolean func_150930_a(NBTTagCompound p_150930_0_)
    {
        if (p_150930_0_ == null)
        {
            return false;
        }
        else if (!p_150930_0_.hasKey("pages", 9))
        {
            return false;
        }
        else
        {
            NBTTagList nbttaglist = p_150930_0_.getTagList("pages", 8);

            for (int i = 0; i < nbttaglist.tagCount(); ++i)
            {
                String s = nbttaglist.getStringTagAt(i);

                if (s == null || s.length() > 256)
                {
                    return false;
                }
            }

            return true;
        }
    }
}