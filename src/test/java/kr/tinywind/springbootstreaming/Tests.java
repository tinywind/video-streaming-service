package kr.tinywind.springbootstreaming;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import kr.tinywind.springbootstreaming.model.Material;
import kr.tinywind.springbootstreaming.model.MaterialRepository;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class Tests {
    private static final byte[] MOVIE = "0123456789abcdefghijklmnopqrstuv".getBytes(StandardCharsets.UTF_8);
    private static final String HTML_NAME = "<img src=x onerror=window.videoFixture=1>.mp4";
    private static ConfigurableApplicationContext application;
    private static String base;

    @BeforeClass
    public static void startIsolatedApplication() throws Exception {
        Path temporary = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), "video-contract-");
        Path videos = Files.createDirectory(temporary.resolve("videos"));
        Files.write(videos.resolve("clip.mp4"), MOVIE);
        Files.write(videos.resolve("empty.mp4"), new byte[0]);
        Files.write(videos.resolve(HTML_NAME), MOVIE);
        Files.write(videos.resolve("log\u2028FORGED.mp4"), MOVIE);
        Files.write(videos.resolve("ignored.txt"), MOVIE);
        Files.write(Files.createDirectory(videos.resolve("nested")).resolve("nested.mp4"), MOVIE);
        Path outside = Files.createDirectory(temporary.resolve("outside"));
        Path secret = Files.write(outside.resolve("private.mp4"), "outside-owned-fixture".getBytes(StandardCharsets.UTF_8));
        Files.createSymbolicLink(videos.resolve("escape.mp4"), secret);
        Files.createSymbolicLink(videos.resolve("escape-dir"), outside);
        System.setProperty("video.root", videos.toString());
        application = SpringApplication.run(Launcher.class, "--server.port=0", "--server.address=127.0.0.1",
                "--spring.datasource.url=jdbc:h2:mem:video-contracts;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                "--spring.jpa.hibernate.ddl-auto=create-drop");
        base = "http://127.0.0.1:" + application.getEnvironment().getProperty("local.server.port");
    }

    @AfterClass
    public static void stopIsolatedApplication() {
        if (application != null) application.close();
        System.clearProperty("video.root");
    }

    @Test
    public void listsRootAndNestedDirectories() throws Exception {
        for (String path : new String[]{"/list", "/list/"}) {
            try (Response response = get(path, null)) {
                assertEquals(200, response.status);
                String body = response.text();
                assertTrue(body.contains("clip.mp4"));
                assertTrue(body.contains("nested"));
                assertFalse(body.contains("ignored.txt"));
            }
        }
        try (Response response = get("/list/nested", null)) {
            assertEquals(200, response.status);
            assertTrue(response.text().contains("nested.mp4"));
        }
    }

    @Test
    public void downloadsAndStreamsCompleteFiles() throws Exception {
        for (String path : new String[]{"/video/clip.mp4", "/video-random-accessible/clip.mp4"}) {
            try (Response response = get(path, null)) {
                assertEquals(200, response.status);
                assertArrayEquals(MOVIE, response.bytes());
            }
        }
    }

    @Test
    public void servesZeroBasedNormalAndSuffixRanges() throws Exception {
        for (String[] sample : new String[][]{{"bytes=0-3", "0", "4"}, {"bytes=4-7", "4", "8"}, {"bytes=-4", "28", "32"}}) {
            int start = Integer.parseInt(sample[1]);
            int end = Integer.parseInt(sample[2]);
            try (Response response = get("/video-random-accessible/clip.mp4", sample[0])) {
                assertEquals(206, response.status);
                assertEquals("bytes " + start + "-" + (end - 1) + "/32", response.header("Content-Range"));
                assertArrayEquals(Arrays.copyOfRange(MOVIE, start, end), response.bytes());
            }
        }
    }

    @Test
    public void rejectsMalformedOverflowingAndUnsatisfiableRanges() throws Exception {
        for (String range : new String[]{"invalid", "bytes=40-", "bytes=10-2", "bytes=0-999999999999999999999999"}) {
            try (Response response = get("/video-random-accessible/clip.mp4", range)) {
                assertEquals(416, response.status);
            }
        }
        try (Response response = get("/video-random-accessible/empty.mp4", "bytes=0-0")) {
            assertEquals(416, response.status);
        }
    }

    @Test
    public void confinesFilesAndDirectorySymlinksToTheConfiguredRoot() throws Exception {
        for (String path : new String[]{"/video/escape.mp4", "/video-random-accessible/escape.mp4", "/list/escape-dir"}) {
            try (Response response = get(path, null)) {
                assertEquals(403, response.status);
            }
        }
        for (String path : new String[]{"/video/../outside/private.mp4", "/video/%2e%2e%2foutside/private.mp4"}) {
            try (Response response = get(path, null)) {
                assertTrue(response.status == 400 || response.status == 403 || response.status == 404);
            }
        }
    }

    @Test
    public void persistsAnnotationsAndExportsTheirCsv() throws Exception {
        String name = "/video-random-accessible/clip.mp4";
        Map<String, String> form = new LinkedHashMap<>();
        form.put("videoName", name);
        form.put("materialDataList[0].timestamp", "1.25");
        form.put("materialDataList[0].key", "q");
        form.put("materialDataList[1].timestamp", "2.5");
        form.put("materialDataList[1].key", "w");
        try (Response response = post("/save", form)) { assertEquals(302, response.status); }
        MaterialRepository repository = application.getBean(MaterialRepository.class);
        Material material = repository.findAll().stream().filter(value -> name.equals(value.getVideoName())).findFirst().get();
        try {
            assertEquals(2, material.getMaterialDataList().size());
            assertEquals(material.getId(), material.getMaterialDataList().get(0).getMaterial().getId());
            try (Response response = get("/download-csv/" + material.getId(), null)) {
                assertEquals(200, response.status);
                String csv = response.text();
                assertTrue(csv.contains("timestamp,key"));
                assertTrue(csv.contains("1.25,q"));
                assertTrue(csv.contains("2.5,w"));
                assertTrue(response.header("Content-Disposition").contains("clip.mp4"));
            }
        } finally {
            repository.delete(material);
            repository.flush();
        }
    }

    @Test
    public void rendersAnEmptySavedDataView() throws Exception {
        try (Response response = get("/material-list", null)) { assertEquals(200, response.status); }
    }

    @Test
    public void escapesVideoNamesInSavedDataViews() throws Exception {
        String name = "/video-random-accessible/" + HTML_NAME;
        Map<String, String> form = new LinkedHashMap<>();
        form.put("videoName", name);
        try (Response response = post("/save", form)) { assertEquals(302, response.status); }
        MaterialRepository repository = application.getBean(MaterialRepository.class);
        Material material = repository.findAll().stream().filter(value -> name.equals(value.getVideoName())).findFirst().get();
        try (Response response = get("/material-list", null)) {
            assertEquals(200, response.status);
            String html = response.text();
            assertFalse(html.contains("<img src=x"));
            assertTrue(html.contains("&lt;img src=x"));
        } finally {
            repository.delete(material);
            repository.flush();
        }
    }

    @Test
    public void doesNotPrintUntrustedFilenameLineSeparators() throws Exception {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("kr.tinywind.springbootstreaming");
        ListAppender<ILoggingEvent> events = new ListAppender<>();
        events.start();
        logger.addAppender(events);
        try (Response response = get("/video/" + URLEncoder.encode("log\u2028FORGED.mp4", "UTF-8"), null)) {
            for (ILoggingEvent event : events.list) assertFalse(event.getFormattedMessage().contains("\u2028"));
            assertEquals(200, response.status);
            assertArrayEquals(MOVIE, response.bytes());
        } finally {
            logger.detachAppender(events);
            events.stop();
        }
    }

    @Test
    public void developmentConsolesAreDisabled() throws Exception {
        for (String path : new String[]{"/console/", "/h2-console/"}) {
            try (Response response = get(path, null)) { assertEquals(404, response.status); }
        }
    }

    private static Response get(String path, String range) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(base + path).openConnection();
        if (range != null) connection.setRequestProperty("Range", range);
        return new Response(connection);
    }

    private static Response post(String path, Map<String, String> values) throws Exception {
        StringBuilder form = new StringBuilder();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (form.length() > 0) form.append('&');
            form.append(URLEncoder.encode(entry.getKey(), "UTF-8")).append('=').append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        HttpURLConnection connection = (HttpURLConnection) new URL(base + path).openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.getOutputStream().write(form.toString().getBytes(StandardCharsets.UTF_8));
        return new Response(connection);
    }

    private static class Response implements AutoCloseable {
        final HttpURLConnection connection;
        final int status;
        Response(HttpURLConnection connection) throws Exception {
            this.connection = connection;
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            status = connection.getResponseCode();
        }
        String header(String name) { return connection.getHeaderField(name); }
        byte[] bytes() throws Exception {
            try (InputStream input = status >= 400 ? connection.getErrorStream() : connection.getInputStream()) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int count;
                if (input != null) while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
                return output.toByteArray();
            }
        }
        String text() throws Exception { return new String(bytes(), StandardCharsets.UTF_8); }
        @Override public void close() { connection.disconnect(); }
    }
}
