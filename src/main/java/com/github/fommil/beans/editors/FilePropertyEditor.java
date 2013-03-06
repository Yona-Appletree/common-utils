/*
 * Created 01-Jun-2012
 * 
 * Copyright Samuel Halliday 2012
 * PROPRIETARY/CONFIDENTIAL. Use is subject to licence terms.
 */
package com.github.fommil.beans.editors;

import java.beans.PropertyEditor;
import java.io.File;
import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JFileChooser;
import lombok.extern.java.Log;

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
