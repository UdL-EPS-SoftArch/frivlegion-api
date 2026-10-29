package com.frivlegion.platform.handler;

import com.frivlegion.platform.domain.Player;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
public class PlayerEventHandler {

    @HandleBeforeCreate
    public void handlePlayerPreCreate(Player player) {
        player.encodePassword();
    }

    @HandleBeforeSave
    public void handlePlayerPreSave(Player player) {
        player.encodePassword();
    }

    @HandleAfterCreate
    @HandleAfterSave
    public void clearRawPassword(Player player) {
        player.setRawPassword(null);
    }
}
