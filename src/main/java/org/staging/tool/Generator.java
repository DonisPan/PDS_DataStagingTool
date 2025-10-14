package org.staging.tool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Generator {

    public Generator() {}

    public void generate() throws IOException {
        // example how to create a file for insert scripts
        File f = makeFile("Test1");
        FileWriter fw = new FileWriter(f, true);
        fw.append("This is a test file.\n");
        fw.close();
    }

    private File makeFile(String fileName) {
        String projectDir = System.getProperty("user.dir");
        return new File(projectDir + "/out_files", fileName + ".sql");
    }
}
