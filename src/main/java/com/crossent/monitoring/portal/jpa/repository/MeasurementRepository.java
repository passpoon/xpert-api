package com.crossent.monitoring.portal.jpa.repository;

import com.crossent.monitoring.portal.jpa.domain.Measurement;
import com.crossent.monitoring.portal.jpa.domain.Metric;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;


public interface MeasurementRepository extends JpaRepository<Measurement, Integer> {

    //public Measurement findByName(@Param("name") String Name);

    public Measurement findById(Integer id);

    public Page<Measurement> findByNameLike(Pageable pageable, String name);
    public Page<Measurement> findByDescriptionLike(Pageable pageable, String description);
    public Measurement findByIdAndMetrics_Id(Integer id, Integer metricId);

    public void deleteByIdIn(Integer[] ids);
}
