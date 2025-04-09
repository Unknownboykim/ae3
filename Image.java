package uk.ac.nulondon;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class Image {
    final List<Pixel> rows;

    private int width;
    private final int height;


    public Image(BufferedImage img) {
        width = img.getWidth();
        height = img.getHeight();
        rows = new ArrayList<>();
        Pixel current = null;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Pixel pixel = new Pixel(img.getRGB(col, row));
                if (col == 0) {
                    rows.add(pixel);
                } else {
                    current.right = pixel;
                    pixel.left = current;
                }
                current = pixel;
            }
        }
    }

    public BufferedImage toBufferedImage() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int row = 0; row < height; row++) {
            Pixel pixel = rows.get(row);
            int col = 0;
            while (pixel != null) {
                image.setRGB(col++, row, pixel.color.getRGB()); // 👈 Uses updated color
                pixel = pixel.right;
            }
        }
        return image;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    double energy(Pixel above, Pixel current, Pixel below) {
        //TODO: Calculate energy based on neighbours of the current pixel
        // For edge pixels, we use brightness as mentioned in the assessment brief (note 2)
        if (isEdgePixel(current)) {
            return current.brightness();
        }

        // Get all neighbors
        Pixel topLeft = getAboveLeft(above);
        Pixel topRight = getAboveRight(above);
        Pixel left = current.left;
        Pixel right = current.right;
        Pixel bottomLeft = getBelowLeft(below);
        Pixel bottomRight = getBelowRight(below);

        // Calculate horizontal energy component using the formula from the brief
        double horizEnergy = 0;
        if (topLeft != null && left != null && bottomLeft != null) {
            horizEnergy = (topLeft.brightness() + 2 * left.brightness() + bottomLeft.brightness()) -
                    (topRight.brightness() + 2 * right.brightness() + bottomRight.brightness());
        }

        // Calculate vertical energy component using the formula from the brief
        double vertEnergy = 0;
        if (topLeft != null && topRight != null && bottomLeft != null && bottomRight != null) {
            vertEnergy = (topLeft.brightness() + 2 * above.brightness() + topRight.brightness()) -
                    (bottomLeft.brightness() + 2 * below.brightness() + bottomRight.brightness());
        }

        // Return total energy (magnitude of the gradient)
        return Math.sqrt(horizEnergy * horizEnergy + vertEnergy * vertEnergy);
    }

    private boolean isEdgePixel(Pixel pixel) {
        // Check if this is a pixel on the image boundary
        int row = getRowOf(pixel);
        int col = getColumnOf(pixel, row);

        return row == 0 || row == height - 1 || col == 0 || col == width - 1;
    }

    private Pixel getAboveLeft(Pixel above) {
        return above != null ? above.left : null;
    }

    private Pixel getAboveRight(Pixel above) {
        return above != null ? above.right : null;
    }

    private Pixel getBelowLeft(Pixel below) {
        return below != null ? below.left : null;
    }

    private Pixel getBelowRight(Pixel below) {
        return below != null ? below.right : null;
    }

    int getRowOf(Pixel pixel) {
        for (int row = 0; row < height; row++) {
            Pixel current = rows.get(row);
            while (current != null) {
                if (current == pixel) {
                    return row;
                }
                current = current.right;
            }
        }
        return -1;
    }

    public void calculateEnergy() {
        for (int row = 0; row < height; row++) {
            Pixel current = rows.get(row);
            Pixel above = (row > 0) ? rows.get(row - 1) : null;
            Pixel below = (row < height - 1) ? rows.get(row + 1) : null;

            Pixel a = above;
            Pixel b = below;

            while (current != null) {
                current.energy = energy(a, current, b);
                if (a != null) a = a.right;
                if (b != null) b = b.right;
                current = current.right;
            }
        }
    }


    public List<Pixel> highlightSeam(List<Pixel> seam, Color color) {
        List<Pixel> originalColors = new ArrayList<>();

        if (seam == null) {
            return originalColors;
        }

        for (Pixel pixel : seam) {
            originalColors.add(new Pixel(pixel.color));
            // Create a pixel with the highlight color
            Pixel highlightedPixel = new Pixel(color);
            highlightedPixel.energy = pixel.energy;

            // Maintain connections
            highlightedPixel.left = pixel.left;
            highlightedPixel.right = pixel.right;

            if (pixel.left != null) {
                pixel.left.right = highlightedPixel;
            }

            if (pixel.right != null) {
                pixel.right.left = highlightedPixel;
            }

            // Update the row start if necessary
            int row = getRowOf(pixel);
            if (row >= 0 && rows.get(row) == pixel) {
                rows.set(row, highlightedPixel);
            }
        }

        return originalColors;
    }

    public int getColumnOf(Pixel pixel, int row) {
        Pixel current = rows.get(row);
        int col = 0;
        while (current != null && current != pixel) {
            current = current.right;
            col++;
        }
        return (current != null) ? col : -1;
    }



    public void removeSeam(List<Pixel> seam) {
        for (Pixel pixel : seam) {
            if (pixel.left != null) {
                pixel.left.right = pixel.right;
            } else {
                int rowIndex = rows.indexOf(pixel);
                rows.set(rowIndex, pixel.right);
            }
            if (pixel.right != null) {
                pixel.right.left = pixel.left;
            }
        }
        width--;
    }


    public void addSeam(List<Pixel> seam) {
        for (Pixel pixel : seam) {
            Pixel left = pixel.left;
            Pixel right = pixel.right;

            if (left != null) {
                left.right = pixel;
            } else {
                int rowIndex = rows.indexOf(right); // insert at row head
                rows.set(rowIndex, pixel);
            }

            if (right != null) {
                right.left = pixel;
            }
        }
        width++;
    }


    private List<Pixel> getSeamMaximizing(Function<Pixel, Double> valueGetter) {
        List<Pixel[]> dp = new ArrayList<>();

        for (int row = 0; row < height; row++) {
            Pixel[] rowPixels = new Pixel[width];
            Pixel current = rows.get(row);
            for (int col = 0; col < width; col++) {
                rowPixels[col] = current;
                current = current.right;
            }
            dp.add(rowPixels);
        }

        double[][] values = new double[height][width];
        int[][] backtrack = new int[height][width];

        // Initialize first row
        for (int col = 0; col < width; col++) {
            values[0][col] = valueGetter.apply(dp.getFirst()[col]);
        }

        // DP populate
        for (int row = 1; row < height; row++) {
            for (int col = 0; col < width; col++) {
                double best = values[row - 1][col];
                int from = col;

                if (col > 0 && values[row - 1][col - 1] > best) {
                    best = values[row - 1][col - 1];
                    from = col - 1;
                }

                if (col < width - 1 && values[row - 1][col + 1] > best) {
                    best = values[row - 1][col + 1];
                    from = col + 1;
                }

                values[row][col] = best + valueGetter.apply(dp.get(row)[col]);
                backtrack[row][col] = from;
            }
        }

        // Find best last column
        double max = Double.NEGATIVE_INFINITY;
        int col = 0;
        for (int c = 0; c < width; c++) {
            if (values[height - 1][c] > max) {
                max = values[height - 1][c];
                col = c;
            }
        }

        // Backtrack
        List<Pixel> seam = new LinkedList<>();
        for (int row = height - 1; row >= 0; row--) {
            seam.addFirst(dp.get(row)[col]);
            col = backtrack[row][col];
        }

        return seam;
    }


    public List<Pixel> getGreenestSeam() {
        return getSeamMaximizing(Pixel::getGreen);
        /*Or, since we haven't lectured on lambda syntax in Java, this can be
        return getSeamMaximizing(new Function<Pixel, Double>() {
            @Override
            public Double apply(Pixel pixel) {
                return pixel.getGreen();
            }
        });*/

    }

    public List<Pixel> getLowestEnergySeam() {
        calculateEnergy();
        /*
        Maximizing negation of energy is the same as minimizing the energy.
         */
        return getSeamMaximizing((pixel) -> -pixel.energy);

        /*Or, since we haven't lectured on lambda syntax in Java, this can be
        return getSeamMaximizing(new Function<Pixel, Double>() {
            @Override
            public Double apply(Pixel pixel) {
                return -pixel.energy;
            }
        });
        */
    }
}
