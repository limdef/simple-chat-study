package threaded.command;

import threaded.ClientThread;

import java.util.concurrent.ConcurrentHashMap;

class WhisperCommand implements Command {
    @Override
    public void execute(ConcurrentHashMap<String, ClientThread> concurrentHashMap, ClientThread clientThread, String[] cmd) {
        // TODO
    }
}
