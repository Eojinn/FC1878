package Eojinn.FC1878;

import org.springframework.boot.SpringApplication;

public class TestFc1878Application {

	public static void main(String[] args) {
		SpringApplication.from(Fc1878Application::main).with(TestcontainersConfiguration.class).run(args);
	}

}
