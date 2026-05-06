package com.serverless.platformselector.service;

import com.serverless.platformselector.config.AppDeploymentProperties;
import com.serverless.platformselector.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class DeploymentArtifactService {

    private final AppDeploymentProperties properties;

    public DeploymentArtifactService(AppDeploymentProperties properties) {
        this.properties = properties;
    }

    public Path storeArtifact(String base64Zip, String artifactFileName) {
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(base64Zip);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Function package must be valid Base64 ZIP content");
        }

        if (decoded.length == 0) {
            throw new BadRequestException("Function package cannot be empty");
        }

        if (decoded.length > properties.getMaxArtifactSizeBytes()) {
            throw new BadRequestException("Function package exceeds the maximum allowed size");
        }

        try {
            Path uploadsRoot = resolveArtifactsRoot().resolve("uploads");
            Files.createDirectories(uploadsRoot);
            Path artifactPath = uploadsRoot.resolve(artifactFileName);
            Files.write(artifactPath, decoded);
            return artifactPath;
        } catch (IOException ex) {
            throw new BadRequestException("Failed to store deployment artifact");
        }
    }

    public Path createWorkspace(String workspaceName) {
        try {
            Path workspaceRoot = resolveArtifactsRoot().resolve("workspaces").resolve(workspaceName);
            Files.createDirectories(workspaceRoot);
            return workspaceRoot;
        } catch (IOException ex) {
            throw new BadRequestException("Failed to create deployment workspace");
        }
    }

    public void extractZip(Path artifactPath, Path targetDirectory) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(artifactPath))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    zipInputStream.closeEntry();
                    continue;
                }

                String safeEntryName = entry.getName().replace("\\", "/");
                while (safeEntryName.startsWith("/")) {
                    safeEntryName = safeEntryName.substring(1);
                }

                Path targetFile = targetDirectory.resolve(safeEntryName).normalize();
                if (!targetFile.startsWith(targetDirectory.normalize())) {
                    throw new BadRequestException("ZIP archive contains invalid entry paths");
                }

                Path parent = targetFile.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }

                try (OutputStream outputStream = Files.newOutputStream(targetFile)) {
                    zipInputStream.transferTo(outputStream);
                }

                zipInputStream.closeEntry();
            }
        }
    }

    public void cleanup(Path path) {
        if (path == null || Files.notExists(path)) {
            return;
        }

        try {
            Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach(current -> {
                    try {
                        Files.deleteIfExists(current);
                    } catch (IOException ignored) {
                    }
                });
        } catch (IOException ignored) {
        }
    }

    public String truncateLog(String logOutput) {
        if (logOutput == null) {
            return null;
        }

        byte[] bytes = logOutput.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= properties.getLogMaxBytes()) {
            return logOutput;
        }

        String truncated = new String(bytes, 0, properties.getLogMaxBytes(), StandardCharsets.UTF_8);
        return truncated + System.lineSeparator() + "...[truncated]";
    }

    private Path resolveArtifactsRoot() {
        return Path.of(properties.getArtifactsRoot()).toAbsolutePath().normalize();
    }
}
