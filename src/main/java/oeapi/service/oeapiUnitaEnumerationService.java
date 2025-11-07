package oeapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import oeapi.repository.oeapiUnitaEnumerationRepository;

/**
 *
 * @author itziar.urrutia
 */
@Service
public class oeapiUnitaEnumerationService {

    @Autowired
    private oeapiUnitaEnumerationRepository repository;

}
