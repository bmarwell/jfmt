package io.github.bmarwell.jfmt.its.extension;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

class JFmtExtension implements BeforeAllCallback, BeforeEachCallback, ParameterResolver, AfterAllCallback {

    private ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public void beforeAll(ExtensionContext context) {
        final String jdtFmtDirectory = System.getProperty("jfmt.directory");

        if (jdtFmtDirectory == null) {
            throw new IllegalStateException("jfmt.directory system property is not set.");
        }

        final Path jdtFmtPath = Paths.get(jdtFmtDirectory);

        if (!Files.exists(jdtFmtPath) || !Files.isDirectory(jdtFmtPath)) {
            throw new IllegalStateException("jfmt.directory system property is not set to a valid directory.");
        }

        final String jacocoAgentPath = System.getProperty("jacoco.agent.path");
        if (jacocoAgentPath == null || jacocoAgentPath.isEmpty()) {
            throw new IllegalStateException(
                "jacoco.agent.path system property is not set or empty: [" + jacocoAgentPath + "]"
            );
        }

        var jacocoAgent = Paths.get(jacocoAgentPath);
        if (!Files.isReadable(jacocoAgent)) {
            throw new IllegalStateException("jacocoAgentPath does not point to jacoco-agent.jar: " + jacocoAgent);
        }

        final String executable;
        if (System.getProperty("os.name") != null && System.getProperty("os.name").toLowerCase().contains("win")) {
            executable = "jfmt.bat";
        } else {
            executable = "jfmt";
        }

        final Class<?> testClass = context.getRequiredTestClass();
        final List<String> args;
        if (testClass.isAnnotationPresent(JFmtTest.class)) {
            args = List.of(testClass.getAnnotation(JFmtTest.class).args());
        } else {
            args = List.of();
        }

        ExtensionContext.Namespace classNameSpace = ExtensionContext.Namespace.create(context);
        final ExtensionContext.Store classStore = context.getStore(classNameSpace);
        classStore.put("jfmt.jacocoAgent", jacocoAgent);
        classStore.put("jfmt.executable", jdtFmtPath.resolve("bin").resolve(executable));
        classStore.put("jfmt.args", args);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        final Method testMethod = context.getRequiredTestMethod();

        final ExtensionContext testClassContext = context.getParent().orElseThrow();
        ExtensionContext.Namespace classNameSpace = ExtensionContext.Namespace.create(testClassContext);
        final ExtensionContext.Store classStore = testClassContext.getStore(classNameSpace);

        final List<String> args;
        if (testMethod.isAnnotationPresent(JFmtTest.class)) {
            args = List.of(testMethod.getAnnotation(JFmtTest.class).args());
        } else {
            args = classStore.get("jfmt.args", List.class);
        }

        Path jacocoAgentPath = classStore.get("jfmt.jacocoAgent", Path.class);

        final Path jdtApp = classStore.get("jfmt.executable", Path.class);
        List<String> cmd = Stream.concat(
            Stream.of(jdtApp.toString()),
            args.stream()
        ).toList();

        String jacocoAgentArgLine = String.format(
            Locale.ROOT,
            "-javaagent:%s=destfile=target/jacoco-it.exec,append=true",
            jacocoAgentPath
        );

        final ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        processBuilder.environment().put("JAVA_OPTS", jacocoAgentArgLine);
        final Process process = processBuilder.start();

        List<JdtResult.LogLine> logs = new CopyOnWriteArrayList<>();

        CompletableFuture<Void> stdoutFuture = CompletableFuture.runAsync(
            () -> readLines(process.getInputStream(), JdtResult.LogLine.Channel.STDOUT, logs),
            executor
        );

        CompletableFuture<Void> stderrFuture = CompletableFuture.runAsync(
            () -> readLines(process.getErrorStream(), JdtResult.LogLine.Channel.STDERR, logs),
            executor
        );

        boolean finished = process.waitFor(5, TimeUnit.SECONDS);

        // Wait for stream readers to complete
        CompletableFuture.allOf(stdoutFuture, stderrFuture)
            .get(1, TimeUnit.SECONDS);

        int exitCode = process.exitValue();

        // store everything for the parameter resolution
        final var namespace = ExtensionContext.Namespace.create(context);
        final var store = context.getStore(namespace);

        store.put("logs", List.copyOf(logs));
        store.put("exitCode", exitCode);
    }

    private static void readLines(
        java.io.InputStream in,
        JdtResult.LogLine.Channel channel,
        List<JdtResult.LogLine> logs
    ) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logs.add(new JdtResult.LogLine(channel, Instant.now(), line));
            }
        } catch (Exception e) {
            logs.add(new JdtResult.LogLine(channel, Instant.now(), "[ERROR reading stream] " + e));
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
        throws ParameterResolutionException {
        final Class<?> type = parameterContext.getParameter().getType();

        return type.equals(JdtResult.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
        throws ParameterResolutionException {
        final Class<?> type = parameterContext.getParameter().getType();

        if (type.equals(JdtResult.class)) {
            final var namespace = ExtensionContext.Namespace.create(extensionContext);
            final var store = extensionContext.getStore(namespace);

            final int exitCode = store.get("exitCode", Integer.class);
            final List<JdtResult.LogLine> logs = store.get("logs", List.class);

            return new JdtResult(exitCode, List.copyOf(logs));
        }

        throw new ParameterResolutionException("parameter type [" + type.getCanonicalName() + "] is not supported.");
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        this.executor.shutdown();
        this.executor.close();
    }
}
