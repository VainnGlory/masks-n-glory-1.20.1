package net.vainnglory.masksnglory.util;

public class GreaseClientState {

    private static boolean greased = false;
    private static int stacks = 0;
    private static boolean capturedOriginalStepHeight = false;
    private static float originalStepHeight = 1.0f;

    public static void setGreased(boolean state, int stackCount) {
        greased = state;
        stacks = stackCount;
    }

    public static boolean isGreased() {
        return greased;
    }

    public static int getStacks() {
        return stacks;
    }

    public static void captureOriginalStepHeight(float value) {
        if (!capturedOriginalStepHeight) {
            originalStepHeight = value;
            capturedOriginalStepHeight = true;
        }
    }

    public static float getOriginalStepHeight() {
        return originalStepHeight;
    }

    public static void resetCapturedStepHeight() {
        capturedOriginalStepHeight = false;
    }
}