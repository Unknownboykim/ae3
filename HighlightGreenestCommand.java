package uk.ac.nulondon;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

class HighlightGreenestCommand implements Command {
    private final Image image;
    private List<Pixel> seam;
    private List<Pixel> originalColors;

    public HighlightGreenestCommand(Image image) {
        this.image = image;
    }

    @Override
    public void execute() throws IOException {
        seam = image.getGreenestSeam();
        originalColors = image.highlightSeam(seam, Color.BLUE); // use BLUE not GREEN
    }

    @Override
    public void undo() throws IOException {
        // Restore original colors
        if (originalColors != null && !originalColors.isEmpty()) {
            for (int i = 0; i < seam.size(); i++) {
                Pixel seamPixel = seam.get(i);
                Pixel originalPixel = originalColors.get(i);

                // Replace the highlighted pixel with original color
                Pixel restoredPixel = new Pixel(originalPixel.color);
                restoredPixel.energy = seamPixel.energy;
                restoredPixel.left = seamPixel.left;
                restoredPixel.right = seamPixel.right;

                if (seamPixel.left != null) {
                    seamPixel.left.right = restoredPixel;
                }

                if (seamPixel.right != null) {
                    seamPixel.right.left = restoredPixel;
                }
            }
        }
    }
    public List<Pixel> getSeam() {
        return seam;
    }
}