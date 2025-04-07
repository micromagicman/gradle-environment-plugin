package ru.micromagicman.gradle.environment;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static ru.micromagicman.gradle.environment.GenerateExampleEnvFileTask.OUTPUT_FILE_DEFAULT_FILENAME;

class GenerateExampleEnvFileTaskTest {

    private TestProject testProject;

    @BeforeEach
    void setUp() throws IOException {
        testProject = new TestProject( "gradle-environment-plugin-test" );
        createProjectEnvFile();
    }

    @Test
    void testCreateExampleEnvFileTaskWithOutputFileSpecified() throws IOException {
        testProject.addFile(
                "build.gradle",
                """
                        plugins {
                            id 'java'
                            id 'application'
                            id 'ru.micromagicman.environment'
                        }                
                        generateExampleEnvFile {
                            outputFile = file("$projectDir/.env.example")
                        }
                        """
        );
        final BuildResult result = GradleRunner.create()
                .withProjectDir( testProject.directory )
                .withArguments( "generateExampleEnvFile" )
                .withPluginClasspath()
                .build();
        final BuildTask task = result.task( ":generateExampleEnvFile" );
        assertNotNull( task );
        assertEquals( TaskOutcome.SUCCESS, task.getOutcome() );
        testProject.assertProjectFile(
                OUTPUT_FILE_DEFAULT_FILENAME,
                """
                        API_TOKEN=
                        OS_NAME=macos
                        MILLION=1000000                        
                        """
        );
    }

    @Test
    void testCreateExampleEnvFileTaskWithoutExcludeValuePatterns() throws IOException {
        testProject.addFile(
                "build.gradle",
                """
                        plugins {
                            id 'java'
                            id 'application'
                            id 'ru.micromagicman.environment'
                        }                
                        generateExampleEnvFile {
                            sensitiveValuePatterns = []
                            outputFile = file("$projectDir/.env.example")
                        }
                        """
        );
        final BuildResult result = GradleRunner.create()
                .withProjectDir( testProject.directory )
                .withArguments( "generateExampleEnvFile" )
                .withPluginClasspath()
                .build();
        final BuildTask task = result.task( ":generateExampleEnvFile" );
        assertNotNull( task );
        assertEquals( TaskOutcome.SUCCESS, task.getOutcome() );
        testProject.assertProjectFile(
                OUTPUT_FILE_DEFAULT_FILENAME,
                """
                        API_TOKEN=test-token
                        OS_NAME=macos
                        MILLION=1000000                        
                        """
        );
    }

    @Test
    void testCreateExampleEnvFileTaskWithCustomExcludeValuePatterns() throws IOException {
        testProject.addFile(
                "build.gradle",
                """
                        plugins {
                            id 'java'
                            id 'application'
                            id 'ru.micromagicman.environment'
                        }                
                        generateExampleEnvFile {
                            sensitiveValuePatterns = ['token', 'os', 'million']
                            outputFile = file("$projectDir/.env.example")
                        }
                        """
        );
        final BuildResult result = GradleRunner.create()
                .withProjectDir( testProject.directory )
                .withArguments( "generateExampleEnvFile" )
                .withPluginClasspath()
                .build();
        final BuildTask task = result.task( ":generateExampleEnvFile" );
        assertNotNull( task );
        assertEquals( TaskOutcome.SUCCESS, task.getOutcome() );
        testProject.assertProjectFile(
                OUTPUT_FILE_DEFAULT_FILENAME,
                """
                        API_TOKEN=
                        OS_NAME=
                        MILLION=                        
                        """
        );
    }

    @Test
    void testCreateExampleEnvFileTaskWithoutAnyTaskInputSpecified() throws IOException {
        testProject.addFile(
                "build.gradle",
                """
                        plugins {
                            id 'java'
                            id 'application'
                            id 'ru.micromagicman.environment'
                        }                
                        """
        );
        final BuildResult result = GradleRunner.create()
                .withProjectDir( testProject.directory )
                .withArguments( "generateExampleEnvFile" )
                .withPluginClasspath()
                .build();
        final BuildTask task = result.task( ":generateExampleEnvFile" );
        assertNotNull( task );
        assertEquals( TaskOutcome.SUCCESS, task.getOutcome() );
        testProject.assertProjectFile(
                OUTPUT_FILE_DEFAULT_FILENAME,
                """
                        API_TOKEN=
                        OS_NAME=macos
                        MILLION=1000000                        
                        """
        );
    }

    private void createProjectEnvFile() throws IOException {
        testProject.addFile(
                ".env",
                """
                        API_TOKEN=test-token
                        OS_NAME=macos
                        MILLION=1000000
                        """
        );
    }
}
