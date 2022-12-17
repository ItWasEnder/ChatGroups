package me.endergaming.chatgroups.exceptions;

import com.marcusslover.plus.lib.text.Text;
import org.jetbrains.annotations.NotNull;

public class InvalidGroupIdException extends IGroupException {
    final String id;
    public InvalidGroupIdException(String id) {
        this.id = id;
    }

    @Override
    public @NotNull Text message() {
        return Text.of("&cGroup not found. &b%s &cis an invalid group id.".formatted(this.id));
    }
}
