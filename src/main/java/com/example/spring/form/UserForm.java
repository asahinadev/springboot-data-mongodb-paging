package com.example.spring.form;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.groups.Default;

import org.hibernate.validator.constraints.Length;

import com.example.spring.entity.Roles;
import com.example.spring.validation.group.Create;

import lombok.Data;

@Data
public class UserForm {

	String id;

	@NotEmpty(groups = Default.class)
	@Pattern(regexp = "[\\w]+", groups = Default.class)
	@Length(min = 4, max = 16, groups = Default.class)
	String username;

	@NotEmpty(groups = Default.class)
	@Email(groups = Default.class)
	@Length(min = 4, max = 255, groups = Default.class)
	String email;

	@NotEmpty(groups = Create.class)
	@Pattern(regexp = "[\\w]+", groups = Default.class)
	@Length(min = 8, max = 16, groups = Default.class)
	String password;

	boolean enabled;

	boolean locked;

	LocalDateTime credentialsExpired;

	LocalDateTime accountExpired;

	List<Roles> authorities;
}
