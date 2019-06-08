package com.example.spring.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.spring.entity.User;
import com.example.spring.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService
		implements UserDetailsService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	public Page<User> findAll() {
		Pageable page = PageRequest.of(0, 10);
		return userRepository.findAll(page);
	}

	public Page<User> findAll(Pageable page) {

		return userRepository.findAll(page);
	}

	public User findById(String id) {

		return userRepository.findById(id).orElseThrow();
	}

	public User findByUsername(String username) {

		return userRepository.findByUsername(username);
	}

	public User findByEmail(String email) {

		return userRepository.findByEmail(email);
	}

	public List<User> insert(Iterable<User> entities) {

		for (User entity : entities) {
			entity.setId(UUID.randomUUID().toString());
			changePassword(entity);
		}

		return userRepository.insert(entities);
	}

	public User insert(User entity) {

		entity.setId(UUID.randomUUID().toString());
		entity.setEnabled(true);
		changePassword(entity);
		return userRepository.insert(entity);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		Objects.requireNonNull(username, "username is null");

		User user1 = userRepository.findByUsername(username);
		User user2 = userRepository.findByEmail(username);

		log.debug("username {}", user1);
		log.debug("email    {}", user2);

		return Arrays.asList(user1, user2).stream().findFirst().orElseThrow();
	}

	public User save(User entity) {

		User old = userRepository.findById(entity.getId()).orElseThrow();

		if (StringUtils.isEmpty(entity.getPassword())) {
			entity.setPassword(old.getPassword());
		} else if (!StringUtils.equals(entity.getPassword(), old.getPassword())) {
			changePassword(entity);
		}

		return userRepository.save(entity);
	}

	public void delete(User entity) {

		userRepository.findById(entity.getId()).orElseThrow();
		userRepository.delete(entity);
	}

	public List<User> saveAll(Iterable<User> entities) {

		for (User entity : entities) {
			User old = userRepository.findById(entity.getId()).orElseThrow();

			if (!StringUtils.equals(entity.getPassword(), old.getPassword())) {
				changePassword(entity);
			}
		}

		return userRepository.saveAll(entities);
	}

	private void changePassword(User entity) {

		if (StringUtils.isNotEmpty(entity.getPassword())) {
			entity.setPassword(passwordEncoder.encode(entity.getPassword()));
			entity.setAccountExpired(LocalDateTime.now().plusDays(60));
			entity.setCredentialsExpired(LocalDateTime.now().plusDays(30));
		}
	}

}
