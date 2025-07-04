package com.petshop.login.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
public class PostgresDatabaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(PostgresDatabaseInitializer.class);

    @Value("${DB_URL}")
    private String dbUrl;

    @Value("${DB_USERNAME}")
    private String username;

    @Value("${DB_PASSWORD}")
    private String password;

    @PostConstruct
    public void init() {
        logger.info("DB URL recebida: '{}", dbUrl);

        if(dbUrl == null || !dbUrl.contains("/")) {
            logger.error("DB_URL esta invalida: {}", dbUrl);
        }
        try {
            String dbName = dbUrl.substring(dbUrl.lastIndexOf("/") + 1).replaceAll(";", "");
            String baseUrl =dbUrl.substring(0, dbUrl.lastIndexOf("/")) + "/postgres";

            try(Connection conn = DriverManager.getConnection(baseUrl, username,password);
                Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + dbName + "'");
                if(!rs.next()) {
                    stmt.executeUpdate("CREATE DATABASE " + dbName);
                    logger.info("Banco '{}' criado com sucesso.", dbName);
                } else {
                    logger.info("Banco '{}' já existe", dbName);
                }
            }
        } catch (Exception e) {
            logger.error("Erro ao verificar/criar banco de dados: {}", e.getMessage(), e);
        }
    }
}
