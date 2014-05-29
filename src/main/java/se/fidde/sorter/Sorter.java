package se.fidde.sorter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Sorter {

    private static final int DEFAULT_GROUP_SIZE = 50;

    public static void sortDirectory(Path path, String... args) {
        File file = path.toFile();

        if (file.exists() && file.isDirectory()) {
            Stream<File> stream = Stream.of(file.listFiles());

            List<File> fileList = stream.parallel()
                    .filter(f -> !f.isDirectory()).collect(Collectors.toList());

            int size = fileList.size();

            boolean sortBySuffix = hasSuffixArgs(args);
            if (sortBySuffix) {
                sortBySuffix(fileList, path, args);

            } else {
                sortByGroupSize(path, fileList, size, args);
            }
        }
    }

    private static void sortByGroupSize(Path path, List<File> fileList,
            int size, String... args) {
        // we group the list by size so we can add each sublist to a new
        // folder
        int groupSize = getGroupSize(args);
        List<List<File>> groupedList = createGroupedList(fileList, size,
                groupSize);

        groupedList.forEach(list -> {
            File newFolder = createNewFolderIn(path);
            moveFilesToFolder(list, newFolder);
        });
    }

    private static boolean hasSuffixArgs(String[] args) {
        Stream<String> stream = Stream.of(args);
        long count = stream.filter(str -> str.matches("\\.\\w+")).count();
        return count > 0;
    }

    private static List<List<File>> createGroupedList(List<File> fileList,
            int size, int groupSize) {

        List<List<File>> result = new LinkedList<>();
        for (int i = 0; i < size; i += groupSize) {
            result.add(fileList.subList(i, i + (Math.min(groupSize, size - i))));
        }
        return result;
    }

    private static void moveFilesToFolder(List<File> fileList, File newFolder) {
        fileList.parallelStream().forEach(
                file -> {
                    try {
                        Path path = Paths.get(newFolder.toString(),
                                file.getName());
                        Files.move(file.toPath(), path,
                                StandardCopyOption.REPLACE_EXISTING);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private static void sortBySuffix(List<File> fileList, Path path,
            String[] suffix) {

        List<String> suffixList = getSuffixList(suffix);
        List<List<File>> groupedList = createListGroupedBySuffix(fileList,
                suffixList);

        groupedList.parallelStream().forEach(list -> {
            File folder = createNewFolderIn(path);
            moveFilesToFolder(list, folder);
        });
    }

    private static List<List<File>> createListGroupedBySuffix(
            List<File> fileList, List<String> suffixList) {
        List<List<File>> groupedList = new LinkedList<>();
        suffixList.parallelStream().forEach(
                str -> {
                    List<File> list = fileList.parallelStream()
                            .filter(f -> f.getName().contains(str))
                            .collect(Collectors.toList());
                    groupedList.add(list);
                });
        return groupedList;
    }

    private static List<String> getSuffixList(String[] suffix) {
        Stream<String> stream = Stream.of(suffix);
        List<String> suffixList = stream.filter(
                str -> str.matches("\\.\\w{1,3}")).collect(Collectors.toList());

        return suffixList;
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

    private static int getGroupSize(String[] args) {
        Stream<String> stream = Stream.of(args);
        int sum = stream.filter(str -> str.trim().matches("\\d+"))
                .mapToInt(str -> {
                    return Integer.valueOf(str);
                }).sum();

        if (sum < 1) {
            return DEFAULT_GROUP_SIZE;
        }
        return sum;
    }
}
