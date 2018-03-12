package org.stepik.android.adaptive.util;

import android.webkit.WebView;

import org.stepik.android.adaptive.configuration.Config;

public class HtmlUtil {

    private static final String MathJaxScript =
            "<script type=\"text/x-mathjax-config\">\n" +
                    "  MathJax.Hub.Config({" +
                    "messageStyle: \"none\", " +
                    "TeX: {extensions: [ \"color.js\"]}, " +
                    "tex2jax: {preview: \"none\", inlineMath: [['$','$'], ['\\\\(','\\\\)']]}});\n" +
                    "displayMath: [ ['$$','$$'], ['\\[','\\]'] ]" +
                    "</script>\n" +
                    "<script type=\"text/javascript\"\n" +
                    " src=\"file:///android_asset/MathJax/MathJax.js?config=TeX-AMS_HTML\">\n" +
                    "</script>\n";

    private static final String Body = "<html>" +
            "<head>" +
            "<link rel=\"stylesheet\" type=\"text/css\" href=\"css/quiz-card.css\" />" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, user-scalable=no\" />" +
            "<base href=\"%s\">" +
            "%s" +
            "</head>" +
            "<body>" +
            "<div class=\"main\">" +
            "%s" +
            "</div></body></html>";

    private static boolean hasLaTeX(String textString) {
        return textString.contains("$") || textString.contains("\\[");
    }

    public static String prepareCardHtml(final String html) {
        String mathJax = hasLaTeX(html) ? MathJaxScript : "";
        return String.format(Body, Config.getInstance().getHost(), mathJax, html);
    }

    public static void setCardWebViewHtml(final WebView webView, final String html) {
        webView.loadDataWithBaseURL("file:///android_asset/web/", html, "text/html", "UTF-8", null);
    }
}
