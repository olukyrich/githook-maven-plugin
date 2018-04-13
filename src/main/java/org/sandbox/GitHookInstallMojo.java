package org.sandbox;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;

import static java.nio.file.StandardOpenOption.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

@Mojo(name = "install", defaultPhase = LifecyclePhase.INITIALIZE)
public final class GitHookInstallMojo extends AbstractMojo {

    private static final String SHEBANG = "#!/bin/sh";
    private static final Path HOOK_DIR_PATH = Paths.get(".git/hooks");

    @Parameter(name = "hooks")
    private Map<String, String> inlineHooks;
    @Parameter(name = "resourceHooks")
    private Map<String, String> resourceHooks;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!Files.exists(HOOK_DIR_PATH)) {
            throw new MojoExecutionException("not a git repository");
        }
        this.generateInlineHooks();
        this.generateResourceHooks();
    }

    protected void generateInlineHooks() throws MojoExecutionException {
        if (inlineHooks == null) {
            return;
        }
        for (Map.Entry<String, String> hook : inlineHooks.entrySet()) {
            String hookName = hook.getKey();
            String finalScript = SHEBANG + '\n' + hook.getValue();

            getLog().info("Generating " + hookName + " from maven conf");
            generateHookFile(hookName, finalScript);
        }
    }

    protected void generateResourceHooks() throws MojoExecutionException {
        if (resourceHooks == null) {
            return;
        }
        for (Map.Entry<String, String> hook : resourceHooks.entrySet()) {
            String hookName = hook.getKey();

            Path hookFilePath = Paths.get(hook.getValue());
            Path local = Paths.get("");

            if (!hookFilePath.toAbsolutePath().startsWith(local.toAbsolutePath())) {
                throw new MojoExecutionException("only file inside the project can be used to generate git hooks");
            }

            String finalScript;

            try {
                finalScript = Files.lines(hookFilePath).collect(Collectors.joining("\n"));
            } catch (IOException e) {
                throw new MojoExecutionException("could not access hook resource : " + hookFilePath, e);
            }

            getLog().info("Generating " + hookName + " from " + hookFilePath.toString());
            generateHookFile(hookName, finalScript);
        }
    }

    protected void generateHookFile(String hookName, String asStringScript) throws MojoExecutionException {
        try {
            Files.write(HOOK_DIR_PATH.resolve(hookName), asStringScript.getBytes(), CREATE, TRUNCATE_EXISTING);
            Files.setPosixFilePermissions(HOOK_DIR_PATH.resolve(hookName), new HashSet<>(Arrays.asList(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)));
        } catch (IOException e) {
            throw new MojoExecutionException("could not write hook with name : " + hookName, e);
        }
    }

}
