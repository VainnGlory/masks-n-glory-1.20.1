package net.vainnglory.masksnglory.world;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.ChunkPos;
import net.vainnglory.masksnglory.item.custom.AmalgamItem;

public class FarlandsHelper {
    public static final int THRESHOLD = 50000;
    public static final int INNER_THRESHOLD = THRESHOLD + 20000;

    public static boolean isInFarlands(double x, double z) {
        return Math.abs(x) > THRESHOLD || Math.abs(z) > THRESHOLD;
    }

    public static boolean isInInnerFarlands(double x, double z) {
        return Math.abs(x) > INNER_THRESHOLD || Math.abs(z) > INNER_THRESHOLD;
    }

    public static boolean touchesFarlands(ChunkPos pos) {
        return isInFarlands(pos.getStartX(), pos.getStartZ())
                || isInFarlands(pos.getEndX(), pos.getEndZ())
                || isInFarlands(pos.getStartX(), pos.getEndZ())
                || isInFarlands(pos.getEndX(), pos.getStartZ());
    }

    public static boolean hasActiveAmalgam(PlayerEntity player) {
        for (ItemStack stack : player.getInventory().main) {
            if (stack.getItem() instanceof AmalgamItem && stack.getDamage() < stack.getMaxDamage()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSolid(int worldX, int y, int worldZ) {
        boolean xFar = Math.abs(worldX) > THRESHOLD;
        boolean zFar = Math.abs(worldZ) > THRESHOLD;
        int depthX = xFar ? Math.abs(worldX) - THRESHOLD : 0;
        int depthZ = zFar ? Math.abs(worldZ) - THRESHOLD : 0;
        int depth = Math.max(depthX, depthZ);
        if (depth >= 20000) {
            return innerSolid(worldX, worldZ) && y < innerTopY(worldX, worldZ);
        }
        if (xFar && !zFar) { return outerDensity(worldZ, y) > 0; }
        else if (zFar && !xFar) { return outerDensity(worldX, y) > 0; }
        else { return (outerDensity(worldZ, y) + outerDensity(worldX, y)) * 0.5f > 0; }
    }

    private static float outerDensity(int coord, int y) {
        float base;
        if (y < 64) {
            base = (64.0f - y) / 20.0f + 1.0f;
        } else if (y > 230) {
            base = (230.0f - y) / 15.0f;
        } else {
            base = 0.2f;
        }
        float large = valueNoise2D(coord, y, 96, 64) * 0.75f;
        float large2 = valueNoise2D(coord, y, 67, 48) * 0.55f;
        float combined = large + large2;
        float flatBreaker = valueNoise2D(coord, y, 35, 25) * Math.max(0.0f, combined) * 0.7f;
        float medium = valueNoise2D(coord, y, 28, 20) * 0.45f;
        float fine = valueNoise2D(coord, y, 9, 7) * 0.2f;
        float finest = valueNoise2D(coord, y, 4, 3) * 0.1f;
        return base + combined + flatBreaker + medium + fine + finest;
    }

    private static boolean innerSolid(int worldX, int worldZ) {
        float noise = 0; int axes = 0;
        if (Math.abs(worldX) > THRESHOLD) { noise += valueNoise(worldX, 80)*0.7f + valueNoise(worldX,24)*0.3f; axes++; }
        if (Math.abs(worldZ) > THRESHOLD) { noise += valueNoise(worldZ, 80)*0.7f + valueNoise(worldZ,24)*0.3f; axes++; }
        return axes > 0 && (noise / axes) > -0.7f;
    }

    private static int innerTopY(int worldX, int worldZ) {
        boolean xFar = Math.abs(worldX) > THRESHOLD;
        boolean zFar = Math.abs(worldZ) > THRESHOLD;
        float h;
        if (xFar && !zFar) { h = valueNoise(worldX,80)*0.85f + valueNoise(worldZ,20)*0.15f; }
        else if (zFar && !xFar) { h = valueNoise(worldZ,80)*0.85f + valueNoise(worldX,20)*0.15f; }
        else { h = (valueNoise(worldX,80)+valueNoise(worldZ,80))*0.4f + (valueNoise(worldX,20)+valueNoise(worldZ,20))*0.1f; }
        return (int)(130.0f + h * 35.0f);
    }

    private static float valueNoise(int x, int period) {
        int i = Math.floorDiv(x, period);
        float t = (float) Math.floorMod(x, period) / period;
        t = t * t * (3.0f - 2.0f * t);
        float a = hashFloat(i); float b = hashFloat(i + 1);
        return a + (b - a) * t;
    }

    private static float valueNoise2D(int x, int y, int periodX, int periodY) {
        int ix = Math.floorDiv(x, periodX); int iy = Math.floorDiv(y, periodY);
        float tx = (float) Math.floorMod(x, periodX) / periodX;
        float ty = (float) Math.floorMod(y, periodY) / periodY;
        tx = tx*tx*(3.0f-2.0f*tx); ty = ty*ty*(3.0f-2.0f*ty);
        float a=hashFloat2D(ix,iy), b=hashFloat2D(ix+1,iy), c=hashFloat2D(ix,iy+1), d=hashFloat2D(ix+1,iy+1);
        float ab=a+(b-a)*tx, cd=c+(d-c)*tx;
        return ab+(cd-ab)*ty;
    }

    private static float hashFloat(int n) { return (hash(n) & 0xffff) / 32767.5f - 1.0f; }
    private static float hashFloat2D(int x, int y) { return (hash(x*1664525+y*1013904223) & 0xffff)/32767.5f-1.0f; }
    private static int hash(int n) {
        n = ((n>>>16)^n)*0x45d9f3b; n = ((n>>>16)^n)*0x45d9f3b; return (n>>>16)^n;
    }
}