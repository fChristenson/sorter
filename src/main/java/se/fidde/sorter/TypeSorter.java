package se.fidde.sorter;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class TypeSorter {
    static List<List<File>> sortBySuffix(List<File> fileList, String[] suffix) {

        List<String> suffixList = getSuffixList(suffix);
        return createListGroupedBySuffix(fileList, suffixList);
    }

    private static List<List<File>> createListGroupedBySuffix(
            List<File> fileList, List<String> suffixList) {
        List<List<File>> groupedList = new LinkedList<>();
        suffixList.stream().forEach(
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
