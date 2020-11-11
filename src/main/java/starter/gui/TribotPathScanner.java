package starter.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class TribotPathScanner {

    public File findTribotInstallDirectory() {
        for (File path : File.listRoots()) {
            Finder finder = new Finder();
            try {
                Files.walkFileTree(path.toPath(), finder);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            if (finder.getFile() != null) {
                return finder.getFile();
            }
        }
        return null;
    }

    public class Finder
            extends SimpleFileVisitor<Path> {

        private File file;

        public File getFile() {
            return this.file;
        }

        @Override
        public FileVisitResult visitFile(Path file,
                                         BasicFileAttributes attrs) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir,
                                                 BasicFileAttributes attrs) {
            final File f = dir.toFile();
            if (f.isDirectory() && f.getName().equals("tribot-gradle-launcher")
                    && f.getParent() != null && f.getParentFile().getName().equals("TRiBot")) {
                file = f.getParentFile();
                return FileVisitResult.TERMINATE;
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file,
                                               IOException exc) {
            return FileVisitResult.CONTINUE;
        }
    }
	
}
