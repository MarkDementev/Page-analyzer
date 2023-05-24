package hexlet.code;

import hexlet.code.controllers.RootController;
import hexlet.code.controllers.UrlsController;

import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;

import org.thymeleaf.TemplateEngine;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public final class App {
    public static void main(String[] args) {
        Javalin app = getApp();
        app.start(getPort());
    }

    public static Javalin getApp() {
        Javalin app = Javalin.create(config -> {
            if (!isProduction()) {
                config.plugins.enableDevLogging();
            }
            JavalinThymeleaf.init(getTemplateEngine());
            config.staticFiles.enableWebjars();
        });
        addRoutes(app);

        app.before(ctx -> {
            ctx.attribute("ctx", ctx);
        });

        return app;
    }

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "8080");
        return Integer.valueOf(port);
    }

    private static boolean isProduction() {
        return getMode().equals("production");
    }

    private static String getMode() {
        return System.getenv().getOrDefault("APP_ENV", "development");
    }

    private static TemplateEngine getTemplateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();

        templateResolver.setPrefix("/templates/");
        templateResolver.setCharacterEncoding("UTF-8");

        templateEngine.addDialect(new LayoutDialect());
        templateEngine.addDialect(new Java8TimeDialect());
        templateEngine.setTemplateResolver(templateResolver);

        return templateEngine;
    }

    private static void addRoutes(Javalin app) {
        app.get("/", RootController.mainPage);

        app.routes(() -> {
            path("urls", () -> {
                post(UrlsController.createUrl);
                get(UrlsController.showAllAddedUrls);
                path("{id}", () -> {
                    get(UrlsController.showUrl);
                });
            });
        });
    }
}
