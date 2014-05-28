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
    private static final String SIZE_FLAG = "-s";

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
                createGroupedList(fileList, size, groupedList,
                        DEFAULT_GROUP_SIZE);

                groupedList.forEach(list -> {
                    File newFolder = createNewFolderIn(path);
                    moveFilesToFolder(list, newFolder);
                });
            }
        }
    }

    private static void createGroupedList(List<File> fileList, int size,
            List<List<File>> groupedList, int groupSize) {

        for (int i = 0; i < size; i += groupSize) {
            groupedList.add(fileList.subList(i,
                    i + (Math.min(groupSize, size - i))));
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
        File file = path.toFile();

        if (file.exists() && file.isDirectory()) {
            File[] listFiles = file.listFiles();
            List<File> asList = Arrays.asList(listFiles);

            List<File> fileList = asList.parallelStream()
                    .filter(f -> !f.isDirectory()).collect(Collectors.toList());

            int size = fileList.size();

            int groupSize = getGroupSize(args);
            if (groupSize < 1) {
                throw new IllegalArgumentException("group size less than 1");

            } else if (size <= groupSize) {
                File newFolder = createNewFolderIn(path);
                moveFilesToFolder(fileList, newFolder);

            } else {
                List<List<File>> groupedList = new LinkedList<>();
                // we group the list by size so we can add each sublist to a new
                // folder
                createGroupedList(fileList, size, groupedList, groupSize);

                groupedList.forEach(list -> {
                    File newFolder = createNewFolderIn(path);
                    moveFilesToFolder(list, newFolder);
                });
            }
        }
    }

    private static int getGroupSize(String[] args) {
        List<String> asList = Arrays.asList(args);
        asList.stream().filter(str -> str.substring(0, 2).equals(SIZE_FLAG))
                .mapToInt(str -> {
                    String number = str.substring(2).trim();
                    if (number.matches("\\d{1,}")) {
                        return Integer.valueOf(number);
                    }
                    return -1;
                });
        return 0;
    }
}
