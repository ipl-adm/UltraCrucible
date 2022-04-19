package net.minecraft.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
// Cauldron start
import org.bukkit.craftbukkit.command.CraftSimpleCommandMap;
import org.bukkit.craftbukkit.command.ModCustomCommand;
import cpw.mods.fml.common.FMLCommonHandler;
// Cauldron end

public class CommandHandler implements ICommandManager
{
    private static final Logger logger = LogManager.getLogger();
    private final Map commandMap = new HashMap();
    private final Set commandSet = new HashSet();
    private static final String __OBFID = "CL_00001765";

    public int executeCommand(ICommandSender p_71556_1_, String p_71556_2_)
    {
        p_71556_2_ = p_71556_2_.trim();

        if (p_71556_2_.startsWith("/"))
        {
            p_71556_2_ = p_71556_2_.substring(1);
        }

        String[] astring = p_71556_2_.split(" ");
        String s1 = astring[0];
        astring = dropFirstString(astring);
        ICommand icommand = (ICommand)this.commandMap.get(s1);
        int i = this.getUsernameIndex(icommand, astring);
        int j = 0;
        ChatComponentTranslation chatcomponenttranslation;

        try
        {
            if (icommand == null)
            {
                throw new CommandNotFoundException();
            }

            if (true || icommand.canCommandSenderUseCommand(p_71556_1_)) // Cauldron start - disable check for permissions since we handle it on Bukkit side
            {
                CommandEvent event = new CommandEvent(icommand, p_71556_1_, astring);
                if (MinecraftForge.EVENT_BUS.post(event))
                {
                    if (event.exception != null)
                    {
                        throw event.exception;
                    }
                    return 1;
                }

                if (i > -1)
                {
                    EntityPlayerMP[] aentityplayermp = PlayerSelector.matchPlayers(p_71556_1_, astring[i]);
                    String s2 = astring[i];
                    EntityPlayerMP[] aentityplayermp1 = aentityplayermp;
                    int k = aentityplayermp.length;

                    for (int l = 0; l < k; ++l)
                    {
                        EntityPlayerMP entityplayermp = aentityplayermp1[l];
                        astring[i] = entityplayermp.getCommandSenderName();

                        try
                        {
                            icommand.processCommand(p_71556_1_, astring);
                            ++j;
                        }
                        catch (CommandException commandexception1)
                        {
                            ChatComponentTranslation chatcomponenttranslation1 = new ChatComponentTranslation(commandexception1.getMessage(), commandexception1.getErrorOjbects());
                            chatcomponenttranslation1.getChatStyle().setColor(EnumChatFormatting.RED);
                            p_71556_1_.addChatMessage(chatcomponenttranslation1);
                        }
                    }

                    astring[i] = s2;
                }
                else
                {
                    try
                    {
                        icommand.processCommand(p_71556_1_, astring);
                        ++j;
                    }
                    catch (CommandException commandexception)
                    {
                        chatcomponenttranslation = new ChatComponentTranslation(commandexception.getMessage(), commandexception.getErrorOjbects());
                        chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.RED);
                        p_71556_1_.addChatMessage(chatcomponenttranslation);
                    }
                }
            }
            else
            {
                ChatComponentTranslation chatcomponenttranslation2 = new ChatComponentTranslation("commands.generic.permission", new Object[0]);
                chatcomponenttranslation2.getChatStyle().setColor(EnumChatFormatting.RED);
                p_71556_1_.addChatMessage(chatcomponenttranslation2);
            }
        }
        catch (WrongUsageException wrongusageexception)
        {
            chatcomponenttranslation = new ChatComponentTranslation("commands.generic.usage", new Object[] {new ChatComponentTranslation(wrongusageexception.getMessage(), wrongusageexception.getErrorOjbects())});
            chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.RED);
            p_71556_1_.addChatMessage(chatcomponenttranslation);
        }
        catch (CommandException commandexception2)
        {
            chatcomponenttranslation = new ChatComponentTranslation(commandexception2.getMessage(), commandexception2.getErrorOjbects());
            chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.RED);
            p_71556_1_.addChatMessage(chatcomponenttranslation);
        }
        catch (Throwable throwable)
        {
            chatcomponenttranslation = new ChatComponentTranslation("commands.generic.exception", new Object[0]);
            chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.RED);
            p_71556_1_.addChatMessage(chatcomponenttranslation);
            logger.error("Couldn\'t process command: \'" + p_71556_2_ + "\'", throwable);
        }

        return j;
    }

    public ICommand registerCommand(ICommand p_71560_1_)
    {
    // Cauldron start - register commands with permission nodes, defaulting to class name
        return registerCommand(p_71560_1_, p_71560_1_.getClass().getName());
    }

    public ICommand registerCommand(String permissionGroup, ICommand par1ICommand)
    {
        return registerCommand(par1ICommand, permissionGroup + "." + par1ICommand.getCommandName());
    }

    public ICommand registerCommand(ICommand par1ICommand, String permissionNode)
    {
        List list = par1ICommand.getCommandAliases();
        this.commandMap.put(par1ICommand.getCommandName(), par1ICommand);
        this.commandSet.add(par1ICommand);
        // register vanilla commands with Bukkit to support permissions.
        CraftSimpleCommandMap commandMap = FMLCommonHandler.instance().getMinecraftServerInstance().server.getCraftCommandMap();
        ModCustomCommand customCommand = new ModCustomCommand(par1ICommand.getCommandName());
        customCommand.setPermission(permissionNode);
        if (list != null)
            customCommand.setAliases(list);
        commandMap.register(par1ICommand.getCommandName(), customCommand);
        LogManager.getLogger().info("Registered command " + par1ICommand.getCommandName() + " with permission node " + permissionNode);

        if (list != null)
        {
            Iterator iterator = list.iterator();

            while (iterator.hasNext())
            {
                String s = (String)iterator.next();
                ICommand icommand1 = (ICommand)this.commandMap.get(s);

                if (icommand1 == null || !icommand1.getCommandName().equals(s))
                {
                    this.commandMap.put(s, par1ICommand);
                }
            }
        }

        return par1ICommand;
    }
    // Cauldron end

    private static String[] dropFirstString(String[] p_71559_0_)
    {
        String[] astring1 = new String[p_71559_0_.length - 1];

        for (int i = 1; i < p_71559_0_.length; ++i)
        {
            astring1[i - 1] = p_71559_0_[i];
        }

        return astring1;
    }

    public List getPossibleCommands(ICommandSender p_71558_1_, String p_71558_2_)
    {
        String[] astring = p_71558_2_.split(" ", -1);
        String s1 = astring[0];

        if (astring.length == 1)
        {
            ArrayList arraylist = new ArrayList();
            Iterator iterator = this.commandMap.entrySet().iterator();

            while (iterator.hasNext())
            {
                Entry entry = (Entry)iterator.next();

                if (CommandBase.doesStringStartWith(s1, (String)entry.getKey()) && ((ICommand)entry.getValue()).canCommandSenderUseCommand(p_71558_1_))
                {
                    arraylist.add(entry.getKey());
                }
            }

            return arraylist;
        }
        else
        {
            if (astring.length > 1)
            {
                ICommand icommand = (ICommand)this.commandMap.get(s1);

                if (icommand != null)
                {
                    return icommand.addTabCompletionOptions(p_71558_1_, dropFirstString(astring));
                }
            }

            return null;
        }
    }

    public List getPossibleCommands(ICommandSender p_71557_1_)
    {
        ArrayList arraylist = new ArrayList();
        Iterator iterator = this.commandSet.iterator();

        while (iterator.hasNext())
        {
            ICommand icommand = (ICommand)iterator.next();

            if (icommand.canCommandSenderUseCommand(p_71557_1_))
            {
                arraylist.add(icommand);
            }
        }

        return arraylist;
    }

    public Map getCommands()
    {
        return this.commandMap;
    }

    private int getUsernameIndex(ICommand p_82370_1_, String[] p_82370_2_)
    {
        if (p_82370_1_ == null)
        {
            return -1;
        }
        else
        {
            for (int i = 0; i < p_82370_2_.length; ++i)
            {
                if (p_82370_1_.isUsernameIndex(p_82370_2_, i) && PlayerSelector.matchesMultiplePlayers(p_82370_2_[i]))
                {
                    return i;
                }
            }

            return -1;
        }
    }
}