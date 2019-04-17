package com.saratms.risetodo.ViewPagerPackage;

import com.saratms.risetodo.R;

public enum CustomPagerEnum {

    PAGE1(R.string.page_one_number, R.layout.splash_view_one),
    PAGE2(R.string.page_two_number, R.layout.splash_view_two),
    PAGE3(R.string.page_three_number, R.layout.splash_view_three);

    private int mTitleResId;
    private int mLayoutResId;

    CustomPagerEnum(int titleResId, int layoutResId) {
        mTitleResId = titleResId;
        mLayoutResId = layoutResId;
    }

    public int getTitleResId() {
        return mTitleResId;
    }

    public int getLayoutResId() {
        return mLayoutResId;
    }

}