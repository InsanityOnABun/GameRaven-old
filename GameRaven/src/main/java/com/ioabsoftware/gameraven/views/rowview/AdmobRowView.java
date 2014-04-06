package com.ioabsoftware.gameraven.views.rowview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.BaseRowView;

public class AdmobRowView extends BaseRowView {

    private static AdView adView;

    public AdmobRowView(Context context) {
        super(context);
    }

    public AdmobRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdmobRowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    protected void init(Context context) {
        adView = new AdView(getContext());
        adView.setAdSize(AdSize.SMART_BANNER);
        adView.setAdUnitId("ca-app-pub-3449574924284542/6675970914");

        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        adView.setLayoutParams(lp);

        this.setWeightSum(1);
        this.addView(adView);

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // Emulator
//                .addTestDevice("7A613198BE05ED33B6053F18AFD3A209")  // My Phone
                .build();

        adView.loadAd(adRequest);
    }

    public static void pauseAd() {
        if (adView != null)
            adView.pause();
    }

    public static void resumeAd() {
        if (adView != null)
            adView.resume();
    }

    public static void destroyAd() {
        if (adView != null)
            adView.destroy();
    }

    @Override
    protected void retheme(int color, float scale) {
        // nada
    }

    @Override
    protected void showView(BaseRowData data) {
        // nada
    }
}
