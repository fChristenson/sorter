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
        root.toFile().mkdir();

        int length = root.toFile().list().length;
        if (length != 0) {
            deleteFolder(root);
            assertEquals("folder empty", 0, length);
        }
        foldersInRoot = 0;
        filesInFolders = 0;
        filesInRoot = 0;
    }

    @After
    protected void tearDown() throws Exception {
        deleteFolder(root);
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
    public void test50() throws Exception {
        makeFolderWithMockFiles(50);

        Sorter.sortDirectory(root);
        runBasicTests(1, 50, 1);
    }

    private void runBasicTests(int foldersExpected, int filesExpected,
            int remainingExpected) {
        getRootNumbers();
        getNumFilesInRootFolders();

        assertEquals("folders in root", foldersExpected, foldersInRoot);
        assertEquals("files in folders", filesExpected, filesInFolders);
        assertEquals("files in root", remainingExpected, filesInRoot);
    }

    private void getRootNumbers() {
        foldersInRoot = getRootFolders().size();
        filesInRoot = root.toFile().list().length;
    }

    @Test
    public void test51Files() throws Exception {
        makeFolderWithMockFiles(51);

        Sorter.sortDirectory(root);
        runBasicTests(2, 51, 2);
        assertFileSpread(1, 50);
    }

    @Test
    public void test10000Files() throws Exception {
        makeFolderWithMockFiles(10000);

        Sorter.sortDirectory(root);
        runBasicTests(200, 10000, 200);
    }

    @Test
    public void test10001Files() throws Exception {
        makeFolderWithMockFiles(10001);

        Sorter.sortDirectory(root);
        runBasicTests(201, 10001, 201);
    }

    @Test
    public void testSizeEqualToFiles() throws Exception {
        makeFolderWithMockFiles(1);

        Sorter.sortDirectory(root, "1");
        runBasicTests(1, 1, 1);
    }

    @Test
    public void testSizeLargerThanFiles() throws Exception {
        makeFolderWithMockFiles(1);

        Sorter.sortDirectory(root, "2");
        runBasicTests(1, 1, 1);
    }

    @Test
    public void testSizeLessThanFiles() throws Exception {
        makeFolderWithMockFiles(2);

        Sorter.sortDirectory(root, "1");
        runBasicTests(2, 2, 2);
    }

    @Test
    public void testByType() throws Exception {
        makeFolderWithMockFiles(2, JPG, GIF);

        Sorter.sortDirectory(root, JPG);
        assertTrue("file is correct type",
                getRootFolders().get(0).list()[0].contains(JPG));

        runBasicTests(1, 1, 2);
    }

    @Test
    public void testByType_2Types() throws Exception {
        makeFolderWithMockFiles(10, JPG, GIF);

        Sorter.sortDirectory(root, JPG, GIF);
        assertTypeSpread(5, 5);
        runBasicTests(2, 10, 2);
    }

    @Test
    public void testByType_fail() throws IOException {
        makeFolderWithMockFiles(1, JPG);

        try {
            Sorter.sortDirectory(root, "fail");
            fail("no exception");

        } catch (Exception e) {
            assertTrue("is right error", e.getMessage().contains("no valid"));

            String[] files = root.toFile().list();
            assertEquals("right amount of files in root", 1, files.length);
            assertTrue("file has not been touched", files[0].contains(JPG));
        }

    }

    @Test
    public void testByTypeAndSize_1TypeSize1() throws Exception {
        makeFolderWithMockFiles(3, ".jpg");

        Sorter.sortDirectory(root, ".jpg", "1");
        runBasicTests(3, 3, 3);
    }

    @Test
    public void testByTypeAndSize_3TypesSize1() throws Exception {
        makeFolderWithMockFiles(3, ".jpg", ".gif", ".flv");

        Sorter.sortDirectory(root, ".jpg", "1", ".gif", ".flv");
        runBasicTests(3, 3, 3);
    }

    private void getNumFilesInRootFolders() {
        filesInFolders = getRootFolders().stream()
                .mapToInt(f -> f.list().length).sum();
    }

    @Test
    public void testByTypeAndSize_2TypesSize2() throws Exception {
        makeFolderWithMockFiles(3, JPG, GIF);

        Sorter.sortDirectory(root, ".jpg", "2", ".gif");
        runBasicTests(2, 3, 2);
        assertFileSpread(1, 2);
        assertTypeSpread(1, 2);
    }

    private void assertTypeSpread(int smaller, int larger) {
        String string = getRootFolders().get(0).list()[0];

        if (string.contains(JPG)) {
            assertNumFilesOfType(larger, JPG, getRootFolders().get(0).list());
            assertNumFilesOfType(smaller, GIF, getRootFolders().get(1).list());

        } else {
            assertNumFilesOfType(smaller, GIF, getRootFolders().get(0).list());
            assertNumFilesOfType(larger, JPG, getRootFolders().get(1).list());

        }
    }

    private void assertFileSpread(int smaller, int larger) {
        String[] folder1 = getRootFolders().get(0).list();
        String[] folder2 = getRootFolders().get(1).list();
        assertEquals("folder with 1 file", smaller,
                Math.min(folder1.length, folder2.length));

        assertEquals("folder with 2 files", larger,
                Math.max(folder1.length, folder2.length));
    }

    private void assertNumFilesOfType(int expected, String type,
            String... fileName) {

        Stream<String> stream = Stream.of(fileName);
        long count = stream.filter(str -> str.contains(type)).count();

        assertEquals("right number of files", expected, count);
        assertEquals("no unwanted files", expected, fileName.length);
    }

    private List<File> getRootFolders() {
        if (rootFolders == null) {
            Stream<File> stream = Stream.of(root.toFile().listFiles());
            rootFolders = stream.filter(File::isDirectory).collect(
                    Collectors.toList());
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