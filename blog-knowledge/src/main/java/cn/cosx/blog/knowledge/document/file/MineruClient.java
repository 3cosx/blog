package cn.cosx.blog.knowledge.document.file;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Mineru客户端
 * 用于调用mineru文档解析服务
 */
@Slf4j
@Component
public class MineruClient {

    @Value("${mineru.url:https://mineru.cn/api/v4/extract/task}")
    private String mineruUrl;

    @Value("${mineru.token:}")
    private String token;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 提交文档解析任务
     *
     * @param fileUrl 文件URL（MinIO或其他可访问的URL）
     * @return 任务ID
     */
    public String submitTask(String fileUrl) {
        log.info("[Mineru] 提交文档解析任务，文件URL: {}", fileUrl);
        try {
            URL url = new URL(mineruUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Accept", "*/*");
            conn.setDoOutput(true);

            String jsonBody = String.format("""
                {
                    "url": "%s",
                    "model_version": "vlm"
                }
                """, fileUrl);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes());
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    JsonNode jsonResponse = objectMapper.readTree(response.toString());
                    JsonNode data = jsonResponse.get("data");
                    String taskId = data != null ? data.get("task_id").asText() : null;
                    log.info("[Mineru] 任务提交成功，taskId: {}", taskId);
                    return taskId;
                }
            } else {
                log.error("[Mineru] 任务提交失败，响应码: {}", responseCode);
                throw new RuntimeException("任务提交失败，响应码: " + responseCode);
            }
        } catch (Exception e) {
            log.error("[Mineru] 提交文档解析任务异常", e);
            throw new RuntimeException("提交文档解析任务失败", e);
        }
    }

    /**
     * 查询任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态：pending/running/done/failed/converting
     */
    public String getTaskStatus(String taskId) {
        log.info("[Mineru] 查询任务状态，任务ID: {}", taskId);
        try {
            String statusUrl = mineruUrl + "/" + taskId;
            URL url = new URL(statusUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Accept", "*/*");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    JsonNode jsonResponse = objectMapper.readTree(response.toString());
                    JsonNode data = jsonResponse.get("data");
                    String state = data != null ? data.get("state").asText() : null;
                    log.info("[Mineru] 任务状态查询成功，taskId: {}, state: {}", taskId, state);
                    return state;
                }
            } else {
                log.error("[Mineru] 任务状态查询失败，响应码: {}", responseCode);
                throw new RuntimeException("任务状态查询失败，响应码: " + responseCode);
            }
        } catch (Exception e) {
            log.error("[Mineru] 查询任务状态异常", e);
            throw new RuntimeException("查询任务状态失败", e);
        }
    }

    /**
     * 获取任务结果URL
     *
     * @param taskId 任务ID
     * @return zip文件的下载URL
     */
    public String getResultUrl(String taskId) {
        log.info("[Mineru] 获取任务结果URL，taskId: {}", taskId);
        try {
            String statusUrl = mineruUrl + "/" + taskId;
            URL url = new URL(statusUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Accept", "*/*");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    JsonNode jsonResponse = objectMapper.readTree(response.toString());
                    JsonNode data = jsonResponse.get("data");
                    String zipUrl = data != null && data.has("full_zip_url") ? data.get("full_zip_url").asText() : null;
                    log.info("[Mineru] 获取结果URL成功，taskId: {}, zipUrl: {}", taskId, zipUrl);
                    return zipUrl;
                }
            } else {
                log.error("[Mineru] 获取结果URL失败，响应码: {}", responseCode);
                throw new RuntimeException("获取结果URL失败，响应码: " + responseCode);
            }
        } catch (Exception e) {
            log.error("[Mineru] 获取任务结果URL异常", e);
            throw new RuntimeException("获取任务结果URL失败", e);
        }
    }

    /**
     * 下载解析结果（zip文件）
     *
     * @param taskId  任务ID
     * @param zipPath 保存zip文件的路径
     * @return 下载后的zip文件路径
     */
    public Path downloadResult(String taskId, Path zipPath) {
        log.info("[Mineru] 下载解析结果，任务ID: {}, 保存路径: {}", taskId, zipPath);
        try {
            String zipUrl = getResultUrl(taskId);
            if (zipUrl == null || zipUrl.isEmpty()) {
                throw new RuntimeException("获取不到结果URL，任务可能未完成");
            }

            URL url = new URL(zipUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                Files.createDirectories(zipPath.getParent());
                try (InputStream is = conn.getInputStream();
                     FileOutputStream fos = new FileOutputStream(zipPath.toFile())) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
                log.info("[Mineru] 下载解析结果成功，保存至: {}", zipPath);
            } else {
                log.error("[Mineru] 下载解析结果失败，响应码: {}", responseCode);
                throw new RuntimeException("下载解析结果失败，响应码: " + responseCode);
            }
        } catch (Exception e) {
            log.error("[Mineru] 下载解析结果异常", e);
            throw new RuntimeException("下载解析结果失败", e);
        }
        return zipPath;
    }

    /**
     * 获取任务错误信息
     *
     * @param taskId 任务ID
     * @return 错误信息
     */
    public String getErrorMessage(String taskId) {
        log.info("[Mineru] 获取任务错误信息，taskId: {}", taskId);
        try {
            String statusUrl = mineruUrl + "/" + taskId;
            URL url = new URL(statusUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Accept", "*/*");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    JsonNode jsonResponse = objectMapper.readTree(response.toString());
                    JsonNode data = jsonResponse.get("data");
                    String errMsg = data != null && data.has("err_msg") ? data.get("err_msg").asText() : null;
                    log.info("[Mineru] 获取错误信息成功，taskId: {}, errMsg: {}", taskId, errMsg);
                    return errMsg;
                }
            } else {
                log.error("[Mineru] 获取错误信息失败，响应码: {}", responseCode);
                return null;
            }
        } catch (Exception e) {
            log.error("[Mineru] 获取任务错误信息异常", e);
            return null;
        }
    }

    /**
     * 等待任务完成
     *
     * @param taskId 任务ID
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return 是否完成
     */
    public boolean waitForCompletion(String taskId, long timeout, TimeUnit unit) {
        long startTime = System.currentTimeMillis();
        long timeoutMillis = unit.toMillis(timeout);

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            String status = getTaskStatus(taskId);
            log.info("[Mineru] 轮询任务状态，taskId: {}, status: {}", taskId, status);
            if ("done".equals(status)) {
                return true;
            } else if ("failed".equals(status)) {
                return false;
            } else if ("pending".equals(status) || "running".equals(status) || "converting".equals(status)) {
                // 继续等待
            } else {
                log.warn("[Mineru] 未知状态: {}, 继续等待", status);
            }

            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }
}