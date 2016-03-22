package org.groebl.smsmms.common;

import android.content.Context;

import org.groebl.smsmms.common.AnalyticsManagerBase;

public class AnalyticsManager implements AnalyticsManagerBase {
    private AnalyticsManager() {
    }

    private static class AnalyticsManagerHolder {
        private final static AnalyticsManager INSTANCE = new AnalyticsManager();
    }

    public static AnalyticsManager getInstance() {
        return AnalyticsManagerHolder.INSTANCE;
    }

    @Override
    public void init(Context context) {

    }

    @Override
    public void sendEvent(String category, String action, String label) {

    }

    @Override
    public void sendEvent(String category, String action, String label, long value) {

    }
}
