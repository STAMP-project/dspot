package fr.inria.diversify.dspot.dynamic;

import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.DSpotUtils;
import fr.inria.diversify.dspot.dynamic.processor.PrimitiveForNewProcessor;
import fr.inria.diversify.dspot.value.ValueFactory;
import fr.inria.diversify.processor.ProcessorUtil;
import fr.inria.diversify.processor.main.AddBlockEverywhereProcessor;
import fr.inria.diversify.profiling.processor.main.AbstractLoggingInstrumenter;
import fr.inria.diversify.dspot.dynamic.processor.FieldUsedInstrumenter;
import fr.inria.diversify.dspot.dynamic.processor.TestFinderProcessor;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.InitUtils;
import fr.inria.diversify.util.LoggerUtils;
import fr.inria.diversify.util.PrintClassUtils;
import spoon.reflect.factory.Factory;

import java.io.File;
import java.io.IOException;

/**
 * User: Simon
 * Date: 18/03/16
 * Time: 11:54
 */
public class AddLoggerMain {
    InputProgram inputProgram;

    public AddLoggerMain(InputConfiguration inputConfiguration) throws Exception, InvalidSdkException {
        InitUtils.initLogLevel(inputConfiguration);
        inputProgram = InitUtils.initInputProgram(inputConfiguration);

        String outputDirectory = inputConfiguration.getProperty("tmpDir") + "/tmp_" + System.currentTimeMillis();

        FileUtils.copyDirectory(inputProgram.getProgramDir(), outputDirectory);
        inputProgram.setProgramDir(outputDirectory);


        InitUtils.initDependency(inputConfiguration, false);
        String mavenHome = inputConfiguration.getProperty("maven.home",null);
        String mavenLocalRepository = inputConfiguration.getProperty("maven.localRepository",null);
        DSpotUtils.compileTests(inputProgram, mavenHome, mavenLocalRepository);
        addLogger();
    }

    protected void addLogger() throws IOException {
        Factory factory = InitUtils.initSpoon(inputProgram, false);

        LoggerUtils.applyProcessor(factory, new AddBlockEverywhereProcessor(inputProgram));

        ValueFactory valueFactory = new ValueFactory(inputProgram);

        FieldUsedInstrumenter m = new FieldUsedInstrumenter(inputProgram);
        m.setLogger(fr.inria.diversify.dspot.dynamic.logger.Logger.class.getCanonicalName());
        AbstractLoggingInstrumenter.reset();
        LoggerUtils.applyProcessor(factory, m);

        PrimitiveForNewProcessor pfnp = new PrimitiveForNewProcessor(inputProgram);
        pfnp.setLogger(fr.inria.diversify.dspot.dynamic.logger.Logger.class.getCanonicalName());
        LoggerUtils.applyProcessor(factory, pfnp);

        TestFinderProcessor tfp = new TestFinderProcessor(inputProgram, valueFactory);
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
        new AddLoggerMain(inputConfiguration);
    }
}
