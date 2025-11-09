package org.apache.maven;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.maven.api.MonotonicClock;
import org.apache.maven.resolver.RepositorySystemSessionFactory;
import org.apache.maven.session.scope.internal.SessionScope;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.RepositorySystemSession.CloseableSession;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.sisu.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import static java.util.stream.Collectors.toSet;

public class MavenCore {
}
