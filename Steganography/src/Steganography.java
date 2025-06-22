import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Steganography extends JFrame {

    private final String inputImagePath = "D:\\\\pexels-mariannaole-757889.png";
    private final String outputImagePath = "D:\\\\output-stego.png";

    public Steganography() {
        setTitle("Steganography Tool BY Shaik Ali Azgar");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Steganography", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        add(titleLabel, BorderLayout.NORTH);

        JTextArea messageArea = new JTextArea(5, 30);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);

        JButton encodeButton = new JButton("Encode Message");
        JButton decodeButton = new JButton("Decode Message");

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(encodeButton);
        buttonPanel.add(decodeButton);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        encodeButton.addActionListener(e -> {
            String message = messageArea.getText().trim();
            if (message.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a message to encode.");
                return;
            }
            try {
                encode(inputImagePath, outputImagePath, message);
                JOptionPane.showMessageDialog(this, "Successfully encoded.");
                messageArea.setText("");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        decodeButton.addActionListener(e -> {
            try {
                String decoded = decode(outputImagePath);
                String imageName = new File(outputImagePath).getName();
                messageArea.setText("Decoded message is:\n" + decoded + "\n\nImage name is: " + imageName);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        setVisible(true);
    }

    public void encode(String inputPath, String outputPath, String message) throws IOException {
        BufferedImage image = ImageIO.read(new File(inputPath));
        message += "EOF";
        byte[] msgBytes = message.getBytes();
        int msgIndex = 0, bitIndex = 0;

        outer:
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;

                if (msgIndex < msgBytes.length) {
                    int bit = (msgBytes[msgIndex] >> (7 - bitIndex)) & 1;
                    blue = (blue & 0xFE) | bit;
                    bitIndex++;
                    if (bitIndex == 8) {
                        bitIndex = 0;
                        msgIndex++;
                    }
                } else break outer;

                int newPixel = (red << 16) | (green << 8) | blue;
                image.setRGB(x, y, newPixel);
            }
        }

        ImageIO.write(image, "png", new File(outputPath));
    }

    public String decode(String imagePath) throws IOException {
        BufferedImage image = ImageIO.read(new File(imagePath));
        StringBuilder message = new StringBuilder();
        int byteVal = 0, bitCount = 0;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int blue = image.getRGB(x, y) & 0xFF;
                byteVal = (byteVal << 1) | (blue & 1);
                bitCount++;

                if (bitCount == 8) {
                    char c = (char) byteVal;
                    message.append(c);
                    if (message.toString().endsWith("EOF")) {
                        return message.substring(0, message.length() - 3);
                    }
                    bitCount = 0;
                    byteVal = 0;
                }
            }
        }

        return "No message found!";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Steganography::new);
    }
}
