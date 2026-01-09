package oeapi;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import oeapi.model.Organization;
import oeapi.model.Role;
import oeapi.model.User;
import oeapi.model.oeapiFieldsOfStudy;
import oeapi.payload.OrganizationDTO;
import oeapi.repository.RoleRepository;
import oeapi.repository.UserRepository;
import oeapi.repository.oeapiFieldsOfStudyRepository;
import oeapi.service.OrganizationService;

/**
 * The type Ooapi unita application.
 *
 * @author Carlos Alonso - losalo@unavarra.es
 */
@SpringBootApplication(scanBasePackages = {"oeapi.*"})
@EnableJpaRepositories(basePackages = {"oeapi.repository"})

@EntityScan(basePackages = {"oeapi.*"})
@ComponentScan(basePackages = {"oeapi"})

public class oeapiApplication {

    private static final String dateFormat = "yyyy-MM-dd";
    private static final String timeFormat = "HH:mm";

    static Logger logger = LoggerFactory.getLogger(oeapiApplication.class);

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {

        // Initialize Spring context to get DataSource bean
        ApplicationContext context = SpringApplication.run(oeapiApplication.class, args);

        // Get DataSource from Spring Boot
        DataSource dataSource = context.getBean(DataSource.class);

        logger.info("\n\n -------> Initialization of the OEAPI endpoint....\n\n");
        logger.info("-->Before starting, let's check the database availability...");

        while (!isDatabaseOnline(dataSource)) {
            logger.error("-->Database is still offline. Retrying in 5 seconds...");
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.exit(1);
            }
        }

       logger.info("\n\n-->Database is online. Application is ready!\n\n");
        //SpringApplication.run(oeapiApplication.class, args);
    }

    private static boolean isDatabaseOnline(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    @Value("${ooapi.security.default.users.pass:unset}")
    private String adminPassword;

    @Value("${ooapi.security.default.users.emails:test@example.com}")
    private List<String> adminEmails;

    private User createUserIfNotExists(UserRepository userRepo, String email, String password, List<Role> roles) {
        Optional<User> existing = userRepo.findByEmail(email);
        if (existing.isPresent()) {
            logger.info("User {} already exists..", email);
            return existing.get();
        }

        User user = new User();
        user.setEmail(email);
        user.setRoles(roles);

        String generatedPassword = null;
        if (password.equals("unset")) {
            generatedPassword = oeapiUtils.generatePassword();
            user.setPassword(generatedPassword);
        } else {
            user.setPassword(password);
        }

        userRepo.save(user);

        logger.info("Created user \"{}\", password {}, and roles: {}.",
                    email,
                    generatedPassword == null ? "from configuration" : String.format("\"%s\"", generatedPassword),
                    String.join(", ", roles.stream().map(Role::getName).toList()));

        return user;
    }

    @Bean
    public CommandLineRunner createUsersAndRoles(UserRepository userRepo, RoleRepository roleRepo) {
        return (args) -> {
            logger.info("-->Inserting/Updating users and roles from application properties");

            for (String roleName : Arrays.asList("ROLE_ADMIN", "ROLE_USER")) {
                roleRepo.findByName(roleName).orElseGet(() -> roleRepo.save(new Role(roleName)));
            }

            Role adminRole = roleRepo.findByName("ROLE_ADMIN").get();
            for (String email : adminEmails) {
                createUserIfNotExists(userRepo, email, adminPassword, Arrays.asList(adminRole));
            }
        };
    }

    @Bean
    public CommandLineRunner createOrganizations(OrganizationService orgService) {
        return (args) -> {
            logger.info("-->Inserting/Updating organizations from /orgs.json");

            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = getClass().getResourceAsStream("/orgs.json");
            List<OrganizationDTO> organizations = mapper.readValue(
                    inputStream,
                    new TypeReference<List< OrganizationDTO>>() {
            });

            for (OrganizationDTO dto : organizations) {
                Organization org = orgService.toEntity(dto);
                if (!orgService.exists(org)) {
                    orgService.create(org);
                }
            }
        };
    }

    @Bean
    public CommandLineRunner loadFieldsOfStudy(oeapiFieldsOfStudyRepository repo) {
        return (args) -> {
            logger.info("-->Inserting/Updating Fields of Study from /fieldsOfStudy.json");
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = getClass().getResourceAsStream("/fieldsOfStudy.json");
            List<oeapiFieldsOfStudy> fieldsOfStudies = mapper.readValue(inputStream,
                    new TypeReference<List< oeapiFieldsOfStudy>>() {
            });

            for (oeapiFieldsOfStudy fos : fieldsOfStudies) {
                oeapiFieldsOfStudy fos_entity = new oeapiFieldsOfStudy();
                fos_entity.setFieldsOfStudyId(fos.getFieldsOfStudyId());
                fos_entity.setLevel(fos.getLevel());
                fos_entity.setTxtEn(fos.getTxtEn());
                fos_entity.setTxtFr(fos.getTxtFr());
                if (fos.getParent() != "") {
                    fos_entity.setParent(fos.getParent());
                }
                repo.save(fos_entity);
            }
        };
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return new Jackson2ObjectMapperBuilderCustomizer() {
            @Override
            public void customize(Jackson2ObjectMapperBuilder builder) {
                // Custom Date format for LocalDate

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(dateFormat);
                builder.deserializers(new LocalDateDeserializer(dateFormatter));
                builder.serializers(new LocalDateSerializer(dateFormatter));

                // Custom Time format for LocalTime
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(timeFormat);
                builder.deserializers(new LocalTimeDeserializer(timeFormatter));
                builder.serializers(new LocalTimeSerializer(timeFormatter));
            }
        };
    }

    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Converter from String to LocalDate
        Converter<String, LocalDate> stringToLocalDate = new Converter<String, LocalDate>() {
            @Override
            public LocalDate convert(MappingContext<String, LocalDate> context) {
                return LocalDate.parse(context.getSource(), DateTimeFormatter.ISO_LOCAL_DATE);
            }
        };

        // Converter from LocalDate to String
        Converter<LocalDate, String> localDateToString = new Converter<LocalDate, String>() {
            @Override
            public String convert(MappingContext<LocalDate, String> context) {
                return context.getSource().format(DateTimeFormatter.ISO_LOCAL_DATE);
            }
        };

        // Add converters
        modelMapper.addConverter(stringToLocalDate);
        modelMapper.addConverter(localDateToString);

        return modelMapper;
    }
}
