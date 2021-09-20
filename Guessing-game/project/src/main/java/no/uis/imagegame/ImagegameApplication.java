package no.uis.imagegame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import no.uis.players.User;

@SpringBootApplication
public class ImagegameApplication implements WebMvcConfigurer {

	public static void main(String[] args) {
		User.loadUsers();
		User.getUser("jone").addScore(100);
		User.getUser("asbjorn").addScore(200);
		SegmentStatistics.loadSegmentStatistics();
		SpringApplication.run(ImagegameApplication.class, args);
	}
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
	}

}
