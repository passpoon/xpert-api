package com.crossent.monitoring.portal.jpa.repository;

import com.crossent.monitoring.portal.jpa.domain.MgApp;
import com.crossent.monitoring.portal.jpa.domain.MgAppPK;
import org.springframework.data.repository.CrudRepository;


public interface MgAppRepository extends CrudRepository<MgApp, MgAppPK> {


}
