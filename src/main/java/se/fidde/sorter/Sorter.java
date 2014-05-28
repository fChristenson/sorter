package se.fidde.sorter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.LinkedList;
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
                moveFilesToFolder(fileList, newFolder);

            } else {
                List<List<File>> groupedList = new LinkedList<>();
                // we group the list by size so we can add each sublist to a new
                // folder
                createGroupedList(fileList, size, groupedList);

                groupedList.forEach(list -> {
                    File newFolder = createNewFolderIn(path);
                    moveFilesToFolder(list, newFolder);
                });
            }
        }
    }

    private static void createGroupedList(List<File> fileList, int size,
            List<List<File>> groupedList) {

        for (int i = 0; i < size; i += DEFAULT_GROUP_SIZE) {
            groupedList.add(fileList.subList(i,
                    i + (Math.min(DEFAULT_GROUP_SIZE, size - i))));
        }
    }

    private static void moveFilesToFolder(List<File> fileList, File newFolder) {
        fileList.parallelStream().forEach(
                file -> {
                    try {
                        Path path = Paths.get(newFolder.toString(),
                                file.getName());
                        Files.move(file.toPath(), path,
                                StandardCopyOption.REPLACE_EXISTING);
                        file.delete();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
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

    public static void sortDirectory(Path path, String[] args) {

    }
}
