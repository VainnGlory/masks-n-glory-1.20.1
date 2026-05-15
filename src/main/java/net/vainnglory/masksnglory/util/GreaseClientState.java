package net.vainnglory.masksnglory.util;

public class GreaseClientState {

    private static boolean greased = false;

    public static void setGreased(boolean state) {
        greased = state;
    }

    public static boolean isGreased() {
        return greased;
    }
}
