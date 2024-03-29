package ru.job4j.articles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.articles.service.ArticleService;
import ru.job4j.articles.service.SimpleArticleService;
import ru.job4j.articles.service.generator.ArticleGenerator;
import ru.job4j.articles.service.generator.RandomArticleGenerator;
import ru.job4j.articles.store.ArticleStore;
import ru.job4j.articles.store.WordStore;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Application {

    private final Logger logger = LoggerFactory.getLogger(Application.class.getSimpleName());

    public final int targetCount = 1_000_000;

    Connection connection;
    Properties properties;

    public Application() {
        properties = loadProperties();
        createConnection();
    }

    public int getTargetCount() {
        return targetCount;
    }

    public Connection getConnection() {
        return connection;
    }

    public static void main(String[] args) {
        Application application = new Application();
        var wordStore = new WordStore(application.getConnection());
        var articleStore = new ArticleStore(application.getConnection());
        ArticleGenerator articleGenerator = new RandomArticleGenerator();
        ArticleService articleService = new SimpleArticleService(articleGenerator);
        articleService.generate(wordStore, application.getTargetCount(), articleStore);
    }

    private Properties loadProperties() {
        logger.info("Загрузка настроек приложения");
        var properties = new Properties();
        try (InputStream in = Application.class.getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(in);
        } catch (Exception e) {
            logger.error("Не удалось загрузить настройки. { }", e.getCause());
            throw new IllegalStateException();
        }
        return properties;
    }

    private void createConnection() {
        logger.info("Подключение к базе данных");
        try {
            Class.forName(properties.getProperty("driver"));
            connection = DriverManager.getConnection(
                    properties.getProperty("url"),
                    properties.getProperty("username"),
                    properties.getProperty("password")
            );
        } catch (SQLException e) {
            logger.error("Не удалось выполнить операцию: { }", e.getCause());
            throw new IllegalStateException();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
