package tv.ender.chatgroups.exceptions;

import com.marcusslover.plus.lib.text.Text;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;

public abstract class IGroupException extends ExecutionException {
    public abstract @NotNull Text message();

    @Override
    public String toString() {
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', this.message().raw()));
    }
}
