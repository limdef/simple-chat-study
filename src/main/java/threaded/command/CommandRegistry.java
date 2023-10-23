package threaded.command;

import java.util.HashMap;
import java.util.Map;

public class CommandRegistry {
    private final Map<String, Command> commands = new HashMap<>();

    public CommandRegistry() {
        commands.put("/name", new NameCommand());
        commands.put("/w", new WhisperCommand());
        commands.put("/r", new ReplyCommand());
        commands.put("", new ReplyCommand());
    }

    public void registerCommand(String commandName, Command command) {
        commands.put(commandName, command);
    }

    public Command getCommand(String commandName) {
        return commands.get(commandName);
    }
}
