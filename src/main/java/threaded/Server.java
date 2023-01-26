package threaded;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server {
    public static ConcurrentLinkedQueue<ClientThread> clients = new ConcurrentLinkedQueue<>();

    private int port;
    private long clientNum = 1L;

    public Server(int port) {
        this.port = port;
    }

    public void run() throws IOException {
        ServerSocket server = new ServerSocket(port);

        while (true) {
            Socket clientSocket = server.accept();

            if (clients.size() < 100) {

                ClientThread curClient = new ClientThread(clientSocket, clients, clientNum);
                clients.add(curClient);
                curClient.start();
                clientNum++;

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

class ClientThread extends Thread {
    private String clientName;
    private Socket clientSocket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private ConcurrentLinkedQueue<ClientThread> clients;

    public ClientThread(Socket clientSocket, ConcurrentLinkedQueue<ClientThread> clients, long clientNum) throws IOException {
        this.clientName = "member-" + clientNum;
        this.clientSocket = clientSocket;
        this.clients = clients;

        broadcast("[SERVER] " + clientName + " has joined");
    }

    public void run() {
        try {

            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
            writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8));

            unicast("[SERVER] welcome to our chat server!");

            while (true) {
                String msg;
                if ((msg = reader.readLine()) != null) {
                    // TODO 명령어 기능 추가
                    broadcast(msg);
                } else {
                    throw new IOException();
                }
            }

        } catch (IOException e) {
            // read IOException
            System.out.println(clientName + " session terminated");

        } finally {
            close();
        }
    }

    private void unicast(String msg) throws IOException {
        writer.write(msg);
        writer.write('\n');
        writer.flush();
    }

    private void broadcast(String msg) throws IOException {
        for (ClientThread client : clients) {
            client.unicast(clientName + " : " + msg);
        }
    }

    private void close() {
        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
            if (clients != null) clients.remove(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
