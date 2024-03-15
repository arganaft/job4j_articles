package ru.job4j.articles.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.articles.model.Word;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class WordStore implements Store<Word>, AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(WordStore.class.getSimpleName());
    private Connection connection;
    String findAllsql = "select * from dictionary";

    public WordStore(Connection connection) {
        this.connection = connection;
        initScheme();
        initWords();
    }

    private void initScheme() {
        logger.info("Создание схемы таблицы слов");
        try (var statement = connection.createStatement()) {
            var sql = Files.readString(Path.of("db/scripts", "dictionary.sql"));
            statement.execute(sql);
        } catch (Exception e) {
            logger.error("Не удалось выполнить операцию: { }", e.getCause());
            throw new IllegalStateException();
        }
    }

    private void initWords() {
        logger.info("Заполнение таблицы слов");
        try (var statement = connection.createStatement()) {
            var sql = Files.readString(Path.of("db/scripts", "words.sql"));
            statement.executeLargeUpdate(sql);
        } catch (Exception e) {
            logger.error("Не удалось выполнить операцию: { }", e.getCause());
            throw new IllegalStateException();
        }
    }

    @Override
    public Word save(Word model) {
        logger.info("Добавление слова в базу данных");
        var sql = "insert into dictionary(word) values(?);";
        try (var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, model.getValue());
            statement.executeUpdate();
            var key = statement.getGeneratedKeys();
            if (key.next()) {
                model.setId(key.getInt(1));
            }
        } catch (Exception e) {
            logger.error("Не удалось выполнить операцию: { }", e.getCause());
            throw new IllegalStateException();
        }
        return model;
    }

    @Override
    public List<Word> findAll() {
        logger.info("Загрузка всех слов");
        var words = new ArrayList<Word>(1000);
        try (var statement = connection.prepareStatement(findAllsql)) {
            var selection = statement.executeQuery();
            while (selection.next()) {
                words.add(new Word(
                        selection.getInt("id"),
                        selection.getString("word")
                ));
            }
        } catch (Exception e) {
            logger.error("Не удалось выполнить операцию: { }", e.getCause());
            throw new IllegalStateException();
        }
        return words;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

}
