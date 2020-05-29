package ru.javabegin.training.fastjava.mp3.interfaces;

import java.io.File;

public interface PlayList {

    void next();

    void prev();
    
    void delete();
    
    void clear();

    boolean search(String name);

    boolean savePlaylist(File file);

    boolean openFiles(File[] files);
    
    boolean openPlayList(File file);
    
    void playFile();    

}
