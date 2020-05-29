package ru.javabegin.training.fastjava.mp3.interfaces;

public interface PlayControlListener {
    
    void playStarted(String name);
    
    void processScroll(int position);
    
    void playFinished();
    
}
