/*
 * Created 22-Jun-2012
 * 
 * Copyright Samuel Halliday 2012
 * PROPRIETARY/CONFIDENTIAL. Use is subject to licence terms.
 */
package com.github.fommil.beans;

import com.google.common.base.Objects;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;

/**
 * Convenience for creating {@link VetoableChangeListener}s by simplifying the
 * veto process and automatically ignoring the (utterly useless) "rollback"
 * event created by {@link VetoableChangeSupport}.
 * 
 * @author Samuel Halliday
 */
public abstract class VetoableChangeListenerAdapter implements VetoableChangeListener {

    private volatile PropertyChangeEvent last;

    @Override
    public void vetoableChange(final PropertyChangeEvent evt) throws PropertyVetoException {
        if (last != null) {
            if (Objects.equal(last.getSource(), evt.getSource())
                    && Objects.equal(last.getPropertyName(), evt.getPropertyName())
                    && Objects.equal(last.getOldValue(), evt.getNewValue())) {
                // rollback event, stupid VetoableChangeSupport
                return;
            }
        }
        last = evt;
        if (isVetoed(evt)) {
            throw new PropertyVetoException(null, evt);
        }
    }

    /**
     * @param evt
     * @return {@code true} if the change is to be vetoed
     * @throws PropertyVetoException (optionally) if information on the veto is desired
     */
    public abstract boolean isVetoed(PropertyChangeEvent evt) throws PropertyVetoException;
}
