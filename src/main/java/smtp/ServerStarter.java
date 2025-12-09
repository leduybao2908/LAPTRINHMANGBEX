package smtp;

import smtp.server.FileServer;
import smtp.server.FileServerUI;

public class ServerStarter {    public static void main(String[] args) {

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
    }
}

