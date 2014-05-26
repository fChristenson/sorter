package se.amigos.sorter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

public class SortTest extends TestCase {

    private static final int NUM_FILES = 10;
    private Sorter sorter;
    private Path path;

    protected void setUp() throws Exception {
        sorter = new Sorter();
        path = Paths.get("testfolder");
        makeFolder(path);
    }

    private void makeFolder(Path path) throws IOException {
        File file = path.toFile();
        if (file.exists()) {
            return;
        }
        file.mkdir();

        for (int i = 0; i < NUM_FILES; ++i) {
            Path subFile = Paths.get(path.toString() + "/" + i);
            subFile.toFile().createNewFile();
        }
    }

    protected void tearDown() throws Exception {
        deleteFolder(path);
    }

    private void deleteFolder(Path path) {
        File file = path.toFile();

        if (file.list().length < 1) {
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
    public void testSortFiles() throws Exception {
        Sorter.sortDirectory(path);
        File[] listFiles = path.toFile().listFiles();
        List<File> asList = Arrays.asList(listFiles);

        long count = asList.parallelStream().filter(File::isDirectory).count();
        assertTrue("sorter adds files to folders by defailt groupsize",
                count > 0);
    }
}
