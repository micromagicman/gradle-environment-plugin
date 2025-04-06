package ru.micromagicman.gradle.environment;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A Gradle task that generates an example environment file based on the original environment file
 * (which is typically excluded from version control).
 * <p>
 * The example environment file serves as a template documenting the required environment variables
 * for the application, with sensitive values masked or excluded.
 */
@Setter
@Getter
public class GenerateExampleEnvFileTask extends DefaultTask {

    /**
     * A list of case-insensitive patterns used to identify environment variable names
     * whose values should be excluded from the generated example file.
     * <p>
     * By default, excludes variables containing "password" or "token" in their names.
     */
    @Input
    private List<String> excludeValuePatterns = List.of( "password", "token" );

    /**
     * The output file where the example environment configuration will be generated.
     * <p>
     * This file will contain all non-sensitive environment variables with their names
     * but without sensitive values.
     */
    @OutputFile
    private File outputFile;

    /**
     * Executes the task action to generate the example environment file.
     * <p>
     * The method:
     * <ol>
     *   <li>Creates the output file if it doesn't exist</li>
     *   <li>Merges the project's environment variables with the target file</li>
     *   <li>Excludes sensitive values based on {@code excludeValuePatterns}</li>
     *   <li>Writes the result to the output file</li>
     * </ol>
     *
     * @throws RuntimeException if file creation fails or an IO error occurs
     */
    @TaskAction
    void execute() {
        try {
            if ( !createOutputFileIfDoesNotExists() ) {
                throw new RuntimeException( "Cannot create file " + outputFile.getName() );
            }
            final EnvFile target = new EnvFile( outputFile.getParentFile(), outputFile.getName() );
            target.mergeWith( EnvFile.forProject( getProject() ), key -> {
                for ( String pattern : excludeValuePatterns ) {
                    if ( key.toLowerCase().contains( pattern ) ) {
                        return false;
                    }
                }
                return true;
            } );
            target.flush();
        } catch ( IOException exception ) {
            throw new RuntimeException( "Error create example environment file", exception );
        }
    }

    /**
     * Ensures the output file exists by creating it if necessary.
     *
     * @return {@code true} if the file exists or was successfully created,
     * {@code false} if the file could not be created
     * @throws IOException if an I/O error occurs during file creation
     */
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