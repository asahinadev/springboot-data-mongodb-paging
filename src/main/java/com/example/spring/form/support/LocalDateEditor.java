package com.example.spring.form.support;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;

public class LocalDateEditor extends PropertyEditorSupport {

	// YYYY-MM-dd
	DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

	public void setAsText(String text) throws IllegalArgumentException {

		if (StringUtils.isEmpty(text)) {
			return;
		}

		this.setValue(LocalDate.parse(text, formatter));
	}

	@Override
	public LocalDate getValue() {

		if (super.getValue() instanceof LocalDate) {
			return (LocalDate) super.getValue();
		}
		return null;

	}

	@Override
	public String getAsText() {

		LocalDate value = getValue();
		if (value == null) {
			return null;
		}

		return value.format(formatter);
	}
}
