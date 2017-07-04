package org.stepik.android.adaptive.pdd.ui.fragment;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import org.jetbrains.annotations.Nullable;
import org.stepik.android.adaptive.pdd.R;
import org.stepik.android.adaptive.pdd.databinding.FragmentPhotoViewBinding;

import uk.co.senab.photoview.PhotoViewAttacher;


public class PhotoViewFragment extends Fragment {

    private static final String pathKey = "pathKey";

    public static PhotoViewFragment newInstance(String path) {
        Bundle args = new Bundle();
        args.putString(pathKey, path);
        PhotoViewFragment fragment = new PhotoViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private FragmentPhotoViewBinding binding;

    PhotoViewAttacher photoViewAttacher;

    private SimpleTarget<Bitmap> target = new SimpleTarget<Bitmap>() {
        @Override
        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
            binding.internetProblemRoot.setVisibility(View.GONE);
            binding.zoomableImage.setImageBitmap(resource);
            photoViewAttacher.update();
        }

        @Override
        public void onLoadFailed(Exception e, Drawable errorDrawable) {
            binding.internetProblemRoot.setVisibility(View.VISIBLE);
        }
    };

    @Nullable
    String url = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        url = getArguments().getString(pathKey);
    }


    @android.support.annotation.Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @android.support.annotation.Nullable ViewGroup container, @android.support.annotation.Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_photo_view, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @android.support.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpToolbar();
        photoViewAttacher = new PhotoViewAttacher(binding.zoomableImage);
        binding.retryButton.setOnClickListener(v -> {
            binding.internetProblemRoot.setVisibility(View.GONE);
            loadImage();
        });
        loadImage();
    }

    private void loadImage() {
        Glide.with(getContext())
                .load(url)
                .asBitmap()
                .fitCenter()
                .into(target);
    }

    private void setUpToolbar() {
        final AppCompatActivity appCompatActivity = ((AppCompatActivity) getActivity());
        appCompatActivity.setSupportActionBar(binding.toolbar);
        final ActionBar supportActionBar = appCompatActivity.getSupportActionBar();

        if (supportActionBar != null) {
            supportActionBar.setDisplayShowTitleEnabled(false);
            supportActionBar.setDisplayShowHomeEnabled(true);
            supportActionBar.setHomeButtonEnabled(true);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }
    }

}
