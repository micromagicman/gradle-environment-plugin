package ru.micromagicman.gradle.environment;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EnvironmentPluginTest {

    private static final Pattern APPLICATION_OUTPUT_PATTERN =
            Pattern.compile( "Application started\n((?:[^\n]+\n)+)Application ended" );

    private File projectDir;

    @BeforeEach
    void setUp() throws IOException {
        projectDir = Files.createTempDirectory( "gradle-environment-plugin-test" ).toFile();
    }

    @Test
    void testEnvironmentPluginWithoutEnvFile() throws IOException {
        createFile(
                "build.gradle",
                """
                        plugins {
                            id 'java'
                            id 'application'
                            id 'ru.micromagicman.environment'
                        }
                        application {
                            mainClass = 'Application'
                        }
                        """
        );
        createFile(
                "src/main/java/Application.java",
                """
                        class Application {
                            public static void main(final String[] args) {
                                System.out.println("Application started");
                                System.out.println(System.getenv("API_TOKEN"));
                                System.out.println(System.getenv("OS_NAME"));
                                System.out.println(System.getenv("MILLION"));
                                System.out.println("Application ended");
                            }
                        }
                        """
        );
        final BuildResult result = GradleRunner.create()
                .withProjectDir( projectDir )
                .withArguments( "run" )
                .withPluginClasspath()
                .build();
        final BuildTask runTask = result.task( ":run" );
        assertNotNull( runTask );
        assertEquals( TaskOutcome.SUCCESS, runTask.getOutcome() );
        assertEnvironmentOutput( new String[]{ "null", "null", "null" }, result );
    }

    @Test
    void testEnvironmentPlugin() throws IOException {
        createFile(
                "build.gradle",
                """
                        plugins {
                            id 'java'
                            id 'application'
                            id 'ru.micromagicman.environment'
                        }
                        application {
                            mainClass = 'Application'
                        }
                        """
        );
        createFile(
                ".env",
                """
                        API_TOKEN=test-token
                        OS_NAME=macos
                        MILLION=1000000
                        """
        );
        createFile(
                "src/main/java/Application.java",
                """
                        class Application {
                            public static void main(final String[] args) {
                                System.out.println("Application started");
                                System.out.println(System.getenv("API_TOKEN"));
                                System.out.println(System.getenv("OS_NAME"));
                                System.out.println(System.getenv("MILLION"));
                                System.out.println("Application ended");
                            }
                        }
                        """
        );
        final BuildResult result = GradleRunner.create()
                .withProjectDir( projectDir )
                .withArguments( "run" )
                .withPluginClasspath()
                .build();
        BuildTask run = result.task( ":run" );
        assertNotNull( run );
        assertEquals( TaskOutcome.SUCCESS, run.getOutcome() );
        assertEnvironmentOutput( new String[]{ "test-token", "macos", "1000000" }, result );
    }

    @Test
    void testCreateExampleEnvFileTask() throws IOException {
        createFile(
                "build.gradle",
                """
                        plugins {
                            id 'java'
                            id 'application'
                            id 'ru.micromagicman.environment'
                        }
                        """
        );
        createFile(
                ".env",
                """
                        API_TOKEN=test-token
                        OS_NAME=macos
                        MILLION=1000000
                        """
        );
        final BuildResult result = GradleRunner.create()
                .withProjectDir( projectDir )
                .withArguments( "generateExampleEnvFile" )
                .withPluginClasspath()
                .build();
        final BuildTask task = result.task( ":generateExampleEnvFile" );
        assertNotNull( task );
        assertEquals( TaskOutcome.SUCCESS, task.getOutcome() );
    }

    private void assertEnvironmentOutput( final String[] expectedOutput, final BuildResult result ) {
        final String output = result.getOutput();
        final Matcher matcher = APPLICATION_OUTPUT_PATTERN.matcher( output );
        if ( !matcher.find() ) {
            throw new AssertionError( "Expected output not found" );
        }
        final String environmentOutput = matcher.group( 1 ).trim();
        assertNotNull( environmentOutput, "Expected output not found" );
        assertArrayEquals( expectedOutput, environmentOutput.split( "\n" ) );
    }

    private void createFile( final String fileName, final String content ) throws IOException {
        final File buildFile = new File( projectDir, fileName );
        final File parentDir = buildFile.getParentFile();
        if ( !parentDir.exists() && !parentDir.mkdirs() ) {
            throw new IOException( "Can't create parent directory: " + parentDir );
        }
        try ( final OutputStream outputStream = new FileOutputStream( buildFile ) ) {
            outputStream.write( content.getBytes() );
        }
    }
}
