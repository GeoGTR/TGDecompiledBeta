package org.telegram.ui.Components.Paint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.telegram.messenger.AndroidUtilities;

public class UndoStore {
    private UndoStoreDelegate delegate;
    private List<UUID> operations = new ArrayList();
    private Map<UUID, Runnable> uuidToOperationMap = new HashMap();

    public interface UndoStoreDelegate {
        void historyChanged();
    }

    public boolean canUndo() {
        return this.operations.isEmpty() ^ 1;
    }

    public void setDelegate(UndoStoreDelegate undoStoreDelegate) {
        this.delegate = undoStoreDelegate;
    }

    public void registerUndo(UUID uuid, Runnable runnable) {
        this.uuidToOperationMap.put(uuid, runnable);
        this.operations.add(uuid);
        notifyOfHistoryChanges();
    }

    public void unregisterUndo(UUID uuid) {
        this.uuidToOperationMap.remove(uuid);
        this.operations.remove(uuid);
        notifyOfHistoryChanges();
    }

    public void undo() {
        if (this.operations.size() != 0) {
            int size = this.operations.size() - 1;
            UUID uuid = (UUID) this.operations.get(size);
            Runnable runnable = (Runnable) this.uuidToOperationMap.get(uuid);
            this.uuidToOperationMap.remove(uuid);
            this.operations.remove(size);
            runnable.run();
            notifyOfHistoryChanges();
        }
    }

    public void reset() {
        this.operations.clear();
        this.uuidToOperationMap.clear();
        notifyOfHistoryChanges();
    }

    private void notifyOfHistoryChanges() {
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                if (UndoStore.this.delegate != null) {
                    UndoStore.this.delegate.historyChanged();
                }
            }
        });
    }
}
