package ru.micromagicman.gradle.environment;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.process.ProcessForkOptions;

/**
 * Environment plugin implementation.<br/>
 * This plugin attaches all variables from project .env file to all gradle task,
 * which implements {@link ProcessForkOptions process fork functionality}.
 *
 * @author micromagicman
 */
public class EnvironmentPlugin implements Plugin<Project> {

    @Override
    public void apply( final Project project ) {
        final TaskContainer tasks = project.getTasks();
        final EnvFile environmentFile;
        environmentFile = EnvFile.forProject( project );
        tasks.all( task -> {
            if ( task instanceof ProcessForkOptions processForkTask ) {
                environmentFile.applyForTask( processForkTask );
            }
        } );
        tasks.register( "generateExampleEnvFile", GenerateExampleEnvFileTask.class );
    }
}
