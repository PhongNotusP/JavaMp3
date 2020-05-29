package ru.javabegin.training.fastjava.mp3.utils;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class PlaylistFileFilter extends FileFilter {
    
    private final String fileExtension;
    private final String fileDescription;

    public PlaylistFileFilter(String fileExtension, String fileDescription) {
        this.fileExtension = fileExtension;
        this.fileDescription = fileDescription;
    }
  
    @Override
    public boolean accept(File file) {// chỉ cho các tệp đuôi mp3
        return file.isDirectory() || file.getAbsolutePath().endsWith(fileExtension);
    }   

    @Override
    public String getDescription() {// định dạng mp3 khi được chọn
        return fileDescription+" (*."+fileExtension+")";
    }
}
