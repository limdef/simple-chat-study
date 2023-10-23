package threaded.command;

import threaded.ClientThread;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

public class CommandHandler {

    private final CommandRegistry registry;

    public CommandHandler(CommandRegistry registry) {
        this.registry = registry;
    }

    public void handle(ConcurrentHashMap<String, ClientThread> clients, ClientThread clientThread, byte[] buffer) throws IOException {
        String text = new String(buffer, StandardCharsets.UTF_8);
        String[] splits = text.split(" ");
        Command command = registry.getCommand(splits[0]);

        if (command != null) {
            command.execute(clients, clientThread, splits);
        } else {
            clientThread.broadcast(buffer);
        }
    }
}
