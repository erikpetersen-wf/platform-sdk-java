
import com.workiva.platform.Platform;

import io.undertow.util.StatusCodes;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    public static void main(String[] args) {
        final Logger log = LoggerFactory.getLogger(Platform.class);

        // Show it running with defaults.
        Platform.builder().start();

        try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("http://localhost:8888/_wk/ready");
            HttpResponse httpFrugalResp = httpClient.execute(httpGet);
            int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
            log.info("Readiness probe responded {}", statusCode);
            if (statusCode == StatusCodes.OK) {
                log.info("Success!");
            }
        } catch (Exception ex) {
            log.info(ex.getMessage());
        }

        try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("http://localhost:8888/_wk/alive");
            HttpResponse httpFrugalResp = httpClient.execute(httpGet);
            int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
            log.info("Liveness probe responded {}", statusCode);
            if (statusCode == StatusCodes.OK) {
                log.info("Success!");
            }
        } catch (Exception ex) {
            log.info(ex.getMessage());
        }


        // Show it running with escape hatches.

        // TODO
    }
}