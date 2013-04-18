// Copyright (c) 2013 Samuel Halliday
package com.github.fommil.beans;

import com.github.fommil.beans.editors.DatePropertyEditor;
import com.github.fommil.beans.editors.FilePropertyEditor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorManager;
import java.beans.SimpleBeanInfo;
import java.io.File;
import java.util.Date;

/**
 * Interactive demo.
 * 
 * @author Samuel Halliday
 */
@Log
public class JBeanEditorTest {

    public static void main(String[] args) {
        PropertyEditorManager.registerEditor(File.class, FilePropertyEditor.class);
        PropertyEditorManager.registerEditor(Date.class, DatePropertyEditor.class);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JBeanEditor editor = new JBeanEditor();
        BeanHelper ender = new BeanHelper(new Object() {

            @Getter @Setter private File file;

            @Getter @Setter private Boolean button = false;

            @Getter @Setter private String name = "text";

//            private Color colour;
            @Getter @Setter private Date date;

        }, new SimpleBeanInfo() {
//            @Override
//            public Image getIcon(int iconKind) {
//                String logo = "http://docs.oracle.com/javase/6/docs/technotes/guides/deployment/deployment-guide/upgrade-guide/images/java_logo.gif";
//                try {
//                    return ImageIO.read(new URL(logo));
//                } catch (IOException ex) {
//                }
//                return null;
//            }
        });
        ender.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                JBeanEditorTest.log.info("Received Change: " + evt.getNewValue());
            }
        });
//        ender.addVetoableChangeListener(new VetoableChangeListener() {
//
//            @Override
//            public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
//                log.info("Received VetoableChange: " + evt.getNewValue());
//                throw new PropertyVetoException("No", evt);
//            }
//        });

        editor.setBeanHelper(ender);
        frame.add(editor, BorderLayout.CENTER);
        frame.setSize(600, 400);
        frame.pack();
        frame.setVisible(true);
    }
}
