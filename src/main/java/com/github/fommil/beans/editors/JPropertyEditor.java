/*
 * Created 01-Jun-2012
 * 
 * Copyright Samuel Halliday 2012
 * PROPRIETARY/CONFIDENTIAL. Use is subject to licence terms.
 */
package com.github.fommil.beans.editors;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import javax.swing.*;
import org.jdesktop.swingx.JXButton;

/**
 * Allows {@link PropertyEditor}s to be written as popups (by extending this)
 * with a text field and icon as placeholder. Although not a {@link Component}
 * itself, the main purpose of this is the return value of
 * {@link #getCustomEditor()}.
 * 
 * @author Samuel Halliday
 */
public abstract class JPropertyEditor extends PropertyEditorSupport {

    private final JLabel label = new JLabel();

    {
        label.setFocusable(false);
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    private final ActionListener action = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            showEditor();
        }
    };

    @Override
    public void setValue(Object value) {
        super.setValue(value);
        label.setText(getAsText());
    }

    @Override
    public Component getCustomEditor() {
        JPanel jp = new JPanel();
        jp.setLayout(new BorderLayout());
        jp.add(label, BorderLayout.CENTER);

        Icon icon = getIcon();
        JXButton button;
        if (icon != null) {
            button = new JXButton(icon);
        } else {
            button = new JXButton("edit");
        }
        button.addActionListener(action);
        button.setFocusable(false);
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        toolbar.setLayout(new FlowLayout(FlowLayout.CENTER));
        toolbar.add(button);
        jp.add(toolbar, BorderLayout.WEST);

        return jp;
    }

    /**
     * Trigger for the custom editor to appear (in a pop-up or otherwise).
     * This should only exit when the custom editor has selected an item.
     * Remember to set the value by calling {@link #setValue(Object)}
     */
    public abstract void showEditor();

    /**
     * Intentionally for sub-classing.
     * 
     * @return icon as a prompt for the user to click for the popup. {@code null}
     * will be replaced by a text button.
     */
    public Icon getIcon() {
        return null;
    }
}