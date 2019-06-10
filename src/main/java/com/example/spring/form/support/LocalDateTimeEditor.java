package com.example.spring.form.support;

import java.beans.PropertyEditorSupport;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;

public class LocalDateTimeEditor extends PropertyEditorSupport {

	// YYYY-MM-dd'T'HH:mm
	DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

	public void setAsText(String text) throws IllegalArgumentException {

		if (StringUtils.isEmpty(text)) {
			return;
		}

		this.setValue(LocalDateTime.parse(text, formatter));
	}

	@Override
	public LocalDateTime getValue() {

		if (super.getValue() instanceof LocalDateTime) {
			return (LocalDateTime) super.getValue();
		}
		return null;

	}

	@Override
	public String getAsText() {

		LocalDateTime value = getValue();
		if (value == null) {
			return null;
		}

		return value.format(formatter);
	}
}
