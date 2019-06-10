package com.example.spring.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {

		PageableHandlerMethodArgumentResolver pageable = new PageableHandlerMethodArgumentResolver();
		pageable.setFallbackPageable(PageRequest.of(0, 10, Sort.by("_id")));
		resolvers.add(pageable);

	}
}
