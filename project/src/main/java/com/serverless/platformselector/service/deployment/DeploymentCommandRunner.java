package com.serverless.platformselector.service.deployment;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public interface DeploymentCommandRunner {

    CommandResult run(Path workingDirectory, List<String> command, Map<String, String> environment, Duration timeout)
        throws Exception;
}
