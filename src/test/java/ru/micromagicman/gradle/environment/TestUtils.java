package ru.micromagicman.gradle.environment;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@UtilityClass
class TestUtils {

    static void assertFileContents(
            final File directory,
            final String expectedFileName,
            final String expectedFileContent ) throws IOException {
        final File[] found = directory.listFiles( file -> Objects.equals( expectedFileName, file.getName() ) );
        assertNotNull( found );
        assertEquals( 1, found.length, "Expected file not found: " + expectedFileName );
        try ( final InputStream inputStream = new FileInputStream( found[0] ) ) {
            assertEquals( expectedFileContent, new String( inputStream.readAllBytes() ) );
        }
    }
}
