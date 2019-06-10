package com.example.spring.controller.advice;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

import com.example.spring.form.support.BooleanEditor;

@ControllerAdvice
public class AppControllerAdvice {

	@InitBinder
	public void initBuilder(WebDataBinder binder) {

		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
		binder.registerCustomEditor(boolean.class, new BooleanEditor());
		binder.registerCustomEditor(Boolean.class, new BooleanEditor());

	}
}
