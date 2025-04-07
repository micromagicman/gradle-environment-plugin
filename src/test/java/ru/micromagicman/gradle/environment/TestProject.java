package ru.micromagicman.gradle.environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestProject {

    final File directory;

    TestProject( final String dirname ) throws IOException {
        this.directory = Files.createTempDirectory( dirname ).toFile();
    }

    void addFile( final String fileName, final String content ) throws IOException {
        final File buildFile = new File( directory, fileName );
        final File parentDir = buildFile.getParentFile();
        if ( !parentDir.exists() && !parentDir.mkdirs() ) {
            throw new IOException( "Can't create parent directory: " + parentDir );
        }
        try ( final OutputStream outputStream = new FileOutputStream( buildFile ) ) {
            outputStream.write( content.getBytes() );
        }
    }

    void assertProjectFile(
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
