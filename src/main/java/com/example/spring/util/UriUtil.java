package com.example.spring.util;

import org.springframework.web.util.UriComponentsBuilder;

public class UriUtil {

	public static String redirect(String path, Object... pathValue) {

		return "redirect:"
				+ UriComponentsBuilder.fromPath(path).build(pathValue).toASCIIString();

	}
}
