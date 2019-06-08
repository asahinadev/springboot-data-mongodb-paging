package com.example.spring.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.groups.Default;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.spring.entity.Roles;
import com.example.spring.entity.User;
import com.example.spring.form.UserForm;
import com.example.spring.service.UserService;
import com.example.spring.validation.group.Create;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping(UsersController.URI_PREFIX)
public class UsersController {

	public static final String URI_PREFIX = "/user";
	public static final String TPL_PREFIX = "user/";
	public static final String PAGE_INDEX = "index";
	public static final String PAGE_CREATE = "create";
	public static final String PAGE_MODIFY = "modify";
	public static final String PAGE_DELETE = "delete";
	public static final String PAGE_VIEW = "view";
	public static final String PATH_INDEX = "";
	public static final String PATH_PAGE = "/page/{page}";
	public static final String PATH_TEST = "test";
	public static final String PATH_CREATE = "/create";
	public static final String PATH_MODIFY = "/modify/{id}";
	public static final String PATH_DELETE = "/delete/{id}";
	public static final String PATH_VIEW = "/view/{id}";

	@Autowired
	protected UserService service;

	@ModelAttribute("roles")
	public Roles[] roles() {

		return Roles.values();
	}

	@ModelAttribute("user")
	public User user(@PathVariable(value = "id", required = false) String id) {

		if (StringUtils.isNotEmpty(id)) {
			return service.findById(id);
		}
		return new User();

	}

	//	/**
	//	 * index ページ用.
	//	 * 
	//	 * @param request リクエスト情報.
	//	 * @return 画面表示用ワード（テンプレート、リダイレクト）.
	//	 */
	//	@GetMapping(PATH_TEST)
	//	public String test(HttpServletRequest request) {
	//
	//		String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYYMMddHHmmss"));
	//		List<User> users = service.findAll();
	//		for (int i = 0; i < 1000; i++) {
	//			User user = new User();
	//			user.setUsername(now + String.format("%06d", i));
	//			user.setEmail(now + String.format("%06d@example.com", i));
	//			user.setPassword(UUID.randomUUID().toString());
	//			users.add(user);
	//		}
	//		service.insert(users);
	//		return "redirect:"
	//				+ UriComponentsBuilder.fromPath(URI_PREFIX + PATH_INDEX).toUriString();
	//	}

	/**
	 * index ページ用.
	 * 
	 * @param request リクエスト情報.
	 * @return 画面表示用ワード（テンプレート、リダイレクト）.
	 */
	@GetMapping(PATH_INDEX)
	public String index(HttpServletRequest request) {
		return index(request, 1);
	}

	/**
	 * index ページ用.
	 * 
	 * @param request リクエスト情報.
	 * @return 画面表示用ワード（テンプレート、リダイレクト）.
	 */
	@GetMapping(PATH_PAGE)
	public String index(HttpServletRequest request, @PathVariable("page") Integer page) {
		log.debug("page {}", page);
		PageRequest pageRequest = PageRequest.of(0, 10);
		if (page != null) {
			pageRequest = PageRequest.of(Math.max(page - 1, 0), 10);
		}

		Page<User> users = service.findAll(pageRequest);
		log.debug("{}", users);

		Map<String, String> paging = new LinkedHashMap<>();

		int start = Math.max(users.getPageable().getPageNumber() - 5, 0);
		int end = start + 10;

		if (end > 10 && users.getTotalPages() < end) {
			end = users.getTotalPages();
			start = end - 10;
		}

		log.debug("start => {}", start);
		log.debug("end   => {}", end);
		log.debug("total => {}", users.getTotalPages());

		put(paging, "First", 1, users.isFirst());
		put(paging, "Prev", page - 1, !users.hasPrevious());
		for (int i = start + 1; i <= end; i++) {
			if (i == page) {
				put(paging, i, -1, i > users.getTotalPages());
			} else {
				put(paging, i, i, i > users.getTotalPages());
			}
		}
		put(paging, "Next", Math.min(page + 1, users.getTotalPages()), !users.hasNext());
		put(paging, "Last", users.getTotalPages(), users.isLast());

		// 値の設定
		request.setAttribute("users", users);
		request.setAttribute("paging", paging);

		return TPL_PREFIX + PAGE_INDEX;

	}

	private void put(Map<String, String> paging, Object key, int page, boolean disabled) {
		String url = "#";
		if (!disabled) {
			url = uri(URI_PREFIX + PATH_PAGE, Math.max(page, 1));
		}
		if (page == -1 && key instanceof Number) {
			url = "#CURRENT";
		}
		paging.put(key.toString(), url);
	}

	/**
	 * create ページ用 (GET).
	 * 
	 * @param request リクエスト情報.
	 * @param form    入力フォーム.
	 * @return 画面表示用ワード（テンプレート、リダイレクト）.
	 */
	@GetMapping(PATH_CREATE)
	public String create(@ModelAttribute("form") UserForm form) {

		return TPL_PREFIX + PAGE_CREATE;
	}

