package se.amigos.sorter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    private void deleteFolder(Path path) {
        File file = path.toFile();

        if (!file.exists()) {
            return;

        } else if (file.list().length < 1) {
            file.delete();

        } else {
            File[] listFiles = file.listFiles();
            List<File> asList = Arrays.asList(listFiles);
            asList.forEach(File::delete);

            file.delete();
        }
    }

    @Test
    public void testGetSorter() throws Exception {
        assertNotNull("can get sorter", sorter);
    }

    @Test
    public void testSortFiles_50_files() throws Exception {
        makeFolderWithMockFiles(path, 50);

        Sorter.sortDirectory(path);
        File[] listFiles = path.toFile().listFiles();
        List<File> asList = Arrays.asList(listFiles);

        List<File> list = asList.parallelStream().filter(File::isDirectory)
                .collect(Collectors.toList());

        assertEquals("sorter creates 1 folder", 1, list.size());

        File folder = list.get(0);
        assertEquals("folder has same amount of files", 50, folder.list().length);

    }

    private void makeFolderWithMockFiles(Path path, int numFiles)
            throws IOException {
        File file = path.toFile();
        if (file.exists()) {
            return;
        }
        file.mkdir();

        for (int i = 0; i < numFiles; ++i) {
            File newFile = new File(path.toString(), String.valueOf(i));
            newFile.createNewFile();
        }
    }

}
