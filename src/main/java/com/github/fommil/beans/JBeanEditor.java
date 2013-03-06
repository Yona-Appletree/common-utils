// Copyright (c) 2013 Samuel Halliday
package com.github.fommil.beans;

import com.github.fommil.beans.BeanHelper.Property;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.extern.java.Log;
import org.jdesktop.swingx.JXImageView;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.beans.*;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Iterables.filter;

/**
 * An automatically-generated Swing Form for editing arbitrary objects
 * matching the JavaBeans get/set pattern (properties) and optionally providing
 * a {@link BeanInfo}.
 * <p>
 * This class was created in order to provide a light-weight editor to do a lot
 * of boilerplate work in GUI design. Existing solutions require huge frameworks
 * and are neither attractive nor intuitive to end users.
 * <p>
 * Editing of Javabean properties is supported at runtime through the programmatic
 * provision of a suitable {@link PropertyEditor} to the {@link PropertyEditorManager},
 * or - for a specific property - by setting the value to be returned by
 * {@link PropertyDescriptor#createPropertyEditor(Object)} in the
 * {@link BeanInfo}.
 * <p>
 * The {@link BeanInfo} is used for the following:
 * <ul>
 * <li>{@link BeanInfo#getIcon(int)} is displayed, if available.</li>
 * <li>{@link PropertyDescriptor#isHidden()} is respected.</li>
 * <li>{@link PropertyDescriptor#isExpert()} results in the property name being shaded.</li>
 * <li>{@link PropertyDescriptor#getShortDescription()} will be shown as the
 * tooltip text for the entry.</li>
 * </ul>
 * This is not capable of detecting changes made to the
 * underlying bean by means other than the {@link BeanHelper} API,
 * in which case a call to {@link #refresh()} is recommended.
 * <p>
 * Read-only entries can be enforced using the {@link VetoableChangeListener}
 * exposed by the {@link BeanHelper} support class, but be warned that users
 * will still see the {@link PropertyEditor} and expect to be able to edit
 * the property.
 * 
 * @see <a href="http://stackoverflow.com/questions/10840078">Origin on Stack Overflow</a>
 * @author Samuel Halliday
 */
@Log
public final class JBeanEditor extends JPanel {

    /*
     * Implementation note: this uses JXTable as a backend because that
     * provides support for some basic object types and exposes functionality
     * to access entries by row/column index. If, at a later date, the
     * JXTable backend causes problems, it should be possible to rewrite this
     * as a pure JPanel with an appropriate Layout, e.g. GridBagLayout.
     * 
     * Note JDesktop.org BeansBinding might help reduce some boilerplate.
     */
    private final JPanel top = new JPanel();
    
    @Getter
    private volatile BeanHelper beanHelper;

    // links the table to the BeanHelper
    private static class MyTableModel extends AbstractTableModel {
        
        private final List<Property> properties;
        
        public MyTableModel(Iterable<Property> properties) {
            this.properties = Lists.newArrayList(properties);
        }
        
        @Override
        public void setValueAt(Object value, int row, int col) {
            Preconditions.checkArgument(row >= 0 && row < getRowCount());
            Preconditions.checkArgument(col >= 0 && col < getColumnCount());
            
            properties.get(row).setValue(value, JBeanEditor.class);
        }
        
        @Override
        public int getRowCount() {
            return properties.size();
        }
        
        @Override
        public int getColumnCount() {
            return 2;
        }
        
        public Class<?> getClassAt(int row, int col) {
            Preconditions.checkArgument(row >= 0 && row < getRowCount());
            Preconditions.checkArgument(col >= 0 && col < getColumnCount());
            switch (col) {
                case 0:
                    return String.class;
                default:
                    return properties.get(row).getPropertyClass();
            }
        }
        
        @Override
        public Object getValueAt(int row, int col) {
            Preconditions.checkArgument(row >= 0 && row < getRowCount());
            Preconditions.checkArgument(col >= 0 && col < getColumnCount());
            switch (col) {
                case 0:
                    return properties.get(row).getDisplayName() + ":";
                default:
                    return properties.get(row).getValue();
            }
        }
        
        @Override
        public boolean isCellEditable(int row, int col) {
            Preconditions.checkArgument(row >= 0 && row < getRowCount());
            if (col == 0) {
                return false;
            }
            return true;
        }
        
        @Override
        public String getColumnName(int col) {
            Preconditions.checkArgument(col >= 0 && col < getColumnCount());
            switch (col) {
                case 0:
                    return "names";
                default:
                    return "values";
            }
        }
        
        public String getToolTipText(int row) {
            Preconditions.checkArgument(row >= 0 && row < getRowCount());
            return properties.get(row).getShortDescription();
        }
        
        public boolean isExpert(int row, int col) {
            Preconditions.checkArgument(row >= 0 && row < getRowCount());
            Preconditions.checkArgument(col >= 0 && col < getColumnCount());
            return properties.get(row).isExpert();
        }
    }

