package com.oopstudio.lms.services;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.springframework.stereotype.Service;

@Service
public class CodeExecutionService {

	private static final int TEST_TIMEOUT_SECONDS = 2;
	private static final String SOLVE_METHOD_NAME = "solve";

	public CodeExecutionResult executeCode(String studentCode, String testCasesJson) {
		if (studentCode == null || studentCode.isBlank()) {
			return CodeExecutionResult.rejected("Student code must not be blank.");
		}

		List<TestCase> testCases;
		try {
			testCases = TestCaseParser.parse(testCasesJson);
		} catch (IllegalArgumentException exception) {
			return CodeExecutionResult.rejected("Invalid testCasesJson: " + exception.getMessage());
		}

		if (testCases.isEmpty()) {
			return CodeExecutionResult.rejected("At least one test case is required.");
		}

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			return CodeExecutionResult.rejected(
					"No system Java compiler is available. Run OOP Studio LMS on a full JDK, not a JRE."
			);
		}

		SubmissionSource submissionSource = prepareSubmissionSource(studentCode);
		Path compilationDirectory = null;

		try {
			compilationDirectory = Files.createTempDirectory("oop-studio-code-runner-");
			CompilationResult compilationResult = compileSource(
					compiler,
					submissionSource.className(),
					submissionSource.sourceCode(),
					compilationDirectory
			);
			if (!compilationResult.succeeded()) {
				return new CodeExecutionResult(
						false,
						false,
						testCases.size(),
						0,
						List.of(),
						compilationResult.diagnostics(),
						"Compilation failed."
				);
			}

			try (URLClassLoader classLoader = new URLClassLoader(
					new URL[] { compilationDirectory.toUri().toURL() },
					CodeExecutionService.class.getClassLoader()
			)) {
				Class<?> submissionClass = Class.forName(submissionSource.className(), true, classLoader);
				Method solveMethod = submissionClass.getMethod(SOLVE_METHOD_NAME, String.class);
				List<TestCaseResult> testResults = runTestCases(solveMethod, testCases);
				int passedTests = (int) testResults.stream()
						.filter(TestCaseResult::passed)
						.count();
				boolean executionSucceeded = testResults.stream()
						.noneMatch(result -> result.timedOut() || result.errorMessage() != null);

				return new CodeExecutionResult(
						true,
						executionSucceeded,
						testCases.size(),
						passedTests,
						testResults,
						compilationResult.diagnostics(),
						executionSucceeded ? null : "One or more test cases failed during runtime execution."
				);
			}
		} catch (ReflectiveOperationException | IOException exception) {
			return new CodeExecutionResult(
					false,
					false,
					testCases.size(),
					0,
					List.of(),
					List.of(),
					exception.getMessage()
			);
		} finally {
			deleteDirectoryQuietly(compilationDirectory);
		}
	}

	private SubmissionSource prepareSubmissionSource(String studentCode) {
		if (studentCode.contains("class Solution")) {
			return new SubmissionSource("Solution", studentCode);
		}

		String className = "StudentSubmission_" + UUID.randomUUID().toString().replace("-", "");
		return new SubmissionSource(className, buildSubmissionSource(className, studentCode));
	}

	private String buildSubmissionSource(String className, String studentCode) {
		return """
				import java.util.*;
				import java.math.*;
				import java.time.*;
				import java.util.stream.*;

				public final class %s {

					private %s() {
					}

					public static String solve(String input) throws Exception {
				%s
					}
				}
				""".formatted(className, className, indentStudentCode(studentCode));
	}

	private String indentStudentCode(String studentCode) {
		return studentCode.lines()
				.map(line -> "\t\t\t" + line)
				.reduce((left, right) -> left + System.lineSeparator() + right)
				.orElse("");
	}

	private CompilationResult compileSource(
			JavaCompiler compiler,
			String className,
			String sourceCode,
			Path compilationDirectory
	) throws IOException {
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
		try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(
				diagnostics,
				Locale.ROOT,
				StandardCharsets.UTF_8
		)) {
			fileManager.setLocationFromPaths(StandardLocation.CLASS_OUTPUT, List.of(compilationDirectory));
			JavaFileObject sourceFile = new StringJavaFileObject(className, sourceCode);
			List<String> options = List.of("--release", "25");
			Boolean succeeded = compiler.getTask(
					null,
					fileManager,
					diagnostics,
					options,
					null,
					List.of(sourceFile)
			).call();

			return new CompilationResult(Boolean.TRUE.equals(succeeded), formatDiagnostics(diagnostics));
		}
	}

	private List<String> formatDiagnostics(DiagnosticCollector<JavaFileObject> diagnostics) {
		return diagnostics.getDiagnostics().stream()
				.map(diagnostic -> "%s at line %d, column %d: %s".formatted(
						diagnostic.getKind(),
						diagnostic.getLineNumber(),
						diagnostic.getColumnNumber(),
						diagnostic.getMessage(Locale.ROOT)
				))
				.toList();
	}

	private List<TestCaseResult> runTestCases(Method solveMethod, List<TestCase> testCases) {
		List<TestCaseResult> results = new ArrayList<>();
		for (int index = 0; index < testCases.size(); index++) {
			TestCase testCase = testCases.get(index);
			results.add(runSingleTestCase(solveMethod, testCase, index + 1));
		}

		return List.copyOf(results);
	}

	private TestCaseResult runSingleTestCase(Method solveMethod, TestCase testCase, int testNumber) {
		AtomicReference<Thread> runningThread = new AtomicReference<>();
		ThreadFactory threadFactory = runnable -> {
			Thread thread = new Thread(runnable, "oop-studio-code-runner-" + testNumber);
			thread.setDaemon(true);
			runningThread.set(thread);
			return thread;
		};
		ExecutorService executorService = Executors.newSingleThreadExecutor(threadFactory);
		Callable<String> invocation = () -> invokeSolveMethod(solveMethod, testCase.input());
		Future<String> future = executorService.submit(invocation);

		try {
			String actualOutput = future.get(TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			String normalizedActual = normalizeOutput(actualOutput);
			String normalizedExpected = normalizeOutput(testCase.expected());

			return new TestCaseResult(
					testNumber,
					testCase.input(),
					testCase.expected(),
					actualOutput,
					normalizedExpected.equals(normalizedActual),
					false,
					null
			);
		} catch (TimeoutException exception) {
			future.cancel(true);
			Thread thread = runningThread.get();
			if (thread != null) {
				thread.interrupt();
			}

			return new TestCaseResult(
					testNumber,
					testCase.input(),
					testCase.expected(),
					null,
					false,
					true,
					"Execution exceeded " + TEST_TIMEOUT_SECONDS + " seconds."
			);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			return new TestCaseResult(
					testNumber,
					testCase.input(),
					testCase.expected(),
					null,
					false,
					false,
					"Execution was interrupted."
			);
		} catch (ExecutionException exception) {
			return new TestCaseResult(
					testNumber,
					testCase.input(),
					testCase.expected(),
					null,
					false,
					false,
					extractRootCauseMessage(exception)
			);
		} finally {
			executorService.shutdownNow();
		}
	}

	private String invokeSolveMethod(Method solveMethod, String input) throws Exception {
		try {
			Object target = Modifier.isStatic(solveMethod.getModifiers())
					? null
					: solveMethod.getDeclaringClass().getDeclaredConstructor().newInstance();
			Object result = solveMethod.invoke(target, input);
			return result == null ? "" : result.toString();
		} catch (InvocationTargetException exception) {
			Throwable targetException = exception.getTargetException();
			if (targetException instanceof Exception exceptionTarget) {
				throw exceptionTarget;
			}
			if (targetException instanceof Error errorTarget) {
				throw errorTarget;
			}
			throw new IllegalStateException(targetException);
		}
	}

	private String normalizeOutput(String output) {
		return output == null ? "" : output.strip();
	}

	private String extractRootCauseMessage(ExecutionException exception) {
		Throwable cause = exception.getCause();
		if (cause == null) {
			return exception.getMessage();
		}

		String message = cause.getMessage();
		return cause.getClass().getSimpleName() + (message == null || message.isBlank() ? "" : ": " + message);
	}

	private void deleteDirectoryQuietly(Path directory) {
		if (directory == null || !Files.exists(directory)) {
			return;
		}

		try (var paths = Files.walk(directory)) {
			paths.sorted(Comparator.reverseOrder())
					.forEach(path -> {
						try {
							Files.deleteIfExists(path);
						} catch (IOException ignored) {
							// Temporary runner artifacts should not block the request response.
						}
					});
		} catch (IOException ignored) {
			// Temporary runner artifacts should not block the request response.
		}
	}

	private record CompilationResult(boolean succeeded, List<String> diagnostics) {
	}

	private record SubmissionSource(String className, String sourceCode) {
	}

	private record TestCase(String input, String expected) {
	}

	public record CodeExecutionResult(
			boolean compilationSucceeded,
			boolean executionSucceeded,
			int totalTests,
			int passedTests,
			List<TestCaseResult> testResults,
			List<String> compilerDiagnostics,
			String runtimeError
	) {

		public CodeExecutionResult {
			testResults = List.copyOf(Objects.requireNonNull(testResults, "testResults must not be null"));
			compilerDiagnostics = List.copyOf(Objects.requireNonNull(
					compilerDiagnostics,
					"compilerDiagnostics must not be null"
			));
		}

		private static CodeExecutionResult rejected(String message) {
			return new CodeExecutionResult(false, false, 0, 0, List.of(), List.of(), message);
		}
	}

	public record TestCaseResult(
			int testNumber,
			String input,
			String expectedOutput,
			String actualOutput,
			boolean passed,
			boolean timedOut,
			String errorMessage
	) {
	}

	private static final class StringJavaFileObject extends SimpleJavaFileObject {

		private final String sourceCode;

		private StringJavaFileObject(String className, String sourceCode) {
			super(URI.create("string:///" + className + JavaFileObject.Kind.SOURCE.extension), JavaFileObject.Kind.SOURCE);
			this.sourceCode = sourceCode;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) {
			return sourceCode;
		}
	}

	private static final class TestCaseParser {

		private final String json;
		private int index;

		private TestCaseParser(String json) {
			this.json = json;
		}

		private static List<TestCase> parse(String testCasesJson) {
			if (testCasesJson == null || testCasesJson.isBlank()) {
				throw new IllegalArgumentException("JSON test case array must not be blank.");
			}

			TestCaseParser parser = new TestCaseParser(testCasesJson);
			List<TestCase> testCases = parser.parseArray();
			parser.skipWhitespace();
			if (!parser.isAtEnd()) {
				throw new IllegalArgumentException("Unexpected trailing content at index " + parser.index + ".");
			}

			return testCases;
		}

		private List<TestCase> parseArray() {
			skipWhitespace();
			expect('[');
			skipWhitespace();

			List<TestCase> testCases = new ArrayList<>();
			if (consumeIf(']')) {
				return List.of();
			}

			while (true) {
				testCases.add(parseObject());
				skipWhitespace();
				if (consumeIf(']')) {
					return List.copyOf(testCases);
				}
				expect(',');
			}
		}

		private TestCase parseObject() {
			skipWhitespace();
			expect('{');
			skipWhitespace();

			Map<String, String> fields = new java.util.LinkedHashMap<>();
			if (consumeIf('}')) {
				throw new IllegalArgumentException("Test case object must contain input and expected fields.");
			}

			while (true) {
				String key = parseString();
				skipWhitespace();
				expect(':');
				skipWhitespace();
				String value = parseString();
				fields.put(key, value);
				skipWhitespace();

				if (consumeIf('}')) {
					break;
				}
				expect(',');
				skipWhitespace();
			}

			String input = fields.get("input");
			String expected = fields.get("expected");
			if (input == null || expected == null) {
				throw new IllegalArgumentException("Every test case must contain string fields named input and expected.");
			}

			return new TestCase(input, expected);
		}

		private String parseString() {
			skipWhitespace();
			expect('"');

			StringBuilder builder = new StringBuilder();
			while (!isAtEnd()) {
				char current = json.charAt(index++);
				if (current == '"') {
					return builder.toString();
				}
				if (current == '\\') {
					builder.append(parseEscapedCharacter());
				} else {
					builder.append(current);
				}
			}

			throw new IllegalArgumentException("Unterminated string literal.");
		}

		private char parseEscapedCharacter() {
			if (isAtEnd()) {
				throw new IllegalArgumentException("Invalid escape sequence at end of JSON.");
			}

			char escaped = json.charAt(index++);
			return switch (escaped) {
				case '"' -> '"';
				case '\\' -> '\\';
				case '/' -> '/';
				case 'b' -> '\b';
				case 'f' -> '\f';
				case 'n' -> '\n';
				case 'r' -> '\r';
				case 't' -> '\t';
				case 'u' -> parseUnicodeEscape();
				default -> throw new IllegalArgumentException("Unsupported escape sequence \\" + escaped + ".");
			};
		}

		private char parseUnicodeEscape() {
			if (index + 4 > json.length()) {
				throw new IllegalArgumentException("Incomplete unicode escape sequence.");
			}

			String hex = json.substring(index, index + 4);
			index += 4;
			try {
				return (char) Integer.parseInt(hex, 16);
			} catch (NumberFormatException exception) {
				throw new IllegalArgumentException("Invalid unicode escape sequence: \\u" + hex + ".");
			}
		}

		private void expect(char expected) {
			skipWhitespace();
			if (isAtEnd() || json.charAt(index) != expected) {
				throw new IllegalArgumentException("Expected '" + expected + "' at index " + index + ".");
			}
			index++;
		}

		private boolean consumeIf(char expected) {
			skipWhitespace();
			if (!isAtEnd() && json.charAt(index) == expected) {
				index++;
				return true;
			}

			return false;
		}

		private void skipWhitespace() {
			while (!isAtEnd() && Character.isWhitespace(json.charAt(index))) {
				index++;
			}
		}

		private boolean isAtEnd() {
			return index >= json.length();
		}
	}
}
