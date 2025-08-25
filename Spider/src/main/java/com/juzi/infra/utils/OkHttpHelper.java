package com.juzi.infra.utils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OkHttpHelper {

  public static volatile OkHttpClient okHttpClient;

  public static final MediaType JSON = MediaType.get("application/json");

  /**
   * singleton
   *
   * @return OkHttpClient
   */
  public OkHttpClient getInstance() {
    if (Objects.isNull(okHttpClient)) {
      synchronized (OkHttpHelper.class) {
        if (Objects.isNull(okHttpClient)) {
          okHttpClient = new OkHttpClient.Builder()
              .callTimeout(30, TimeUnit.SECONDS)
              .readTimeout(10, TimeUnit.SECONDS)
              .connectTimeout(10, TimeUnit.SECONDS)
              .writeTimeout(5, TimeUnit.SECONDS)
              .build();
        }
      }
    }
    return okHttpClient;
  }

  public String post(String url, String json) {
    RequestBody body = RequestBody.create(json, JSON);
    Request request = new Request.Builder()
        .url(url)
        .post(body)
        .build();
    try (Response response = getInstance().newCall(request).execute()) {
      ResponseBody result = response.body();
      return Objects.nonNull(result) ? result.string() : null;
    } catch (Exception e) {
      log.error("post error: ", e);
    }
    return null;
  }

  public static File downloadPdf(String fileUrl) {
    File tempFile = null;

    try (InputStream in = new URL(fileUrl).openStream()) {
      tempFile = File.createTempFile("download-", ".pdf");

      try (OutputStream out = Files.newOutputStream(tempFile.toPath())) {
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
          out.write(buffer, 0, bytesRead);
        }
      }

    } catch (Exception e) {
      log.error("downloadPdf error: ", e);
      return null; // 下载失败时返回 null，调用方可判断是否为 null
    }

    return tempFile;
  }

  /**
   * 发送多媒体文件
   *
   * @param url     请求的URL
   * @param file    要上传的文件
   * @param chatId  聊天ID
   * @param caption 文件描述
   * @return 响应字符串
   */
  public String postMultipart(String url, File file, Long chatId, String caption) {
    MediaType mediaType = MediaType.parse("application/pdf");

    MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("chat_id", String.valueOf(chatId))
        .addFormDataPart("caption", caption)
        .addFormDataPart("document", file.getName(),
            RequestBody.create(file, mediaType));

    RequestBody requestBody = multipartBuilder.build();

    Request request = new Request.Builder()
        .url(url)
        .post(requestBody)
        .build();

    try (Response response = getInstance().newCall(request).execute()) {
      ResponseBody result = response.body();
      return Objects.nonNull(result) ? result.string() : null;
    } catch (Exception e) {
      log.error("postMultipart error: ", e);
    }
    return null;
  }


}