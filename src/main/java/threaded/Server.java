package threaded;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class Server {
    public static ConcurrentLinkedQueue<ClientThread> clients = new ConcurrentLinkedQueue<>();

    private final int port;
    private final AtomicLong clientNum = new AtomicLong(0L);

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
                clientNum.getAndIncrement();

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
    private final int RANDOM_STR_LEN = 8;
    private String name;
    private final Socket socket;
    private AtomicLong clientNum;
    private OutputStream os;
    private DataInputStream dis;
    private ConcurrentLinkedQueue<ClientThread> clients;

    public ClientThread(Socket clientSocket, ConcurrentLinkedQueue<ClientThread> clients, AtomicLong clientNum) throws IOException {
        this.name = RandomStringUtils.randomAlphanumeric(RANDOM_STR_LEN);
        this.socket = clientSocket;
        this.clients = clients;
        this.clientNum = clientNum;

        broadcast(("[SERVER] " + this.name + " has joined").getBytes(StandardCharsets.UTF_8));
    }

    public void run() {
        try {
            dis = new DataInputStream(socket.getInputStream());
            os = socket.getOutputStream();

            unicast("[SERVER] welcome to our chat server!".getBytes(StandardCharsets.UTF_8));

            while (true) {
                int msgLen = dis.readInt();
                byte[] buffer = new byte[msgLen];
                int read = dis.read(buffer, 0, msgLen);
                if (read == -1) {
                    throw new IOException();
                }
                broadcast(buffer);
            }

        } catch (IOException e) {
            // read IOException
            System.out.println(name + " session terminated");
            clientNum.getAndDecrement();

        } finally {
            close();
        }
    }

    private void unicast(byte[] msg) throws IOException {
        os.write(msg);
        os.flush();
    }

    private void unicast(String name, byte[] msg) throws IOException {
        os.write((name + " : ").getBytes());
        os.write(msg);
        os.write('\n');
        os.flush();
    }

    private void broadcast(byte[] msg) throws IOException {
        for (ClientThread client : clients) {
            client.unicast(this.name, msg);
        }
    }

    private void close() {
        try {
            if (os != null) os.close();
            if (dis != null) dis.close();
            if (socket != null && !socket.isClosed()) socket.close();
            if (clients != null) clients.remove(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
