package org.minetweak.command;

import net.minecraft.src.EnumChatFormatting;
import org.minetweak.Minetweak;
import org.minetweak.Server;
import org.minetweak.entity.Player;

public class CommandDeop extends CommandExecutor {

    @Override
    public void executeCommand(CommandSender sender, String overallCommand, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(EnumChatFormatting.AQUA + "Usage: /deop <player>");
            return;
        }
        Player targetPlayer = Minetweak.getPlayerByName(args[0]);

        Server.deopPlayer(args[0]);

        if (Minetweak.isPlayerOnline(args[0])) {
            targetPlayer.sendMessage("You have been deopped by: " + sender.getName());
            sender.sendMessage("You deopped " + args[0]);
        } else {
            sender.sendMessage("You deopped " + args[0] + " which is offline at the moment");
        }
    }

}
