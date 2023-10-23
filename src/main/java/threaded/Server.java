package threaded;

import threaded.command.CommandHandler;
import threaded.command.CommandRegistry;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private final ConcurrentHashMap<String, ClientThread> clientsMap = new ConcurrentHashMap<>();
    private final int port;
    public Server(int port) {
        this.port = port;
    }

    public void run() throws IOException {
        ServerSocket server = new ServerSocket(port);

        CommandHandler commandHandler = new CommandHandler(new CommandRegistry());

        while (true) {
            Socket clientSocket = server.accept();

            if (clientsMap.size() < 100) {

                ClientThread curClient = new ClientThread(clientSocket, clientsMap, commandHandler);
                curClient.start();

            } else {
                // 100명 초과일 경우
                OutputStream os = clientSocket.getOutputStream();
                os.write("[SERVER] chatroom can not allow more than 100 users".getBytes());
                os.flush();

                if (!clientSocket.isClosed()) clientSocket.close();
            }
        }
    }
}

