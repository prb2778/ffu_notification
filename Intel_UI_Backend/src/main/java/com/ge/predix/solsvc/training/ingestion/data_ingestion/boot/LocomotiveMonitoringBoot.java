package com.ge.predix.solsvc.training.ingestion.data_ingestion.boot;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.web.context.support.StandardServletEnvironment;

import com.ge.predix.solsvc.training.ingestion.data_ingestion.service.DataIngestionServiceController;




/**
 * 
 * @author predix -
 */
@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages={"com.ge.predix.solsvc.bootstrap.tsb.client","com.ge.predix.solsvc","com.ge.predix.solsvc.bootstrap.tsb","com.ge.predix.solsvc.training.ingestion.data_ingestion"})
@PropertySource("classpath:application-default.properties")
public class LocomotiveMonitoringBoot {
	private static Logger log = LoggerFactory.getLogger(LocomotiveMonitoringBoot.class);
    /**
     * @param args -
     */
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(LocomotiveMonitoringBoot.class);
        ConfigurableApplicationContext ctx = app.run(args);

		log.info("Let's inspect the properties provided by Spring Boot:");
		MutablePropertySources propertySources = ((StandardServletEnvironment) ctx
				.getEnvironment()).getPropertySources();
		Iterator<org.springframework.core.env.PropertySource<?>> iterator = propertySources
				.iterator();
		while (iterator.hasNext()) {
			Object propertySourceObject = iterator.next();
			if (propertySourceObject instanceof org.springframework.core.env.PropertySource) {
				org.springframework.core.env.PropertySource<?> propertySource = (org.springframework.core.env.PropertySource<?>) propertySourceObject;
				log.info("propertySource=" + propertySource.getName()
						+ " values=" + propertySource.getSource() + "class="
						+ propertySource.getClass());
			}
		}

		log.info("Let's inspect the profiles provided by Spring Boot:");
		String profiles[] = ctx.getEnvironment().getActiveProfiles();
		for (int i = 0; i < profiles.length; i++)
			log.info("profile=" + profiles[i]);
	    
	    LocomotiveMonitoringBoot loc = new LocomotiveMonitoringBoot();
		loc.setCronService();
    }
    /**
     * @return -
     */
    @Bean
    public TomcatEmbeddedServletContainerFactory tomcatEmbeddedServletContainerFactory() {
        return new TomcatEmbeddedServletContainerFactory();
    }
	
    /**
    *
    *The job is runing once the app is deployed.
    */	
    private void setCronService() {
    	try
        {
    		log.info("Monitor is startting");
    		Properties prop = new Properties();
			String propFileName = "config.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
			try {
    			Timer t = new Timer();
    			DataIngestionServiceController task = new DataIngestionServiceController();
    			t.scheduleAtFixedRate(task, 0, new Long (prop.getProperty("periodic.basis")));
    		} catch (Exception e) {
    			e.printStackTrace();
    			
    		}
    		
    	}catch (Throwable e){
            log.error("Failure in /startTSMonitor POST ", e);
            
        }
    }
    

}
