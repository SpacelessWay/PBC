package com.example.pbc.service;
import com.example.pbc.work_databased.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Service {
    private static final Logger log = LoggerFactory.getLogger(Service.class);

    private final DatabaseManager schemaManager;

    public Service(DatabaseManager schemaManager) {
        this.schemaManager = schemaManager;
    }

    public void init() {
        log.info("Проверяю структуру базы данных...");
        try {
            schemaManager.createTablesIfNotExists();
            log.info("Структура БД успешно проверена и создана");
        } catch (Exception e) {
            log.error("Ошибка при инициализации структуры БД", e);
            throw e;
        }
    }
}
