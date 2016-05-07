package com.ioabsoftware.gameraven.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.ioabsoftware.gameraven.R;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.MaterialCommunityIcons;
import com.joanzapata.iconify.fonts.MaterialIcons;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Style;

public final class Theming {

    private static float pjButtonTextBaseSize,
            pjLabelTextBaseSize;

    private static int theme;

    public static int theme() {
        return theme;
    }

    private static int colorBackground;

    public static int colorBackground() {
        return colorBackground;
    }

    private static int colorBackgroundInverseResource;

    public static int colorBackgroundInverseResource() {
        return colorBackgroundInverseResource;
    }

    private static int colorPrimary;

    public static int colorPrimary() {
        return colorPrimary;
    }

    private static int colorPrimaryDark;

    public static int colorPrimaryDark() {
        return colorPrimaryDark;
    }

    private static int colorAccent;

    public static int colorAccent() {
        return colorAccent;
    }

    private static int colorReadTopic;

    public static int colorReadTopic() {
        return colorReadTopic;
    }

    private static int colorHiddenSpoiler;

    public static int colorHiddenSpoiler() {
        return colorHiddenSpoiler;
    }

    private static int colorRevealedSpoiler;

    public static int colorRevealedSpoiler() {
        return colorRevealedSpoiler;
    }

    private static float textScale = 1f;

    public static float textScale() {
        return textScale;
    }

    private static Style croutonStyle;

    public static Style croutonStyle() {
        return croutonStyle;
    }

    private static Drawable[] topicStatusIcons = new Drawable[4];

    public static Drawable[] topicStatusIcons() {
        return topicStatusIcons;
    }

    private static Drawable selectableItemBackground;

    public static Drawable selectableItemBackground() {
        return selectableItemBackground.getConstantState().newDrawable().mutate();
    }

    private static Configuration croutonShort = new Configuration.Builder().setDuration(2500).build();

    public static void preInit(SharedPreferences settings) {
        String themePref = settings.getString("gfTheme", "Light Blue");
        switch (themePref) {
            case "Light Blue":
                theme = R.style.MyThemes_LightBlue;
                break;
            case "Dark Blue":
                theme = R.style.MyThemes_DarkBlue;
                break;
            case "Light Red":
                theme = R.style.MyThemes_LightRed;
                break;
            case "Dark Red":
                theme = R.style.MyThemes_DarkRed;
                break;
            case "Light Green":
                theme = R.style.MyThemes_LightGreen;
                break;
            case "Dark Green":
                theme = R.style.MyThemes_DarkGreen;
                break;
            case "Light Orange":
                theme = R.style.MyThemes_LightOrange;
                break;
            case "Dark Orange":
                theme = R.style.MyThemes_DarkOrange;
                break;
            case "Light Purple":
                theme = R.style.MyThemes_LightPurple;
                break;
            case "Dark Purple":
                theme = R.style.MyThemes_DarkPurple;
                break;
        }
    }

    public static void init(Context c, SharedPreferences settings) {
        Resources resources = c.getResources();
        textScale = settings.getInt("textScale", 100) / 100f;

        // Obtain the styled attributes. 'themedContext' is a context with a
        // theme, typically the current Activity (i.e. 'this')
        TypedArray ta = c.obtainStyledAttributes(new int[] {
                R.attr.colorBackground,
                R.attr.colorBackgroundInverse,
                R.attr.colorPrimary,
                R.attr.colorPrimaryDark,
                R.attr.colorAccent,
                R.attr.hiddenSpoilerColor,
                R.attr.revealedSpoilerColor,
                R.attr.themedTopicStatusIconColor,
                R.attr.readTopic,
                android.R.attr.selectableItemBackground
        });

        // Get the individual values
        colorBackground = ta.getColor(0, resources.getColor(R.color.gf_background_dark));
        colorBackgroundInverseResource = ta.getResourceId(1, R.color.gf_background_light);
        colorPrimary = ta.getColor(2, resources.getColor(R.color.gf_blue_dark));
        colorPrimaryDark = ta.getColor(3, resources.getColor(R.color.gf_blue_dark_secondary));
        colorAccent = ta.getColor(4, resources.getColor(R.color.gf_blue_dark_accent));
        colorHiddenSpoiler = ta.getColor(5, resources.getColor(R.color.white));
        colorRevealedSpoiler = ta.getColor(6, resources.getColor(R.color.black));
        int topicStatusIconColor = ta.getColor(7, resources.getColor(R.color.white));
        colorReadTopic = ta.getColor(8, resources.getColor(R.color.read_topic));
        selectableItemBackground = ta.getDrawable(9);

        // Finally, free the resources used by TypedArray
        ta.recycle();

        topicStatusIcons[0] = new IconDrawable(c, MaterialCommunityIcons.mdi_poll)
                .color(topicStatusIconColor).sizeDp(18);
        topicStatusIcons[1] = new IconDrawable(c, MaterialIcons.md_lock)
                .color(topicStatusIconColor).sizeDp(18);
        topicStatusIcons[2] = new IconDrawable(c, MaterialCommunityIcons.mdi_archive)
                .color(topicStatusIconColor).sizeDp(18);
        topicStatusIcons[3] = new IconDrawable(c, MaterialCommunityIcons.mdi_pin)
                .color(topicStatusIconColor).sizeDp(18);

        croutonStyle = new Style.Builder()
                .setBackgroundColorValue(colorPrimaryDark)
                .setTextColorValue(Color.WHITE)
                .setConfiguration(croutonShort)
                .build();
    }

    public static void setTextSizeBases(float pjButton, float pjLabel) {
        pjButtonTextBaseSize = pjButton;
        pjLabelTextBaseSize = pjLabel;
    }

    public static float getScaledPJButtonTextSize() {
        return pjButtonTextBaseSize * textScale;
    }

    public static float getScaledPJLabelTextSize() {
        return pjLabelTextBaseSize * textScale;
    }

    /**
     * returns true if new scale is different from old scale
     */
    public static boolean updateTextScale(float newScale) {
        if (textScale != newScale) {
            textScale = newScale;
            return true;
        } else
            return false;
    }

    /**
     * Colors the overscroll of the activity
     *
     * @param context The context of the activity
     */
    public static void colorOverscroll(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //glow
            int glowDrawableId = context.getResources().getIdentifier("overscroll_glow", "drawable", "android");
            Drawable androidGlow = context.getResources().getDrawable(glowDrawableId);
            androidGlow.setColorFilter(colorPrimary, PorterDuff.Mode.SRC_IN);
            //edge
            int edgeDrawableId = context.getResources().getIdentifier("overscroll_edge", "drawable", "android");
            Drawable androidEdge = context.getResources().getDrawable(edgeDrawableId);
            androidEdge.setColorFilter(colorPrimary, PorterDuff.Mode.SRC_IN);
        }
    }

    /**
     * Converts a DP value into a PX value, based on the current device's density
     *
     * @param c  Context needed to find the display density.
     * @param dp The DP value to convert to PX
     * @return The dp value converted to pixels
     */
    public static int convertDPtoPX(Context c, float dp) {
        // Get the screen's density scale
        final float scale = c.getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return ((int) (dp * scale + 0.5f));
    }

}
