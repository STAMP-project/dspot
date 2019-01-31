package com.atlassian.clover.reporters.html;

import clover.com.google.common.collect.Lists;
import clover.com.google.common.collect.Sets;
import clover.org.apache.velocity.VelocityContext;
import clover.antlr.TokenStreamException;
import com.atlassian.clover.api.registry.BlockMetrics;
import com.atlassian.clover.api.registry.BranchInfo;
import com.atlassian.clover.api.registry.ClassInfo;
import com.atlassian.clover.api.registry.MethodInfo;
import com.atlassian.clover.api.registry.SourceInfo;
import com.atlassian.clover.api.registry.StatementInfo;
import com.atlassian.clover.registry.entities.FullFileInfo;
import com.atlassian.clover.registry.entities.FullPackageInfo;
import com.atlassian.clover.registry.entities.FullProjectInfo;
import com.atlassian.clover.registry.util.EntityVisitorUtils;
import com.atlassian.clover.reporters.Column;
import com.atlassian.clover.reporters.Current;
import com.atlassian.clover.reporters.html.source.SourceRenderHelper;
import com.atlassian.clover.BitSetCoverageProvider;
import com.atlassian.clover.CloverDatabase;
import com.atlassian.clover.CoverageData;
import com.atlassian.clover.Logger;
import com.atlassian.clover.context.ContextSet;
import com.atlassian.clover.api.registry.ElementInfo;
import com.atlassian.clover.registry.FileElementVisitor;
import com.atlassian.clover.registry.metrics.HasMetricsFilter;
import com.atlassian.clover.registry.entities.TestCaseInfo;
import com.atlassian.clover.reporters.json.JSONException;
import com.atlassian.clover.reporters.json.JSONObject;
import com.atlassian.clover.reporters.util.CloverChartFactory;
import com.atlassian.clover.util.CloverUtils;
import eu.stamp_project.diff_test_selection.CloverReader;

import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static clover.com.google.common.collect.Lists.newArrayList;
import static clover.com.google.common.collect.Maps.newHashMap;

/**
 */
public class RenderFileAction implements Callable {
    protected static ThreadLocal<List<Column>> columnsTL;
    protected static ThreadLocal<ContextSet> contextSetTL;

    private final static Comparator TEST_METRICS_COMPARATOR = new Comparator() {
        @Override
        public int compare(Object object, Object object1) {
            BlockMetrics m = (BlockMetrics) ((Map.Entry) object).getValue();
            BlockMetrics m1 = (BlockMetrics) ((Map.Entry) object1).getValue();
            return (int) (1000 * (m1.getPcCoveredElements() - m.getPcCoveredElements()));
        }
    };

    protected final FullFileInfo fileInfo; // shared: call made to setDataProvider on local copy
    protected final HtmlRenderingSupportImpl renderingHelper; // shared - has static Pattern(thread safe) objects.
    protected final Current reportConfig; // shared - read only
    protected final VelocityContext velocity; // not shared
    protected final CloverDatabase database; /// shared - read + write to cache in mDb.getRegistry().getContextsAsString - synchronized cache there.
    protected final FullProjectInfo fullModel; // shared - call buildCaches first!
    protected final Map<Integer, CloverChartFactory.ChartInfo> charts;
    protected List<TestCaseInfo>[] testLineInfo;

    public RenderFileAction(
            FullFileInfo fileInfo,
            HtmlRenderingSupportImpl renderingHelper,
            Current report,
            VelocityContext velocity,
            CloverDatabase database,
            FullProjectInfo fullModel,
            Map<Integer, CloverChartFactory.ChartInfo> charts) {

        this.fileInfo = fileInfo;
        this.renderingHelper = renderingHelper;
        this.reportConfig = report;
        this.velocity = velocity;
        this.database = database;
        this.fullModel = fullModel;
        this.charts = charts;
    }

    /**
     * Initialises all thread locals.
     * This is to be called once before the {@link #call} method .
     */
    public static void initThreadLocals() {
        columnsTL = new ThreadLocal<List<Column>>();
        contextSetTL = new ThreadLocal<ContextSet>();
    }

    /**
     * Resets all thread locals.
     * This is to be called once all files have been rendered.
     * NB: {@link ThreadLocal#remove} can't be used since it is since jdk 1.5 .
     */
    public static void resetThreadLocals() {
        columnsTL = null;
        contextSetTL = null;
    }

    @Override
    public Object call() throws Exception {
        //First action to be called per-thread sets up the TLS columns and contextset
        if (columnsTL.get() == null) {
            columnsTL.set(reportConfig.getColumns().getMethodColumnsCopy());
        }
        if (contextSetTL.get() == null) {
            contextSetTL.set(database.getContextSet(reportConfig.getFormat().getFilter()));
        }

        HtmlReportUtil.addColumnsToContext(velocity, columnsTL.get(), fileInfo, fileInfo.getClasses());
        render();
        return null;
    }

