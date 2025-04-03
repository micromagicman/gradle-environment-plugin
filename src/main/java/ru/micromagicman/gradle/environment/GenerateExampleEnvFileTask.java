package ru.micromagicman.gradle.environment;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.Map;

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
            if ( !createOutputFileIfDoesNotExists() ) {
                throw new RuntimeException( "Cannot create file " + outputFile.getName() );
            }
            final EnvFile source = EnvFile.forProject( getProject() );
            final EnvFile target = new EnvFile( outputFile.getParentFile(), outputFile.getName() );
            final Map<String, String> sourceRecords = source.all();
            for ( final String entry : sourceRecords.keySet() ) {
                target.put( entry, sourceRecords.get( entry ) );
            }
            target.flush();
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
