package pvz;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class ProjectMerger {
    public static void main(String[] args) throws IOException {
        Path startingDir = Paths.get("app/src/main/java/pvz/"); // Looks in your src folder
        Path outputFile = Paths.get("MergedProject.txt");

        Files.writeString(outputFile, "=== COMPLETE PVZ PROJECT ===\n\n");

        Files.walkFileTree(startingDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".java") && !file.toString().contains("ProjectMerger")) {
                    Files.writeString(outputFile, "\n\n======================================================\n",
                            StandardOpenOption.APPEND);
                    Files.writeString(outputFile, "FILE: " + file.getFileName() + "\n", StandardOpenOption.APPEND);
                    Files.writeString(outputFile, "======================================================\n\n",
                            StandardOpenOption.APPEND);

                    String content = Files.readString(file);
                    Files.writeString(outputFile, content, StandardOpenOption.APPEND);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        System.out.println("Done! Check your project folder for Merged_Project.txt");
    }
}