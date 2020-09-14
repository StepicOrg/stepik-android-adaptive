package org.stepik.android.adaptive.ui.fragment;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import org.jetbrains.annotations.Nullable;
import org.stepik.android.adaptive.R;
import org.stepik.android.adaptive.databinding.FragmentPhotoViewBinding;

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


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_photo_view, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
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

            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_close_white_24dp);
            DrawableCompat.setTint(drawable, ContextCompat.getColor(getContext(), R.color.photo_view_home_indicator_color));
            supportActionBar.setHomeAsUpIndicator(drawable);
        }
    }

}
