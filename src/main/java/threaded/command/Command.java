package threaded.command;

import threaded.ClientThread;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

interface Command {
    void execute(ConcurrentHashMap<String, ClientThread> concurrentHashMap, ClientThread clientThread, String[] cmd) throws IOException;
}
