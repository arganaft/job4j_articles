package ru.job4j.articles.service.generator;

import ru.job4j.articles.model.Article;
import ru.job4j.articles.model.Word;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RandomArticleGenerator implements ArticleGenerator {
    private StringBuilder articleBuilder = new StringBuilder(6000);
    private final String separator = " ";
    @Override
    public Article generate(List<Word> words) {
        Collections.shuffle(words);
        articleBuilder.setLength(0);
        for (Word word : words) {
            articleBuilder.append(word.getValue());
            articleBuilder.append(separator);
        }
        return new Article(articleBuilder.toString());
    }
}
