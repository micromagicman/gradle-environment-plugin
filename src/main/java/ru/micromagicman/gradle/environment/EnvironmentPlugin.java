package ru.micromagicman.gradle.environment;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.process.ProcessForkOptions;

import java.io.FileNotFoundException;

public class EnvironmentPlugin implements Plugin<Project> {

    @Override
    public void apply( final Project project ) {
        final TaskContainer tasks = project.getTasks();
        final EnvFile environmentFile;
        try {
            environmentFile = EnvFile.forProject( project );
        } catch ( FileNotFoundException e ) {
            throw new RuntimeException( e );
        }
        tasks.all( task -> {
            if ( task instanceof ProcessForkOptions processForkTask ) {
                environmentFile.applyForTask( processForkTask );
            }
        } );
    }
}
