package com.frivlegion.platform.handler;

import com.frivlegion.platform.domain.Player;
import com.frivlegion.platform.domain.Record;
import com.frivlegion.platform.repository.RecordRepository;
import com.frivlegion.platform.security.CurrentPlayer;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
public class RecordEventHandler {

    private final RecordRepository recordRepository;
    private final CurrentPlayer currentPlayer;

    public RecordEventHandler(RecordRepository recordRepository, CurrentPlayer currentPlayer) {
        this.recordRepository = recordRepository;
        this.currentPlayer = currentPlayer;
    }

    @HandleBeforeCreate
    public void handleRecordPreCreate(Record record) {
        record.setOwnedBy(currentPlayer.require());
        if (record.getStatus() == null) {
            record.setStatus(Record.Status.PRIVATE);
        }
    }

    @HandleBeforeSave
    public void handleRecordPreSave(Record record) {
        Player owner = persistedOwner(record);
        currentPlayer.requireOwner(owner);
        record.setOwnedBy(owner);
    }

    @HandleBeforeDelete
    public void handleRecordPreDelete(Record record) {
        currentPlayer.requireOwner(persistedOwner(record));
    }

    private Player persistedOwner(Record record) {
        if (record.getId() == null) {
            return record.getOwnedBy();
        }
        return recordRepository.findById(record.getId())
            .map(Record::getOwnedBy)
            .orElse(null);
    }
}
