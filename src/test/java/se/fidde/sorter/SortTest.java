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

    private Path root;
    private int foldersInRoot;
    private int filesInFolders;
    private int filesInRoot;
    private List<File> rootFiles;

    @Before
    protected void setUp() throws Exception {
        root = Paths.get("testfolder");
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
    public void testSortFiles_50() throws Exception {
        makeFolderWithMockFiles(50);

        Sorter.sortDirectory(root);

        foldersInRoot = getRootFiles().size();
        filesInFolders = getRootFiles().get(0).list().length;
        filesInRoot = root.toFile().list().length;

        assertCorrectNumFiles(1, 50, 1);
    }

    private void assertCorrectNumFiles(int foldersExpected, int filesExpected,
            int remainingExpected) {
        assertEquals("folders in root", foldersExpected, foldersInRoot);

        assertEquals("files in folders", filesExpected, filesInFolders);

        assertEquals("files in root", remainingExpected, filesInRoot);
    }

    @Test
    public void testSortFiles_51() throws Exception {
        makeFolderWithMockFiles(51);

        Sorter.sortDirectory(root);

        foldersInRoot = getRootFiles().size();
        int folder1 = getRootFiles().get(0).list().length;
        int folder2 = getRootFiles().get(1).list().length;
        filesInFolders = folder1 + folder2;
        filesInRoot = root.toFile().list().length;

        assertCorrectNumFiles(2, 51, 2);

        assertEquals("there is a folder with 1 file", 1,
                Math.min(folder1, folder2));

        assertEquals("there is a folder with 50 file", 50,
                Math.max(folder1, folder2));

    }

    @Test
    public void testSortFiles_10000() throws Exception {
        makeFolderWithMockFiles(10000);

        Sorter.sortDirectory(root);

        foldersInRoot = getRootFiles().size();
        filesInFolders = getRootFiles().stream().mapToInt(f -> f.list().length)
                .sum();
        filesInRoot = root.toFile().list().length;

        assertCorrectNumFiles(200, 10000, 200);
    }

    @Test
    public void testSortFiles_10001() throws Exception {
        makeFolderWithMockFiles(10001);

        Sorter.sortDirectory(root);

        foldersInRoot = getRootFiles().size();
        filesInFolders = getRootFiles().stream().mapToInt(f -> f.list().length)
                .sum();
        filesInRoot = root.toFile().list().length;

        assertCorrectNumFiles(201, 10001, 201);
    }

    @Test
    public void testSortFiles_groupSizeEqualToFiles() throws Exception {
        makeFolderWithMockFiles(1);

        String[] args = { "1" };
        Sorter.sortDirectory(root, args);

        foldersInRoot = getRootFiles().size();
        filesInFolders = getRootFiles().get(0).list().length;
        filesInRoot = root.toFile().list().length;

        assertCorrectNumFiles(1, 1, 1);
    }

    @Test
    public void testSortFiles_groupSizeLargerThanFiles() throws Exception {
        makeFolderWithMockFiles(1);

        String[] args = { "2" };
        Sorter.sortDirectory(root, args);

        foldersInRoot = getRootFiles().size();
        filesInFolders = getRootFiles().get(0).list().length;
        filesInRoot = root.toFile().list().length;

        assertCorrectNumFiles(1, 1, 1);
    }

    @Test
    public void testSortFiles_groupSizeLessThanFiles() throws Exception {
        makeFolderWithMockFiles(2);

        String[] args = { "1" };
        Sorter.sortDirectory(root, args);

        foldersInRoot = getRootFiles().size();
        int folder1 = getRootFiles().get(0).list().length;
        int folder2 = getRootFiles().get(1).list().length;
        filesInFolders = folder1 + folder2;
        filesInRoot = root.toFile().list().length;

        assertCorrectNumFiles(2, 2, 2);
    }

    @Test
    public void testSortFiles_groupByType() throws Exception {
        makeFolderWithMockFiles(2, ".jpg", ".gif");

        String[] args = { ".jpg" };
        Sorter.sortDirectory(root, args);

        assertTrue("file is correct type",
                getRootFiles().get(0).list()[0].contains(".jpg"));

        foldersInRoot = getRootFiles().size();
        filesInFolders = getRootFiles().get(0).list().length;
        filesInRoot = root.toFile().list().length;

        assertCorrectNumFiles(1, 1, 2);
    }

    @Test
    public void testSortFiles_groupByType_2() throws Exception {
        fail("make test with 2 types");
    }

    private List<File> getRootFiles() {
        if (rootFiles == null) {
            Stream<File> stream = Stream.of(root.toFile().listFiles());
            rootFiles = stream.parallel().filter(File::isDirectory)
                    .collect(Collectors.toList());
            return rootFiles;
        }
        return rootFiles;
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