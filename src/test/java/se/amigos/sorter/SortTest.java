package se.amigos.sorter;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SortTest extends TestCase {

    private Sorter sorter;
    private Path path;

    @Before
    protected void setUp() throws Exception {
        sorter = new Sorter();
        path = Paths.get("testfolder");
    }

    @After
    protected void tearDown() throws Exception {
        deleteFolder(path);
    }

    private void deleteFolder(Path path) throws IOException {
        BiPredicate<Path, BasicFileAttributes> matcher = (p, attr) -> attr
                .isDirectory() || attr.isRegularFile();

        Stream<Path> streamToFiles = Files.find(path, 1, matcher,
                FileVisitOption.FOLLOW_LINKS);

        streamToFiles.forEach(p -> {
            File file = p.toFile();
            File[] listFiles = file.listFiles();
            List<File> asList = Arrays.asList(listFiles);

            asList.forEach(File::delete);
        });
        streamToFiles.forEach(p -> path.toFile().delete());
        streamToFiles.close();
        path.toFile().delete();
    }

    @Test
    public void testSortFiles_50_files() throws Exception {
        makeFolderWithMockFiles(path, 50);

        Sorter.sortDirectory(path);
        File[] listFiles = path.toFile().listFiles();
        List<File> asList = Arrays.asList(listFiles);

        List<File> fileList = asList.parallelStream().filter(File::isDirectory)
                .collect(Collectors.toList());

        assertEquals("sorter creates 1 folder", 1, fileList.size());

        File folder = fileList.get(0);
        assertEquals("folder has same amount of files", 50,
                folder.list().length);

        assertEquals("old files are removed", 1, path.toFile().list().length);

    }

    private void makeFolderWithMockFiles(Path path, int numFiles)
            throws IOException {
        File file = path.toFile();
        if (!file.exists()) {
            file.mkdir();
        }

        for (int i = 0; i < numFiles; ++i) {
            File newFile = new File(path.toString(), String.valueOf(i));
            newFile.createNewFile();
        }
    }

}
