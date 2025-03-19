package ru.micromagicman.gradle.environment;

import lombok.extern.slf4j.Slf4j;
import org.gradle.api.Project;
import org.gradle.internal.impldep.org.eclipse.jgit.annotations.NonNull;
import org.gradle.process.ProcessForkOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
class EnvFile extends File {

    private static final String DEFAULT_FILE_NAME = ".env";

    private final Map<String, String> variables;

    EnvFile( final File parent, final String children ) {
        super( parent, children );
        this.variables = parseEnvironmentFile( this );
    }

    @NonNull
    Map<String, String> all() {
        return variables;
    }

    void applyForTask( final ProcessForkOptions processForkTask ) {
        variables.forEach( processForkTask::environment );
    }

    @NonNull
    static EnvFile forProject( final Project project ) throws FileNotFoundException {
        return new EnvFile( project.getProjectDir(), DEFAULT_FILE_NAME );
    }

    @NonNull
    static Map<String, String> parseEnvironmentFile( final File file ) {
        if ( !file.exists() ) {
            return Collections.emptyMap();
        }
        final Map<String, String> variables = new HashMap<>();
        try ( final BufferedReader reader = new BufferedReader( new FileReader( file ) ) ) {
            String line = reader.readLine();
            while ( null != line ) {
                if ( !line.isBlank() ) {
                    final String[] split = line.split( "=" );
                    if ( split.length >= 2 ) {
                        variables.put( split[0].trim(), split[1].trim() );
                    }
                }
                line = reader.readLine();
            }
            return Map.copyOf( variables );
        } catch ( IOException exception ) {
            log.error( "Error reading environment file {}", file, exception );
            return Collections.emptyMap();
        }
    }
}
