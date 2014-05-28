package se.fidde.sorter;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SortTest extends TestCase {

    private Path path;

    @Before
    protected void setUp() throws Exception {
        path = Paths.get("testfolder");
    }

    @After
    protected void tearDown() throws Exception {
        deleteFolder(path);
    }

    private void deleteFolder(Path path) throws IOException {
        Stream<Path> streamToFiles = Files.find(path, 1,
                (p, attr) -> attr.isDirectory() || attr.isRegularFile(),
                FileVisitOption.FOLLOW_LINKS);

        streamToFiles.forEach(p -> {
            File file = p.toFile();
            deleteFile(file);
        });

        streamToFiles.close();
        deleteFile(path.toFile());
    }

    private void deleteFile(File file) {
        if (file.isDirectory() && file.list().length > 0) {
            File[] listFiles = file.listFiles();
            List<File> asList = Arrays.asList(listFiles);

            asList.forEach(File::delete);

        } else {
            file.delete();
        }
    }

    @Test
    public void testSortFiles_50_files() throws Exception {
        makeFolderWithMockFiles(path, 50);

        Sorter.sortDirectory(path);
        List<File> fileList = getFileList();

        assertEquals("sorter creates folders", 1, fileList.size());

        File folder = fileList.get(0);
        assertEquals("folder has same amount of files", 50,
                folder.list().length);

        assertEquals("old files are removed", 1, path.toFile().list().length);
    }

    @Test
    public void testSortFiles_51_files() throws Exception {
        makeFolderWithMockFiles(path, 51);

        Sorter.sortDirectory(path);
        List<File> folderList = getFileList();

        assertEquals("sorter creates folders", 2, folderList.size());

        int folder1 = folderList.get(0).list().length;
        int folder2 = folderList.get(1).list().length;

        assertEquals("folders has same amount of files", 51, folder1 + folder2);

        assertEquals("there is a folder with 1 file", 1,
                Math.min(folder1, folder2));

        assertEquals("there is a folder with 50 file", 50,
                Math.max(folder1, folder2));

        assertEquals("old files are removed", 2, path.toFile().list().length);
    }

    @Test
    public void testSortFiles_10000_files() throws Exception {
        makeFolderWithMockFiles(path, 10000);

        Sorter.sortDirectory(path);
        List<File> folderList = getFileList();

        assertEquals("sorter creates folders", 200, folderList.size());

        int sum = folderList.stream().mapToInt(f -> f.list().length).sum();
        assertEquals("all files are moved", 10000, sum);

        assertEquals("old files are removed", 200, path.toFile().list().length);
    }

    @Test
    public void testSortFiles_10001_files() throws Exception {
        makeFolderWithMockFiles(path, 10001);

        Sorter.sortDirectory(path);
        List<File> folderList = getFileList();

        assertEquals("sorter creates folders", 201, folderList.size());

        int sum = folderList.stream().mapToInt(f -> f.list().length).sum();
        assertEquals("all files are moved", 10001, sum);

        assertEquals("old files are removed", 201, path.toFile().list().length);
    }

    @Test
    public void testSortFiles_group_size_100() throws Exception {
        makeFolderWithMockFiles(path, 100);

        String[] args = { "-s 100" };
        Sorter.sortDirectory(path, args);

        List<File> fileList = getFileList();

        assertEquals("sorter creates folders", 1, fileList.size());

        File folder = fileList.get(0);
        assertEquals("folder has same amount of files", 100,
                folder.list().length);

        assertEquals("old files are removed", 1, path.toFile().list().length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSortFiles_group_size_100_fail() throws Exception {
        makeFolderWithMockFiles(path, 100);

        String[] args = { "-s 0" };
        Sorter.sortDirectory(path, args);

        List<File> fileList = getFileList();

        assertEquals("sorter creates folders", 1, fileList.size());

        File folder = fileList.get(0);
        assertEquals("folder has same amount of files", 100,
                folder.list().length);

        assertEquals("old files are removed", 1, path.toFile().list().length);
    }

    private List<File> getFileList() {
        File[] listFiles = path.toFile().listFiles();
        List<File> asList = Arrays.asList(listFiles);

        List<File> fileList = asList.parallelStream().filter(File::isDirectory)
                .collect(Collectors.toList());
        return fileList;
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
