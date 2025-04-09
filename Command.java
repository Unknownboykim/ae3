package uk.ac.nulondon;

import java.io.IOException;

public interface Command {
    void execute() throws IOException;
    void undo() throws IOException;
}