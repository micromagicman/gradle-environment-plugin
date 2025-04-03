package ru.micromagicman.gradle.environment;

import lombok.extern.slf4j.Slf4j;
import org.gradle.api.Project;
import org.gradle.internal.impldep.org.eclipse.jgit.annotations.NonNull;
import org.gradle.process.ProcessForkOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Environment file.
 * Consists of KEY=value lines.
 */
@Slf4j
class EnvFile extends File {

    private static final String DEFAULT_FILE_NAME = ".env";

    private static final String EMPTY_VALUE = "";

    private final Map<String, String> variables;

    EnvFile( final File parent, final String children ) {
        super( parent, children );
        this.variables = parseEnvironmentFile( this );
    }

    void put( final String name, final Object value ) {
        variables.put(
                Objects.requireNonNull( name, "" ),
                null != value ? value.toString() : EMPTY_VALUE
        );
    }

    @NonNull
    Map<String, String> all() {
        return Map.copyOf( variables );
    }

    void applyForTask( final ProcessForkOptions processForkTask ) {
        variables.forEach( processForkTask::environment );
    }

    void flush() {
        try ( final OutputStream outputStream = new FileOutputStream( this ) ) {
            for ( final Map.Entry<String, String> variable : variables.entrySet() ) {
                outputStream.write( ( variable.getKey() + "=" + variable.getValue() + "\n" ).getBytes() );
            }
        } catch ( IOException exception ) {
            throw new RuntimeException( "Error flushing environment file on disk", exception );
        }
    }

    @NonNull
    static EnvFile forProject( final Project project ) {
        return new EnvFile( project.getProjectDir(), DEFAULT_FILE_NAME );
    }

    @NonNull
    static Map<String, String> parseEnvironmentFile( final File file ) {
        final Map<String, String> variables = new LinkedHashMap<>();
        if ( !file.exists() ) {
            return variables;
        }
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
            return variables;
        } catch ( IOException exception ) {
            throw new RuntimeException( "Error parsing environment file", exception );
        }
    }
}
