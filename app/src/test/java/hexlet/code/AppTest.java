package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;

import io.javalin.Javalin;

import io.ebean.Database;
import io.ebean.DB;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.MockResponse;

public final class AppTest {
    private static final String PROJECT_TITLE = "Анализатор страниц";
    private static final String URLS_TABLE_FIRST_COLUMN_TITLE = "ID";
    private static final String URL_EXAMPLE = "https://www.example.com";
    private static final String TEST_FILE_PATH = "./src/test/resources/fixtures/test-file.html";
    private static final String TEST_FILE_TITLE = "test-file title";
    private static final String TEST_FILE_H_1 = "test-file h1";
    private static final String TEST_FILE_DESCRIPTION = "test-file description text";
    private static MockWebServer mockServer;
    private static MockResponse mockedResponse;
    private static String url;
    private static Javalin app;
    private static String baseUrl;
    private static Database database;

    @BeforeAll
    public static void beforeAll() throws IOException {
        Path filePath = Paths.get(TEST_FILE_PATH).toAbsolutePath().normalize();
        String body = Files.readString(filePath);

        mockServer = new MockWebServer();
        mockedResponse = new MockResponse().setBody(body);
        mockServer.enqueue(mockedResponse);
        mockServer.start();

        url = mockServer.url("/").toString();

        app = App.getApp();
        app.start(0);

        int port = app.port();
        baseUrl = "http://localhost:" + port;

        database = DB.getDefault();
    }

    @BeforeEach
    void beforeEach() {
        database.script().run("/truncate-db-tables.sql");
    }

    @AfterAll
    public static void afterAll() throws IOException {
        mockServer.shutdown();
        app.stop();
    }

    @Test
    void testMainPage() {
        HttpResponse<String> response = Unirest.get(baseUrl).asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains(PROJECT_TITLE);
    }

    @Test
    void testShowAllAddedUrls() {
        HttpResponse<String> response = Unirest.get(baseUrl + "/urls").asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains(URLS_TABLE_FIRST_COLUMN_TITLE);
    }

    @Test
    void testShowUrl() {
        HttpResponse<String> response = Unirest.get(baseUrl + "/urls/1").asString();

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getBody()).doesNotContain(URL_EXAMPLE);
    }

    @Test
    void testCreateUrl() {
        HttpResponse addUrlResponse = Unirest.post(baseUrl + "/urls")
                .field("url", URL_EXAMPLE).asEmpty();
        Url addedUrlFromDB = new QUrl()
                .name.equalTo(URL_EXAMPLE)
                .findOne();
        HttpResponse<String> checkAddedUrlResponse = Unirest
                .get(baseUrl + "/urls/" + addedUrlFromDB.getId()).asString();

        assertThat(addUrlResponse.getStatus()).isEqualTo(302);
        assertThat(addedUrlFromDB).isNotNull();
        assertThat(checkAddedUrlResponse.getStatus()).isEqualTo(200);
        assertThat(checkAddedUrlResponse.getBody()).contains(URL_EXAMPLE);

        HttpResponse repeatAddUrlResponse = Unirest.post(baseUrl + "/urls")
                .field("url", URL_EXAMPLE).asEmpty();
        List<Url> addedUrlsFromDB = new QUrl()
                .name.equalTo(URL_EXAMPLE)
                .findList();

        assertThat(repeatAddUrlResponse.getStatus()).isEqualTo(302);
        assertThat(addedUrlsFromDB.size() == 1).isTrue();
    }

    @Test
    void testAddCheck() {
        database.script().run("/insert-to-return-id.sql");
        UrlCheck addedCheck = new QUrlCheck()
                .title.equalTo("addedTestCheckWithId")
                .findOne();
        long idFromAddedCheck = addedCheck.getId();
        long idToAddNewCheck = ++idFromAddedCheck;

        HttpResponse addUrlResponse = Unirest.post(baseUrl + "/urls")
                .field("url", url).asEmpty();
        HttpResponse addCheckResponse = Unirest.post(baseUrl + "/urls/" + idToAddNewCheck + "/checks")
                .field("url", url).asEmpty();
        HttpResponse<String> urlsResponse = Unirest.get(baseUrl + "/urls").asString();
        HttpResponse<String> urlResponse = Unirest.get(baseUrl + "/urls/" + idToAddNewCheck).asString();

        assertThat(addUrlResponse.getStatus()).isEqualTo(302);
        assertThat(addCheckResponse.getStatus()).isEqualTo(302);
        assertThat(urlsResponse.getStatus()).isEqualTo(200);
        assertThat(urlResponse.getStatus()).isEqualTo(200);

        assertThat(urlsResponse.getBody()).contains(url.substring(0, url.length() - 1));
        assertThat(urlResponse.getBody()).contains(TEST_FILE_TITLE);
        assertThat(urlResponse.getBody()).contains(TEST_FILE_H_1);
        assertThat(urlResponse.getBody()).contains(TEST_FILE_DESCRIPTION);

        List<UrlCheck> addedChecksFromDB = new QUrlCheck()
                .title.equalTo(TEST_FILE_TITLE)
                .findList();
        assertThat(addedChecksFromDB.size() == 1).isTrue();
    }
}
