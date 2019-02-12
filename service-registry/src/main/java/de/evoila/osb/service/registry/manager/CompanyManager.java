package de.evoila.osb.service.registry.manager;

import de.evoila.osb.service.registry.data.repositories.CompanyRepository;
import de.evoila.osb.service.registry.model.Company;
import org.springframework.stereotype.Service;

@Service
public class CompanyManager extends BasicManager<Company> {

    public CompanyManager(CompanyRepository companyRepository) {
        super(companyRepository);
    }
}
