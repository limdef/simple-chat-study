package threaded;

import org.apache.commons.lang3.RandomStringUtils;
import threaded.command.CommandHandler;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientThread extends Thread {
    private final Socket socket;
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private final ConcurrentHashMap<String, ClientThread> clients;
    private final CommandHandler commandHandler;
    private String name;
    private String lastWhisperName;
    private OutputStream os;
    private DataInputStream dis;

    public ClientThread(
            Socket clientSocket,
            ConcurrentHashMap<String, ClientThread> clients,
            CommandHandler commandHandler
    ) throws IOException {
        this.socket = clientSocket;
        this.clients = clients;
        this.commandHandler = commandHandler;

        add(this);

        dis = new DataInputStream(socket.getInputStream());
        os = socket.getOutputStream();

        notify(("[SERVER] " + this.name + " has joined").getBytes(StandardCharsets.UTF_8));
    }

    private void add(ClientThread clientThread) {
        String name;
        do {
            name = RandomStringUtils.randomAlphanumeric(8);
        } while (clients.putIfAbsent(name, clientThread) != null);

        this.name = name;
    }

    public void run() {
        try {
            unicast("[SERVER] welcome to our chat server!".getBytes(StandardCharsets.UTF_8));

            while (true) {
                int msgLen = dis.readInt();
                byte[] buffer = new byte[msgLen];
                int read = dis.read(buffer, 0, msgLen);
                if (read == -1) {
                    throw new IOException();
                }
                commandHandler.handle(clients, this, buffer);
            }

        } catch (IOException e) {
            // read IOException
            System.out.println(name + " session terminated");
            clients.remove(this.name);

        } finally {
            close();
        }
    }

    public void unicast(byte[] msg) throws IOException {
        os.write(msg);
        os.flush();
    }

    public void unicast(String name, byte[] msg) throws IOException {
        baos.write((name + " : ").getBytes(StandardCharsets.UTF_8));
        baos.write(msg);
        os.write(baos.toByteArray());
        os.flush();
        baos.reset();
    }

    public void broadcast(byte[] msg) throws IOException {
        for (Map.Entry<String, ClientThread> entry : clients.entrySet()) {
            entry.getValue().unicast(name, msg);
        }
    }

    public void notify(byte[] msg) throws IOException {
        for (Map.Entry<String, ClientThread> entry : clients.entrySet()) {
            entry.getValue().unicast(msg);
        }
    }

    private void close() {
        try {
            if (os != null) os.close();
            if (dis != null) dis.close();
            if (socket != null && !socket.isClosed()) socket.close();
            if (clients != null) clients.remove(this.name);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMyName() {
        return name;
    }

    public void setMyName(String s) {
        this.name = s;
    }

    public String getLastWhisperName() {
        return lastWhisperName;
    }

    public void setLastWhisperName(String s) {
        this.lastWhisperName = s;
    }
}
