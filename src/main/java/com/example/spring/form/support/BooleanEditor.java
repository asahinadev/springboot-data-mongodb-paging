package com.example.spring.form.support;

import java.beans.PropertyEditorSupport;

public class BooleanEditor extends PropertyEditorSupport {

	public void setAsText(String text) throws IllegalArgumentException {

		this.setValue(Boolean.valueOf(text));
	}

	@Override
	public Boolean getValue() {

		if (super.getValue() instanceof Boolean) {
			return (Boolean) super.getValue();
		}
		return false;

	}
}
