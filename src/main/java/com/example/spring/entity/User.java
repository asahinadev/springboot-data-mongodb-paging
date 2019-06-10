package com.example.spring.entity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@SuppressWarnings("serial")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("users")
public class User
		implements UserDetails {

	@Id
	@Field("id")
	String id;

	@Field("username")
	String username;

	@Field("email")
	String email;

	@Field("password")
	String password;

	@Field("enabled")
	boolean enabled;

	@Field("locked")
	boolean locked;

	@Field("credentials_expired")
	LocalDate credentialsExpired;

	@Field("account_expired")
	LocalDate accountExpired;

	@Field("roles")
	List<Roles> authorities;

	public List<Roles> getAuthorities() {

		if (authorities == null || authorities.isEmpty()) {
			return Arrays.asList(Roles.GUEST);
		}

		return authorities;
	}

	@Override
	public boolean isAccountNonExpired() {

		if (accountExpired == null) {
			return true;
		}
		return accountExpired.isAfter(LocalDate.now());
	}

	@Override
	public boolean isAccountNonLocked() {

		return !locked;
	}

	@Override
	public boolean isCredentialsNonExpired() {

		if (credentialsExpired == null) {
			return true;
		}
		return credentialsExpired.isAfter(LocalDate.now());
	}

}