    public void render() throws Exception {
        final String srcname = fileInfo.getName();
        final String basename = new File(srcname).getName();
        final String outname = createOutFileName(basename);
        final File outfile = CloverUtils.createOutFile(fileInfo, outname, reportConfig.getOutFile());
        final String jsonOutFilename = createOutFileBaseName(basename) + "js";

        velocity.put("basename", basename);
        velocity.put("currentPageURL", outname);
        velocity.put("jsonFilename", jsonOutFilename);
        velocity.put("showLambdaFunctions", Boolean.valueOf(reportConfig.isShowLambdaFunctions()));
        velocity.put("showInnerFunctions", Boolean.valueOf(reportConfig.isShowInnerFunctions()));
        velocity.put("entityUtils", new EntityVisitorUtils());
        velocity.put("packageName", fileInfo.getContainingPackage().getName());

        try {
            insertLineInfos(insertSrcFileProperties(), testLineInfo);
        } catch (Exception e) {
            Logger.getInstance().error("Invalid Java source found or Clover failed to parse it: " + fileInfo.getPhysicalFile().getAbsolutePath());
            velocity.put("filename", fileInfo.getPhysicalFile().getAbsolutePath());
            velocity.put("message", e.getMessage());
            List srclines = SourceRenderHelper.getSrcLines(fileInfo);
            velocity.put("srclines", srclines);
            HtmlReportUtil.mergeTemplateToFile(outfile, velocity, "src-file-error.vm");
            return;
        }

        velocity.put("chartInfo", CloverChartFactory.getChartForFile(fileInfo, charts));

        HtmlReportUtil.mergeTemplateToFile(outfile, velocity, "src-file.vm");
        HtmlReportUtil.mergeTemplateToFile(
                CloverUtils.createOutFile(fileInfo, jsonOutFilename, reportConfig.getOutFile()),
                velocity, "src-file-json.vm");
    }

    @SuppressWarnings("unchecked")
    public FullFileInfo insertSrcFileProperties() throws TokenStreamException, JSONException {
        velocity.put("headerMetrics", fileInfo.getMetrics());
        velocity.put("headerMetricsRaw", fileInfo.getRawMetrics());
        velocity.put("fileInfo", fileInfo);
        final FullProjectInfo projInfo = fullModel;
        velocity.put("projInfo", projInfo);
        velocity.put("cloverDb", database);

        HtmlReportUtil.addFilteredPercentageToContext(velocity, fileInfo);

        /*
          generate map of testid -> methods hit
          generate map of testid -> statements / branches

          1. get list of tests that hit this file

          2. copy the fileinfo

          3. for each test
          3.1 get the coverage data provider for the test
          3.2 give the fileinfo copy the coverage data provider
          3.3 visit the nodes of the file, and build the maps

        */

        final Map<TestCaseInfo, BitSet> targetMethods = newHashMap();  // contains testid -> methodInfos
        final Map<TestCaseInfo, BitSet> targetElements = newHashMap(); // contains testid -> statements & branches
        final Map<TestCaseInfo, BlockMetrics> testMetrics = newHashMap(); // testid -> metrics
        Set<TestCaseInfo> testHits = database.getTestHits(fileInfo);
        FullFileInfo fcopy = fileInfo.copy((FullPackageInfo) fileInfo.getContainingPackage(), HasMetricsFilter.ACCEPT_ALL);
        Set<TestCaseInfo> testSet = Sets.newHashSet();

        final List<TestCaseInfo>[] testLineInfo = (List<TestCaseInfo>[]) new ArrayList[fcopy.getLineCount() + 1];

        for (final TestCaseInfo tci : testHits) {
            testSet.clear();
            testSet.add(tci);

            final CoverageData data = database.getCoverageData();
            fcopy.setDataProvider(new BitSetCoverageProvider(data.getHitsFor(testSet, fcopy), data));

            testMetrics.put(tci, fcopy.getMetrics());

            fcopy.visitElements(new FileElementVisitor() {
                @Override
                public void visitClass(ClassInfo info) {

                }

                @Override
                public void visitMethod(MethodInfo info) {
                    if (info.getHitCount() > 0) {
                        updateTestLineInfo(info);
                        BitSet set = targetMethods.get(tci);
                        if (set == null) {
                            set = new BitSet();
                            targetMethods.put(tci, set);
                        }
                        set.set(info.getStartLine());
                    }
                }

                @Override
                public void visitStatement(StatementInfo info) {
                    visitNode(info);
                }

                @Override
                public void visitBranch(BranchInfo info) {
                    visitNode(info);
                }

                private void visitNode(ElementInfo info) {
                    if (info.getHitCount() > 0) {
                        updateTestLineInfo(info);
                        BitSet set = targetElements.get(tci);
                        if (set == null) {
                            set = new BitSet();
                            targetElements.put(tci, set);
                        }
                        set.set(info.getStartLine());

                    }
                }

                private void updateTestLineInfo(SourceInfo r) {
                    int sl = r.getStartLine();
                    List<TestCaseInfo> tests = testLineInfo[sl];
                    if (tests == null) {
                        tests = newArrayList();
                        testLineInfo[sl] = tests;
                    }
                    if (!isSetForThisTest(targetElements, sl) && !isSetForThisTest(targetMethods, sl)) {
                        tests.add(tci);
                    }
                }

                private boolean isSetForThisTest(Map<TestCaseInfo, BitSet> m, int i) {
                    BitSet bsc = m.get(tci);
                    return bsc != null && bsc.get(i);
                }
            });
        }

        this.testLineInfo = testLineInfo;

        final Map<TestCaseInfo, BlockMetrics> orderedTestMetrics =
                new LinkedHashMap<TestCaseInfo, BlockMetrics>(testMetrics.size());
        final List<Map.Entry<TestCaseInfo, BlockMetrics>> testMetricList =
                Lists.newLinkedList(testMetrics.entrySet());
        Collections.sort(testMetricList, TEST_METRICS_COMPARATOR);

        final List<Map.Entry<TestCaseInfo, BlockMetrics>> sublist;
        if (reportConfig.getMaxTestsPerFile() >= 0 && // ensure a value has been set
                !testMetricList.isEmpty() &&
                testMetricList.size() > reportConfig.getMaxTestsPerFile()) {
            sublist = testMetricList.subList(0, reportConfig.getMaxTestsPerFile());
        } else {
            sublist = testMetricList;
        }
        for (Map.Entry<TestCaseInfo, BlockMetrics> entry : sublist) {
            orderedTestMetrics.put(entry.getKey(), entry.getValue());
        }

        velocity.put("testMetrics", orderedTestMetrics);
        velocity.put("numTargetMethods", new Integer(targetMethods.size()));
        velocity.put("testsPerFile", new Integer(reportConfig.getMaxTestsPerFile()));

        /*
            Here, we introduce a way to get the json containing the information we need,
             i.e., the contribution of each test case
             TODO Implement our own reporter, that compute this information, and do not build the whole HTML report
             TODO which is unused in DSpot
         */
        final JSONObject jsonTestTargets = JSONObjectFactory.getJSONTestTargets(targetMethods, targetElements);
        final String targetClassName = this.fileInfo.getContainingPackage().getName() + "." + this.fileInfo.getName().split("\\.")[0];
        if (!CloverReader.coveragePerTestMethods.containsKey(targetClassName)) {
            this.buildCoverage(sublist, jsonTestTargets, targetClassName);
        }
        velocity.put("jsonTestTargets", jsonTestTargets);
        velocity.put("jsonPageData", JSONObjectFactory.getJSONPageData(fileInfo));

        if (sublist.size() < testMetricList.size()) {
            velocity.put("numTestsHidden", new Integer(testMetricList.size() - sublist.size()));
        }

        return fcopy;
    }

