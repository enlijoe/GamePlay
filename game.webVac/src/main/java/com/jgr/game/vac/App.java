package com.jgr.game.vac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = {"com.jgr.game.vac.entity", "com.jgr.game.vac.config","com.jgr.game.vac.controler", "com.jgr.game.vac.dao", "com.jgr.game.vac.service"})
public class App extends SpringBootServletInitializer {
	private static Logger logger = LoggerFactory.getLogger(App.class);

	
    public static void main(String... args) {
    	logger.info("Before String init");
    	SpringApplication app = new SpringApplication(App.class);
    	logger.info("After String init");
    	app.run(args);
    }
    
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(App.class);
    }    
    
    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
    	return new CommandLineRunner() {
    		@Override
    		public void run(String... args) throws Exception {
    			logger.info("Spring in up and kicking.");
    		}
    	};
    }
    
}
