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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Represents an environment configuration file (.env) containing key-value pairs.
 * <p>The file format follows standard .env conventions with one variable per line.
 */
@Slf4j
class EnvFile extends File {

    private static final String DEFAULT_FILE_NAME = ".env";
    private static final String EMPTY_VALUE = "";
    private final Map<String, String> variables;

    /**
     * Creates a new environment file instance.
     */
    EnvFile( final File parent, final String children ) {
        super( parent, children );
        this.variables = parseEnvironmentFile( this );
    }

    /**
     * Merges this environment file with another, applying a key-filter predicate.
     */
    void mergeWith( final EnvFile other, final Predicate<String> keyPredicate ) {
        for ( final Map.Entry<String, String> entry : other.variables.entrySet() ) {
            final String key = entry.getKey();
            final String value = keyPredicate.test( key ) ? entry.getValue() : null;
            put( key, value );
        }
    }

    /**
     * Adds or updates an environment variable.
     */
    void put( final String name, final Object value ) {
        variables.put(
                Objects.requireNonNull( name, "Key cannot be null" ),
                null != value ? value.toString() : EMPTY_VALUE
        );
    }

    /**
     * Returns an unmodifiable view of all environment variables.
     */
    @NonNull
    Map<String, String> all() {
        return Collections.unmodifiableMap( variables );
    }

    /**
     * Applies all environment variables to a Gradle task.
     */
    void applyForTask( final ProcessForkOptions processForkTask ) {
        variables.forEach( processForkTask::environment );
    }

    /**
     * Writes all environment variables to disk in KEY=value format.
     */
    void flush() {
        try ( final OutputStream outputStream = new FileOutputStream( this ) ) {
            for ( final Map.Entry<String, String> variable : variables.entrySet() ) {
                outputStream.write( ( variable.getKey() + "=" + variable.getValue() + "\n" ).getBytes() );
            }
        } catch ( IOException exception ) {
            throw new RuntimeException( "Error flushing environment file on disk", exception );
        }
    }

    /**
     * Creates an EnvFile instance for the given project's default location.
     */
    @NonNull
    static EnvFile forProject( final Project project ) {
        return new EnvFile( project.getProjectDir(), DEFAULT_FILE_NAME );
    }

    /**
     * Parses an environment file into a map of key-value pairs.
     * <p>
     * Each non-empty line should contain KEY=value format.
     * Blank lines and lines without '=' are ignored.
     */
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