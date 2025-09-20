package io.github.bmarwell.jfmt.test;

public record CommandExecutionResult(int returncode, java.util.List<String> stdout, java.util.List<String> stderr) {}
