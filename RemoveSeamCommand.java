package uk.ac.nulondon;

import uk.ac.nulondon.Command;
import uk.ac.nulondon.Image;
import uk.ac.nulondon.Pixel;

import java.io.IOException;
import java.util.List;

class RemoveSeamCommand implements Command {
    private final Image image;
    private final List<Pixel> seam;
    private final List<Pixel> removedSeam;

    public RemoveSeamCommand(Image image, List<Pixel> seam) {
        this.image = image;
        this.seam = seam;
        this.removedSeam = seam;
    }

    @Override
    public void execute() throws IOException {
        // Save a copy of the seam before removal for potential undo
        image.removeSeam(seam);
    }

    @Override
    public void undo() throws IOException {
        image.addSeam(removedSeam);
    }
}