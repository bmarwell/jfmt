package io.github.bmarwell.jdtfmt.its.extension;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public record JdtResult(int exitCode, List<LogLine> logLines) {

    record LogLine(Channel channel, Instant time, String message) {
        enum Channel {
            STDOUT,
            STDERR
        }
    }

    public String getStdout() {
        return logLines.stream()
            .filter(l -> l.channel() == LogLine.Channel.STDOUT)
            .map(LogLine::message)
            .collect(Collectors.joining("\n"));
    }

    public String getStderr() {
        return logLines.stream()
            .filter(l -> l.channel() == LogLine.Channel.STDERR)
            .map(LogLine::message)
            .collect(Collectors.joining("\n"));
    }
}
