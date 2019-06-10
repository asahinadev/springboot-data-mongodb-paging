package com.example.spring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.example.spring.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig
		extends WebSecurityConfigurerAdapter {

	public static final String HOME_PAGE = "/";

	public static final String LOGIN_PAGE = "/login";

	public static final String LOGIN_AREA = "/login/**";

	public static final String ERROR_AREA = "/error/**";

	public static final String SIGNUP_AREA = "/signup/**";

	public static final String PARAM_USERNAME = "username";

	public static final String PARAM_PASSWORD = "password";

	@Autowired
	UserService userService;

	@Bean
	public PasswordEncoder passwordEncoder() {

		return new BCryptPasswordEncoder();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder authentication) throws Exception {

		super.configure(authentication);
		authentication
				.userDetailsService(userService)
				.passwordEncoder(passwordEncoder());

	}

	@Override
	public void configure(WebSecurity web) throws Exception {

		// 標準
		super.configure(web);
		web.ignoring().antMatchers(
				"/webjars/**",
				"/error/**",
				"/css/**",
				"/js/**",
				"/images/**",
				"/favicon.ico"

		);

	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		log.debug("{}::configure(http)", getClass());

		// 認可の設定
		http
				.antMatcher("/**").authorizeRequests()
				// エラーページ関連、ホームページ、サインアップ関連、ログイン関連
				.antMatchers(ERROR_AREA, HOME_PAGE, SIGNUP_AREA, LOGIN_AREA).permitAll()
				// その他ページ郡
				.anyRequest().authenticated();

		// ログイン設定
		http.formLogin()
				.loginProcessingUrl(LOGIN_PAGE)
				.loginPage(LOGIN_PAGE)
				.failureUrl(LOGIN_PAGE + "?error")
				.defaultSuccessUrl("/")
				.usernameParameter(PARAM_USERNAME)
				.passwordParameter(PARAM_PASSWORD)
				.permitAll();

		// ログアウト設定
		http.logout()
				.logoutRequestMatcher(new AntPathRequestMatcher("/logout**"))
				.logoutSuccessUrl(LOGIN_PAGE + "?logout");

		// リメンバーミー
		http.rememberMe();
		/**/

	}
}
