package ru.micromagicman.gradle.environment;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Task generates example environment file based on original environment file (which is basically excluded from VCS).
 * Example environment file may be useful for information about environment variables, which your application need to.
 */
@Setter
@Getter
public class GenerateExampleEnvFileTask extends DefaultTask {

    /**
     * Output example environment file.
     */
    @OutputFile
    private File outputFile;

    @TaskAction
    void execute() {
        try {
            System.out.println( outputFile.getAbsolutePath() );
            if ( !createOutputFileIfDoesNotExists() ) {
                throw new RuntimeException( "Cannot create file " + outputFile.getName() );
            }
            try ( final OutputStream outputStream = new FileOutputStream( outputFile ) ) {
                outputStream.write( "".getBytes() );
            }
        } catch ( IOException exception ) {
            throw new RuntimeException( exception );
        }
    }

    private boolean createOutputFileIfDoesNotExists() throws IOException {
        if ( outputFile.exists() ) {
            return true;
        }
        final File directory = outputFile.getParentFile();
        if ( !directory.exists() && !directory.mkdirs() ) {
            return false;
        }
        return outputFile.createNewFile();
    }
}
