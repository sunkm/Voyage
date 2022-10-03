package com.manchuan.tools.adapter;

import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.ArrayList;

public class AppInfoAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();

    public AppInfoAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment) {
        mFragmentList.add(fragment);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "应用信息";
            case 1:
                return "活动";
            case 2:
                return "服务";
            case 3:
                return "接收器";
            case 4:
                return "提供器";
            case 5:
                return "应用程序操作";
            case 6:
                return "使用权限";
            case 7:
                return "使用特征";
            case 8:
                return "签名";
            case 9:
                return "原生库";
        }
        return null;
    }

}

