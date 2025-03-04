package com.example.fasttype;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.checkerframework.checker.nullness.qual.NonNull;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private User user;
    public ViewPagerAdapter(@NonNull FragmentActivity activity, User user) {
        super(activity);
        this.user = user;
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
                return ProfileFragment.newInstance(user);
            default:
                return new TestFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
