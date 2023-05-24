package hexlet.code;

import hexlet.code.domain.Url;

import io.javalin.Javalin;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.ebean.Database;
import io.ebean.DB;

import static org.assertj.core.api.Assertions.assertThat;

public class AppTest {
    private static final String PROJECT_TITLE = "Анализатор страниц";
    private static final String URL_EXAMPLE = "Пример: https://www.example.com";
    private static final String URLS_TABLE_FIRST_COLUMN_TITLE = "ID";
    private static final String URLS_TABLE_SECOND_COLUMN_TITLE = "Имя";
    private static Javalin app;
    private static String baseUrl;
    private static Url existingUrl;
    private static Database database;

    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start(0);

        int port = app.port();
        baseUrl = "http://localhost:" + port;

        database = DB.getDefault();
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    @BeforeEach
    void beforeEach() {
        database.script().run("/truncateUrl.sql");
    }

    @Test
    void testIndex() {
        HttpResponse<String> response = Unirest.get(baseUrl).asString();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains(PROJECT_TITLE);
        assertThat(response.getBody()).contains(URL_EXAMPLE);
    }

    @Test
    void testUrls() {
        HttpResponse<String> response = Unirest.get(baseUrl + "/urls").asString();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains(URLS_TABLE_FIRST_COLUMN_TITLE);
        assertThat(response.getBody()).contains(URLS_TABLE_SECOND_COLUMN_TITLE);
    }

//    @Test
//    void testAddUrl() {
//        //проверка, что сначала нет инфы про урл тут
//        //идёт добавка урла
//        //проверка, что добавлено //через показВсех
//        //добавка того же самого урла
//        //проверка, что он не добавился ещё раз
//    }
}

