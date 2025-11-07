/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package oeapi.service;

import java.util.Optional;
import oeapi.model.oeapiUnitaEnumeration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import oeapi.repository.oeapiUnitaEnumerationRepository;

/**
 *
 * @author itziar.urrutia
 */
@Service
public class oeapiEnumConversionService {

    private oeapiUnitaEnumerationRepository enumRepository;

    @Autowired
    public oeapiEnumConversionService(oeapiUnitaEnumerationRepository repository) {
        this.enumRepository = repository;

    }

    public String convertIdToValue(Long id) {
        Optional<oeapiUnitaEnumeration> ooapiEnum = enumRepository.findById(id);
        if (ooapiEnum.isPresent()) {
            return ooapiEnum.get().getValue();
        }
        return null;
    }

    public Long convertValueToId(String type, String value) {
        Optional<oeapiUnitaEnumeration> ooapiEnum = enumRepository.findEnumeratorByNameAndValue(type, value);
        if (!ooapiEnum.isPresent()) {
            // Create new entry
            oeapiUnitaEnumeration newEnum = new oeapiUnitaEnumeration();
            newEnum.setName(type);
            newEnum.setValue(value);  // Assumes you provide it
            return enumRepository.save(newEnum).getId();
        } else {
            return ooapiEnum.get().getId();
        }
    }
}
