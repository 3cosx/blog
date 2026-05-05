package cn.cosx.blog.knowledge.document.factory.file.impl;

import cn.cosx.blog.knowledge.document.domain.entity.KnowledgeDocument;
import cn.cosx.blog.knowledge.document.domain.entity.TableMeta;
import cn.cosx.blog.knowledge.document.domain.mapper.TableMetaMapper;
import cn.cosx.blog.knowledge.document.factory.file.FileProcessService;
import cn.cosx.blog.knowledge.document.infra.enums.FileType;
import cn.cosx.blog.knowledge.document.infra.enums.UseTypeEnums;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ExcelFileProcessServiceImpl implements FileProcessService {

    @Autowired
    private TableMetaMapper tableMetaMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String TABLE_PREFIX = "custom_data_query_";
    private static final int BATCH_SIZE = 500;

    @Override
    public void processDocument(KnowledgeDocument document, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String tableName = buildTableName(document);
        log.info("[Excel] 开始处理，docId: {}, tableName: {}", document.getDocId(), tableName);

        try {
            File tempFile = File.createTempFile("excel_", getExtension(originalFilename));
            file.transferTo(tempFile);

            List<List<String>> allRows = readExcel(tempFile);
            if (allRows.isEmpty()) {
                throw new RuntimeException("Excel 文件为空");
            }

            List<String> headers = allRows.get(0);
            List<String> columns = sanitizeColumns(headers);
            List<List<String>> dataRows = allRows.subList(1, allRows.size());

            if (tableMetaMapper.checkTableExists(tableName) > 0) {
                log.info("[Excel] 表已存在，删除旧表: {}", tableName);
                tableMetaMapper.dropTable(tableName);
            }

            String createSql = buildCreateTableSql(tableName, columns);
            log.info("[Excel] 建表SQL: {}", createSql);
            tableMetaMapper.executeCreateTable(createSql);

            String insertSql = buildInsertSql(tableName, columns);
            int totalRows = dataRows.size();
            for (int i = 0; i < totalRows; i += BATCH_SIZE) {
                int batchEnd = Math.min(i + BATCH_SIZE, totalRows);
                List<List<String>> batch = dataRows.subList(i, batchEnd);
                jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int index) throws SQLException {
                        List<String> row = batch.get(index);
                        for (int col = 0; col < columns.size(); col++) {
                            ps.setString(col + 1, col < row.size() ? row.get(col) : "");
                        }
                    }

                    @Override
                    public int getBatchSize() {
                        return batch.size();
                    }
                });
                log.info("[Excel] 批量插入完成，{}/{}", batchEnd, totalRows);
            }
            log.info("[Excel] 数据插入完成，总行数: {}", totalRows);

            TableMeta meta = new TableMeta();
            meta.setTableName(tableName);
            meta.setDescription(document.getDocTitle());
            meta.setCreateSql(createSql);
            meta.setColumnsInfo(buildColumnsInfo(headers, columns));
            tableMetaMapper.insert(meta);

            tempFile.delete();
            log.info("[Excel] 处理完成，tableName: {}", tableName);

        } catch (Exception e) {
            log.error("[Excel] 处理失败，docId: {}", document.getDocId(), e);
            throw new RuntimeException("Excel 处理失败", e);
        }
    }

    private String buildTableName(KnowledgeDocument document) {
        String raw = document.getTableName();
        if (raw == null || raw.isBlank()) {
            raw = "doc_" + document.getDocId();
        }
        return TABLE_PREFIX + raw.trim().replaceAll("[^a-zA-Z0-9_\\u4e00-\\u9fa5]", "_")
                .replaceAll("^_+|_+$", "")
                .replaceAll("_+", "_");
    }

    private List<List<String>> readExcel(File file) {
        List<List<String>> result = new ArrayList<>();
        EasyExcel.read(file, new ReadListener<Map<Integer, String>>() {
            @Override
            public void invoke(Map<Integer, String> data, AnalysisContext context) {
                List<String> row = new ArrayList<>();
                for (int i = 0; ; i++) {
                    if (data.containsKey(i)) {
                        row.add(data.get(i) != null ? data.get(i).trim() : "");
                    } else {
                        break;
                    }
                }
                if (!row.isEmpty()) {
                    result.add(row);
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {}
        }).headRowNumber(0).sheet().doRead();
        return result;
    }

    private List<String> sanitizeColumns(List<String> headers) {
        List<String> columns = new ArrayList<>();
        List<String> seen = new ArrayList<>();
        for (String header : headers) {
            String col = header.replaceAll("[^a-zA-Z0-9_\\u4e00-\\u9fa5]", "_")
                    .replaceAll("^_+|_+$", "")
                    .replaceAll("_+", "_");
            if (col.isEmpty()) {
                col = "col";
            }
            String deduped = col;
            int suffix = 1;
            while (seen.contains(deduped)) {
                deduped = col + "_" + (++suffix);
            }
            seen.add(deduped);
            columns.add(deduped);
        }
        return columns;
    }

    private String buildCreateTableSql(String tableName, List<String> columns) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE `").append(tableName).append("` (\n");
        sql.append("  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',\n");
        for (String col : columns) {
            sql.append("  `").append(col).append("` TEXT COMMENT '").append(col).append("',\n");
        }
        sql.append("  PRIMARY KEY (`id`)\n");
        sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci");
        return sql.toString();
    }

    private String buildInsertSql(String tableName, List<String> columns) {
        StringBuilder cols = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                cols.append(", ");
                placeholders.append(", ");
            }
            cols.append("`").append(columns.get(i)).append("`");
            placeholders.append("?");
        }
        return String.format("INSERT INTO `%s` (%s) VALUES (%s)", tableName, cols, placeholders);
    }

    private String buildColumnsInfo(List<String> headers, List<String> columns) {
        List<Map<String, String>> info = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            Map<String, String> entry = new LinkedHashMap<>();
            entry.put("header", headers.get(i));
            entry.put("column", columns.get(i));
            info.add(entry);
        }
        return JSON.toJSONString(info);
    }

    private String getExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return ".xlsx";
    }

    @Override
    public boolean supports(FileType fileType, UseTypeEnums useTypeEnums) {
        if (FileType.EXCEL.equals(fileType) || FileType.CSV.equals(fileType)) {
            return useTypeEnums == UseTypeEnums.DATA_QUERY;
        }
        return false;
    }
}
