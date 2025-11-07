package oeapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import oeapi.model.oeapiIdentifierEntry;
import oeapi.model.oeapiLanguageTypedString;

import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 *
 * @author itziar.urrutia
 */
public class oeapiUtils {

    static Logger logger = LoggerFactory.getLogger(oeapiUtils.class);

    static ObjectMapper customObjectMapper;

    private static final List<String> JSONUnitaObjects = Arrays.asList("primaryCode", "studyLoad", "address");
    private static final List<String> JSONUnitaJoins = Arrays.asList("courses");

    // For password generator
    private static final SecureRandom random = new SecureRandom();

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()_+";

    /* Useful to customize ObjectMapper. For example for Java 8 issues
     */
    public static ObjectMapper ooapiObjectMapper() {

        if (customObjectMapper == null) {
            customObjectMapper = new ObjectMapper();
            customObjectMapper.registerModule(new JavaTimeModule());
            customObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            customObjectMapper.writerWithDefaultPrettyPrinter();
        }
        return customObjectMapper;
    }

    public static String debugJSON(Object object) {

        // For debugging, see how the parsed JSON looks like
        ObjectMapper objectMapper = ooapiObjectMapper();

        String jsonObjectAsString = "{error: conversion failed}";

        try {
            jsonObjectAsString = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            logger.error("Error converting JSON to String. " + ex.getLocalizedMessage());
        }

        return (jsonObjectAsString);
    }

    public static Map<String, Object> flattenJSONObject(JSONObject jsonObject) {
        Map<String, Object> flatMap = new HashMap<>();
        flatten(jsonObject, flatMap, "");
        return flatMap;
    }

    // Normally, raising an ooapiException is the best way to handle errors and 
    // messages to users in REST dialogues, but in certain cases 
    // (i.e authentication chain) the exception never reaches Spring’s 
    // ExceptionHandler chain and goes directly to the servlet container(Tomcat) 
    // so responses in these cases have to be handled explicitly
    public static void sendJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> error = new HashMap<>();
        error.put("error", HttpServletResponse.SC_UNAUTHORIZED == status ? "Unauthorized" : "Forbidden");
        error.put("message", message);
        error.put("status", status);

