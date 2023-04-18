package tv.ender.chatgroups.exceptions;

import com.marcusslover.plus.lib.text.Text;
import org.jetbrains.annotations.NotNull;

public class NonPlayerException extends IGroupException {
    @Override
    @NotNull
    public Text message() {
        return Text.of("&cYou must be a player to execute this command.");
    }
}
