package smtp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 * Worker xử lý kết nối control (CHAT) cho 1 client.
 * Protocol:
 *  - client gửi clientName (writeUTF) ngay sau connect
 *  - server gửi messages (writeUTF)
 *  - client gửi commands dạng UTF:
 *      - "PM|target|message" -> private message
 *      - (other commands can be added)
 */
public class ServerWorker extends Thread {

    private final Socket socket;
    private final FileServer server;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String clientName = "unknown";

    public ServerWorker(Socket socket, FileServer server) {
        this.socket = socket;
        this.server = server;
    }

    public String getClientName() {
        return clientName;
    }

    public void send(String utf) {
        try {
            dos.writeUTF(utf);
            dos.flush();
        } catch (Exception ignored) {}
    }

    @Override
    public void run() {
        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            // first message from client is its name
            clientName = dis.readUTF();
            server.addClient(this);

            // listen for chat commands
            while (true) {
                String cmd = dis.readUTF(); // blocks
                if (cmd == null) break;
                if (cmd.startsWith("PM|")) {
                    // PM|target|message
                    String[] p = cmd.split("\\|", 3);
                    if (p.length >= 3) {
                        String target = p[1];
                        String message = p[2];
                        server.sendPrivate(clientName, target, message);
                    }
                } else if (cmd.equalsIgnoreCase("QUIT")) {
                    break;
                } else {
                    // unknown - ignore or log
                    server.getUi().log("Unknown cmd from " + clientName + ": " + cmd);
                }
            }
        } catch (Exception e) {
            server.getUi().log("Client disconnected: " + clientName);
        } finally {
            try { socket.close(); } catch (Exception ignored) {}
            server.removeClient(this);
        }
    }
}
