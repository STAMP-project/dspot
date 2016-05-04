package fr.inria.diversify.dspot.dynamic;

import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.profiling.processor.ProcessorUtil;
import fr.inria.diversify.profiling.processor.main.AbstractLoggingInstrumenter;
import fr.inria.diversify.profiling.processor.main.FieldUsedInstrumenter;
import fr.inria.diversify.profiling.processor.main.TestFinderProcessor;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.InitUtils;
import fr.inria.diversify.util.LoggerUtils;
import fr.inria.diversify.util.PrintClassUtils;
import org.apache.commons.io.FileUtils;
import spoon.reflect.factory.Factory;

import java.io.File;
import java.io.IOException;

/**
 * User: Simon
 * Date: 18/03/16
 * Time: 11:54
 */
public class DynamicTestMain {
    InputProgram inputProgram;

    public DynamicTestMain(InputConfiguration inputConfiguration) throws Exception, InvalidSdkException {
        InitUtils.initLogLevel(inputConfiguration);
        inputProgram = InitUtils.initInputProgram(inputConfiguration);

        String outputDirectory = inputConfiguration.getProperty("tmpDir") + "/tmp_" + System.currentTimeMillis();

        FileUtils.copyDirectory(new File(inputProgram.getProgramDir()), new File(outputDirectory));
        inputProgram.setProgramDir(outputDirectory);

        InitUtils.initDependency(inputConfiguration);
        addLogger();
    }


    protected void addLogger() throws IOException {
        Factory factory = InitUtils.initSpoon(inputProgram, false);

        FieldUsedInstrumenter m = new FieldUsedInstrumenter(inputProgram);
        m.setLogger(fr.inria.diversify.dspot.dynamic.logger.Logger.class.getCanonicalName());
        AbstractLoggingInstrumenter.reset();
        LoggerUtils.applyProcessor(factory, m);

        TestFinderProcessor tfp = new TestFinderProcessor(inputProgram);
        tfp.setLogger(fr.inria.diversify.dspot.dynamic.logger.Logger.class.getCanonicalName());
        LoggerUtils.applyProcessor(factory, tfp);

        File fileFrom = new File(inputProgram.getAbsoluteSourceCodeDir());
        PrintClassUtils.printAllClasses(factory, fileFrom, fileFrom);

        String loggerPackage = fr.inria.diversify.dspot.dynamic.logger.Logger.class.getPackage().getName().replace(".", "/");
        File destDir = new File(inputProgram.getAbsoluteSourceCodeDir() + "/" + loggerPackage);
        File srcDir = new File(System.getProperty("user.dir") + "/src/main/java/" + loggerPackage);
        FileUtils.forceMkdir(destDir);

        FileUtils.copyDirectory(srcDir, destDir);

        ProcessorUtil.writeInfoFile(inputProgram.getProgramDir());
    }

    public static void main(String[] args) throws Exception, InvalidSdkException {
        InputConfiguration inputConfiguration = new InputConfiguration(args[0]);
        new DynamicTestMain(inputConfiguration);
    }
}
