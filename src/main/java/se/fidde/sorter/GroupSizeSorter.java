package se.fidde.sorter;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

class GroupSizeSorter extends AbstractSorter {
    private static final int DEFAULT_GROUP_SIZE = 50;

    static void sortByGroupSize(Path path, List<File> fileList, String[] args) {
        int groupSize = getGroupSize(args);
        List<List<File>> groupedList = createGroupedList(fileList,
                fileList.size(), groupSize);

        groupedList.forEach(list -> {
            File newFolder = createNewFolderIn(path);
            moveFilesToFolder(list, newFolder);
        });
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

    private static List<List<File>> createGroupedList(List<File> fileList,
            int size, int groupSize) {

        List<List<File>> result = new LinkedList<>();
        for (int i = 0; i < size; i += groupSize) {
            result.add(fileList.subList(i, i + (Math.min(groupSize, size - i))));
        }
        return result;
    }

}
