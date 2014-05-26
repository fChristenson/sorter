package se.amigos.sorter;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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

            int size = fileList.size();

            if (size <= DEFAULT_GROUP_SIZE) {
                File newFolder = createNewFolderIn(path);
                copyFilesToFolder(fileList, newFolder);
                removeFilesOnDisk(fileList);
            }
        }
    }

    private static void copyFilesToFolder(List<File> fileList, File newFolder) {

    }

    private static void removeFilesOnDisk(List<File> fileList) {

    }

    private static File createNewFolderIn(Path path) {
        File newFolder = new File(path.toString(), UUID.randomUUID().toString());
        boolean mkdir = newFolder.mkdir();

        if (mkdir) {
            return newFolder;
        }
        throw new IllegalArgumentException("Could not create folder: "
                + newFolder.toString());
    }
}
