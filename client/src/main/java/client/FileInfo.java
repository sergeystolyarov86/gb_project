package client;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileInfo {
    public enum FileType{
        File("FILE"),DIRECTORY("\uD83D\uDCC1");
        private String name;
        public  String getName(){
            return name;
        }
        FileType(String name){
            this.name = name;
        }
    }
    private String filename;
    private FileType type;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public FileType getType() {
        return type;
    }

    public void setType(FileType type) {
        this.type = type;
    }
    public FileInfo(Path path){
        this.filename = path.getFileName().toString();
        this.type= Files.isDirectory(path)? FileType.DIRECTORY:FileType.File;
    }
}
