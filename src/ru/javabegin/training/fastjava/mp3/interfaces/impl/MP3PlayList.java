package ru.javabegin.training.fastjava.mp3.interfaces.impl;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import ru.javabegin.training.fastjava.mp3.gui.JListDropHandler;
import ru.javabegin.training.fastjava.mp3.interfaces.PlayList;
import ru.javabegin.training.fastjava.mp3.interfaces.Player;
import ru.javabegin.training.fastjava.mp3.objects.MP3;
import ru.javabegin.training.fastjava.mp3.utils.FileUtils;

// плейлист на основе компонента JList
public class MP3PlayList implements PlayList {

    public static final String PLAYLIST_FILE_EXTENSION = "pls";
    public static final String PLAYLIST_FILE_DESCRIPTION = "Danh sách phát";

    private static final String EMPTY_STRING = "";

    private Player player;

    private JList playlist;
    private DefaultListModel model = new DefaultListModel();

    public MP3PlayList(JList playlist, Player player) {
        this.playlist = playlist;
        this.player = player;
        initDragDrop();
        initPlayList();
    }

    @Override
    public void next() {
        int nextIndex = playlist.getSelectedIndex() + 1;
        if (nextIndex <= model.getSize() - 1) {// nếu bạn không thể rời khỏi danh sách
            playlist.setSelectedIndex(nextIndex);
            playFile();
        }
    }

    @Override
    public void prev() {
        int nextIndex = playlist.getSelectedIndex() - 1;
        if (nextIndex >= 0) {// nếu bạn không thể rời khỏi danh sách
            playlist.setSelectedIndex(nextIndex);
            playFile();
        }
    }

    @Override
    public boolean search(String name) {

        // nếu không nhập gì vào tìm kiếm
        if (name == null || name.trim().equals(EMPTY_STRING)) {
            return false;
        }

        // Tất cả bài được tìm thấy bởi keyword trong bộ sưu tập
        ArrayList<Integer> mp3FindedIndexes = new ArrayList<Integer>();

        // tìm tên bài hát phù hợp khi nhập vào thanh tìm kiếm
        for (int i = 0; i < model.getSize(); i++) {
            MP3 mp3 = (MP3) model.getElementAt(i);
            // tìm kiếm bài hát không phân biệt hoa thường
            if (mp3.getName().toUpperCase().contains(name.toUpperCase())) {
                mp3FindedIndexes.add(i);// chọn bài vào bộ sưu tập
            }
        }

        // tập hợp các mục vào 1 mảng
        int[] selectIndexes = new int[mp3FindedIndexes.size()];

        if (selectIndexes.length == 0) {// nếu không tìm thấy bài nào với tiêu chí tìm kiếm
            return false;
        }
        
        for (int i = 0; i < selectIndexes.length; i++) {
            selectIndexes[i] = mp3FindedIndexes.get(i).intValue();
        }

        
        playlist.setSelectedIndices(selectIndexes);

        return true;
    }

    @Override
    public boolean savePlaylist(File file) {
        try {
            String fileExtension = FileUtils.getFileExtension(file);

            //tên file 
            String fileNameForSave = (fileExtension != null && fileExtension.equals(PLAYLIST_FILE_EXTENSION)) ? file.getPath() : file.getPath() + "." + PLAYLIST_FILE_EXTENSION;

            FileUtils.serialize(model, fileNameForSave);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean openFiles(File[] files) {

        boolean status = false;

        for (File file : files) {
            MP3 mp3 = new MP3(file.getName(), file.getPath());

           // nếu đã có trong danh sách thì không thêm nữa
            if (!model.contains(mp3)) {
                model.addElement(mp3);
                status = true;
            }
        }

        return status;
    }

    @Override
    public void playFile() {
        int[] indexPlayList = playlist.getSelectedIndices();
        if (indexPlayList.length > 0) {
            Object selectedItem = model.getElementAt(indexPlayList[0]);
            if (!(selectedItem instanceof MP3)) {
                return;
            }
            MP3 mp3 = (MP3) selectedItem;// tìm thấy bài hát được chọn đầu tiên 
            player.play(mp3.getPath());
        }

    }

    @Override
    public boolean openPlayList(File file) {
        try {
            DefaultListModel mp3ListModel = (DefaultListModel) FileUtils.deserialize(file.getPath());
            this.model = mp3ListModel;
            playlist.setModel(mp3ListModel);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void delete() {
        int[] indexPlayList = playlist.getSelectedIndices();// các lựa chọn số seri của bài hát

        if (indexPlayList.length > 0) {// khi chọn ít nhất 1 bài

            ArrayList<MP3> mp3ListForRemove = new ArrayList<MP3>();// lưu các bài đã xóa trong 1 file riêng

            for (int i = 0; i < indexPlayList.length; i++) {// xóa tất cả các bài đã chọn ra khỏi danh sách
                MP3 mp3 = (MP3) model.getElementAt(indexPlayList[i]);
                mp3ListForRemove.add(mp3);
            }

            // xóa bài ra khỏi danh sách
            for (MP3 mp3 : mp3ListForRemove) {
                model.removeElement(mp3);
            }

        }
    }

    @Override
    public void clear() {
        model.clear();
    }

    private void initPlayList() {

        playlist.setModel(model);
        playlist.setToolTipText("Danh sách bài hát");

        playlist.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // click chuột 2 lần
                if (evt.getModifiers() == InputEvent.BUTTON1_MASK && evt.getClickCount() == 2) {
                    playFile();
                }
            }
        });

        playlist.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                int key = evt.getKeyCode();
                if (key == KeyEvent.VK_ENTER) {
                    playFile();
                }
            }
        });
    }

    private DropTarget dropTarget;

    private void initDragDrop() {

        try {
            dropTarget = new DropTarget(playlist, DnDConstants.ACTION_COPY_OR_MOVE, null);
            dropTarget.addDropTargetListener(new JListDropHandler(playlist));

        } catch (TooManyListenersException ex) {
            Logger.getLogger(MP3PlayList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
