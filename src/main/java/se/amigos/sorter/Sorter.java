package se.amigos.sorter;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Sorter {

    private static final int DEFAULT_GROUP_SIZE = 50;

    public static void sortDirectory(Path path) {
        File file = path.toFile();

        if (file.exists() && file.isDirectory()) {
            File[] listFiles = file.listFiles();
            List<File> asList = Arrays.asList(listFiles);

            List<File> fileList = asList.parallelStream()
                    .filter(f -> !f.isDirectory()).collect(Collectors.toList());

        }
    }
}
