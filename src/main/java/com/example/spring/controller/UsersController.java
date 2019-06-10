package com.example.spring.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.groups.Default;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.spring.entity.Roles;
import com.example.spring.entity.User;
import com.example.spring.form.UserForm;
import com.example.spring.service.UserService;
import com.example.spring.util.PaginationUtil;
import com.example.spring.util.UriUtil;
import com.example.spring.validation.group.Create;

import lombok.SneakyThrows;
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

	public static final String PATH_UPLOAD_CSV = "/upload/csv";

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

	/**
	 * index ページ用.
	 * 
	 * @param request リクエスト情報.
	 * @return 画面表示用ワード（テンプレート、リダイレクト）.
	 */
	@GetMapping(PATH_INDEX)
	public String index(HttpServletRequest request,
			@PageableDefault(page = 0, size = 10, sort = { "username" }) Pageable pageable) {

		log.debug("pageable {}", pageable);

		Page<User> users = service.findAll(pageable);
		log.debug("{}", users);

		Map<String, String> paging = PaginationUtil.<User>of()
				.first(0)
				.prev(pageable.getPageNumber() - 1)
				.page(users)
				.next(pageable.getPageNumber() + 1)
				.last(users.getTotalPages() - 1)
				.maxPageView(10)
				.build().pagination(UriComponentsBuilder.fromPath(URI_PREFIX + PATH_INDEX), pageable);

		// 値の設定
		request.setAttribute("users", users);
		request.setAttribute("paging", paging);
		request.setAttribute("sorted", pageable.getSort().get().findFirst().get().getProperty());
		request.setAttribute("direction", pageable.getSort().get().findFirst().get().getDirection().name());
		request.setAttribute("pageSize", pageable.getPageSize());

		// テンプレート
		return TPL_PREFIX + PAGE_INDEX;

	}

	@GetMapping(PATH_UPLOAD_CSV)
	public String upload(RedirectAttributes redirect) {

		// リダイレクト
		redirect.addFlashAttribute("success", "直接アクセスしてはいけない");
		return UriUtil.redirect(URI_PREFIX + PATH_INDEX);

	}

	/**
	 * csv upload ページ用 (POST).
	 * 
	 * @param request  リクエスト情報.
	 * @param file     アップロードファイル
	 * @param redirect リダイレクト情報
	 * @return 画面表示用ワード（テンプレート、リダイレクト）.
	 */
	@SneakyThrows
	@PostMapping(PATH_UPLOAD_CSV)
	public String upload(
			HttpServletRequest request,
			@RequestParam("file") MultipartFile file,
			RedirectAttributes redirect) {

		// CSV インサート
		service.insert(file, file.getBytes());

		// リダイレクト
		redirect.addFlashAttribute("success", "登録に成功しました。");
		return UriUtil.redirect(URI_PREFIX + PATH_INDEX);
	}

	/**
	 * create ページ用 (GET).
	 * 
	 * @param form 入力フォーム.
	 * @return 画面表示用ワード（テンプレート、リダイレクト）.
	 */
	@GetMapping(PATH_CREATE)
	public String create(@ModelAttribute("form") UserForm form) {

		// テンプレート
		return TPL_PREFIX + PAGE_CREATE;
	}

	/**
	 * create ページ用 (POST).
	 * 
	 * @param form     入力フォーム.
	 * @param result   バリデーション結果
	 * @param redirect リダイレクト情報
	 * @return 画面表示用ワード（テンプレート、リダイレクト）.
	 */
	@PostMapping(PATH_CREATE)
	public String create(
			@ModelAttribute("form") @Validated(Create.class) UserForm form,
			BindingResult result,
			RedirectAttributes redirect) {

		// エラー判定
		if (result.hasErrors()) {

			// テンプレート
			return TPL_PREFIX + PAGE_CREATE;
		}

		// 登録処理
		User user = service.insert(service.copy(form));
		log.debug("user {}", user);

		// リダイレクト
		redirect.addFlashAttribute("success", "登録に成功しました。");
		return UriUtil.redirect(URI_PREFIX + PATH_MODIFY, user.getId());
	}

	/**
	 * modify ページ用 (GET).
	 * 
	 * @param user ユーザー情報
	 * @param form 入力フォーム.
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

		// テンプレート
		return TPL_PREFIX + PAGE_MODIFY;
	}

	/**
	 * modify ページ用 (POST).
	 * 
	 * @param user     ユーザー情報
	 * @param form     入力フォーム.
	 * @param result   バリデーション結果
	 * @param redirect リダイレクト情報
	 * @return 画面表示用ワード（テンプレート、リダイレクト）.
	 */
	@PostMapping(PATH_MODIFY)
	public String modify(
			@ModelAttribute("user") User user,
			@ModelAttribute("form") @Validated(Default.class) UserForm form,
			BindingResult result,
			RedirectAttributes redirect) {

		// エラー判定
		if (result.hasErrors()) {
			return TPL_PREFIX + PAGE_MODIFY;
		}

		// 更新処理
		user = service.save(service.copy(form, user));
		log.debug("user {}", user);

		// リダイレクト
		redirect.addFlashAttribute("success", "更新に成功しました。");
		return UriUtil.redirect(URI_PREFIX + PATH_MODIFY, user.getId());
	}

	/**
	 * view ページ用 (GET).
	 * 
	 * @param user ユーザー情報
	 * @param form 入力フォーム.
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

		// テンプレート
		return TPL_PREFIX + PAGE_VIEW;
	}

	/**
	 * delete ページ用 (GET).
	 * 
	 * @param user ユーザー情報
	 * @param form 入力フォーム.
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

		// テンプレート
		return TPL_PREFIX + PAGE_DELETE;
	}

	/**
	 * delete ページ用 (POST/DELETE).
	 * 
	 * @param user     ユーザー情報
	 * @param redirect リダイレクト情報
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

		// リダイレクト
		redirect.addFlashAttribute("success", "削除に成功しました。");
		return UriUtil.redirect(URI_PREFIX + PATH_INDEX);
	}
}
