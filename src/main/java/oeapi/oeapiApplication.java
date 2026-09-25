package oeapi;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalTimeSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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

    @Value("${ooapi.security.default.users.pass:}")
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
        if (password.isEmpty()) {
            generatedPassword = oeapiUtils.generatePassword();
            password = generatedPassword;
        }
        user.setPassword(new BCryptPasswordEncoder().encode(password));

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

            for (String roleName : Arrays.asList("ROLE_ADMIN", "ROLE_USER", "ROLE_GUEST")) {
                roleRepo.findByName(roleName).orElseGet(() -> roleRepo.save(new Role(roleName)));
            }

            Role adminRole = roleRepo.findByName("ROLE_ADMIN").get();
            for (String email : adminEmails) {
                createUserIfNotExists(userRepo, email, adminPassword, Arrays.asList(adminRole));
            }
        };
    }


    @Value("${quickdashboard.auto-create-file.organizations:}")
    private String createOrganizationsFile;

    @Bean
    public CommandLineRunner createOrganizations(OrganizationService orgService) {
        return (args) -> {
            if (createOrganizationsFile.isEmpty()) return;

            logger.info("-->Inserting/Updating organizations from: {}", createOrganizationsFile);

            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = new FileInputStream(createOrganizationsFile);
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

    /**
     * The OOAPI date and time formats on the wire.
     *
     * <p>Boot 4 renames the hook - Jackson2ObjectMapperBuilderCustomizer becomes
     * JsonMapperBuilderCustomizer - and hands over Jackson's own JsonMapper.Builder instead
     * of Spring's Jackson2ObjectMapperBuilder. That builder has no serializers() /
     * deserializers() shortcut, so the four serializers are carried by a module, which is
     * what those shortcuts did underneath anyway.</p>
     *
     * <p>This bean is the reason the migration cannot be judged by whether it compiles.
     * Jackson 3 writes dates as ISO-8601 strings by default where Jackson 2 wrote numeric
     * timestamps; these formatters are what keep the responses OOAPI-shaped, and if they
     * stop being applied nothing fails to build - the dates just come out wrong. Check
     * an academicSession and a courseOffering by hand after this lands.</p>
     */
    @Bean
    public JsonMapperBuilderCustomizer jsonCustomizer() {
        return new JsonMapperBuilderCustomizer() {
            @Override
            public void customize(JsonMapper.Builder builder) {

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(dateFormat);
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(timeFormat);

                SimpleModule ooapiDateFormats = new SimpleModule("ooapi-date-formats");

                // Custom Date format for LocalDate
                ooapiDateFormats.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
                ooapiDateFormats.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));

                // Custom Time format for LocalTime
                ooapiDateFormats.addSerializer(LocalTime.class, new LocalTimeSerializer(timeFormatter));
                ooapiDateFormats.addDeserializer(LocalTime.class, new LocalTimeDeserializer(timeFormatter));

                builder.addModule(ooapiDateFormats);
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
