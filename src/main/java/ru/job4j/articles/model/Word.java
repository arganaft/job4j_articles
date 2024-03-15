package ru.job4j.articles.model;

public class Word implements Cloneable {

    private int id;

    private String value;

    public Word(int id, String value) {
        this.id = id;
        this.value = value.intern();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public int getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
