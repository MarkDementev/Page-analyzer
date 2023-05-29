package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;

import io.ebean.PagedList;

import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class UrlsController {
    public static Handler createUrl = ctx -> {
        String inputUrl = ctx.formParam("url");
        String normalizedUrl = transformUrl(Objects.requireNonNull(inputUrl));

        if (normalizedUrl == null) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }
        Url checkedUrl = new QUrl()
                .name.equalTo(normalizedUrl)
                .findOne();

        if (checkedUrl != null) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }
        Url url = new Url(normalizedUrl);
        url.save();

        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flash-type", "success");

        ctx.redirect("/urls");
    };

    public static Handler showAllAddedUrls = ctx -> {
        String term = ctx.queryParamAsClass("term", String.class).getOrDefault("");
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;
        int rowsPerPage = 10;

        PagedList<Url> pagedUrls = new QUrl()
                .name.icontains(term)
                .setFirstRow(page * rowsPerPage)
                .setMaxRows(rowsPerPage)
                .orderBy()
                .id.asc()
                .findPagedList();
        List<Url> urls = pagedUrls.getList();

        int lastPage = pagedUrls.getTotalPageCount() + 1;
        int currentPage = pagedUrls.getPageIndex() + 1;
        List<Integer> pages = IntStream
                .range(1, lastPage)
                .boxed()
                .collect(Collectors.toList());

        ctx.attribute("term", term);
        ctx.attribute("urls", urls);
        ctx.attribute("currentPage", currentPage);
        ctx.attribute("pages", pages);

        ctx.render("urls/index.html");
    };

    public static Handler showUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", long.class).getOrDefault(null);
        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (url == null) {
            throw new NotFoundResponse("Url with id - " + id + " is not found in database!");
        }
        ctx.attribute("url", url);

        ctx.render("urls/url.html");
    };

    public static Handler addCheck = ctx -> {
        long id = ctx.pathParamAsClass("id", long.class).getOrDefault(null);
        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (url == null) {
            throw new NotFoundResponse("Url with id - " + id + " is not found in database!");
        }
        HttpResponse<String> response = Unirest.get(url.getName()).asString();
        int statusCode = response.getStatus();
//        int statusCode = 0;
        String title = "Заглушка";
        String h1 = "Заглушка";
        String description = "Заглушка";

        UrlCheck urlCheck = new UrlCheck(statusCode, title, h1, description, url);
        urlCheck.save();

        ctx.sessionAttribute("flash", "Страница успешно проверена");
        ctx.sessionAttribute("flash-type", "success");

        ctx.redirect("/urls/" + id);
    };

    private static String transformUrl(String inputUrl) throws MalformedURLException {
        if (!(inputUrl.startsWith("http://") || inputUrl.startsWith("https://"))) {
            return null;
        }
        URL urlToTransform = new URL(inputUrl);
        String protocol = urlToTransform.getProtocol();
        String authority = urlToTransform.getAuthority();

        return protocol + "://" + authority;
    }
}
