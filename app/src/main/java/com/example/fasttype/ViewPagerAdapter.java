package com.example.fasttype;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.checkerframework.checker.nullness.qual.NonNull;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new TestFragment();
            case 1:
                return new RecordsFragment();
            case 2:
                return new ProfileFragment();
            default:
                return new TestFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
