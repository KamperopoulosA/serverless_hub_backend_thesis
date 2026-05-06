package com.serverless.platformselector.service.deployment;

import com.serverless.platformselector.exception.DeploymentException;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class ProcessDeploymentCommandRunner implements DeploymentCommandRunner {

    @Override
    public CommandResult run(
            Path workingDirectory,
            List<String> command,
            Map<String, String> environment,
            Duration timeout) throws Exception {
        List<String> processCommand = new ArrayList<>();
        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        if (windows) {
            processCommand.add("cmd.exe");
            processCommand.add("/c");
        }
        processCommand.addAll(command);

        ProcessBuilder processBuilder = new ProcessBuilder(processCommand);
        processBuilder.directory(workingDirectory.toFile());
        processBuilder.redirectErrorStream(true);
        processBuilder.environment().putAll(environment);

        Process process = processBuilder.start();
        StringBuilder output = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }

        boolean completed = process.waitFor(timeout.toSeconds(), TimeUnit.SECONDS);
        if (!completed) {
            process.destroyForcibly();
            throw new DeploymentException("Deployment timed out after " + timeout.toSeconds() + " seconds");
        }

        return new CommandResult(process.exitValue(), output.toString());
    }
}
