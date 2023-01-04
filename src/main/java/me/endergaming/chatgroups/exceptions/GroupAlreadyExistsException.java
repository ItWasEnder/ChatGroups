package me.endergaming.chatgroups.exceptions;

import com.marcusslover.plus.lib.text.Text;
import org.jetbrains.annotations.NotNull;

public class GroupAlreadyExistsException extends IGroupException {
    final String id;

    public GroupAlreadyExistsException(String id) {
        this.id = id;
    }

    @Override
    public @NotNull Text message() {
        return Text.of("&eThe group &b%s&e already exists!".formatted(this.id));
    }
}
