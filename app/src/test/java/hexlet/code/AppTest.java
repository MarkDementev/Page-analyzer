package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;

import io.javalin.Javalin;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.ebean.Database;
import io.ebean.DB;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AppTest {
    private static final String PROJECT_TITLE = "Анализатор страниц";
    private static final String URL_EXAMPLE = "https://www.example.com";
    private static final String URLS_TABLE_FIRST_COLUMN_TITLE = "ID";
    private static final String URLS_TABLE_SECOND_COLUMN_TITLE = "Имя";
    private static Javalin app;
    private static String baseUrl;
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
    }

    @Test
    void testUrls() {
        HttpResponse<String> response = Unirest.get(baseUrl + "/urls").asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains(URLS_TABLE_FIRST_COLUMN_TITLE);
        assertThat(response.getBody()).contains(URLS_TABLE_SECOND_COLUMN_TITLE);
    }

    @Test
    void testUrlWithoutAdd() {
        HttpResponse<String> response = Unirest.get(baseUrl + "/urls/1").asString();

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getBody()).doesNotContain(URL_EXAMPLE);
    }

    @Test
    void testAddUrl() {
        HttpResponse addUrlResponse = Unirest.post(baseUrl + "/urls")
                .field("url", URL_EXAMPLE).asEmpty();
        HttpResponse<String> checkAddedUrlResponse = Unirest.get(baseUrl + "/urls/1").asString();
        Url addedUrlFromDB = new QUrl()
                .name.equalTo(URL_EXAMPLE)
                .findOne();

        assertThat(addUrlResponse.getStatus()).isEqualTo(200);
        assertThat(checkAddedUrlResponse.getStatus()).isEqualTo(200);
        assertThat(addedUrlFromDB).isNotNull();
        assertThat(checkAddedUrlResponse.getBody()).contains(URL_EXAMPLE);

        HttpResponse repeatAddUrlResponse = Unirest.post(baseUrl + "/urls")
                .field("url", URL_EXAMPLE).asEmpty();
        List<Url> addedUrlsFromDB = new QUrl()
                .name.equalTo(URL_EXAMPLE)
                .findList();

        assertThat(repeatAddUrlResponse.getStatus()).isEqualTo(200);
        assertThat(addedUrlsFromDB.size() == 1).isTrue();
    }
}

