package io.github.bmarwell.jfmt.commands;

import static java.nio.file.Files.isRegularFile;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.Patch;
import io.github.bmarwell.jfmt.config.ConfigLoader;
import io.github.bmarwell.jfmt.config.NamedConfig;
import io.github.bmarwell.jfmt.format.FileProcessingResult;
import io.github.bmarwell.jfmt.format.FormatterMode;
import io.github.bmarwell.jfmt.imports.CliNamedImportOrder;
import io.github.bmarwell.jfmt.imports.ImportOrderConfiguration;
import io.github.bmarwell.jfmt.imports.ImportOrderLoader;
import io.github.bmarwell.jfmt.imports.NamedImportOrder;
import io.github.bmarwell.jfmt.nio.PathUtils;
import io.github.bmarwell.jfmt.writer.OutputWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import picocli.CommandLine;

public abstract class AbstractCommand { }
