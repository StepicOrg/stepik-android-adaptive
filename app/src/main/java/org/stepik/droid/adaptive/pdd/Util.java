package org.stepik.droid.adaptive.pdd;

import android.content.Context;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.StreamEncoder;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.caverock.androidsvg.SVG;

import org.stepik.droid.adaptive.pdd.data.SharedPreferenceMgr;
import org.stepik.droid.adaptive.pdd.ui.fragment.FragmentMgr;
import org.stepik.droid.adaptive.pdd.util.svg.SvgDecoder;
import org.stepik.droid.adaptive.pdd.util.svg.SvgDrawableTranscoder;
import org.stepik.droid.adaptive.pdd.util.svg.SvgSoftwareLayerSetter;

import java.io.InputStream;

public class Util {
    public static void initMgr(final AppCompatActivity context) {
        FragmentMgr.init(context);
        SharedPreferenceMgr.init(context);
    }



    private final static String SVG_EXTENSION = ".svg";


    public static String prepareHTML(final String html) {
        return "<html>" +
                "<head>" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"quiz-card.css\" />" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />" +
                "</head>" +
                "<body>" +
                "<div class=\"main\">" +
                html +
                "</div></body></html>";
    }


    public static void loadImageFromNetworkAsync(final String path, final ImageView view, final int placeholder) {
        final Context context = view.getContext();
        if (path.endsWith(SVG_EXTENSION)) {
            GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> requestBuilder = Glide
                    .with(context.getApplicationContext())
                    .using(Glide.buildStreamModelLoader(Uri.class, context), InputStream.class)
                    .from(Uri.class)
                    .as(SVG.class)
                    .transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
                    .sourceEncoder(new StreamEncoder())
                    .cacheDecoder(new FileToStreamDecoder<>(new SvgDecoder()))
                    .decoder(new SvgDecoder())
                    .placeholder(placeholder)
                    .listener(new SvgSoftwareLayerSetter());

            Uri uri = Uri.parse(path);
            requestBuilder
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .load(uri)
                    .into(view);
        } else {
            Glide
                    .with(context.getApplicationContext())
                    .load(path)
                    .asBitmap()
                    .placeholder(placeholder)
                    .into(view);
        }
    }
}
