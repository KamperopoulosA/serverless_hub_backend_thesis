package com.serverless.platformselector.service;

import com.serverless.platformselector.config.AppDeploymentProperties;
import com.serverless.platformselector.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeploymentArtifactServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void storeArtifactRejectsInvalidBase64() {
        AppDeploymentProperties properties = new AppDeploymentProperties();
        properties.setArtifactsRoot(tempDir.toString());

        DeploymentArtifactService service = new DeploymentArtifactService(properties);

        assertThrows(BadRequestException.class, () -> service.storeArtifact("not-base64", "bad.zip"));
    }

    @Test
    void extractZipRejectsPathTraversalEntries() throws IOException {
        AppDeploymentProperties properties = new AppDeploymentProperties();
        properties.setArtifactsRoot(tempDir.toString());

        DeploymentArtifactService service = new DeploymentArtifactService(properties);
        Path zipPath = tempDir.resolve("evil.zip");
        Files.write(zipPath, zipWithEntry("../evil.txt", "hello"));

        Path workspace = tempDir.resolve("workspace");
        Files.createDirectories(workspace);

        assertThrows(BadRequestException.class, () -> service.extractZip(zipPath, workspace));
    }

    @Test
    void truncateLogUsesConfiguredCap() {
        AppDeploymentProperties properties = new AppDeploymentProperties();
        properties.setArtifactsRoot(tempDir.toString());
        properties.setLogMaxBytes(5);

        DeploymentArtifactService service = new DeploymentArtifactService(properties);
        String truncated = service.truncateLog("123456789");

        assertTrue(truncated.startsWith("12345"));
        assertTrue(truncated.contains("[truncated]"));
    }

    private byte[] zipWithEntry(String entryName, String content) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            zipOutputStream.putNextEntry(new ZipEntry(entryName));
            zipOutputStream.write(content.getBytes());
            zipOutputStream.closeEntry();
        }
        return outputStream.toByteArray();
    }
}
