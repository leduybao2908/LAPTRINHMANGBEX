package smtp;

import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatLightLaf;

import smtp.server.FileServer;
import smtp.server.FileServerUI;

public class ServerStarter {
    public static void main(String[] args) {
        // Set FlatLaf Look and Feel
        try {
            FlatLightLaf.setup();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            // Tạo UI trước
            FileServerUI ui = new FileServerUI();

            // Tạo server và gắn UI
            FileServer server = new FileServer(ui);

            // Gắn server lại vào UI (nếu UI cần gọi server)
            ui.setServer(server);

            // Hiển thị giao diện server
            ui.setVisible(true);

            // Chạy server
            server.start();
        });
    }
}

