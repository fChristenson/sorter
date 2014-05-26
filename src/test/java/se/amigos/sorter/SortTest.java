package se.amigos.sorter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

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

        long count = asList.parallelStream().filter(File::isDirectory).count();
        assertEquals("sorter creates 1 folder", 1, count);

        count = asList.parallelStream().filter(f -> !f.isDirectory()).count();
        assertEquals("folder has same amount of files", 50, count);
    }

    @Test
    public void testSortFiles_51_files() throws Exception {
        makeFolderWithMockFiles(path, 51);

        Sorter.sortDirectory(path);
        File[] listFiles = path.toFile().listFiles();
        List<File> asList = Arrays.asList(listFiles);

        long count = asList.parallelStream().filter(File::isDirectory).count();
        assertEquals("sorter creates 2 folders", 2, count);

        count = asList.parallelStream().filter(f -> !f.isDirectory()).count();
        assertEquals("folder has same amount of files", 50, count);
    }

    private void makeFolderWithMockFiles(Path path, int numFiles) throws IOException {
        File file = path.toFile();
        if (file.exists()) {
            return;
        }
        file.mkdir();

        for (int i = 0; i < numFiles; ++i) {
            Path subFile = Paths.get(path.toString() + "/" + i);
            subFile.toFile().createNewFile();
        }
    }

}
