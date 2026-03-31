package command;

import java.util.ArrayDeque;
import java.util.Deque;

public class CommandHistory {
    private final Deque<TransactionCommand> undoStack = new ArrayDeque<>();
    private final Deque<TransactionCommand> redoStack = new ArrayDeque<>();

    public void execute(TransactionCommand command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
    }

    public boolean undo() {
        if (undoStack.isEmpty()) {
            return false;
        }
        TransactionCommand command = undoStack.pop();
        command.undo();
        redoStack.push(command);
        return true;
    }

    public boolean redo() {
        if (redoStack.isEmpty()) {
            return false;
        }
        TransactionCommand command = redoStack.pop();
        command.execute();
        undoStack.push(command);
        return true;
    }
}
