package android.support.v7.widget;

import android.content.res.Resources;

/**
 * Created by Charles on 11/9/2014.
 */
public class RRDWSBackgroundHack extends RoundRectDrawableWithShadow {
    public RRDWSBackgroundHack(Resources resources, int backgroundColor, float radius, float shadowSize, float maxShadowSize) {
        super(resources, backgroundColor, radius, shadowSize, maxShadowSize);
    }
}
