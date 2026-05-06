package com.serverless.platformselector.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Profile("worker")
public class ServerlessCliHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            List<String> command = new ArrayList<>();
            boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
            if (windows) {
                command.add("cmd.exe");
                command.add("/c");
            }
            command.add("serverless");
            command.add("--version");

            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }

            boolean completed = process.waitFor(5, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                return Health.down()
                        .withDetail("reason", "Timed out while checking serverless CLI")
                        .build();
            }

            if (process.exitValue() != 0) {
                return Health.down()
                        .withDetail("exitCode", process.exitValue())
                        .withDetail("output", output.toString().trim())
                        .build();
            }

            return Health.up()
                    .withDetail("version", output.toString().trim())
                    .build();
        } catch (Exception ex) {
            return Health.down(ex).build();
        }
    }
}
