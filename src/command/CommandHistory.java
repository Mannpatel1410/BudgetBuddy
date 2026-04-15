package command;

import java.util.Stack;

public class CommandHistory {
    private final Stack<TransactionCommand> undoStack = new Stack<>();
    private final Stack<TransactionCommand> redoStack = new Stack<>();

    public void executeCommand(TransactionCommand cmd) {
        cmd.execute();
        undoStack.push(cmd);
        redoStack.clear();
    }

    public void undo() {
        if (undoStack.isEmpty()) return;
        TransactionCommand cmd = undoStack.pop();
        try {
            cmd.undo();
            redoStack.push(cmd);
        } catch (RuntimeException e) {
            // Re-insert failed — put the command back so the user can retry
            undoStack.push(cmd);
            throw e;
        }
    }

    public void redo() {
        if (redoStack.isEmpty()) return;
        TransactionCommand cmd = redoStack.pop();
        try {
            cmd.execute();
            undoStack.push(cmd);
        } catch (RuntimeException e) {
            redoStack.push(cmd);
            throw e;
        }
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }
}
