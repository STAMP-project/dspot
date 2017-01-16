package fr.inria.diversify.dspot.support;

import fr.inria.diversify.buildSystem.DiversifyClassLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/16/17
 */
public class DSpotClassLoader extends DiversifyClassLoader {

    public DSpotClassLoader(ClassLoader parent, List<String> classPaths) {
        super(parent, classPaths);
    }

    public DSpotClassLoader(ClassLoader parent, String classPath) {
        super(parent, classPath);
    }

    @Override
    protected Class loadClass0(File classFile, String fullName) throws IOException {
        String url;
        if (new File(classFile.getAbsolutePath()).exists())
            url = "file:" + classFile.getAbsolutePath();
        else {
            url = "file:" + classFile.getPath();
        }
        URL myUrl = new URL(url);
        URLConnection connection = myUrl.openConnection();
        InputStream input = connection.getInputStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int data = input.read();

        while (data != -1) {
            buffer.write(data);
            data = input.read();
        }

        input.close();

        byte[] classData = buffer.toByteArray();
        return defineClass(fullName,
                classData, 0, classData.length);
    }
}
