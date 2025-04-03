package ru.micromagicman.gradle.environment;

import org.gradle.api.Project;
import org.gradle.api.tasks.JavaExec;
import org.gradle.internal.impldep.org.eclipse.jgit.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EnvironmentFileTest {

    @Test
    void testEnvironmentFileNotFound() {
        assertEquals(
                Collections.emptyMap(),
                envFileFromResources( "notFound.env" ).all()
        );
    }

    @Test
    void testEnvironmentFileReadError() throws IOException {
        final File tempDirectory = Files.createTempDirectory( "create-env" ).toFile();
        final File envFile = new File( tempDirectory, ".env" );
        assertTrue( envFile.createNewFile(), "Cannot create file" );
        assertTrue( envFile.setReadable( false ), "Cannot set read = false flag for file" );
        assertThrows(
                RuntimeException.class,
                () -> new EnvFile( tempDirectory, ".env" )
        );
    }

    @Test
    void testAll() {
        final EnvFile envFile = envFileFromResources( "sample1.env" );
        final Map<String, String> all = envFile.all();
        assertEquals(
                Map.of(
                        "A", "2",
                        "B", "false",
                        "SOME_STRING", "hello world!",
                        "variable", "value"
                ),
                all
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> all.put( "A", "3" )
        );
    }

    @Test
    void testApplyForTask() {
        final EnvFile envFile = envFileFromResources( "sample1.env" );
        final JavaExec javaExecTask = mock( JavaExec.class );
        envFile.applyForTask( javaExecTask );
        verify( javaExecTask, times( envFile.all().size() ) )
                .environment( anyString(), anyString() );
    }

    @Test
    void testForProjectOk() {
        final Project projectMock = mock( Project.class );
        when( projectMock.getProjectDir() )
                .thenReturn( new File( "src/test/resources" ) );
        assertDoesNotThrow( () -> EnvFile.forProject( projectMock ) );
    }

    @Test
    void testFlushSuccess() throws IOException {
        final File tempDirectory = Files.createTempDirectory( "flush-test" ).toFile();
        final EnvFile envFile = new EnvFile( tempDirectory, ".env" );
        envFile.put( "HELLO", "WORLD" );
        envFile.put( "THIS_IS_TRUE", true );
        envFile.put( "EMPTY", null );
        envFile.flush();
        TestUtils.assertFileContents(
                tempDirectory,
                ".env",
                """
                        HELLO=WORLD
                        THIS_IS_TRUE=true
                        EMPTY=
                        """
        );
    }

    @Test
    void testFlushWithError() throws IOException {
        final File tempDirectory = Files.createTempDirectory( "flush-error-test" ).toFile();
        final EnvFile envFile = new EnvFile( tempDirectory, ".env" );
        envFile.put( "HELLO", "WORLD" );
        assertTrue( tempDirectory.setWritable( false ), "Cannot set read-only dir" );
        assertThrows(
                RuntimeException.class,
                envFile::flush
        );
    }

    @NonNull
    private static EnvFile envFileFromResources( final String fileName ) {
        return new EnvFile( new File( "src/test/resources" ), fileName );
    }
}
