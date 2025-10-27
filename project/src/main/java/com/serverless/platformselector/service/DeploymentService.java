package com.serverless.platformselector.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.platformselector.dto.DeploymentRequestDTO;
import com.serverless.platformselector.dto.DeploymentResultDTO;
import com.serverless.platformselector.entity.DeploymentRecord;
import com.serverless.platformselector.entity.Platform;
import com.serverless.platformselector.entity.UserCredentials;
import com.serverless.platformselector.enums.DeploymentStatus;
import com.serverless.platformselector.exception.DeploymentException;
import com.serverless.platformselector.exception.ResourceNotFoundException;
import com.serverless.platformselector.repository.DeploymentRecordRepository;
import com.serverless.platformselector.repository.PlatformRepository;
import com.serverless.platformselector.util.EncryptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Transactional
public class DeploymentService {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentService.class);
    private static final int DEPLOYMENT_TIMEOUT_MINUTES = 10;

    @Autowired
    private DeploymentRecordRepository deploymentRecordRepository;

    @Autowired
    private PlatformRepository platformRepository;

    @Autowired
    private UserCredentialsService userCredentialsService;

    @Autowired
    private EncryptionUtil encryptionUtil;
    // Jackson mapper για parsing JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DeploymentResultDTO deployFunction(DeploymentRequestDTO deploymentRequest) {
        logger.info("Starting deployment for function: {} on platform: {}",
                deploymentRequest.getFunctionName(), deploymentRequest.getPlatformId());

        Platform platform = platformRepository.findById(deploymentRequest.getPlatformId())
                .orElseThrow(() -> new ResourceNotFoundException("Platform not found with id: " + deploymentRequest.getPlatformId()));

        DeploymentRecord deploymentRecord = new DeploymentRecord(
                deploymentRequest.getUserId(),
                platform,
                deploymentRequest.getFunctionName(),
                DeploymentStatus.PENDING
        );
        deploymentRecord = deploymentRecordRepository.save(deploymentRecord);

        try {
            // Πάρε όλα τα credentials σαν map
            Map<String, String> credentialsMap = userCredentialsService.getAllUserCredentialsMap(
                    deploymentRequest.getUserId(), platform.getId());

            // Δημιούργησε προσωρινό φάκελο
            Path tempDir = createTempDeploymentDirectory(deploymentRecord.getId().toString());
            Path tempDir2 = createTempDeploymentDirectory("security");
            // Εξαγωγή function package
            extractFunctionPackage(deploymentRequest.getFunctionPackageBase64(), tempDir);

            // Δημιουργία serverless.yml με χρήση map credentials
            createServerlessConfig(tempDir, deploymentRequest.getFunctionName(), platform,
                    credentialsMap, deploymentRequest.getRuntime(), deploymentRequest.getHandler(), deploymentRequest.getRegion());

            // Εκτέλεση deploy
            System.out.println("[DEPLOY] Executing serverless deployment...");
            String deploymentOutput = executeServerlessDeployment(tempDir, platform);

            String endpointUrl = parseEndpointUrl(deploymentOutput);
            //makeFunctionPublic(deploymentRequest.getFunctionName(), platform.getName());

            deploymentRecord.setDeploymentStatus(DeploymentStatus.SUCCESS);
            deploymentRecord.setEndpointUrl(endpointUrl);
            deploymentRecord.setLog(deploymentOutput);
            deploymentRecordRepository.save(deploymentRecord);

            cleanupTempDirectory(tempDir);

            logger.info("Deployment completed successfully for function: {}", deploymentRequest.getFunctionName());

            return new DeploymentResultDTO(
                    deploymentRecord.getId(),
                    DeploymentStatus.SUCCESS,
                    endpointUrl,
                    deploymentOutput
            );

        } catch (Exception e) {
            logger.error("Deployment failed for function: {}", deploymentRequest.getFunctionName(), e);
            deploymentRecord.setDeploymentStatus(DeploymentStatus.FAILED);
            deploymentRecord.setLog(e.getMessage());
            deploymentRecordRepository.save(deploymentRecord);

            return new DeploymentResultDTO(
                    deploymentRecord.getId(),
                    DeploymentStatus.FAILED,
                    e.getMessage(),
                    e.getMessage()
            );
        }
    }

    private Path createTempDeploymentDirectory(String deploymentId) throws Exception {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "serverless-deployment-" + deploymentId);
        Files.createDirectories(tempDir);
        logger.info("Created temporary deployment directory: {}", tempDir);
        return tempDir;
    }

    private void extractFunctionPackage(String base64Package, Path tempDir) throws Exception {
        byte[] packageBytes = Base64.getDecoder().decode(base64Package);
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(packageBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                String[] parts = entryName.split("/", 2);
                if (parts.length == 2) {
                    entryName = parts[1];
                } else if (parts.length == 1 && entry.isDirectory()) {
                    zis.closeEntry();
                    continue;
                }
                Path newFile = tempDir.resolve(entryName);
                if (entry.isDirectory()) {
                    Files.createDirectories(newFile);
                } else {
                    Files.createDirectories(newFile.getParent());
                    try (OutputStream fos = Files.newOutputStream(newFile)) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
        logger.info("Extracted function package to: {}", tempDir);
    }

    private void createServerlessConfig(Path tempDir, String functionName, Platform platform, Map<String,String> credentialsMap,
                                        String runtime, String handler, String region) throws Exception {
        String serverlessYml = generateServerlessYml(functionName, platform, credentialsMap, runtime, handler, region,tempDir);
        Path configFile = tempDir.resolve("serverless.yml");
        Files.write(configFile, serverlessYml.getBytes());
        logger.info("Created serverless.yml configuration");
    }

    private String generateServerlessYml(String functionName, Platform platform, Map<String,String> credentialsMap,
                                         String runtime, String handler, String region, Path tempDir) throws Exception {
        StringBuilder yml = new StringBuilder();
        yml.append("service: ").append(functionName.toLowerCase()).append("\n");
        yml.append("frameworkVersion: ^3.40.0\n");
        yml.append("provider:\n");

        String platformName = platform.getName().toLowerCase();

        switch (platformName) {
            case "aws":
                yml.append("  name: aws\n");
                yml.append("  runtime: ").append(runtime != null ? runtime : "nodejs16.x").append("\n");
                yml.append("  region: ").append(region != null ? region : "us-east-1").append("\n");
                break;

            case "gcp":
                yml.append("  name: google\n");
                yml.append("  runtime: ").append(runtime != null ? runtime : "nodejs18").append("\n");
                yml.append("  region: ").append(region != null ? region : "us-central1").append("\n");

                String gcpCredentialsJson = credentialsMap.get("GOOGLE_APPLICATION_CREDENTIALS");
                if (gcpCredentialsJson == null) {
                    throw new DeploymentException("Missing GOOGLE_APPLICATION_CREDENTIALS in user credentials");
                }

                // 1. Μετατρέπουμε τα escaped newlines σε πραγματικά newlines
                //gcpCredentialsJson = fixCredentialNewlines(gcpCredentialsJson);
                gcpCredentialsJson = fixPrivateKeyNewlines(gcpCredentialsJson);
                // 2. Parse το JSON (για να βγάλουμε το project_id)
                JsonNode rootNode = objectMapper.readTree(gcpCredentialsJson);
                String projectId = extractProjectId(gcpCredentialsJson);

                // 3. Γράφουμε το pretty printed JSON σε αρχείο στο tempDir
                Path credentialsFilePath = tempDir.resolve(projectId + "-" + rootNode.path("private_key_id").asText() + ".json");
                // Χρησιμοποιούμε ObjectMapper για pretty print
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(credentialsFilePath.toFile(), rootNode);
                logger.info("Wrote GCP credentials file: {}", credentialsFilePath);

                Path relativePath = tempDir.relativize(credentialsFilePath);
                String hardcodedCredentialsPath = "C:\\Users\\Y9GLLM726\\Desktop\\gcloud\\keys\\2\\angelic-hold-468008-b9-de8f5d4945f5.json";
                yml.append("  project: ").append(projectId).append("\n");
                yml.append("  credentials: ").append(hardcodedCredentialsPath).append("\n");


                yml.append("\nplugins:\n");
                yml.append("  - serverless-google-cloudfunctions\n");
                break;


            default:
                throw new DeploymentException("Unsupported platform: " + platform.getName());
        }

        yml.append("\nfunctions:\n");
        yml.append("  ").append(functionName).append(":\n");
        switch(platformName) {
            case "aws":
                yml.append("    handler: ").append(handler != null ? handler : "index.handler").append("\n");
                break;
            case "gcp":
                yml.append("    handler: ").append(handler != null ? handler : "handler").append("\n");
                break;
            default:
                // Προαιρετικά, κάποια default συμπεριφορά ή exception
                break;
        }


        switch (platformName) {
            case "aws":
                yml.append("    events:\n");
                yml.append("      - http:\n");
                yml.append("          path: /").append(functionName.toLowerCase()).append("\n");
                yml.append("          method: get\n");
                break;
            case "gcp":
                yml.append("    events:\n");
                yml.append("      - http: true\n");
                break;
        }

        return yml.toString();
    }


    private Path writeJsonToTempFile(String jsonContent, String prefix, String suffix) throws IOException {
        Path tempFile = Files.createTempFile(prefix, suffix);
        Files.write(tempFile, jsonContent.getBytes());
        logger.info("Created temporary credential file: {}", tempFile);
        return tempFile;
    }

    private String fixCredentialNewlines(String json) {
        if (json == null) return null;
        // Αντικαθιστά τα \\n με πραγματικά newlines \n
        return json.replace("\\n", "\n");
    }

    private String escapeNewlinesForJson(String json) {
        if (json == null) return null;
        // Αντικαθιστά τα πραγματικά newlines \n με escaped \\n
        return json.replace("\n", "\\n");
    }








    /* private String executeServerlessDeployment(Path tempDir) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("sls", "deploy", "--verbose");
        pb.directory(tempDir.toFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                logger.debug("Deployment output: {}", line);
            }
        }

        boolean finished = process.waitFor(DEPLOYMENT_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        if (!finished) {
            process.destroyForcibly();
            throw new DeploymentException("Deployment timed out after " + DEPLOYMENT_TIMEOUT_MINUTES + " minutes");
        }

        if (process.exitValue() != 0) {
            throw new DeploymentException("Deployment failed with exit code: " + process.exitValue() + "\n" + output.toString());
        }

        return output.toString();
    }*/
    private String executeServerlessDeployment(Path tempDir, Platform platform) throws Exception {
        ProcessBuilder pb;
        logger.info("Current PATH: {}", System.getenv("PATH"));

        String path = System.getenv("PATH");
        String npmGlobal = "C:\\Users\\Y9GLLM726\\AppData\\Roaming\\npm";

        // 1. npm init -y
        pb = new ProcessBuilder("cmd.exe", "/c", "npm", "init", "-y");
        pb.directory(tempDir.toFile());
        pb.redirectErrorStream(true);
        pb.environment().put("PATH", npmGlobal + ";" + path);
        runProcess(pb, 2, "[npm init]");

        // 2. npm install serverless --save-dev
        pb = new ProcessBuilder("cmd.exe", "/c", "npm", "install", "serverless@3.40.0", "--save-dev");
        pb.directory(tempDir.toFile());
        pb.redirectErrorStream(true);
        pb.environment().put("PATH", npmGlobal + ";" + path);
        runProcess(pb, 5, "[npm install serverless]");

        /*// 3. Install provider plugin if needed
        switch (platform.getName().toLowerCase()) {
            case "aws lambda":
                // No extra plugin needed, AWS is default
                break;
            case "google cloud functions":*/
        String platformNameLower = platform.getName().toLowerCase();

        if (platformNameLower.equals("gcp")) {
            // install GCP plugin
            pb = new ProcessBuilder("cmd.exe", "/c", "npm", "install", "serverless-google-cloudfunctions", "--save-dev");
            pb.directory(tempDir.toFile());
            pb.redirectErrorStream(true);
            pb.environment().put("PATH", npmGlobal + ";" + path);
            runProcess(pb, 3, "[npm install GCP plugin]");
        }
// Για AWS, **δεν χρειάζεται plugin**.
/*
        pb = new ProcessBuilder("cmd.exe", "/c", "npm", "install", "serverless-google-cloudfunctions", "--save-dev");
                pb.directory(tempDir.toFile());
                pb.redirectErrorStream(true);
                pb.environment().put("PATH", npmGlobal + ";" + path);
                runProcess(pb, 3, "[npm install GCP plugin]");*/
                /*break;
            case "azure functions":
                pb = new ProcessBuilder("cmd.exe", "/c", "npm", "install", "serverless-azure-functions", "--save-dev");
                pb.directory(tempDir.toFile());
                pb.redirectErrorStream(true);
                pb.environment().put("PATH", npmGlobal + ";" + path);
                runProcess(pb, 3, "[npm install Azure plugin]");
                break;
            default:
                throw new DeploymentException("Unsupported platform for plugin installation: " + platform.getName());*//*
        }*/

        // 4. Run local serverless binary deploy
        String serverlessCmd = tempDir.resolve("node_modules")
                .resolve(".bin")
                .resolve("serverless.cmd")
                .toString();

        pb = new ProcessBuilder("cmd.exe", "/c", serverlessCmd, "deploy", "--verbose");
        pb.directory(tempDir.toFile());
        pb.redirectErrorStream(true);
        pb.environment().put("PATH", npmGlobal + ";" + path);
        return runProcess(pb, DEPLOYMENT_TIMEOUT_MINUTES, "[serverless deploy]");
    }



    private String runProcess(ProcessBuilder pb, int timeoutMinutes, String tag) throws Exception {
        Process process = pb.start();
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                logger.debug("{} {}", tag, line);
            }
        }
        boolean finished = process.waitFor(timeoutMinutes, TimeUnit.MINUTES);
        if (!finished) {
            process.destroyForcibly();
            throw new DeploymentException(tag + " timed out after " + timeoutMinutes + " minutes");
        }
        if (process.exitValue() != 0) {
            throw new DeploymentException(tag + " failed with exit code: " + process.exitValue() + "\n" + output);
        }
        return output.toString();
    }
    private String parseEndpointUrl(String deploymentOutput) {
        // Parse endpoint URL from serverless framework output
        // This regex pattern may need adjustment based on the actual output format
        Pattern pattern = Pattern.compile("https://[a-zA-Z0-9.-]+/[a-zA-Z0-9/-]*");
        Matcher matcher = pattern.matcher(deploymentOutput);

        if (matcher.find()) {
            return matcher.group();
        }

        logger.warn("Could not parse endpoint URL from deployment output");
        return null;
    }

    private void cleanupTempDirectory(Path tempDir) {
        try {
            Files.walk(tempDir)
                    .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (Exception e) {
                            logger.warn("Failed to delete temporary file: {}", path, e);
                        }
                    });
            logger.info("Cleaned up temporary deployment directory: {}", tempDir);
        } catch (Exception e) {
            logger.warn("Failed to cleanup temporary directory: {}", tempDir, e);
        }
    }
    private void makeFunctionPublic(String functionName, String platformName) throws Exception {
        if (!"gcp".equalsIgnoreCase(platformName)) {
            return; // μόνο για GCP
        }

        String region = "us-central1"; // ίδιο με το serverless.yml
        String stage = "dev";          // ίδιο με το serverless.yml
        String fullFunctionName = functionName.toLowerCase() + "-" + stage + "-" + functionName;

        String path = System.getenv("PATH");
        String npmGlobal = "C:\\Users\\Y9GLLM726\\AppData\\Roaming\\npm";

        ProcessBuilder pb = new ProcessBuilder(
                "cmd.exe", "/c",
                "gcloud", "functions", "add-iam-policy-binding", fullFunctionName,
                "--region=" + region,
                "--member=allUsers",
                "--role=roles/cloudfunctions.invoker"
        );

        pb.redirectErrorStream(true);
        pb.environment().put("PATH", npmGlobal + ";" + path);

        String output = runProcess(pb, 2, "[gcloud make public]");
        logger.info("Made function public: {}", output);
    }

    private String extractProjectId(String gcpCredentialsJson) throws DeploymentException {
        Pattern pattern = Pattern.compile("\"project_id\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(gcpCredentialsJson);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new DeploymentException("project_id missing in GOOGLE_APPLICATION_CREDENTIALS JSON");
    }

    private String fixPrivateKeyNewlines(String json) {
        // Θα βρει το πεδίο private_key και θα αντικαταστήσει raw newlines μέσα σε αυτό με \n
        Pattern pattern = Pattern.compile("(\"private_key\"\\s*:\\s*\")(.*?)(\")", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            String before = matcher.group(1);
            String privateKeyValue = matcher.group(2);
            String after = matcher.group(3);

            // Αντικαθιστούμε raw newlines και backslash+newline με \n
            String fixedKey = privateKeyValue
                    .replace("\\\n", "\\n")  // backslash + newline -> \n
                    .replace("\n", "\\n")    // raw newline -> \n

                    // Αν χρειαστεί μπορείς να κάνεις και άλλα escapes εδώ

                    ;

            return matcher.replaceFirst(before + fixedKey + after);
        }
        return json;
    }

}