    private void buildCoverage(List<Map.Entry<TestCaseInfo, BlockMetrics>> sublist,
                               JSONObject jsonTestTargets,
                               String targetClassName) throws JSONException {
        final Iterator keys = jsonTestTargets.keys();
        while (keys.hasNext()) {
            final String key = (String) keys.next();
            final TestCaseInfo testCaseInfo = getTestCaseInfo(sublist, key.split("_")[1]);
            final String testClassName = testCaseInfo.getRuntimeTypeName();
            final String testName = testCaseInfo.getTestName();
            if (!CloverReader.coveragePerTestMethods.containsKey(testClassName)) {
                CloverReader.coveragePerTestMethods.put(testClassName, new HashMap<>());
            }
            if (!CloverReader.coveragePerTestMethods.get(testClassName).containsKey(testName)) {
                CloverReader.coveragePerTestMethods.get(testClassName).put(testName, new HashMap<>());
            }
            if (!CloverReader.coveragePerTestMethods.get(testClassName).get(testName).containsKey(targetClassName)) {
                CloverReader.coveragePerTestMethods.get(testClassName).get(testName).put(targetClassName, new ArrayList<>());
            }
            JSONObject currentValues = jsonTestTargets.getJSONObject((String) key);
            ((List) currentValues.get("statements")).stream()
                    .map(list -> ((Map) list).get("sl"))
                    .forEach(line ->
                            CloverReader.coveragePerTestMethods.get(testClassName).get(testName).get(targetClassName).add((Integer) line)
                    );

        }
    }

    private TestCaseInfo getTestCaseInfo(List<Map.Entry<TestCaseInfo, BlockMetrics>> sublist, String s) {
        return sublist.stream().filter(entry -> entry.getKey().getId().equals(Integer.parseInt(s))).findFirst().get().getKey();
    }


    private void insertLineInfos(FullFileInfo fcopy, List[] testLineInfo) throws TokenStreamException {
        new SourceRenderHelper(database, reportConfig, renderingHelper)
                .insertLineInfosForFile(fcopy, velocity, getContextSet(), "&#160;", testLineInfo);
    }

    protected ContextSet getContextSet() {
        return contextSetTL.get();
    }

    static String createOutFileName(String basename) {
        return createOutFileBaseName(basename) + "html";
    }

    protected static String createOutFileBaseName(String basename) {
        return basename.substring(0, basename.lastIndexOf(".") + 1);
    }
}
