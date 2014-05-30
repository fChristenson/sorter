package se.fidde.sorter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Sorter extends AbstractSorter {

    public static void sortDirectory(Path path, String... args)
            throws Exception {

        if (Files.exists(path) && Files.isDirectory(path)) {
            Stream<File> stream = Stream.of(path.toFile().listFiles());

            List<File> fileList = stream.parallel()
                    .filter(f -> !f.isDirectory()).collect(Collectors.toList());

            sort(path, fileList, args);
        }
    }

    private static void sort(Path path, List<File> fileList, String... args)
            throws Exception {

        boolean sortBySize = hasSizeArgs(args);
        boolean sortBySuffix = hasSuffixArgs(args);

        if (sortBySize && sortBySuffix) {
            List<List<File>> suffixList = TypeSorter.sortBySuffix(fileList,
                    args);

            suffixList.parallelStream().forEach(list -> {
                GroupSizeSorter.sortByGroupSize(path, fileList, args);
            });

        } else if (sortBySuffix) {
            List<List<File>> groupedList = TypeSorter.sortBySuffix(fileList,
                    args);

            groupedList.parallelStream().forEach(list -> {
                File folder = createNewFolderIn(path);
                moveFilesToFolder(list, folder);
            });

        } else {
            GroupSizeSorter.sortByGroupSize(path, fileList, args);
        }
    }

    private static boolean hasSizeArgs(String[] args) {
        Stream<String> stream = Stream.of(args);
        long count = stream.filter(str -> str.matches("\\d+")).count();
        if (count > 0) {
            return true;
        }
        return false;
    }

    private static boolean hasSuffixArgs(String[] args) throws Exception {
        Stream<String> stream = Stream.of(args);
        long count = stream.filter(str -> str.matches("\\.\\w+")).count();
        if (count < 1) {
            checkForSuffixErrors(Stream.of(args));
        }
        return count > 0;
    }

    private static void checkForSuffixErrors(Stream<String> stream)
            throws Exception {
        long count = stream.filter(str -> str.matches("\\D+")).count();
        if (count > 0) {
            throw new Exception("no valid suffix");
        }
    }
}