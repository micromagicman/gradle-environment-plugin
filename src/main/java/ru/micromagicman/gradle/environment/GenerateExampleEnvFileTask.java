package ru.micromagicman.gradle.environment;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

/**
 * Task generates example environment file based on original environment file (which is basically excluded from VCS).
 * Example environment file may be useful for information about environment variables, which your application need to.
 */
@Setter
@Getter
public class GenerateExampleEnvFileTask extends DefaultTask {

    private static final List<String> DEFAULT_EXCLUSION_KEY_PATTERNS = List.of(
            "password",
            "token"
    );

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
            final EnvFile target = new EnvFile( outputFile.getParentFile(), outputFile.getName() );
            target.mergeWith( EnvFile.forProject( getProject() ), key -> {
                for ( String pattern : DEFAULT_EXCLUSION_KEY_PATTERNS ) {
                    if ( key.toLowerCase().contains( pattern ) ) {
                        return false;
                    }
                }
                return true;
            } );
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
