package test.ui;

import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public final class TestDisplaySupport {

    private TestDisplaySupport() {
    }

    public static Display getOrCreateDisplay() {
        try {
            Display existing = Display.getCurrent();
            if (existing != null) {
                return existing;
            }
            return new Display();
        } catch (SWTException ex) {
            return null;
        } catch (UnsatisfiedLinkError ex) {
            return null;
        }
    }

    public static Shell createHiddenShell(final Display display) {
        return new Shell(display);
    }
}
