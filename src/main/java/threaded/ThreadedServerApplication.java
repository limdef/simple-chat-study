package threaded;

import java.io.IOException;

public class ThreadedServerApplication {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("please enter port number");
            System.exit(1);
        }
        new Server(Integer.parseInt(args[0])).run();
    }
}
