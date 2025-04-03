package ru.micromagicman.gradle.environment;

import org.gradle.api.Project;
import org.gradle.api.tasks.JavaExec;
import org.gradle.internal.impldep.org.eclipse.jgit.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void testAll() {
        assertEquals(
                Map.of(
                        "A", "2",
                        "B", "false",
                        "SOME_STRING", "hello world!",
                        "variable", "value"
                ),
                envFileFromResources( "sample1.env" ).all()
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

    @NonNull
    private static EnvFile envFileFromResources( final String fileName ) {
        return new EnvFile( new File( "src/test/resources" ), fileName );
    }
}
