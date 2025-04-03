package ru.micromagicman.gradle.environment;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GenerateExampleEnvFileTaskTest extends EnvironmentPluginTest {

    @Test
    void testCreateExampleEnvFileTaskWithOutputFileSpecified() throws IOException {
        createProjectFile(
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
        createProjectFile(
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
        assertProjectFile(
                ".env.example",
                ""
        );
    }
}
