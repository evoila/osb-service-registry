package de.evoila.osb.service.registry.data.repositories;

import de.evoila.osb.service.registry.model.Company;
import org.springframework.data.repository.CrudRepository;

public interface CompanyRepository extends CrudRepository<Company, String> {

}
