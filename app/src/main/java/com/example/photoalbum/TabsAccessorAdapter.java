package com.example.photoalbum;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabsAccessorAdapter extends FragmentPagerAdapter {

    public TabsAccessorAdapter(@NonNull @org.jetbrains.annotations.NotNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @org.jetbrains.annotations.NotNull

    @Override
    public Fragment getItem(int i) {

        switch (i)
        {
            case 0:
                GroupsFragment groupsFragment = new GroupsFragment();
                return groupsFragment;
            case 1:
                EditorFragment editorFragment = new EditorFragment();
                return  editorFragment;
            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return  friendsFragment;
            case 3:
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment ;

            default:
                return null;

        }

    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public CharSequence getPageTitle(int i) {
        switch (i)
        {
            case 0:
                return "Groups";
            case 1:
                return "Editor";
            case 2:
                return "Friends";
            case 3:
                return "Request";

            default:
                return null;

        }
    }
}
