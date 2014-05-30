package se.fidde.sorter;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class TypeSorter extends AbstractSorter {
    static void sortBySuffix(List<File> fileList, Path path, String[] suffix) {

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

}
