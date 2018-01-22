import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.IOException;

public class HttpClient {

    private final OkHttpClient client;

    public HttpClient() {
        this.client = new OkHttpClient();
    }

    public String sendGetRequest(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        return client.newCall(request).execute().body().string();
    }
}