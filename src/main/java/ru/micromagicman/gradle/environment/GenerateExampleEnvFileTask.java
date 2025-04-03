package ru.micromagicman.gradle.environment;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class GenerateExampleEnvFileTask extends DefaultTask {

    @TaskAction
    void execute() {
        System.out.println( "This is generate example environment file task" );
    }
}
