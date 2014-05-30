package se.fidde.sorter;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Sorter extends AbstractSorter {

    public static void sortDirectory(Path path, String... args)
            throws Exception {
        File file = path.toFile();

        if (file.exists() && file.isDirectory()) {
            Stream<File> stream = Stream.of(file.listFiles());

            List<File> fileList = stream.parallel()
                    .filter(f -> !f.isDirectory()).collect(Collectors.toList());

            int size = fileList.size();
            sort(path, fileList, size, args);
        }
    }

    private static void sort(Path path, List<File> fileList, int size,
            String... args) throws Exception {

        boolean sortBySuffix = hasSuffixArgs(args);
        if (sortBySuffix) {
            TypeSorter.sortBySuffix(fileList, path, args);

        } else {
            GroupSizeSorter.sortByGroupSize(path, fileList, size, args);
        }
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
