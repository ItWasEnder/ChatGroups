package tv.ender.chatgroups.command;

import com.marcusslover.plus.lib.command.Command;
import com.marcusslover.plus.lib.command.CommandContext;
import com.marcusslover.plus.lib.command.ICommand;
import com.marcusslover.plus.lib.command.TabCompleteContext;
import com.marcusslover.plus.lib.text.Text;
import tv.ender.chatgroups.ChatGroupsPlugin;
import tv.ender.chatgroups.exceptions.IGroupException;
import tv.ender.chatgroups.groups.Group;
import tv.ender.chatgroups.groups.GroupManagerImpl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Command(name = "chatgroups", aliases = {"cg", "chat"}, permission = "chatgroups.admin")
public class ChatGroupsCommand implements ICommand {
    GroupManagerImpl groupManager;

    static final Text ERROR = Text.of("&c&lERROR &r");
    static final Text SUCCESS = Text.of("&a&lSUCCESS &r");

    List<String> subcommands = List.of("create", "destroy", "list", "join", "leave", "options", "help");
    Text help = Text.of("""
            &a&lChatGroups &8- &7Commands
            &7- &f/cg create <id> &7creates a chat group with default options
            &7- &f/cg destroy <id> &7deletes a chat group
            &7- &f/cg list &7lists all chat groups
            &7- &f/cg join <id> &7joins a chat group
            &7- &f/cg leave &7leaves a chat group
            &7- &f/cg options <id> &7shows options gui for a chat group
            &7- &f/cg help &7shows this message
            """);

    public ChatGroupsCommand(GroupManagerImpl groupManager) {
        this.groupManager = groupManager;
    }

    @Override
    public boolean execute(@NotNull CommandContext cmd) {
        var sender = cmd.sender();
        var args = cmd.args();
        var label = cmd.label();

        if (args.length == 0) {
            this.help.send(sender);
            return false;
        }

        try {
            switch (args[0]) {
                case "create" -> {
                    if (args.length < 2) {
                        Text.of("&cUsage: /%s create <id>".formatted(label)).send(sender);

                        return false;
                    }

                    var id = args[1];
                    this.groupManager.register(new Group(id));

                    SUCCESS.copy().append(Text.of("&7Created chat group &b%s".formatted(id))).send(sender);
                }

                case "destroy" -> {
                    if (args.length < 2) {
                        Text.of("&cUsage: /%s delete <id>".formatted(label)).send(sender);

                        return false;
                    }

                    var id = args[1];

                    this.groupManager.unregister(id);

                    SUCCESS.copy().append(Text.of("&7Destroyed chat group &b%s".formatted(id))).send(sender);
                }

                case "list" -> {
                    Text.of("&e&lChat Groups: &r&8(%d)".formatted(this.groupManager.groups().size())).send(sender);
                    for (var group : this.groupManager.groups().values()) {
                        Text.of("&e- &b" + group.id() + " &7(" + group.members().size() + " members)").send(sender);
                    }
                }

                case "join" -> {
                    if (args.length < 3) {
                        Text.of("&cUsage: /%s join <id> <user>".formatted(label)).send(sender);

                        return false;
                    }

                    var id = args[1];
                    var user = args[2];

                    this.groupManager.joinGroup(user, id);

                    SUCCESS.copy().append(Text.of("&a%s was added to chat group %s!".formatted(user, id))).send(sender);
                }

                case "leave" -> {
                    if (args.length < 3) {
                        Text.of("&cUsage: /%s leave <id> <user>".formatted(label)).send(sender);

                        return false;
                    }

                    var id = args[1];
                    var user = args[2];

                    this.groupManager.leaveGroup(user, id);

                    SUCCESS.copy().append(Text.of("&6%s was removed from chat group %s!".formatted(user, id))).send(sender);
                }

                case "options" -> {
                    if (args.length < 2) {
                        Text.of("&cUsage: /%s options <id>".formatted(label)).send(sender);

                        return false;
                    }

                    var id = args[1];

                    this.groupManager.openOptions(sender, id);

                    SUCCESS.copy().append(Text.of("&eOpening options screen for %s".formatted(id))).send(sender);
                }

                default -> {
                    this.help.send(sender);
                }
            }
        } catch (IGroupException error) {
            ERROR.copy().append(error.message()).send(sender);

            ChatGroupsPlugin.logger().warning(error.toString());

            return false;
        }


        return true;
    }

    @Override
    public @NotNull List<@NotNull String> tab(@NotNull TabCompleteContext tab) {
        var args = tab.args();

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], this.subcommands, new LinkedList<>());
        }

        if (args.length == 2) {
            if (args[0].matches("destroy|join|leave|options")) {
                return StringUtil.copyPartialMatches(args[1], this.groupManager.groups().keySet(), new LinkedList<>());
            } else if (args[0].matches("create")) {
                var unavailable = this.groupManager.groups().keySet();

                if (unavailable.contains(args[1])) {
                    return List.of("§cinvalid id");
                } else {
                    return List.of(args[1]);
                }
            }
        }

        if (args.length == 3) {
            if (args[0].matches("leave")) {
                var leavable = this.groupManager.groups().get(args[1]).members().stream()
                        .map(x -> Bukkit.getOfflinePlayer(x).getName())
                        .filter(Objects::nonNull).toList();

                return StringUtil.copyPartialMatches(args[2], leavable, new LinkedList<>());
            } else if (args[0].matches("join")) {
                var joinable = Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .toList();

                return StringUtil.copyPartialMatches(args[2], joinable, new LinkedList<>());
            }
        }

        return List.of();
    }
}