	/**
	 * create ページ用 (POST).
	 * 
	 * @param request リクエスト情報.
	 * @param form    入力フォーム.
	 * @param result  バリデーション結果
	 * @return 画面表示用ワード（テンプレート、リダイレクト）.
	 */
	@PostMapping(PATH_CREATE)
	public String create(
			@ModelAttribute("form") @Validated({
					Default.class,
					Create.class
			}) UserForm form,
			BindingResult result,
			RedirectAttributes redirect) {

		// エラー判定
		if (result.hasErrors()) {
			return TPL_PREFIX + PAGE_CREATE;
		}

		// 登録処理
		User user = new User();
		BeanUtils.copyProperties(form, user);
		user = service.insert(user);
		log.debug("user {}", user);

		// 更新結果をリダイレクト先に
		redirect.addFlashAttribute("success", "登録に成功しました。");

		return "redirect:"
				+ UriComponentsBuilder.fromPath(URI_PREFIX + PATH_MODIFY).build(user.getId()).toASCIIString();
	}

	/**
	 * modify ページ用 (GET).
	 * 
	 * @param id      識別用ID
	 * @param request リクエスト情報.
	 * @param form    入力フォーム.
	 * @return 画面表示用ワード（テンプレート、リダイレクト）.
	 */
	@GetMapping(PATH_MODIFY)
	public String modify(
			@ModelAttribute("user") User user,
			@ModelAttribute("form") UserForm form) {

		// 対象情報取得
		log.debug("user {}", user);

		// 値の設定
		BeanUtils.copyProperties(user, form);

		return TPL_PREFIX + PAGE_MODIFY;
	}

	/**
	 * modify ページ用 (POST).
	 * 
	 * @param id      識別用ID
	 * @param request リクエスト情報.
	 * @param form    入力フォーム.
	 * @param result  バリデーション結果
	 * @return 画面表示用ワード（テンプレート、リダイレクト）.
	 */
	@PostMapping(PATH_MODIFY)
	public String modify(
			@ModelAttribute("user") User user,
			@ModelAttribute("form") @Validated({
					Default.class
			}) UserForm form,
			BindingResult result,
			RedirectAttributes redirect) {

		// 対象情報取得
		log.debug("user {}", user);

		// エラー判定
		if (result.hasErrors()) {
			return TPL_PREFIX + PAGE_MODIFY;
		}

		// 更新処理
		BeanUtils.copyProperties(form, user);
		user = service.save(user);
		log.debug("user {}", user);

		// 更新結果をリダイレクト先に
		redirect.addFlashAttribute("success", "更新に成功しました。");

		return "redirect:"
				+ UriComponentsBuilder.fromPath(URI_PREFIX + PATH_MODIFY).build(user.getId()).toASCIIString();
	}

	/**
	 * view ページ用 (GET).
	 * 
	 * @param id      識別用ID
	 * @param request リクエスト情報.
	 * @param form    入力フォーム.
	 * @return 画面表示用ワード（テンプレート、リダイレクト）.
	 */
	@GetMapping(PATH_VIEW)
	public String view(
			@ModelAttribute("user") User user,
			@ModelAttribute("form") UserForm form) {

		// 対象情報取得
		log.debug("user {}", user);

		// 値の設定
		BeanUtils.copyProperties(user, form);

		return TPL_PREFIX + PAGE_VIEW;
	}

	/**
	 * delete ページ用 (GET).
	 * 
	 * @param id      識別用ID
	 * @param request リクエスト情報.
	 * @param form    入力フォーム.
	 * @return 画面表示用ワード（テンプレート、リダイレクト）.
	 */
	@GetMapping(PATH_DELETE)
	public String delete(
			@ModelAttribute("user") User user,
			@ModelAttribute("form") UserForm form) {

		// 対象情報取得
		log.debug("user {}", user);

		// 値の設定
		BeanUtils.copyProperties(user, form);

		return TPL_PREFIX + PAGE_DELETE;
	}

	/**
	 * delete ページ用 (POST/DELETE).
	 * 
	 * @param id 識別用ID
	 * @return 画面表示用ワード（テンプレート、リダイレクト）.
	 */
	@PostMapping(PATH_DELETE)
	@DeleteMapping(PATH_DELETE)
	public String delete(
			@ModelAttribute("user") User user,
			RedirectAttributes redirect) {

		// 対象情報取得
		log.debug("user {}", user);

		// 削除処理
		service.delete(user);

		// 更新結果をリダイレクト先に
		redirect.addFlashAttribute("success", "削除に成功しました。");

		return "redirect:"
				+ UriComponentsBuilder.fromPath(URI_PREFIX + PATH_INDEX).toUriString();
	}

	String uri(String path, Object... args) {
		return UriComponentsBuilder.fromPath(path).build(args).toASCIIString();
	}
}
