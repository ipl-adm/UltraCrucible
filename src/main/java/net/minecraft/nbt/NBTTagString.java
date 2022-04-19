package net.minecraft.nbt;

import io.github.crucible.Crucible;
import io.github.crucible.nbt.ICrucibleString;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagString extends NBTBase implements ICrucibleString
{
    private String data;
    private static final String __OBFID = "CL_00001228";

    public NBTTagString()
    {
        this.data = "";
    }

    public NBTTagString(String p_i1389_1_)
    {
        this.data = Crucible.deduplicate(p_i1389_1_);

        if (p_i1389_1_ == null)
        {
            throw new IllegalArgumentException("Empty string not allowed");
        }
    }

    void write(DataOutput p_74734_1_) throws IOException
    {
        p_74734_1_.writeUTF(this.data);
    }

    void func_152446_a(DataInput p_152446_1_, int p_152446_2_, NBTSizeTracker p_152446_3_) throws IOException
    {
        this.data = p_152446_1_.readUTF();
        NBTSizeTracker.readUTF(p_152446_3_, data); // Forge: Correctly read String length including header.
    }

    public byte getId()
    {
        return (byte)8;
    }

    public String toString()
    {
        return "\"" + this.data + "\"";
    }

    public NBTBase copy()
    {
        return new NBTTagString(this.data);
    }

    public boolean equals(Object p_equals_1_)
    {
        if (!super.equals(p_equals_1_))
        {
            return false;
        }
        else
        {
            NBTTagString nbttagstring = (NBTTagString)p_equals_1_;
            return this.data == null && nbttagstring.data == null || this.data != null && this.data.equals(nbttagstring.data);
        }
    }

    public int hashCode()
    {
        return super.hashCode() ^ this.data.hashCode();
    }

    public String func_150285_a_()
    {
        return this.data;
    }

    @Override
    public String crucible_toString()
    {
        return quoteAndEscape(this.data);
    }

    public static String quoteAndEscape(String p_193588_0_)
    {
        StringBuilder stringbuilder = new StringBuilder("\"");

        for (int i = 0; i < p_193588_0_.length(); ++i)
        {
            char c0 = p_193588_0_.charAt(i);

            if (c0 == '\\' || c0 == '"')
            {
                stringbuilder.append('\\');
            }

            stringbuilder.append(c0);
        }

        return stringbuilder.append('"').toString();
    }
}