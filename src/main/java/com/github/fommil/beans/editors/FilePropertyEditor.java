// Copyright (c) 2013 Samuel Halliday
package com.github.fommil.beans.editors;

import lombok.extern.java.Log;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.*;
import java.beans.PropertyEditor;
import java.io.File;

/**
 * {@link PropertyEditor} that brings up a {@link JFileChooser}.
 *
 * @author Samuel Halliday
 */
@Log
@NotThreadSafe
public class FilePropertyEditor extends JPropertyEditor {

    // persists for the session
    private static volatile File lastDir;

    @Override
    public void showEditor() {
        final JFileChooser chooser = new JFileChooser();
        File file = (File) getValue();
        File dir = lastDir;
        if (file != null) {
            dir = file;
        }
        chooser.setCurrentDirectory(getDirectoryFor(dir));

        // we have no way of knowing if the user intends to open or save a file
        int returnVal = chooser.showDialog(null, "Choose File");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            setValue(selected);
            if (selected != null) {
                lastDir = getDirectoryFor(selected);
            }
        }
    }

    @Override
    public String getAsText() {
        File file = (File) getValue();
        return file != null ? file.getName() : "";
    }

    private File getDirectoryFor(File file) {
        if (file == null) {
            return null;
        }
        if (file.isDirectory()) {
            return file;
        }
        File parent = file.getParentFile();
        if (parent == null) {
            return new File(System.getProperty("user.dir"));
        }
        return parent;
    }
}
