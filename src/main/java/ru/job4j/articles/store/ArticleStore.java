package ru.job4j.articles.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.articles.model.Article;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ArticleStore implements Store<Article>, AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(ArticleStore.class.getSimpleName());


    private Connection connection;

    public ArticleStore(Connection connection) {
        this.connection = connection;
        initScheme();
    }


    private void initScheme() {
        logger.info("Инициализация таблицы статей");
        try (var statement = connection.createStatement()) {
            var sql = Files.readString(Path.of("db/scripts", "articles.sql"));
            statement.execute(sql);
        } catch (Exception e) {
            logger.error("Не удалось выполнить операцию: { }", e.getCause());
            throw new IllegalStateException();
        }
    }

    @Override
    public Article save(Article model) {
        logger.info("Сохранение статьи");
        var sql = "insert into articles(text) values(?)";
        try (var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, model.getText());
            statement.executeUpdate();
            var key = statement.getGeneratedKeys();
            while (key.next()) {
                model.setId(key.getInt(1));
            }
        } catch (Exception e) {
            logger.error("Не удалось выполнить операцию: { }", e.getCause());
            throw new IllegalStateException();
        }
        return model;
    }

    @Override
    public List<Article> findAll() {
        logger.info("Загрузка всех статей");
        var sql = "select * from articles";
        var articles = new ArrayList<Article>();
        try (var statement = connection.prepareStatement(sql)) {
            var selection = statement.executeQuery();
            while (selection.next()) {
                articles.add(new Article(
                        selection.getInt("id"),
                        selection.getString("text")
                ));
            }
        } catch (Exception e) {
            logger.error("Не удалось выполнить операцию: { }", e.getCause());
            throw new IllegalStateException();
        }
        return articles;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }
}
