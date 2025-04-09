package uk.ac.nulondon;

import java.awt.*;

public class Pixel {
    Pixel left;
    Pixel right;

    double energy;

    Color color;
    final Color originalColor; // Keep track of original color

    public Pixel(int rgb) {
        this.color = new Color(rgb);
        this.originalColor = this.color;
    }

    public Pixel(Color color) {
        this.color = color;
        this.originalColor = color;
    }

    public double brightness() {
        return (color.getRed() * 0.299 + color.getGreen() * 0.587 + color.getBlue() * 0.114);
    }

    public double getGreen() {
        return color.getGreen();
    }

    public void resetColor() {
        this.color = originalColor;
    }
}
