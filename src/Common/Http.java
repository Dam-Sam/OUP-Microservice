package Common;

import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import Common.Util.Logger;


public class Http {

    private static final HttpClient client = HttpClient.newHttpClient();
    //private static long requestCount;
    private static final AtomicInteger requestCount = new AtomicInteger(0);

    private static int TIMEOUT = 60;

    public static void setTimeout(int seconds) {
        TIMEOUT = seconds;
    }

    static {
        Runtime.getRuntime().addShutdownHook(Thread.ofVirtual().unstarted(client::close));
    }

    public static Response get(String host, String path) {
        return get(host, path, null);
    }

    public static Response get(String host, String path, Map<String, String> headers) {
        URI uri = URI.create(host + path);
        int reqNum = requestCount.incrementAndGet();

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .timeout(Duration.ofSeconds(TIMEOUT))
                .uri(uri);
        if (headers != null)
            headers.forEach(builder::header);
        builder.GET();
        HttpRequest request = builder.build();

        try {

            trace(request, reqNum);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            trace(response);
            return new Response(response.body(), response.statusCode(), "GET", uri.toString());

        } catch (HttpConnectTimeoutException e) {
            Logger.Warn("Http.get: Connection timeout for request:");
            warn(request, reqNum);
            return new Response("Http.get: Connection timeout for request " + uri, 0, "GET", uri.toString());
        } catch (ConnectException e) {
            Logger.Warn("Http.postJson: Could not make HTTP connection:");
            warn(request, reqNum);
            return new Response("Http.get: Could not connect to host " + host, 0, "GET", uri.toString());
        } catch (Exception e) {
            Logger.Log("Http.get: Unhandled exception", e);
            error(request, reqNum);
            return new Response("Unhandled exception", 0, "GET", uri.toString());
        }
    }

    public static Response postJson(String host, String path, String jsonInput) {
        return postJson(host, path, jsonInput, null);
    }

    public static Response postJson(String host, String path, String jsonInput, Map<String, String> headers) {
        URI uri = URI.create(host + path);
        int reqNum = requestCount.incrementAndGet();

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .timeout(Duration.ofSeconds(TIMEOUT))
                .uri(uri)
                .header("Content-Type", "application/json; utf-8");
        if (headers != null)
            headers.forEach(builder::header);
        builder.POST(HttpRequest.BodyPublishers.ofString(jsonInput));
        HttpRequest request = builder.build();

        try {
            trace(request, reqNum);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            trace(response);
            return new Response(response.body(), response.statusCode(), "POST", uri.toString());

        } catch (HttpConnectTimeoutException e) {
            Logger.Warn("Http.postJson: Connection timeout for request:");

            warn(request, reqNum);
            return new Response("Connection timeout for request " + uri, 0, "POST", uri.toString());
        } catch (ConnectException e) {
            Logger.Warn("Http.postJson: Could not make HTTP connection:");
            warn(request, reqNum);
            return new Response("Could not connect to host " + host, 0, "POST", uri.toString());
        } catch (Exception e) {
            Logger.Log("Http.postJson: Unhandled exception", e);
            error(request, reqNum);
            return new Response("Unhandled exception", 0, "POST", uri.toString());
        }

    }

    private static void error(HttpRequest request, int reqCount) {
        Logger.Log(Logger.ERROR, "Http Request (#%d) %s %s %s",
                reqCount, request.method(), request.uri().toString(), request.bodyPublisher().toString());
    }

    private static void warn(HttpRequest request, int reqCount) {
        Logger.Log(Logger.WARN, "Http Request (#%d) %s %s %s",
                reqCount, request.method(), request.uri().toString(), request.bodyPublisher().toString());
    }

    private static void trace(HttpRequest request, int reqCount) {
        Logger.LogTrace("Http Request (#%d) %s %s %s",
                reqCount, request.method(), request.uri().toString(), request.bodyPublisher().toString());
    }

    private static void trace(HttpResponse<String> response) {
        Logger.LogTrace("Http Response %s\n%s",
                response.statusCode(), response.body());
    }

    public static long getRequestCount() {
        return requestCount.get();
    }

    public static boolean isServiceUp(String baseUrl) {
        return Http.get(baseUrl, "/health/up").getCode() == 200;
    }

    public static class Response {
        private final String body;
        private final int code;

        private final String method;

        private final String url;

        public Response(String body, int code, String method, String url) {
            this.body = body;
            this.code = code;
            this.method = method;
            this.url = url;
        }


        public String getBody() {
            return body;
        }

        public int getCode() {
            return code;
        }

        @Override
        public String toString() {
            return method + " " + url + "; " + code + " - " + body;
        }

        public String getMethod() {
            return method;
        }

        public String getUrl() {
            return url;
        }
    }
}