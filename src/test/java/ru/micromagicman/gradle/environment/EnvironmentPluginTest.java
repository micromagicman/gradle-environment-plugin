package ru.micromagicman.gradle.environment;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EnvironmentPluginTest {

    private static final Pattern APPLICATION_OUTPUT_PATTERN =
            Pattern.compile( "Application started\n((?:[^\n]+\n)+)Application ended" );

    private TestProject testProject;

    @BeforeEach
    void setUp() throws IOException {
        testProject = new TestProject( "gradle-environment-plugin-test" );
    }

    @Test
    void testEnvironmentPluginWithoutEnvFile() throws IOException {
        testProject.addFile(
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
        testProject.addFile(
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
                .withProjectDir( testProject.directory )
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
        testProject.addFile(
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
        testProject.addFile(
                ".env",
                """
                        API_TOKEN=test-token
                        OS_NAME=macos
                        MILLION=1000000
                        """
        );
        testProject.addFile(
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
                .withProjectDir( testProject.directory )
                .withArguments( "run" )
                .withPluginClasspath()
                .build();
        BuildTask run = result.task( ":run" );
        assertNotNull( run );
        assertEquals( TaskOutcome.SUCCESS, run.getOutcome() );
        assertEnvironmentOutput( new String[]{ "test-token", "macos", "1000000" }, result );
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
}
