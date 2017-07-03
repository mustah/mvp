package com.elvaco.mvp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseLoader implements CommandLineRunner {
	private final UserRepository repository;

	@Autowired
	public DatabaseLoader(UserRepository repository) {
		this.repository = repository;
	}
	@Override
	public void run(String... args) throws Exception {
		repository.save(new User("Anton", "Löfgren"));
		repository.save(new User("Peter", "Janson"));
		repository.save(new User("Carl", "Helmertz"));


	}
}
