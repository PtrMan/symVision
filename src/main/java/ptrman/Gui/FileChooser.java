/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.Gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.io.FileFilter;
import java.util.function.Consumer;

/**
 * Created by me on 5/1/15.
 */


/**
 This code uses a JList in two forms (layout orientation vertical & horizontal wrap) to
 display a File[].  The renderer displays the file icon obtained from FileSystemView.
 */
public enum FileChooser {
    ;


    public static Component newComponent(File dir, FileFilter filter, boolean vertical, final Consumer<File> l) {
        return newComponent(dir.listFiles(filter), vertical, l);
    }

    public static Component newComponent(File[] all, boolean vertical, final Consumer<File> l) {
        // put File objects in the list..
        JList fileList = new JList(all);
        // ..then use a renderer
        fileList.setCellRenderer(new FileRenderer(!vertical));

        if (!vertical) {
            fileList.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
            fileList.setVisibleRowCount(-1);
        } else {
            fileList.setVisibleRowCount(9);
        }

        if (l!=null) {
            fileList.addListSelectionListener(new ListSelectionListener() {
                File current = null;
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    File f = (File) fileList.getSelectedValue();
                    if (current!=f) {
                        current = f;
                        l.accept(f);
                    }
                }
            });
        }

        return new JScrollPane(fileList);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            File f = new File(System.getProperty("user.home"));
//            FileChooser fl = new FileChooser();
            Component c1 = newComponent(f, new TextFileFilter(), true, null);

            //f = new File(System.getProperty("user.home"));
            Component c2 = newComponent(f.listFiles(new TextFileFilter()), false, null);

            JFrame frame = new JFrame("File List");
            JPanel gui = new JPanel(new BorderLayout());
            gui.add(c1,BorderLayout.WEST);
            gui.add(c2,BorderLayout.CENTER);
            c2.setPreferredSize(new Dimension(375,100));
            gui.setBorder(new EmptyBorder(3,3,3,3));

            frame.setContentPane(gui);
            frame.pack();
            frame.setLocationByPlatform(true);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}



class TextFileFilter implements FileFilter {

    public boolean accept(File file) {
        // implement the logic to select files here..
        String name = file.getName().toLowerCase();
        //return name.endsWith(".java") || name.endsWith(".class");
        return name.length()<20;
    }
}

class FileRenderer extends DefaultListCellRenderer {

    private final boolean pad;
    private final Border padBorder = new EmptyBorder(3,3,3,3);

    FileRenderer(boolean pad) {
        this.pad = pad;
    }

    @Override
    public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        Component c = super.getListCellRendererComponent(
                list,value,index,isSelected,cellHasFocus);
        JLabel l = (JLabel)c;
        File f = (File)value;
        l.setText(f.getName());
        l.setIcon(FileSystemView.getFileSystemView().getSystemIcon(f));
        if (pad) {
            l.setBorder(padBorder);
        }

        return l;
    }
}
