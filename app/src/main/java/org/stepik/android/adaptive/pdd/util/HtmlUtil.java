package org.stepik.android.adaptive.pdd.util;

import android.webkit.WebView;

public class HtmlUtil {

    public static String prepareCardHtml(final String html) {
        return "<html>" +
                "<head>" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"css/quiz-card.css\" />" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />" +
                "</head>" +
                "<body>" +
                "<div class=\"main\">" +
                html +
                "</div></body></html>";
    }

    public static void setCardWebViewHtml(final WebView webView, final String html) {
        webView.loadDataWithBaseURL("file:///android_asset/web/", html, "text/html", "UTF-8", null);
    }
}
