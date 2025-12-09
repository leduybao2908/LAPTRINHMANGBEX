package smtp.client;

import javax.swing.*;

import smtp.server.FileServer;

import java.awt.*;
import java.io.*;
import java.net.Socket;

/**
 * FilePanel for client. Uses file transfer port to upload/list/download.
 * Sends clientName as first UTF when opening file socket.
 */
public class FilePanel extends JPanel {

    private final String clientName;
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> list = new JList<>(model);

    private final String serverHost = "localhost";

    public FilePanel(String clientName) {
        this.clientName = clientName;
        setLayout(new BorderLayout());

        JPanel top = new JPanel();
        JButton upload = new JButton("Upload");
        JButton download = new JButton("Download");
        JButton refresh = new JButton("Refresh");
        top.add(upload); top.add(download); top.add(refresh);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(list), BorderLayout.CENTER);

        upload.addActionListener(e -> doUpload());
        download.addActionListener(e -> doDownload());
        refresh.addActionListener(e -> doRefresh());

        doRefresh();
    }

    public void setClientList(String[] clients) {
        // optional: if you want to show clients in file panel
    }

    private void doUpload() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();

        try (Socket s = new Socket(serverHost, FileServer.FILE_PORT);
             DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
             DataInputStream dis = new DataInputStream(new BufferedInputStream(s.getInputStream()))) {

            // identify
            dos.writeUTF(clientName);
            // command
            dos.writeUTF("UPLOAD");
            // filename & size
            dos.writeUTF(f.getName());
            dos.writeLong(f.length());
            dos.flush();

            // send bytes
            try (FileInputStream fis = new FileInputStream(f)) {
                byte[] buf = new byte[16 * 1024];
                int r;
                while ((r = fis.read(buf)) != -1) {
                    dos.write(buf, 0, r);
                }
            }
            dos.flush();

            String resp = dis.readUTF();
            JOptionPane.showMessageDialog(this, "Upload response: " + resp);
            doRefresh();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Upload failed: " + ex.getMessage());
        }
    }

    private void doRefresh() {
        try (Socket s = new Socket(serverHost, FileServer.FILE_PORT);
             DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
             DataInputStream dis = new DataInputStream(new BufferedInputStream(s.getInputStream()))) {

            dos.writeUTF(clientName);
            dos.writeUTF("LIST");
            dos.flush();

            int count = dis.readInt();
            model.clear();
            for (int i = 0; i < count; i++) {
                String name = dis.readUTF();
                long size = dis.readLong();
                model.addElement(name + " (" + size + " bytes)");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void doDownload() {
        String selected = list.getSelectedValue();
        if (selected == null) return;
        String filename = selected.split(" \\(")[0];

        try (Socket s = new Socket(serverHost, FileServer.FILE_PORT);
             DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
             DataInputStream dis = new DataInputStream(new BufferedInputStream(s.getInputStream()))) {

            dos.writeUTF(clientName);
            dos.writeUTF("DOWNLOAD");
            dos.writeUTF(filename);
            dos.flush();

            String status = dis.readUTF();
            if ("NOT_FOUND".equals(status)) {
                JOptionPane.showMessageDialog(this, "File not found on server");
                return;
            }

            long size = dis.readLong();
            File out = new File("download_" + filename);
            try (FileOutputStream fos = new FileOutputStream(out)) {
                byte[] buf = new byte[16 * 1024];
                long remaining = size;
                while (remaining > 0) {
                    int toRead = (int) Math.min(buf.length, remaining);
                    int r = dis.read(buf, 0, toRead);
                    if (r == -1) break;
                    fos.write(buf, 0, r);
                    remaining -= r;
                }
            }
            JOptionPane.showMessageDialog(this, "Downloaded to " + out.getAbsolutePath());

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Download failed: " + ex.getMessage());
        }
    }
}
