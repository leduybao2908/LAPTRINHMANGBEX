package smtp.client;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

/**
 * Handles UDP-based video streaming for video calls
 */
public class VideoCallClient {
    private static final int MAX_PACKET_SIZE = 65000;
    private static final Dimension VIDEO_SIZE = WebcamResolution.VGA.getSize();
    
    private Webcam webcam;
    private Webcam selectedWebcam;
    private DatagramSocket socket;
    private boolean isRunning = false;
    private Thread sendThread;
    private Thread receiveThread;
    
    private String remoteHost;
    private int remotePort;
    private int localPort;
    
    private Consumer<BufferedImage> onFrameReceived;
    private Consumer<BufferedImage> onLocalFrameCaptured;
    
    /**
     * Creates a video call client
     * @param localPort Port to receive video on
     * @param remoteHost Remote host to send video to
     * @param remotePort Remote port to send video to
     */
    public VideoCallClient(int localPort, String remoteHost, int remotePort) {
        this.localPort = localPort;
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }
    
    /**
     * Sets the callback for when a frame is received
     */
    public void setOnFrameReceived(Consumer<BufferedImage> callback) {
        this.onFrameReceived = callback;
    }
    
    /**
     * Sets the callback for when a local frame is captured
     */
    public void setOnLocalFrameCaptured(Consumer<BufferedImage> callback) {
        this.onLocalFrameCaptured = callback;
    }
    
    /**
     * Sets the webcam to use for the call
     */
    public void setWebcam(Webcam webcam) {
        this.selectedWebcam = webcam;
    }
    
    /**
     * Starts the video call
     */
    public void startCall() throws Exception {
        if (isRunning) {
            return;
        }
        
        // Initialize webcam
        webcam = selectedWebcam != null ? selectedWebcam : Webcam.getDefault();
        if (webcam == null) {
            throw new Exception("No webcam detected");
        }
        
        // Close if already open from previous call
        if (webcam.isOpen()) {
            webcam.close();
            Thread.sleep(500); // Wait for webcam to fully release
        }
        
        webcam.setViewSize(VIDEO_SIZE);
        webcam.open();
        
        // Initialize UDP socket
        socket = new DatagramSocket(localPort);
        
        isRunning = true;
        
        // Start sending thread
        sendThread = new Thread(this::sendVideoFrames);
        sendThread.setDaemon(true);
        sendThread.start();
        
        // Start receiving thread
        receiveThread = new Thread(this::receiveVideoFrames);
        receiveThread.setDaemon(true);
        receiveThread.start();
    }
    
    /**
     * Stops the video call
     */
    public void stopCall() {
        isRunning = false;
        
        if (sendThread != null) {
            sendThread.interrupt();
            try {
                sendThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        if (receiveThread != null) {
            receiveThread.interrupt();
            try {
                receiveThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        if (webcam != null) {
            try {
                if (webcam.isOpen()) {
                    webcam.close();
                }
            } catch (Exception e) {
                // Ignore close errors
            }
        }
        
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
    
    /**
     * Continuously captures and sends video frames
     */
    private void sendVideoFrames() {
        try {
            InetAddress remoteAddress = InetAddress.getByName(remoteHost);
            
            while (isRunning && !Thread.interrupted()) {
                if (webcam.isOpen()) {
                    BufferedImage image = webcam.getImage();
                    
                    if (image != null) {
                        // Notify local frame callback
                        if (onLocalFrameCaptured != null) {
                            onLocalFrameCaptured.accept(image);
                        }
                        
                        // Compress image to JPEG
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(image, "jpg", baos);
                        byte[] imageData = baos.toByteArray();
                        
                        // Send image data in chunks if necessary
                        if (imageData.length <= MAX_PACKET_SIZE) {
                            DatagramPacket packet = new DatagramPacket(
                                imageData, 
                                imageData.length, 
                                remoteAddress, 
                                remotePort
                            );
                            socket.send(packet);
                        } else {
                            // For large images, send in multiple packets
                            sendLargeImage(imageData, remoteAddress);
                        }
                    }
                }
                
                // Limit to ~15 FPS to reduce bandwidth
                Thread.sleep(66);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Sends large images by splitting into multiple packets
     */
    private void sendLargeImage(byte[] imageData, InetAddress remoteAddress) throws IOException {
        int offset = 0;
        int packetNumber = 0;
        int totalPackets = (int) Math.ceil((double) imageData.length / MAX_PACKET_SIZE);
        
        while (offset < imageData.length) {
            int length = Math.min(MAX_PACKET_SIZE - 8, imageData.length - offset);
            
            // Create packet with header: [packetNumber(4 bytes)][totalPackets(4 bytes)][data]
            byte[] packetData = new byte[length + 8];
            packetData[0] = (byte) (packetNumber >> 24);
            packetData[1] = (byte) (packetNumber >> 16);
            packetData[2] = (byte) (packetNumber >> 8);
            packetData[3] = (byte) packetNumber;
            packetData[4] = (byte) (totalPackets >> 24);
            packetData[5] = (byte) (totalPackets >> 16);
            packetData[6] = (byte) (totalPackets >> 8);
            packetData[7] = (byte) totalPackets;
            
            System.arraycopy(imageData, offset, packetData, 8, length);
            
            DatagramPacket packet = new DatagramPacket(
                packetData, 
                packetData.length, 
                remoteAddress, 
                remotePort
            );
            socket.send(packet);
            
            offset += length;
            packetNumber++;
        }
    }
    
    /**
     * Continuously receives video frames
     */
    private void receiveVideoFrames() {
        try {
            byte[] buffer = new byte[MAX_PACKET_SIZE];
            
            while (isRunning && !Thread.interrupted()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                byte[] data = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), 0, data, 0, packet.getLength());
                
                // Try to decode as single-packet image
                try {
                    ByteArrayInputStream bais = new ByteArrayInputStream(data);
                    BufferedImage image = ImageIO.read(bais);
                    
                    if (image != null && onFrameReceived != null) {
                        onFrameReceived.accept(image);
                    }
                } catch (Exception e) {
                    // Ignore corrupted frames
                }
            }
        } catch (Exception e) {
            if (isRunning) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Checks if webcam is available
     */
    public static boolean isWebcamAvailable() {
        Webcam webcam = Webcam.getDefault();
        return webcam != null;
    }
    
    /**
     * Gets list of available webcams
     */
    public static java.util.List<Webcam> getWebcams() {
        return Webcam.getWebcams();
    }
    
    /**
     * Checks if call is active
     */
    public boolean isActive() {
        return isRunning;
    }
}
