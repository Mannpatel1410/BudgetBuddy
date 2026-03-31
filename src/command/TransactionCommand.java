package command;

public interface TransactionCommand {
    void execute();
    void undo();
}
