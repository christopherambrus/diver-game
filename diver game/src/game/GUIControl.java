package game;

import cms.util.maybe.Maybe;
import gui.GUI;

/** Methods for controlling the interaction between the main thread and the GUI, if a GUI is present. */
public class GUIControl {

    /** No objects. */
    private GUIControl(){}

    /** Register that an animation is starting on the GUI, if the GUI is present.
     *  It is the job of the GUI to stop the animation.
     */
    public static void startAnimation(Maybe<GUI> gui) {
        gui.thenDo(g -> g.startAnimating());
    }
    /** Wait until the GUI animation, if any, is complete. */
    public static void waitForAnimation(Maybe<GUI> gui) {
        // TODO: Avoid inefficient spinning while waiting for GUI
        gui.thenDo(g -> {
           g.isAnimating();
        });
    }

}
