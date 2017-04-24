package org.stepik.droid.adaptive.pdd.ui.fragment;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import org.stepik.droid.adaptive.pdd.R;

import java.lang.ref.WeakReference;

public class FragmentMgr {

    private WeakReference<AppCompatActivity> appReference;
    private static FragmentMgr instance;

    private FragmentMgr(final AppCompatActivity context) {
        appReference = new WeakReference<>(context);
    }

    public void attach(final AppCompatActivity context) {
        appReference = new WeakReference<>(context);
    }

    public synchronized static void init(final AppCompatActivity context) {
        if (instance == null) {
            instance = new FragmentMgr(context);
        }
    }

    public synchronized static FragmentMgr getInstance() {
        return instance;
    }

    public void addFragment(final int rootID, final Fragment fragment, final boolean backStack) {
        final AppCompatActivity app = appReference.get();
        if (app == null) return;

        final FragmentTransaction transaction = app.getSupportFragmentManager().beginTransaction();

        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add((rootID == 0 ? R.id.fragment_container : rootID), fragment, fragment.getTag());
        if (backStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    public void replaceFragment(final int rootID, final Fragment fragment, final boolean backStack) {
        final AppCompatActivity app = appReference.get();
        if (app == null) return;

        final FragmentTransaction transaction = app.getSupportFragmentManager().beginTransaction();

        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.replace((rootID == 0 ? R.id.fragment_container : rootID), fragment, fragment.getTag());
        if (backStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    public void back() {
        final AppCompatActivity app = appReference.get();
        if (app == null) return;

        app.getSupportFragmentManager().popBackStack();
    }
}
