package com.example.spring.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Getter
@Builder(builderMethodName = "of")
public class PaginationUtil<E> {

	final Integer first;

	final Integer prev;

	final Integer next;

	final Integer last;

	Page<E> page;

	int maxPageView;

	public Map<String, String> pagination(UriComponentsBuilder builder, Pageable pageable) {

		Function<Integer, String> uri = page -> {
			log.debug("page {}", page);
			return builder
					.replaceQueryParam("page", page)
					.replaceQueryParam("size", pageable.getPageSize())
					.replaceQueryParam("sort", pageable.getSort().get().map(order -> {
						return String.format("%s,%s", order.getProperty(), order.getDirection().name());
					}).toArray())
					.build()
					.toUriString();
		};

		LinkedHashMap<String, String> result = new LinkedHashMap<>();

		if (!page.isEmpty() && page.getTotalPages() > 1) {

			if (first != null) {
				result.put("First", page.isFirst() ? null : uri.apply(first));
			}

			if (prev != null) {
				result.put("Prev", page.hasPrevious() ? uri.apply(prev) : null);
			}

			int start = 0;
			int current = page.getPageable().getPageNumber();
			int end = page.getTotalPages();

			// 最大ページ指定時
			while (maxPageView > 0) {

				// 最大ページ未満の場合 ex. maxPageView = 10 : 1 ～ 10
				if (page.getTotalPages() < maxPageView) {
					end = maxPageView;
					break;
				}

				// 前ページ分が不足
				int div = maxPageView / 2;
				if (current <= div) {
					end = maxPageView;
					break;
				}

				// 後ページ分が不足
				if (current + div > end) {
					start = end - maxPageView;
					break;
				}

				break;
			}

			for (int i = start; i < end; i++) {
				if (i == current) {
					result.put(String.format("%d", i + 1), "#current");
				} else if (i < page.getTotalPages()) {
					result.put(String.format("%d", i + 1), uri.apply(i));
				} else {
					result.put(String.format("%d", i + 1), null);
				}
			}

			if (next != null) {
				result.put("Next", page.hasNext() ? uri.apply(next) : null);
			}
			if (last != null) {
				result.put("Last", page.isLast() ? null : uri.apply(last));
			}
		}

		return result;
	}

}
