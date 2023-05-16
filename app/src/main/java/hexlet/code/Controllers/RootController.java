package hexlet.code.Controllers;

import io.javalin.http.Handler;

public final class RootController {
    public static Handler helloWorld = ctx -> ctx.result("Hello World");
}
