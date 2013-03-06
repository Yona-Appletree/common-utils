// Copyright (c) 2013 Samuel Halliday
package com.github.fommil.beans.editors;

import org.jdesktop.swingx.JXMonthView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * {@link PropertyEditor} that brings up a {@link JXMonthView}.
 * 
 * @author Samuel Halliday
 */
public class DatePropertyEditor extends JPropertyEditor {

    private final DateFormat format = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    public void showEditor() {
        final JDialog dialog = new JDialog();
        final JXMonthView picker = new JXMonthView((Date) getValue());
        picker.setTraversable(true);
        picker.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Date date = picker.getSelectionDate();
                setValue(date);
                dialog.setVisible(false);
            }
        });

        dialog.add(picker);
        dialog.pack();
        dialog.setLocation(MouseInfo.getPointerInfo().getLocation());
        dialog.setVisible(true);
    }

    @Override
    public String getAsText() {
        Date date = (Date) getValue();
        return date != null ? format.format(date) : "";
    }
}
