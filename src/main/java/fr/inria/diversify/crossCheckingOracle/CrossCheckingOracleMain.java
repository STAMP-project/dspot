package fr.inria.diversify.crossCheckingOracle;

import fr.inria.diversify.buildSystem.AbstractBuilder;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.buildSystem.maven.MavenBuilder;
import fr.inria.diversify.coverage.NullCoverageReport;
import fr.inria.diversify.runner.*;
import fr.inria.diversify.transformation.query.FromListQuery;
import fr.inria.diversify.transformation.query.TransformationQuery;
import fr.inria.diversify.transformation.typeTransformation.InstanceTransformation;
import fr.inria.diversify.util.InitUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * User: Simon
 * Date: 02/10/15
 * Time: 10:48
 */
public class CrossCheckingOracleMain {
    private InputProgram inputProgram;
    private String outputDirectory;
    private InputConfiguration inputConfiguration;

    public CrossCheckingOracleMain(String propertiesFile) throws InvalidSdkException, Exception {
        init(propertiesFile);
        run();
    }

    public void run() throws Exception {
        CrossCheckingOracle crossCheckingOracle = new CrossCheckingOracle(inputProgram, outputDirectory);
        String output = crossCheckingOracle.generateTest();

        inputProgram.setCoverageReport(new NullCoverageReport());

        //init timeout and accepted errors
        AbstractBuilder initBuilder = new MavenBuilder(output);
        initBuilder.setGoals(new String[]{"clean", "test"});

        List<String> acceptedErrors = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            initBuilder.initTimeOut();
            acceptedErrors.addAll(initBuilder.getFailedTests());
        }
        int timeOut = initBuilder.getTimeOut() * 4;

        TransformationQuery query = query();

//        List<SinglePointRunner> runners = new ArrayList<>(4);
//        for(int i = 0; i < 4; i++) {
//           try {
               SinglePointRunner runner = new SinglePointRunner(inputConfiguration, output, inputProgram.getRelativeSourceCodeDir());
               runner.init(output, inputConfiguration.getProperty("tmpDir"));
               runner.setTransformationQuery(query);

               AbstractBuilder builder = new MavenBuilder(output);
               builder.setDirectory(runner.getTmpDir());
               builder.setGoals(new String[]{"clean", "test"});
               builder.setTimeOut(timeOut);
               builder.setAcceptedErrors(acceptedErrors);

               runner.setBuilder(builder);
//               runners.add(runner);
//           } catch (Exception e) {
//               e.printStackTrace();
//           }
//       }
//       runners.stream().parallel()
//                .forEach(runner -> {
//                    try {
                        while (query.hasNextTransformation()) {
                            runner.run(100);
                            writeResult(runner);
                        }
                        runner.deleteTmpFiles();

//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                });
    }

    protected void writeResult(AbstractRunner runner) {
        String repo = inputConfiguration.getProperty("gitRepository");

        if (repo.equals("null")) {
            runner.printResult(inputConfiguration.getProperty("result"));
        } else {
            runner.printResultInGitRepo(inputConfiguration.getProperty("result"), repo);
        }
    }

    protected TransformationQuery query() {
        int rangeMin = Integer.parseInt(inputConfiguration.getProperty("transformation.range.min", "0"));
        int rangeMax = Integer.parseInt(inputConfiguration.getProperty("transformation.range.max", "10000000"));
        FromListQuery query = new FromListQuery(inputProgram, rangeMin, rangeMax, true);
        query.getTransformations().stream()
                .forEach(t -> ((InstanceTransformation)t).setWithSwitch(true) );
        return query;
//        return new TypeTransformationQuery(inputProgram, "java.util.Collection:java.util.List:.*", true, true);
    }



    protected void init(String propertiesFile) throws Exception, InvalidSdkException {
        inputConfiguration = new InputConfiguration(propertiesFile);
        InitUtils.initLogLevel(inputConfiguration);
        inputProgram = InitUtils.initInputProgram(inputConfiguration);
        InitUtils.initDependency(inputConfiguration);

        InitUtils.initSpoon(inputProgram, true);

        outputDirectory = inputConfiguration.getProperty("tmpDir") + "/tmp_" + System.currentTimeMillis();
        new File(outputDirectory).mkdirs();
    }

    public static void main(String[] args) throws Exception, InvalidSdkException {
        CrossCheckingOracleMain main = new CrossCheckingOracleMain(args[0]);

    }
}
