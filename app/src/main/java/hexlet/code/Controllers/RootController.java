package hexlet.code.Controllers;

import io.javalin.http.Handler;

public final class RootController {
    public static Handler mainPage = ctx -> ctx.render("index.html");
}
