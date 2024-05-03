package threaded.command;

import threaded.ClientThread;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

class ReplyCommand implements Command {
    @Override
    public void execute(ConcurrentHashMap<String, ClientThread> concurrentHashMap, ClientThread clientThread, String[] cmd) throws IOException {
        if (cmd.length < 2) {
            return;
        }

        String destName = clientThread.getLastWhisperName();
        if (destName == null) {
            clientThread.unicast("[SERVER] user for the reply does not exist".getBytes(StandardCharsets.UTF_8));
            return;
        }

        ClientThread destClient = concurrentHashMap.get(destName);
        if (destClient == null) {
            clientThread.unicast("[SERVER] user for the reply does not exist".getBytes(StandardCharsets.UTF_8));
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < cmd.length; i++) {
            sb.append(cmd[i]);
        }
        String msg = sb.toString();

        // From
        destClient.unicast(("[From] " + clientThread.getMyName() + " : " + msg).getBytes(StandardCharsets.UTF_8));
        destClient.setLastWhisperName(clientThread.getMyName());

        // To
        clientThread.unicast(("[To] " + destName + " : " + msg).getBytes(StandardCharsets.UTF_8));
    }
}
