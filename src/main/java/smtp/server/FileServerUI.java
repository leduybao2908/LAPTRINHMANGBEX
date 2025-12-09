package smtp.server;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Server UI: Log, Clients list, Files list, Delete button.
 */
public class FileServerUI extends JFrame {

    private final JTextArea logArea = new JTextArea();
    private final DefaultListModel<String> clientModel = new DefaultListModel<>();
    private final DefaultListModel<String> fileModel = new DefaultListModel<>();

    private JList<String> fileListUI;
    private JButton deleteBtn;

    private FileServer server;

    public void setServer(FileServer server) {
        this.server = server;
    }

    public FileServerUI() {
        super("Server UI");
        initUI();
        refreshFileList();
    }

    private void initUI() {
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // LEFT = LOG
        JScrollPane logPane = new JScrollPane(logArea);
        logArea.setEditable(false);

        // RIGHT = CLIENTS + FILES
        JPanel right = new JPanel(new BorderLayout());

        // --- Clients ---
        JList<String> clientListUI = new JList<>(clientModel);
        clientListUI.setBorder(BorderFactory.createTitledBorder("Clients"));

        // --- Files ---
        fileListUI = new JList<>(fileModel);
        fileListUI.setBorder(BorderFactory.createTitledBorder("Files"));

        // delete button
        deleteBtn = new JButton("Delete Selected File");
        deleteBtn.addActionListener(e -> deleteSelectedFile());

        JPanel filePanel = new JPanel(new BorderLayout());
        filePanel.add(new JScrollPane(fileListUI), BorderLayout.CENTER);
        filePanel.add(deleteBtn, BorderLayout.SOUTH);

        // Combine
        JSplitPane rightSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(clientListUI),
                filePanel
        );
        rightSplit.setDividerLocation(250);
        right.add(rightSplit, BorderLayout.CENTER);

        JSplitPane mainSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                logPane,
                right
        );
        mainSplit.setDividerLocation(500);

        add(mainSplit, BorderLayout.CENTER);
    }

    /** Log messages */
    public void log(String s) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(s + "\n");
        });
    }

    /** Update client list on UI */
    public void updateClientList(List<String> clients) {
        SwingUtilities.invokeLater(() -> {
            clientModel.clear();
            for (String c : clients) clientModel.addElement(c);
        });
    }

    /** Refresh file list from server_files directory */
    public void refreshFileList() {
        SwingUtilities.invokeLater(() -> {
            fileModel.clear();
            File dir = new File("server_files");
            if (!dir.exists()) dir.mkdirs();

            File[] files = dir.listFiles();
            if (files != null) {
                List<String> names = Arrays.stream(files)
                        .map(File::getName)
                        .collect(Collectors.toList());
                for (String n : names) fileModel.addElement(n);
            }
        });
    }

    /** Delete selected file from server_files */
    private void deleteSelectedFile() {
        String selected = fileListUI.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a file to delete!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete file: " + selected + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        File f = new File("server_files/" + selected);
        if (f.exists()) {
            if (f.delete()) {
                log("Deleted file: " + selected);
                refreshFileList();
            } else {
                JOptionPane.showMessageDialog(this, "Cannot delete file!");
            }
        } else {
            JOptionPane.showMessageDialog(this, "File does not exist!");
        }
    }
}
