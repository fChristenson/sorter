package se.fidde.sorter;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SortTest extends TestCase {

    private static final String GIF = ".gif";
    private static final String JPG = ".jpg";
    private Path root;
    private int foldersInRoot;
    private int filesInFolders;
    private int filesInRoot;
    private List<File> rootFolders;

    @Before
    protected void setUp() throws Exception {
        root = Paths.get("testfolder");
    }

    @After
    protected void tearDown() throws Exception {
        deleteFolder(root);
        foldersInRoot = 0;
        filesInFolders = 0;
        filesInRoot = 0;
    }

    private void deleteFolder(Path path) throws IOException {
        FileVisitor<Path> visitor = getFileVisitor();
        Files.walkFileTree(path, visitor);
    }

    private FileVisitor<Path> getFileVisitor() {
        FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file,
                    BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                    throws IOException {

                if (exc == null) {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;

                } else {
                    throw exc;
                }
            }
        };
        return visitor;
    }

    @Test
    public void testSortFiles_50() throws Exception {
        makeFolderWithMockFiles(50);

        Sorter.sortDirectory(root);

        getRootNumbers();
        filesInFolders = getRootFolders().get(0).list().length;

        assertNumFiles(1, 50, 1);
    }

    private void getRootNumbers() {
        foldersInRoot = getRootFolders().size();
        filesInRoot = root.toFile().list().length;
    }

    private void assertNumFiles(int foldersExpected, int filesExpected,
            int remainingExpected) {

        assertEquals("folders in root", foldersExpected, foldersInRoot);
        assertEquals("files in folders", filesExpected, filesInFolders);
        assertEquals("files in root", remainingExpected, filesInRoot);
    }

    @Test
    public void testSortFiles_51() throws Exception {
        makeFolderWithMockFiles(51);

        Sorter.sortDirectory(root);

        getRootNumbers();
        int folder1 = getRootFolders().get(0).list().length;
        int folder2 = getRootFolders().get(1).list().length;
        filesInFolders = folder1 + folder2;

        assertNumFiles(2, 51, 2);

        assertEquals("there is a folder with 1 file", 1,
                Math.min(folder1, folder2));

        assertEquals("there is a folder with 50 file", 50,
                Math.max(folder1, folder2));

    }

    @Test
    public void testSortFiles_10000() throws Exception {
        makeFolderWithMockFiles(10000);

        Sorter.sortDirectory(root);

        getRootNumbers();
        filesInFolders = getRootFolders().stream()
                .mapToInt(f -> f.list().length).sum();

        assertNumFiles(200, 10000, 200);
    }

    @Test
    public void testSortFiles_10001() throws Exception {
        makeFolderWithMockFiles(10001);

        Sorter.sortDirectory(root);

        getRootNumbers();
        filesInFolders = getRootFolders().stream()
                .mapToInt(f -> f.list().length).sum();

        assertNumFiles(201, 10001, 201);
    }

    @Test
    public void testSortFiles_groupSizeEqualToFiles() throws Exception {
        makeFolderWithMockFiles(1);

        String[] args = { "1" };
        Sorter.sortDirectory(root, args);

        getRootNumbers();
        filesInFolders = getRootFolders().get(0).list().length;

        assertNumFiles(1, 1, 1);
    }

    @Test
    public void testSortFiles_groupSizeLargerThanFiles() throws Exception {
        makeFolderWithMockFiles(1);

        String[] args = { "2" };
        Sorter.sortDirectory(root, args);

        getRootNumbers();
        filesInFolders = getRootFolders().get(0).list().length;

        assertNumFiles(1, 1, 1);
    }

    @Test
    public void testSortFiles_groupSizeLessThanFiles() throws Exception {
        makeFolderWithMockFiles(2);

        String[] args = { "1" };
        Sorter.sortDirectory(root, args);

        getRootNumbers();
        int folder1 = getRootFolders().get(0).list().length;
        int folder2 = getRootFolders().get(1).list().length;
        filesInFolders = folder1 + folder2;

        assertNumFiles(2, 2, 2);
    }

    @Test
    public void testSortFiles_groupByType() throws Exception {
        makeFolderWithMockFiles(2, JPG, GIF);

        String[] args = { JPG };
        Sorter.sortDirectory(root, args);

        assertTrue("file is correct type",
                getRootFolders().get(0).list()[0].contains(JPG));

        getRootNumbers();
        filesInFolders = getRootFolders().get(0).list().length;

        assertNumFiles(1, 1, 2);
    }

    @Test
    public void testSortFiles_groupByType_2() throws Exception {
        makeFolderWithMockFiles(10, JPG, GIF);

        String[] args = { JPG, GIF };
        Sorter.sortDirectory(root, args);

        String string = getRootFolders().get(0).list()[0];
        if (string.contains(JPG)) {
            assertNumFilesOfType(getRootFolders().get(0).list(), 5, JPG);
            assertNumFilesOfType(getRootFolders().get(1).list(), 5, GIF);

        } else {
            assertNumFilesOfType(getRootFolders().get(0).list(), 5, GIF);
            assertNumFilesOfType(getRootFolders().get(1).list(), 5, JPG);

        }

        getRootNumbers();
        filesInFolders = getRootFolders().get(0).list().length
                + getRootFolders().get(1).list().length;

        assertNumFiles(2, 10, 2);
    }

    @Test
    public void testSortFiles_groupByType_fail() throws IOException {
        makeFolderWithMockFiles(1, JPG);

        String[] args = { "fail" };
        try {
            Sorter.sortDirectory(root, args);
            fail("no exception");

        } catch (Exception e) {
            assertTrue("is right error", e.getMessage().contains("no valid"));

            String[] files = root.toFile().list();
            assertEquals("right amount of files in root", 1, files.length);
            assertTrue("file has not been touched", files[0].contains(JPG));
        }

    }

    @Test
    public void testGroupByTypeAndSize() throws Exception {
        makeFolderWithMockFiles(3, ".jpg");
        Sorter.sortDirectory(root, ".jpg", "1");

        getRootNumbers();
        filesInFolders = getRootFolders().stream()
                .mapToInt(f -> f.list().length).sum();
        assertNumFiles(3, 3, 3);
    }

    private void assertNumFilesOfType(String[] strings, int expected,
            String type) {
        Stream<String> stream = Stream.of(strings);
        long count = stream.filter(str -> str.contains(type)).count();
        assertEquals("right number of files", expected, count);
        assertEquals("no unwanted files", expected, strings.length);
    }

    private List<File> getRootFolders() {
        if (rootFolders == null) {
            Stream<File> stream = Stream.of(root.toFile().listFiles());
            rootFolders = stream.parallel().filter(File::isDirectory)
                    .collect(Collectors.toList());
            return rootFolders;
        }
        return rootFolders;
    }

    private void makeFolderWithMockFiles(int numFiles, String... suffixes)
            throws IOException {

        File file = root.toFile();
        if (!file.exists()) {
            file.mkdir();
        }

        for (int i = 0; i < numFiles; ++i) {
            String name = String.valueOf(i);
            if (suffixes.length > 0) {
                int index = i % suffixes.length;
                name += suffixes[index];
            }
            File newFile = new File(root.toString(), name);
            newFile.createNewFile();
        }
    }

}