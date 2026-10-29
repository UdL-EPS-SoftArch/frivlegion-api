package com.frivlegion.platform.handler;

import com.frivlegion.platform.domain.Player;
import com.frivlegion.platform.domain.Record;
import com.frivlegion.platform.repository.RecordRepository;
import org.springframework.data.rest.core.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
public class RecordEventHandler {
    final RecordRepository recordRepository;

    public RecordEventHandler(RecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    @HandleBeforeCreate
    public void handleRecordPreCreate(Record record) {
        Player owner = currentPlayer();
        record.setOwnedBy(owner);
        if (record.getStatus() == null) {
            record.setStatus(Record.Status.PRIVATE);
        }
    }

    @HandleBeforeSave
    public void handleRecordPreSave(Record record) {
        Player owner = currentPlayer();
        if (!owner.getUsername().equals(record.getOwnedBy().getUsername())) {
            throw new SecurityException("Only the owner can modify the record");
        }
    }

    private Player currentPlayer() {
        return (Player) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
