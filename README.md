# gradle-environment-plugin

## Overview
This Gradle plugin automatically loads environment variables from a `.env` file and applies them to all Gradle tasks that support process forking. It simplifies the management of environment variables across your project's build tasks.

## Features
- Automatically loads environment variables from a `.env` file
- Applies variables to all tasks implementing `ProcessForkOptions`
- Non-intrusive design that works with existing tasks
- Simple setup and configuration

## Installation

### Add Plugin to Your Project

#### Using plugins block (Gradle 2.1+)
```groovy
plugins {
    id 'ru.micromagicman.environment' version '0.1.0'
}
```

#### Using buildscript (for older Gradle versions)
```groovy
buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath 'ru.micromagicman:environment-gradle-plugin:1.0.0'
    }
}

apply plugin: 'ru.micromagicman.environment'
```

## Usage

1. **Create a `.env` file** in the root of your project.
2. **Define your environment variables** in the `.env` file, one per line:
    ```
    DB_HOST=localhost
    DB_PORT=5432
    API_KEY=your-api-key
    ```

3. **Apply the plugin** as described in the installation section.

## How It Works
The plugin scans all tasks in your project and applies the environment variables from the `.env` file to any task that implements `ProcessForkOptions`. This includes common tasks like:
- `JavaExec`
- `Test`
- Custom tasks extending `AbstractRunnableTask`

## Limitations
- Only supports tasks that implement `ProcessForkOptions`
- Does not modify environment variables for tasks that do not support forking

## Error Handling
The plugin logs errors if there are issues reading the `.env` file, ensuring that build failures are properly reported.

## License
MIT License(https://opensource.org/licenses/MIT)