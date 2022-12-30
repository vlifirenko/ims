package com.ims.graph;

public class ColorUtils {

    public static Rgb hslToRgb(float h, float s, float l) {
        Rgb rgb = new Rgb();
        if (s == 0) {
            rgb.r = rgb.g = rgb.b = l;
        } else {
            float q = l < 0.5f ? l * (1f + s) : l + s - l * s;
            float p = 2f * l - q;
            rgb.r = hue2rgb(p, q, h + 1f / 3f);
            rgb.g = hue2rgb(p, q, h);
            rgb.b = hue2rgb(p, q, h - 1f / 3f);
        }
        rgb.r = Math.round(rgb.r * 255f);
        rgb.g = Math.round(rgb.g * 255f);
        rgb.b = Math.round(rgb.b * 255f);

        return rgb;
    }

    public static float hue2rgb(float p, float q, float t) {
        if (t < 0f) t += 1f;
        if (t > 1f) t -= 1f;
        if (t < (1f / 6f)) return p + (q - p) * 6f * t;
        if (t < (1f / 2f)) return q;
        if (t < (2f / 3f)) return p + (q - p) * (2f / 3f - t) * 6f;
        return p;
    }

    public static class Rgb {
        public float r;
        public float g;
        public float b;

        public Rgb() {
        }

        @Override
        public String toString() {
            return "r:" + r + " g:" + g + " b:" + b;
        }
    }

}
