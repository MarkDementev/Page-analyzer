package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;

import io.javalin.http.Handler;

import java.net.MalformedURLException;
import java.net.URL;

public final class UrlsController {
    public static Handler createUrl = ctx -> {
        String inputUrl = ctx.formParam("url");
        URL urlToTransform;
        String transformatedUrl;

        try {
            urlToTransform = new URL(inputUrl);
            transformatedUrl = transformUrl(urlToTransform);
        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "alert");
            ctx.redirect("/");
            return;
        }
        Url checkedUrl = new QUrl()
                .name.equalTo(transformatedUrl)
                .findOne();

        if (checkedUrl != null) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "alert");
            ctx.redirect("/");
            return;
        }
        Url url = new Url(transformatedUrl);
        url.save();

        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls");
    };

    private static String transformUrl(URL urlToTransform) {
        String protocol = urlToTransform.getProtocol();
        String authority = urlToTransform.getAuthority();
        String port = String.valueOf(urlToTransform.getPort());

        if (port.equals("-1")) {
            port = "";
        }
        return protocol + "://" + authority + port;
    }
}
