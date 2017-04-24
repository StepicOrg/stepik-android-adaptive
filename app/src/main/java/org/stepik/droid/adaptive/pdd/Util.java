package org.stepik.droid.adaptive.pdd;

import android.support.v7.app.AppCompatActivity;

import org.stepik.droid.adaptive.pdd.data.SharedPreferenceMgr;
import org.stepik.droid.adaptive.pdd.ui.fragment.FragmentMgr;

public class Util {

    public static void initMgr(final AppCompatActivity context) {
        FragmentMgr.init(context);
        SharedPreferenceMgr.init(context);
    }

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
}
