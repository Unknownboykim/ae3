package uk.ac.nulondon;

import java.awt.*;
import java.io.IOException;
import java.util.List;

/**
 * Command to highlight the lowest-energy seam in an image.
 * Supports undo by restoring the original seam colors.
 */
public class HighlightLowestEnergyCommand implements Command {
    private final Image image;
    private List<Pixel> seam;
    private List<Pixel> originalColors;

    public HighlightLowestEnergyCommand(Image image) {
        this.image = image;
    }

    @Override
    public void execute() throws IOException {
        seam = image.getLowestEnergySeam();
        originalColors = image.highlightSeam(seam, Color.RED);
    }



    @Override
    public void undo() throws IOException {
        if (seam == null || originalColors == null || seam.isEmpty()) return;

        for (int i = 0; i < seam.size(); i++) {
            Pixel seamPixel = seam.get(i);
            Pixel originalPixel = originalColors.get(i);

            Pixel restored = new Pixel(originalPixel.color);
            restored.energy = seamPixel.energy;
            restored.left = seamPixel.left;
            restored.right = seamPixel.right;

            if (seamPixel.left != null) {
                seamPixel.left.right = restored;
            }

            if (seamPixel.right != null) {
                seamPixel.right.left = restored;
            }

            // Optional: update image row reference if needed
            int row = image.getRowOf(seamPixel);
            if (row >= 0 && image.rows.get(row) == seamPixel) {
                image.rows.set(row, restored);
            }
        }
    }

    /**
     * For ApprovalTests — returns the seam pixels after execution.
     */
    public List<Pixel> getSeam() {
        return seam;
    }
}
