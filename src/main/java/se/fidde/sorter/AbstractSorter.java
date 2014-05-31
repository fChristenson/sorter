package se.fidde.sorter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

abstract class AbstractSorter {
    protected static void moveFilesToFolder(List<File> fileList, File newFolder) {
        fileList.parallelStream().forEach(
                file -> {
                    try {
                        Path path = Paths.get(newFolder.toString(),
                                file.getName());
                        Files.move(file.toPath(), path,
                                StandardCopyOption.REPLACE_EXISTING);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    protected static File createNewFolderIn(Path path) {
        File newFolder = new File(path.toString(), UUID.randomUUID().toString());
        boolean mkdir = newFolder.mkdir();

        if (mkdir) {
            return newFolder;
        }
        throw new IllegalArgumentException("Could not create folder: "
                + newFolder.toString());
    }

}
