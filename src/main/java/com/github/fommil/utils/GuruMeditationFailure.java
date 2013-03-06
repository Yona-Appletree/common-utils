/*
 * Copyright Samuel Halliday 2008
 * PROPRIETARY/CONFIDENTIAL. Use is subject to licence terms.
 */
package com.github.fommil.utils;

/**
 * Indicates a failure in logic in the application. The name of this exception
 * is a tip of the hat to Amiga OS messages of a similar nature.
 * The term "Guru Meditation Error" was an in-house joke from Amiga's early days.
 * One of the company's products was the joyboard, a game controller much like a
 * joystick but operated by one's feet. Early in the development of the Amiga
 * computer operating system, the company's developers became so frustrated with
 * the system's frequent crashes that, as a relaxation technique, a game was developed
 * where a person would sit cross-legged on the joyboard, resembling an Indian guru.
 * The player was supposed to remain perfectly still with the goal of the game being
 * to stay still the longest. If the player moved, a "guru meditation error" resulted.
 *
 * @author Samuel Halliday
 * @see <a href="http://en.wikipedia.org/Guru_Meditation">Guru Meditation</a>
 */
public class GuruMeditationFailure extends RuntimeException {

    /** serial version 1 */
    public static final long serialVersionUID = 1L;

    /** */
    public GuruMeditationFailure() {
        super();
    }

    /**
     * @param e
     */
    public GuruMeditationFailure(Throwable e) {
        super(e);
    }
}
