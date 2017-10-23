package fr.inria.diversify.utils.sosiefier;

import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtParameter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 18/05/15
 * Time: 10:27
 */
@Deprecated
public class ProcessorUtil {
    protected static List<String> ids = new LinkedList<>();
    protected static List<String> otherInfo = new LinkedList<>();


    public static int idFor(String key) {
        if(!ids.contains(key)) {
            ids.add(key);
        }
        return ids.indexOf(key);
    }



    public static void writeInfoFile(String dir) throws IOException {
        File file = new File(dir + "/log");
        file.mkdirs();
        FileWriter fw = new FileWriter(file.getAbsoluteFile() + "/info");

        for(int i = 0; i < ids.size(); i++) {
            fw.append("id;" + i + ";" + ids.get(i) + "\n");
        }

        for(String s : otherInfo) {
            fw.append(s + "\n");
        }

        fw.close();
    }

    public static void addInfo(String info) {
        otherInfo.add(info);
    }

    public static int methodId(CtExecutable method) {
        return ProcessorUtil.idFor(methodString(method));
    }

    public static String methodString(CtExecutable method){
        return method.getReference().getDeclaringType().getQualifiedName()
                + "." + method.getSimpleName()
                + "("
                + method.getParameters().stream()
                .map(param -> ((CtParameter) param).getType())
                .map(type -> type.toString())
                .collect(Collectors.joining(","))
                + ")";
    }
}
