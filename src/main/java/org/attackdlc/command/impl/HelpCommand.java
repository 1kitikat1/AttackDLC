package org.attackdlc.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.attackdlc.command.ClientCommand;
import org.attackdlc.command.CommandManager;
import org.attackdlc.utils.text.ChatUtil;

public final class HelpCommand extends ClientCommand {
   private final CommandManager manager;

   public HelpCommand(CommandManager manager) {
      super("help", "Shows all available commands", ":question:");
      this.manager = manager;
   }

   @Override
   public void build(LiteralArgumentBuilder<Object> builder) {
      builder.executes(context -> {
         ChatUtil.header("AttackDLC commands");

         for (ClientCommand command : this.manager.getCommands()) {
            ChatUtil.entry(command.emoji(), this.manager.getPrefix() + command.name(), command.description());
         }

         return 1;
      });
   }
}

