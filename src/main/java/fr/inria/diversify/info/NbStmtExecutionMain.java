package fr.inria.diversify.info;

import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.info.processor.LogAllStmtInstrumenter;
import fr.inria.diversify.profiling.processor.ProcessorUtil;
import fr.inria.diversify.profiling.processor.main.AbstractLoggingInstrumenter;
import fr.inria.diversify.profiling.processor.test.TestLoggingInstrumenter;
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
 * Date: 21/04/16
 * Time: 16:20
 */
public class NbStmtExecutionMain {
    InputProgram inputProgram;

    public NbStmtExecutionMain(InputConfiguration inputConfiguration) throws Exception, InvalidSdkException {
        InitUtils.initLogLevel(inputConfiguration);
        inputProgram = InitUtils.initInputProgram(inputConfiguration);

        String outputDirectory = inputConfiguration.getProperty("tmpDir") + "/tmp_" + System.currentTimeMillis();

        FileUtils.copyDirectory(new File(inputProgram.getProgramDir()), new File(outputDirectory));
        inputProgram.setProgramDir(outputDirectory);

        InitUtils.initDependency(inputConfiguration);

        addMainLogger();
        addTestLogger();
        addLogFile();
    }

    protected void addMainLogger() throws IOException {
        Factory factory = InitUtils.initSpoon(inputProgram, false);

        LogAllStmtInstrumenter m = new LogAllStmtInstrumenter(inputProgram);
        m.setLogger(fr.inria.diversify.info.logger.Logger.class.getCanonicalName());
        AbstractLoggingInstrumenter.reset();
        LoggerUtils.applyProcessor(factory, m);


        File fileFrom = new File(inputProgram.getAbsoluteSourceCodeDir());
        PrintClassUtils.printAllClasses(factory, fileFrom, fileFrom);
    }


    protected void addTestLogger() throws IOException {
        Factory factory = InitUtils.initSpoon(inputProgram, true);

        TestLoggingInstrumenter m = new TestLoggingInstrumenter();
        m.setLogger(fr.inria.diversify.info.logger.Logger.class.getCanonicalName());
        AbstractLoggingInstrumenter.reset();
        LoggerUtils.applyProcessor(factory, m);


        File fileFrom = new File(inputProgram.getAbsoluteTestSourceCodeDir());
        PrintClassUtils.printAllClasses(factory, fileFrom, fileFrom);
    }

    protected void addLogFile() throws IOException {
        String loggerPackage = fr.inria.diversify.info.logger.Logger.class.getPackage().getName().replace(".", "/");
        File destDir = new File(inputProgram.getAbsoluteSourceCodeDir() + "/" + loggerPackage);
        File srcDir = new File(System.getProperty("user.dir") + "/src/main/java/" + loggerPackage);
        FileUtils.forceMkdir(destDir);

        FileUtils.copyDirectory(srcDir, destDir);

        ProcessorUtil.writeInfoFile(inputProgram.getProgramDir());
    }
    public static void main(String[] args) throws Exception, InvalidSdkException {
        InputConfiguration inputConfiguration = new InputConfiguration(args[0]);
        new NbStmtExecutionMain(inputConfiguration);
    }
}
