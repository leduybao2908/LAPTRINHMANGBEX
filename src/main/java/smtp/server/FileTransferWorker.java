package smtp.server;

import java.io.*;
import java.net.Socket;

/**
 * Handles one file-transfer connection.
 * Protocol:
 *  - client sends clientName (UTF)
 *  - client sends command (UTF): UPLOAD | LIST | DOWNLOAD
 *  - For UPLOAD:
 *      - sendUTF(filename), sendLong(filesize), then raw bytes
 *      - server replies writeUTF("OK") or error
 *  - For LIST:
 *      - server writes int count, then for each file writeUTF(name), writeLong(size)
 *  - For DOWNLOAD:
 *      - client sendsUTF(filename)
 *      - server responds "NOT_FOUND" or "OK" + file length + bytes
 */
public class FileTransferWorker extends Thread {

    private final Socket socket;
    private final FileServer server;
    private static final int BUFFER = 16 * 1024;
    private static final String STORAGE = "server_files";

    public FileTransferWorker(Socket socket, FileServer server) {
        this.socket = socket;
        this.server = server;
        File dir = new File(STORAGE);
        if (!dir.exists()) dir.mkdirs();
    }

    @Override
    public void run() {
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
             DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))) {

            String clientName = dis.readUTF(); // client identifies itself
            server.getUi().log("File-connection from: " + clientName + " @ " + socket.getInetAddress());

            String cmd = dis.readUTF();
            switch (cmd) {
                case "UPLOAD":
                    handleUpload(dis, dos);
                    break;
                case "LIST":
                    handleList(dos);
                    break;
                case "DOWNLOAD":
                    handleDownload(dis, dos);
                    break;
                default:
                    dos.writeUTF("ERR_UNKNOWN_COMMAND");
                    dos.flush();
            }
        } catch (Exception e) {
            server.getUi().log("FileWorker error: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (Exception ignored) {}
        }
    }

    private void handleUpload(DataInputStream dis, DataOutputStream dos) throws IOException {
        String filename = dis.readUTF();
        long size = dis.readLong();
        File out = new File(STORAGE, new File(filename).getName());
        try (FileOutputStream fos = new FileOutputStream(out)) {
            byte[] buf = new byte[BUFFER];
            long remaining = size;
            while (remaining > 0) {
                int toRead = (int) Math.min(buf.length, remaining);
                int r = dis.read(buf, 0, toRead);
                if (r == -1) break;
                fos.write(buf, 0, r);
                remaining -= r;
            }
            fos.flush();
        }
        dos.writeUTF("OK");
        dos.flush();
        server.notifyFileUploaded(out.getName());
    }

    private void handleList(DataOutputStream dos) throws IOException {
        File dir = new File(STORAGE);
        File[] files = dir.listFiles();
        if (files == null) files = new File[0];
        dos.writeInt(files.length);
        for (File f : files) {
            dos.writeUTF(f.getName());
            dos.writeLong(f.length());
        }
        dos.flush();
    }

    private void handleDownload(DataInputStream dis, DataOutputStream dos) throws IOException {
        String filename = dis.readUTF();
        File f = new File(STORAGE, filename);
        if (!f.exists() || !f.isFile()) {
            dos.writeUTF("NOT_FOUND");
            dos.flush();
            return;
        }
        dos.writeUTF("OK");
        dos.writeLong(f.length());
        try (FileInputStream fis = new FileInputStream(f)) {
            byte[] buf = new byte[BUFFER];
            int r;
            while ((r = fis.read(buf)) != -1) {
                dos.write(buf, 0, r);
            }
        }
        dos.flush();
    }
}
