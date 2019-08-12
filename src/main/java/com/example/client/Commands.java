package com.example.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import org.json.JSONObject;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.PrivateKeyDetails;
import org.apache.http.conn.ssl.PrivateKeyStrategy;
import org.apache.http.conn.ssl.SSLContexts;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import javax.net.ssl.SSLContext;
import java.net.Socket;
import java.util.Map;

@ShellComponent
public class Commands {
  private static final Logger log = LoggerFactory.getLogger(Commands.class);
  public static String API_URL = "https://localhost:8443/api/";

  @ShellMethod("Invoke Server API")
  public String invoke(@ShellOption(value = "--text", defaultValue = ShellOption.NULL) String text) throws Exception {
    log.info("Invoking API call with text = {}", text);
    makeAPICAll();
    return text;
  }

  private void makeAPICAll() throws Exception {
    String certAlias = "1";
    String keystorePassword = "changeit";
    JSONObject postBody = new JSONObject();
    postBody.put("hello", "world");

    KeyStore identityKeyStore = KeyStore.getInstance("PKCS12", "SunJSSE");
    FileInputStream identityKeyStoreFile = new FileInputStream(new File("keystore.p12"));
    identityKeyStore.load(identityKeyStoreFile, keystorePassword.toCharArray());

    KeyStore trustKeyStore = KeyStore.getInstance("jks");
    FileInputStream trustKeyStoreFile = new FileInputStream(new File("truststore.jks"));
    trustKeyStore.load(trustKeyStoreFile, keystorePassword.toCharArray());

    SSLContext sslContext = SSLContexts.custom()
            .loadKeyMaterial(identityKeyStore, keystorePassword.toCharArray(), new PrivateKeyStrategy() {
              @Override
              public String chooseAlias(Map<String, PrivateKeyDetails> aliases, Socket socket) {
                return certAlias;
              }
            })
            .loadTrustMaterial(trustKeyStore, null)
            .build();

    SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext,
            new String[]{"TLSv1.2", "TLSv1.1"},
            null,
            SSLConnectionSocketFactory.getDefaultHostnameVerifier());

    CloseableHttpClient client = HttpClients.custom()
            .setSSLSocketFactory(sslConnectionSocketFactory)
            .build();

    int responseCode = callEndPoint (client, API_URL, postBody);
  }

  private static int callEndPoint (CloseableHttpClient client, String endpoint, JSONObject body) {
    try {
      HttpPost post = new HttpPost(endpoint);
      post.setHeader("Accept", "application/json");
      post.setHeader("Content-type", "application/json");

      StringEntity entity = new StringEntity(body.toString());
      post.setEntity(entity);

      HttpResponse response = client.execute(post);

      int responseCode = response.getStatusLine().getStatusCode();
      log.info("Response Code: " + responseCode);

      return responseCode;
    } catch (Exception ex) {
      System.out.println("Boom, we failed: " + ex);
      ex.printStackTrace();
      return 500;
    }

  }
}
