package net.minecraft.nbt;

import io.github.crucible.nbt.ICrucibleString;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagShort extends NBTBase.NBTPrimitive implements ICrucibleString
{
    private short data;
    private static final String __OBFID = "CL_00001227";

    public NBTTagShort() {}

    public NBTTagShort(short p_i45135_1_)
    {
        this.data = p_i45135_1_;
    }

    void write(DataOutput p_74734_1_) throws IOException
    {
        p_74734_1_.writeShort(this.data);
    }

    void func_152446_a(DataInput p_152446_1_, int p_152446_2_, NBTSizeTracker p_152446_3_) throws IOException
    {
        p_152446_3_.func_152450_a(16L);
        this.data = p_152446_1_.readShort();
    }

    public byte getId()
    {
        return (byte)2;
    }

    public String toString()
    {
        return "" + this.data + "s";
    }

    public NBTBase copy()
    {
        return new NBTTagShort(this.data);
    }

    public boolean equals(Object p_equals_1_)
    {
        if (super.equals(p_equals_1_))
        {
            NBTTagShort nbttagshort = (NBTTagShort)p_equals_1_;
            return this.data == nbttagshort.data;
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        return super.hashCode() ^ this.data;
    }

    public long func_150291_c()
    {
        return (long)this.data;
    }

    public int func_150287_d()
    {
        return this.data;
    }

    public short func_150289_e()
    {
        return this.data;
    }

    public byte func_150290_f()
    {
        return (byte)(this.data & 255);
    }

    public double func_150286_g()
    {
        return (double)this.data;
    }

    public float func_150288_h()
    {
        return (float)this.data;
    }

    @Override
    public String crucible_toString(){
        return this.toString();
    }
}