/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package oeapi;

import javax.persistence.PrePersist;
import oeapi.payload.oeapiEducationDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author itziar.urrutia
 */
@Component
public class oeapiEducationListener {

    @Value("${ooapi.config.defaultTeachingLanguage}")
    private String defaultTeachingLanguage;

    @PrePersist
    public void prePersist(oeapiEducationDTO entity) {
        if (entity.getTeachingLanguage() == null) {
            entity.setTeachingLanguage(defaultTeachingLanguage);
        }
    }

}
