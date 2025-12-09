package smtp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Server chính: khởi động 2 service:
 *  - control/chat service trên port CONTROL_PORT (kết nối bền, dùng ServerWorker)
 *  - file transfer service trên port FILE_PORT (kết nối tạm, dùng FileTransferWorker)
 *
 * Quản lý danh sách clients và định tuyến tin nhắn riêng.
 */
public class FileServer {

    public static final int FILE_PORT = 5001;
    public static final int CONTROL_PORT = 6000;
    private final List<ServerWorker> clients = Collections.synchronizedList(new ArrayList<>());
    private FileServerUI ui;

    public FileServer(FileServerUI ui) {
        this.ui = ui;
    }

    public void start() {
        // start control/chat server
        new Thread(this::startControlServer).start();
        // start file transfer server
        new Thread(this::startFileServer).start();
    }

    private void startControlServer() {
        try (ServerSocket ss = new ServerSocket(CONTROL_PORT)) {
            ui.log("Control server listening on port " + CONTROL_PORT);
            while (true) {
                Socket s = ss.accept();
                ServerWorker w = new ServerWorker(s, this);
                w.start();
            }
        } catch (IOException e) {
            ui.log("Control server error: " + e.getMessage());
        }
    }

    private void startFileServer() {
        try (ServerSocket ss = new ServerSocket(FILE_PORT)) {
            ui.log("File server listening on port " + FILE_PORT);
            while (true) {
                Socket s = ss.accept();
                FileTransferWorker ftw = new FileTransferWorker(s, this);
                ftw.start();
            }
        } catch (IOException e) {
            ui.log("File server error: " + e.getMessage());
        }
    }

    // client lifecycle
    public void addClient(ServerWorker w) {
        clients.add(w);
        ui.log("Client joined: " + w.getClientName());
        broadcastClientList();
    }

    public void removeClient(ServerWorker w) {
        clients.remove(w);
        ui.log("Client left: " + w.getClientName());
        broadcastClientList();
    }

    // send client list to all clients (format CLIENTS|name1,name2,...)
    public void broadcastClientList() {
        StringBuilder sb = new StringBuilder("CLIENTS|");
        synchronized (clients) {
            for (ServerWorker w : clients) {
                sb.append(w.getClientName()).append(",");
            }
        }
        String payload = sb.toString();
        synchronized (clients) {
            for (ServerWorker w : clients) {
                w.send(payload);
            }
        }
        // update UI
        List<String> names = new ArrayList<>();
        synchronized (clients) {
            for (ServerWorker w : clients) names.add(w.getClientName());
        }
        ui.updateClientList(names);
    }

    // send private message from -> to
    public void sendPrivate(String from, String to, String message) {
        synchronized (clients) {
            for (ServerWorker w : clients) {
                if (w.getClientName().equals(to)) {
                    w.send("PM|" + from + "|" + message);
                    ui.log("PM from " + from + " to " + to + ": " + message);
                    return;
                }
            }
        }
        ui.log("PM target not found: " + to);
    }

    // called by file transfer worker to update file list UI
    public void notifyFileUploaded(String filename) {
        ui.log("File uploaded: " + filename);
        ui.refreshFileList(); // UI will scan storage dir
    }

    public FileServerUI getUi() {
        return ui;
    }
}
