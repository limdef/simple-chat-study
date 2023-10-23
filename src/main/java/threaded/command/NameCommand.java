package threaded.command;

import threaded.ClientThread;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

class NameCommand implements Command {
    @Override
    public void execute(ConcurrentHashMap<String, ClientThread> concurrentHashMap, ClientThread clientThread, String[] cmd) throws IOException {
        if (cmd.length > 2) {
            clientThread.unicast("name is not allowed white space".getBytes(StandardCharsets.UTF_8));
            return;
        }

        // TODO 닉네임 허용 문자 체크

        ClientThread ct = concurrentHashMap.putIfAbsent(cmd[1], clientThread);
        if (ct != null) {
            clientThread.unicast("this name is already in used".getBytes(StandardCharsets.UTF_8));
            return;
        }

        concurrentHashMap.remove(clientThread.getMyName());
        clientThread.setMyName(cmd[1]);

        clientThread.unicast(("name changed to " + cmd[1]).getBytes(StandardCharsets.UTF_8));
    }
}
