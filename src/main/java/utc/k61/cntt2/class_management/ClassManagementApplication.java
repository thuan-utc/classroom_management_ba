package utc.k61.cntt2.class_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

//@EnableWebMvc
@SpringBootApplication
public class ClassManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClassManagementApplication.class, args);
	}

}
