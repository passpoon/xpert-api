package com.crossent.monitoring.portal.jpa.repository;

import com.crossent.monitoring.portal.jpa.domain.EventHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface EventHistoryRepository extends CrudRepository<EventHistory, Integer> {
    public Page<EventHistory> findAllByMonGroupIdAndResourceIdAndResourceTypeInOrderByUpdateDttmDescIdDesc(Pageable pageable, Integer monGroupId, Integer resourceId, List<String> resourceTypes);
    public Page<EventHistory> findAllByMonGroupIdAndResourceIdAndResourceTypeInAndStateCodeCodeInOrderByUpdateDttmDescIdDesc(Pageable pageable, Integer monGroupId, Integer resourceId, List<String> resourceTypes, List<String> stateCodes);
    public Page<EventHistory> findAllByMonGroupIdAndResourceIdInAndResourceTypeInAndStateCodeCodeInOrderByUpdateDttmDescIdDesc(Pageable pageable, Integer monGroupId, List<Integer> resourceIds, List<String> resourceTypes, List<String> stateCodes);
    public Page<EventHistory> findAllByMonGroupIdAndResourceTypeInAndStateCodeCodeInOrderByUpdateDttmDescIdDesc(Pageable pageable, Integer monGroupId, List<String> resourceTypes, List<String> stateCodes);

    public Page<EventHistory> findAllByMonGroupIdAndResourceIdAndResourceTypeInAndStateCodeCodeInAndUpdateDttmGreaterThanEqualAndUpdateDttmLessThanEqualOrderByUpdateDttmDescIdDesc(Pageable pageable, Integer monGroupId, Integer resourceId, List<String> resourceTypes, List<String> stateCodes, String startDttm, String endDttm);

    public Page<EventHistory> findAllByMonGroupIdAndResourceIdInAndResourceTypeInAndStateCodeCodeInAndUpdateDttmGreaterThanEqualAndUpdateDttmLessThanEqualOrderByUpdateDttmDescIdDesc(Pageable pageable, Integer monGroupId, List<Integer> resourceIds, List<String> resourceTypes, List<String> stateCodes, String startDttm, String endDttm);

    public Page<EventHistory> findAllByMonGroupIdAndResourceTypeInAndStateCodeCodeInAndUpdateDttmGreaterThanEqualAndUpdateDttmLessThanEqualOrderByUpdateDttmDescIdDesc(Pageable pageable, Integer monGroupId, List<String> resourceTypes, List<String> stateCodes, String startDttm, String endDttm);

    public Page<EventHistory> findAllByMonGroupIdAndResourceIdAndResourceTypeInAndStateCodeCodeOrderByUpdateDttmDescIdDesc(Pageable pageable, Integer monGroupId, Integer resourceId, List<String> resourceTypes, String stateCode);
    public Page<EventHistory> findAllByMonGroupIdAndResourceIdInAndResourceTypeInAndStateCodeCodeOrderByUpdateDttmDescIdDesc(Pageable pageable, Integer monGroupId, List<Integer> resourceIds, List<String> resourceTypes, String stateCode);



//    public Page<EventHistory> findAllByMonGroupIdStateCodeCodeInAndUpdateDttmGreaterThanEqualAndUpdateDttmLessThanEqualOrderByUpdateDttmDescIdDesc(Pageable pageable, Integer monGroupId, List<String> stateCodes, String startDttm, String endDttm);
//
//
//    public Page<EventHistory> findAllByMonGroupIdStateCodeCodeInOrderByUpdateDttmDescIdDesc(Pageable pageable, Integer monGroupId, List<String> stateCodes);





}
