package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.model.FileAnnotation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

/**
 * Tests the class {@link ParserRegistry}.
 */
public class ParserRegistryTest {
    /** Total number of expected warnings. */
    private static final int TOTAL_WARNINGS = 228;
    /** Error message. */
    private static final String WRONG_NUMBER_OF_ANNOTATIONS_PARSED = "Wrong number of annotations parsed";

    /**
     * Checks whether we correctly find all warnings in the log file.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testAllParsersOnOneFile() throws IOException {
        ParserRegistry parserRegistry = createRegistryUnderTest("all.txt", StringUtils.EMPTY, StringUtils.EMPTY, new ArrayList<WarningsParser>());

        Collection<FileAnnotation> annotations = parserRegistry.parse(new File(""));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, TOTAL_WARNINGS, annotations.size());

        ParserResult result = new ParserResult();
        result.addAnnotations(annotations);
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, TOTAL_WARNINGS, result.getNumberOfAnnotations());

        result.addAnnotations(annotations);
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, TOTAL_WARNINGS, result.getNumberOfAnnotations());
    }

    /**
     * Checks whether we correctly find all Oracle invalids in the log file.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testOracleInvalidsParser() throws IOException {
        List<WarningsParser> parsers = new ArrayList<WarningsParser>();
        parsers.add(new InvalidsParser());
        ParserRegistry parserRegistry = createRegistryUnderTest("all.txt", StringUtils.EMPTY, StringUtils.EMPTY, parsers);

        Collection<FileAnnotation> annotations = parserRegistry.parse(new File(""));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, 2, annotations.size());

        ParserResult result = new ParserResult();
        result.addAnnotations(annotations);
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, 2, result.getNumberOfAnnotations());

        result.addAnnotations(annotations);
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, 2, result.getNumberOfAnnotations());
    }

    /**
     * Checks whether we correctly find all warnings of two parsers in the log file.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testTwoParsers() throws IOException {
        List<WarningsParser> parsers = new ArrayList<WarningsParser>();
        parsers.add(new InvalidsParser());
        parsers.add(new JavaDocParser());
        ParserRegistry parserRegistry = createRegistryUnderTest("all.txt", StringUtils.EMPTY, StringUtils.EMPTY, parsers);

        Collection<FileAnnotation> annotations = parserRegistry.parse(new File(""));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, 10, annotations.size());

        ParserResult result = new ParserResult();
        result.addAnnotations(annotations);
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, 10, result.getNumberOfAnnotations());

        result.addAnnotations(annotations);
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, 10, result.getNumberOfAnnotations());
    }

    /**
     * Checks whether we correctly find all warnings in the log file. The file
     * contains 18 additional ANT warnings, 8 of them should be excluded by the single
     * pattern.
     *
     * @throws IOException
     *             if the file could not be read
     * @see <a href="http://issues.hudson-ci.org/browse/HUDSON-2359">Issue 2359</a>
     */
    @Test
    public void issue2359() throws IOException {
        ParserRegistry parserRegistry = createRegistryUnderTest("all.txt", StringUtils.EMPTY, "/tmp/clover*/**", new ArrayList<WarningsParser>());

        Collection<FileAnnotation> annotations = parserRegistry.parse(new File(""));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, TOTAL_WARNINGS - 8, annotations.size());
    }

    /**
     * The file contains 18 additional ANT warnings, 15 of them should be excluded by the
     * two patterns.
     *
     * @throws IOException
     *             if the file could not be read
     * @see <a href="http://issues.hudson-ci.org/browse/HUDSON-2359">Issue 2359</a>
     */
    @Test
    public void multiplePatternsIssue2359() throws IOException {
        ParserRegistry parserRegistry = createRegistryUnderTest("all.txt", StringUtils.EMPTY, "/tmp/clover*/**, **/renderers/*", new ArrayList<WarningsParser>());

        Collection<FileAnnotation> annotations = parserRegistry.parse(new File(""));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, TOTAL_WARNINGS - 18 + 3, annotations.size());
    }

    /**
     * Checks whether we correctly find all warnings in the log file. The file
     * contains 18 additional ANT warnings, 8 of them should be included by the single
     * pattern.
     *
     * @throws IOException
     *             if the file could not be read
     * @see <a href="http://issues.hudson-ci.org/browse/HUDSON-3866">Issue 3866</a>
     */
    @Test
    public void issue3866() throws IOException {
        ParserRegistry parserRegistry = createRegistryUnderTest("all.txt", "/tmp/clover*/**", StringUtils.EMPTY, new ArrayList<WarningsParser>());

        Collection<FileAnnotation> annotations = parserRegistry.parse(new File(""));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, 8, annotations.size());
    }

    /**
     * The file contains 18 additional ANT warnings, 15 of them should be included by the
     * two patterns.
     *
     * @throws IOException
     *             if the file could not be read
     * @see <a href="http://issues.hudson-ci.org/browse/HUDSON-3866">Issue 3866</a>
     */
    @Test
    public void multiplePatternsIssue3866() throws IOException {
        ParserRegistry parserRegistry = createRegistryUnderTest("all.txt", "/tmp/clover*/**, **/renderers/*", StringUtils.EMPTY, new ArrayList<WarningsParser>());

        Collection<FileAnnotation> annotations = parserRegistry.parse(new File(""));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, 15, annotations.size());
    }

    /**
     * The file contains 18 additional ANT warnings, 8 of them should be included by
     * one pattern, 7 of them should be excluded by one pattern.
     *
     * @throws IOException
     *             if the file could not be read
     * @see <a href="http://issues.hudson-ci.org/browse/HUDSON-3866">Issue 3866</a>
     */
    @Test
    public void complexFilterIssue3866() throws IOException {
        ParserRegistry parserRegistry = createRegistryUnderTest("all.txt", "/tmp/clover*/**", "**/renderers/*", new ArrayList<WarningsParser>());

        Collection<FileAnnotation> annotations = parserRegistry.parse(new File(""));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, 1, annotations.size());
    }

    /**
     * Creates the registry under test.
     *
     * @param fileName
     *            file name with the warnings
     * @param includePattern
     *            Ant file-set pattern of files to include in report,
     *            <code>null</code> or an empty string do not filter the input
     * @param excludePattern
     *            Ant file-set pattern of files to exclude from report,
     *            <code>null</code> or an empty string do not filter the output
     * @param parsers
     *            the parsers to use
     * @return the registry
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("SIC")
    private ParserRegistry createRegistryUnderTest(final String fileName, final String includePattern, final String excludePattern, final List<WarningsParser> parsers) {
        ParserRegistry parserRegistry = new ParserRegistry(parsers, "", includePattern, excludePattern) {
            /** {@inheritDoc} */
            @Override
            protected Reader createReader(final File file) throws FileNotFoundException {
                return new InputStreamReader(ParserRegistryTest.class.getResourceAsStream(fileName));
            }
        };
        return parserRegistry;
    }
}

