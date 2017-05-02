package org.stepik.droid.adaptive.pdd.ui.adapter;


import android.databinding.DataBindingUtil;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;

import org.stepik.droid.adaptive.pdd.R;
import org.stepik.droid.adaptive.pdd.Util;
import org.stepik.droid.adaptive.pdd.data.SharedPreferenceMgr;
import org.stepik.droid.adaptive.pdd.data.model.Profile;
import org.stepik.droid.adaptive.pdd.databinding.DrawerHeaderBinding;
import org.stepik.droid.adaptive.pdd.databinding.FragmentRecommendationsBinding;
import org.stepik.droid.adaptive.pdd.ui.dialog.LogoutDialog;
import org.stepik.droid.adaptive.pdd.ui.fragment.FragmentMgr;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public final class NavigationDrawerAdapter {

    private Profile profile;
    private DrawerHeaderBinding headerBinding;

    public NavigationDrawerAdapter() {
        Observable
                .fromCallable(() -> SharedPreferenceMgr.getInstance().getProfile())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setProfile);
    }

    public void bind(final FragmentRecommendationsBinding binding) {
        binding.fragmentRecommendationsMenuButton.setOnClickListener((v) ->
            binding.fragmentRecommendationsDrawerLayout.openDrawer(GravityCompat.START));

        binding.fragmentRecommendationsNavigation.setNavigationItemSelectedListener((item) -> {
            switch (item.getItemId()) {
                case R.id.drawer_logout:
                    FragmentMgr.getInstance().showDialog(new LogoutDialog());
                    return true;
            }
            return false;
        });

        this.headerBinding = DataBindingUtil.bind(binding.fragmentRecommendationsNavigation.getHeaderView(0));
        setProfile(profile);
    }

    public void unbind() {
        headerBinding.unbind();
        headerBinding = null;
    }

    private void setProfile(final Profile profile) {
        if (profile == null) return;
        if (this.profile != profile) {
            this.profile = profile;
        }
        if (headerBinding == null) return;

        Util.loadImageFromNetworkAsync(profile.getAvatar(), headerBinding.headerProfileAvatar, R.drawable.ic_avatar_stub);
        headerBinding.headerProfileName.setText(profile.getFirstName() + ' ' + profile.getLastName());
    }
}