    // allow per-cell rendering and editing via JavaBeans
    private final JXTable table = new JXTable() {
        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {
            MyTableModel model = (MyTableModel) getModel();
            if (column == 0) {
                // JXTable default renderer doesn't align right
                DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
                renderer.setToolTipText(model.getToolTipText(row));
                renderer.setHorizontalAlignment(JLabel.RIGHT);
                if (model.isExpert(row, column)) {
                    renderer.setForeground(Color.GRAY);
                }
                return renderer;
            }

            // code repetition with getCellEditor because of TableCell{Renderer, Editor} non-inheritance
            Class<?> klass = model.getClassAt(row, column);
            PropertyEditorTableAdapter javaBeansRenderer = PropertyEditorTableAdapter.forClass(klass);
            if (javaBeansRenderer != null) {
                return javaBeansRenderer;
            }
            return getDefaultRenderer(klass);
        }
        
        @Override
        public TableCellEditor getCellEditor(int row, int column) {
            MyTableModel model = (MyTableModel) getModel();
            if (column == 0) {
                return null;
            }

            // code repetition with getCellRenderer because of TableCell{Renderer, Editor} non-inheritance
            Class<?> klass = model.getClassAt(row, column);
            PropertyEditorTableAdapter javaBeansEditor = PropertyEditorTableAdapter.forClass(klass);
            if (javaBeansEditor != null) {
                return javaBeansEditor;
            }
            TableCellEditor defaultEditor = getDefaultEditor(klass);
            if (defaultEditor == null) {
                log.warning("No TableCellEditor for " + klass.getName());
            }
            if (defaultEditor instanceof DefaultCellEditor) {
                // default double-click is bad user interaction
                ((DefaultCellEditor) defaultEditor).setClickCountToStart(0);
            }
            return defaultEditor;
        }

//        @Override
//        public Component prepareEditor(TableCellEditor editor, int row, int column) {
//            Component prepareEditor = super.prepareEditor(editor, row, column);
//            customiseMinimumDimensions(prepareEditor.getMinimumSize(), row, column);
//            return prepareEditor;
//        }
        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            Component prepareRenderer = super.prepareRenderer(renderer, row, column);
            customiseMinimumDimensions(prepareRenderer.getMinimumSize(), row, column);
            return prepareRenderer;
        }

        // Set the width/height of columns/rows by the largest rendering entry
        private void customiseMinimumDimensions(Dimension dim, int row, int column) {
            TableColumn tableColumn = getColumnModel().getColumn(column);
            // this required extra margin padding is a mystery to me...
            dim.setSize(dim.width + getColumnMargin(), dim.height + getRowMargin());
            // potential bug: refresh() is needed to reduce size of unusually large temporary entries
            if (tableColumn.getWidth() < dim.width) {
                tableColumn.setMinWidth(dim.width);
                if (column == 0) {
                    tableColumn.setMaxWidth(dim.width);
                }
            }
            if (getRowHeight(row) < dim.height) {
                setRowHeight(row, dim.height);
            }
        }
    };
    
    public JBeanEditor() {
        super();
        setLayout(new BorderLayout());

//        setBackground(UIManager.getColor("window"));
//        table.setBackground(UIManager.getColor("window"));
        table.setTableHeader(null);
//        table.setBackground(null);
        table.setShowGrid(false);
        table.setCellSelectionEnabled(false);
        table.setFocusable(false);
        add(table, BorderLayout.CENTER);

        // should we expose spacing as a user property?
        Dimension spacing = new Dimension(5, 0);
        table.setIntercellSpacing(spacing);
    }
    
    public void refresh() {
        Iterable<Property> properties = Collections.emptyList();
        
        if (beanHelper != null) {
            Image icon = beanHelper.getIcon(BeanInfo.ICON_COLOR_32x32);
            if (icon == null) {
                remove(top);
            } else {
                JXImageView logo = new JXImageView();
                logo.setImage(icon);
                add(logo, BorderLayout.NORTH);
            }
            properties = filter(beanHelper.getProperties(),
                    new Predicate<Property>() {
                        @Override
                        public boolean apply(Property input) {
                            return !input.isHidden();
                        }
                    });
        }
        
        table.setModel(new MyTableModel(properties));

        // should we expose minimum row/column sizes as a user property?
        table.setRowHeight(18); // essentially the minimum row height
        table.getColumnModel().getColumn(0).setMinWidth(1);
        table.getColumnModel().getColumn(0).setMaxWidth(1);
        table.getColumnModel().getColumn(1).setMinWidth(1);
        
        table.packAll();
        revalidate();
    }

    /**
     * @param bean
     */
    public void setBean(Object bean) {
        if (bean instanceof BeanHelper) {
            setBeanHelper((BeanHelper) bean);
        } else {
            setBeanHelper(new BeanHelper(Preconditions.checkNotNull(bean)));
        }
    }
    
    private final PropertyChangeListener beanChangeListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() != beanHelper.getBean() || evt.getPropagationId() == JBeanEditor.class) {
                return;
            }
            refresh();
        }
    };

    /**
     * @param beanHelper
     */
    public void setBeanHelper(final BeanHelper beanHelper) {
        if (this.beanHelper != null) {
            this.beanHelper.removePropertyChangeListener(beanChangeListener);
        }
        this.beanHelper = Preconditions.checkNotNull(beanHelper);
        this.beanHelper.addPropertyChangeListener(beanChangeListener);
        refresh();
    }
}
