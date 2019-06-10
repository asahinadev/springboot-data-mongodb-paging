package com.example.spring.service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.spring.entity.User;
import com.example.spring.repository.UserRepository;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService
		implements UserDetailsService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	public User copy(Object source, String... ignroePropertues) {

		return copy(source, new User(), ignroePropertues);
	}

	public User copy(Object source, User target, String... ignroePropertues) {

		BeanUtils.copyProperties(source, target, ignroePropertues);
		return target;
	}

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

	@Async
	@SneakyThrows
	public void insert(MultipartFile file, byte[] buffer) {

		log.debug("file          {}", file);
		log.debug("file size     {}", file.getSize());
		log.debug("file type     {}", file.getContentType());
		log.debug("file name     {}", file.getName());
		log.debug("file original {}", file.getOriginalFilename());

		try (CSVParser parser = CSVParser.parse(
				new ByteArrayInputStream(buffer),
				StandardCharsets.UTF_8,
				CSVFormat.EXCEL.withFirstRecordAsHeader())) {

			Map<String, Integer> headers = parser.getHeaderMap();

			log.debug("headers: {}", headers);

			List<User> users = parser.getRecords().stream().map(item -> {
				User user = new User();

				for (Map.Entry<String, Integer> entry : headers.entrySet()) {

					switch (entry.getKey()) {

					case "username":
						user.setUsername(item.get(entry.getValue()));
						break;

					case "email":
						user.setEmail(item.get(entry.getValue()));
						break;

					case "password":
						user.setPassword(item.get(entry.getValue()));
						break;

					}

				}
				log.info("csv user {}", user);
				return user;
			}).collect(Collectors.toList());
			insert(users);
		}

	}

	public List<User> insert(Iterable<User> entities) {

		Set<String> emails = new HashSet<>();
		Set<String> usernames = new HashSet<>();
		List<User> users = new ArrayList<>();

		for (User entity : entities) {
			if (!emails.contains(entity.getEmail())) {
				if (!usernames.contains(entity.getUsername())) {

					User e1 = findByEmail(entity.getEmail());
					User e2 = findByUsername(entity.getUsername());

					if (e1 != null) {
						log.warn("registered records [email] {}", entity);
						continue;
					}
					if (e2 != null) {
						log.warn("registered records [username] {}", entity);
						continue;
					}

					emails.add(entity.getEmail());
					usernames.add(entity.getUsername());
					users.add(entity);

				} else {
					log.warn("duplicate records [username] {}", entity);
					continue;
				}
			} else {
				log.warn("duplicate records [email] {}", entity);
				continue;
			}

			entity.setId(UUID.randomUUID().toString());
			changePassword(entity);
		}

		return userRepository.insert(users);
	}

	public User insert(User entity) {

		User e1 = findByEmail(entity.getEmail());
		User e2 = findByUsername(entity.getUsername());

		if (e1 != null) {
			log.warn("registered records [email] {}", entity);
			throw new IllegalArgumentException("registered records [email]");
		}
		if (e2 != null) {
			log.warn("registered records [username] {}", entity);
			throw new IllegalArgumentException("registered records [username]");
		}

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

		return Arrays.asList(user1, user2).stream().filter(item -> item != null).findFirst().orElseThrow();
	}

	public User save(User entity) {

		User e1 = findByEmail(entity.getEmail());
		User e2 = findByUsername(entity.getUsername());

		if (e1 != null && !StringUtils.equals(e1.getId(), entity.getId())) {
			log.warn("registered records [email] {}", entity);
			throw new IllegalArgumentException("registered records [email]");
		}

		if (e2 != null && !StringUtils.equals(e2.getId(), entity.getId())) {
			log.warn("registered records [username] {}", entity);
			throw new IllegalArgumentException("registered records [username]");
		}

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