        new ObjectMapper().writeValue(response.getWriter(), error);
    }    

    
    private static void flatten(JSONObject jsonObject, Map<String, Object> flatMap, String prefix) {
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {

            String key = keys.next();
            if (!JSONUnitaJoins.contains(key)) {
                String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
                Object value = jsonObject.get(key);
                if ((value instanceof JSONObject) && !(JSONUnitaObjects.contains(key.toString()))) //!(key.toString().equals("primaryCode"))) {
                {
                    flatten((JSONObject) value, flatMap, prefix);
                } else {

                    flatMap.put(key, value);
                }
            }
        }
    }

    public static boolean isValidUUID(String str) {
        try {
            UUID uuid = UUID.fromString(str);
            return true; // Valid UUID
        } catch (IllegalArgumentException e) {
            return false; // Invalid UUID
        }
    }

    public static Object getId(Object entity) throws Exception {
        Field idField = null;

        // Search for a field annotated with @Id
        for (Field field : entity.getClass().getDeclaredFields()) {
            logger.debug("Utils getID field revised for Id: " + field);
            if (field.isAnnotationPresent(javax.persistence.Id.class)) {
                idField = field;
                logger.debug("Utils getID, field with Id found: " + field);
                break;
            }
        }

        if (idField == null) {
            return null;
            //throw new RuntimeException("No @Id field found in " + entity.getClass());
        }

        idField.setAccessible(true); // Make private fields accessible
        logger.debug("-->Exiting Utils getID, field with Id found: " + idField);
        return idField.get(entity);
    }

    /* En el caso de que se quiera copiar valores de una entidad a otra,
    pero solo los que estan usados -no sean null- */
    public static void copyNonNullProperties(Object src, Object target) {
        BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
    }

    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<String>();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    public static List<oeapiLanguageTypedString> defaultName(String nameInfo) {

        oeapiLanguageTypedString name_gb = new oeapiLanguageTypedString("en-GB", "[" + nameInfo + "]");
        List<oeapiLanguageTypedString> name = new ArrayList();
        name.add(name_gb);
        return name;
    }

    public static List<oeapiLanguageTypedString> defaultName() {

        return defaultName("AutoGenerated Default Name");

    }

    public static List<oeapiLanguageTypedString> defaultDescription(String language, String description) {

        oeapiLanguageTypedString basicDesc = new oeapiLanguageTypedString(language, description);
        List<oeapiLanguageTypedString> desc = new ArrayList();
        desc.add(basicDesc);
        return desc;

    }

    public static List<oeapiLanguageTypedString> defaultDescription() {

        return defaultDescription("en-GB", "[Default]");
    }

    public static oeapiIdentifierEntry defaultPrimaryCode(String codeType, String code) {
        oeapiIdentifierEntry d = new oeapiIdentifierEntry();
        d.setCode(code);
        d.setCodeType(codeType);
        return d;
    }

    public static oeapiIdentifierEntry defaultPrimaryCode(String codePreffix) {

        return defaultPrimaryCode("identifier", codePreffix + "-DEFAULT");
    }

    public static void setField(Object target, String fieldName, Object valueToSet) {
        Class<?> clazz = target.getClass();

        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true); // if field is private
            field.set(target, valueToSet);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Error setting field: " + fieldName);
            e.printStackTrace();
        }
    }

    /* public static String generatePassayPassword() {
        PasswordGenerator gen = new PasswordGenerator();
        CharacterData lowerCaseChars = EnglishCharacterData.LowerCase;
        CharacterRule lowerCaseRule = new CharacterRule(lowerCaseChars);
        lowerCaseRule.setNumberOfCharacters(2);

        CharacterData upperCaseChars = EnglishCharacterData.UpperCase;
        CharacterRule upperCaseRule = new CharacterRule(upperCaseChars);
        upperCaseRule.setNumberOfCharacters(2);

        CharacterData digitChars = EnglishCharacterData.Digit;
        CharacterRule digitRule = new CharacterRule(digitChars);
        digitRule.setNumberOfCharacters(2);

        CharacterData specialChars = new CharacterData() {
            public String getErrorCode() {
                return "ERROR";
            }

            public String getCharacters() {
                return "!@#$%^&*()_+";
            }
        };
        CharacterRule splCharRule = new CharacterRule(specialChars);
        splCharRule.setNumberOfCharacters(2);

        List<CharacterRule> rules = new ArrayList<>(Arrays.asList(lowerCaseRule, upperCaseRule, digitRule, splCharRule));

        String password = gen.generatePassword(10, rules);

        return password;
    }
    */

    public static String generatePassword() {
        int length = 10;

        // Rules: 2 lower, 2 upper, 2 digits, 2 special, rest random
        List<Character> passwordChars = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            passwordChars.add(randomChar(LOWER));
        }
        for (int i = 0; i < 2; i++) {
            passwordChars.add(randomChar(UPPER));
        }
        for (int i = 0; i < 2; i++) {
            passwordChars.add(randomChar(DIGITS));
        }
        for (int i = 0; i < 2; i++) {
            passwordChars.add(randomChar(SPECIAL));
        }

        // Fill remaining with random from all sets combined
        String allChars = LOWER + UPPER + DIGITS + SPECIAL;
        while (passwordChars.size() < length) {
            passwordChars.add(randomChar(allChars));
        }

        // Shuffle to avoid predictable sequence
        Collections.shuffle(passwordChars, random);

        // Build final password
        StringBuilder password = new StringBuilder();
        for (char c : passwordChars) {
            password.append(c);
        }

        return password.toString();
    }

    private static char randomChar(String chars) {
        return chars.charAt(random.nextInt(chars.length()));
    }

}
