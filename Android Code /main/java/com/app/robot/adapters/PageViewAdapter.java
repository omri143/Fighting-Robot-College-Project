package com.app.robot.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.app.robot.fragments.LaserControlFragment;
import com.app.robot.fragments.MotorsControlFragment;

public class PageViewAdapter extends FragmentStateAdapter {
    final int numOfFragments = 2;
    public PageViewAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        //Changing the fragment according to the position
        switch (position)
        {
            case 0:
                return new MotorsControlFragment();
            case 1:
                return new LaserControlFragment();
            default:
                break;

        }
        return null;
    }

    @Override
    public int getItemCount() {
        return numOfFragments;
    }
}
