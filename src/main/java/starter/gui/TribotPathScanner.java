package starter.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TribotPathScanner {

	private volatile File cached;
	
    public synchronized File findTribotInstallDirectory() {
    	if (this.cached != null) {
    		System.out.println("Using cached tribot dir");
    		return this.cached;
    	}
    	final List<File> search = new ArrayList<>();
    	Collections.addAll(search, File.listRoots());
    	final String pf = System.getenv("ProgramFiles");
    	if (pf != null) {
    		final File f = new File(pf);
    		if (f.exists() && f.isDirectory()) {
    			search.add(0, f);
    		}
    	}
        for (File path : search) {
            Finder finder = new Finder();
            try {
                Files.walkFileTree(path.toPath(), finder);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            if (finder.getFile() != null) {
                this.cached = finder.getFile();
                return this.cached;
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
