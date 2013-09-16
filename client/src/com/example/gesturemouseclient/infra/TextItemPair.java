package com.example.gesturemouseclient.infra;

public class TextItemPair<T> extends Pair<String, T> {

	public TextItemPair(String text, T item) {
		super(text, item);
	}

	public T getItem() {
		return getItem2();
	}

	public String toString() {
		return getItem1();
	}

}
