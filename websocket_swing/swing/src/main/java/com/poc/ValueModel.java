package com.poc;

public class ValueModel<T> {

    private T field;

    public ValueModel(T field) {
        this.field = field;
    }

    public T getField() {
        return field;
    }

    public void setField(T field) {
        this.field = field;
    }
}
