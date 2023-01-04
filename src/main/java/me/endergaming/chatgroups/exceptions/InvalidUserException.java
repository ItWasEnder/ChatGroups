package me.endergaming.chatgroups.exceptions;

import com.marcusslover.plus.lib.text.Text;
import org.jetbrains.annotations.NotNull;

public class InvalidUserException extends IGroupException {
    final String user;

    public InvalidUserException(String user) {
        this.user = user;
    }

    @Override
    public @NotNull Text message() {
        return Text.of("&6%s &cis not a valid player in the server!".formatted(this.user));
    }
}
