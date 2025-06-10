package ftp.gusamyky.server.handler.command;

import java.io.IOException;

/**
 * Interface for FTP server commands.
 * This interface is implemented by all command classes that handle specific FTP
 * commands.
 */
public interface Command {
    /**
     * Executes the command.
     *
     * @throws IOException If an I/O error occurs during command execution
     */
    void execute() throws IOException;

    /**
     * Returns the name of the command.
     *
     * @return The command name
     */
    String getCommandName();
}