package uk.ac.nulondon;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class ImageEditor {

    private Image image;

    private List<Pixel> highlightedSeam = null;

    public void load(String filePath) throws IOException {
        File originalFile = new File(filePath);
        BufferedImage img = ImageIO.read(originalFile);
        image = new Image(img);
    }

    public void save(String filePath) throws IOException {
        BufferedImage img = image.toBufferedImage();
        ImageIO.write(img, "png", new File(filePath));
    }

    private final Deque<Command> history = new ArrayDeque<>();



    public void highlightGreenest() throws IOException {
        HighlightGreenestCommand cmd = new HighlightGreenestCommand(image);
        cmd.execute();
        history.push(cmd);
        // Grab the seam from the command for removal
        highlightedSeam = cmd.getSeam(); // Add this getter method
    }

        public void highlightLowestEnergySeam() throws IOException {
            HighlightLowestEnergyCommand cmd = new HighlightLowestEnergyCommand(image);
            cmd.execute();
            history.push(cmd);
            highlightedSeam = cmd.getSeam();

            // Save intermediate result
            save("target/highlighted_energy.png");
        }

    public void removeHighlighted() throws IOException {
        if (highlightedSeam == null) return;
        Command cmd = new RemoveSeamCommand(image, highlightedSeam);
        cmd.execute();
        history.push(cmd);
        highlightedSeam = null;
    }


    public void undo() throws IOException {
        if (!history.isEmpty()) {
            Command cmd = history.pop();
            cmd.undo();
        }
    }

}