package com.crossent.monitoring.portal.mongroup.mng.service;

import com.crossent.monitoring.portal.common.vo.PagingReqVo;
import com.crossent.monitoring.portal.common.vo.PagingResVo;
import com.crossent.monitoring.portal.common.vo.SearchReqVo;
import com.crossent.monitoring.portal.jpa.domain.*;
import com.crossent.monitoring.portal.jpa.repository.*;
import com.crossent.monitoring.portal.mongroup.mng.dto.MgServerGroupDto;
import org.apache.catalina.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ServerGroupService {
    private static Logger logger = LoggerFactory.getLogger(ServerGroupService.class);

    @Autowired
    MgServerGroupRepository mgServerGroupRepository;

    @Autowired
    MgServerGroupCriticalValueRepository mgServerGroupCriticalValueRepository;

    @Autowired
    ServerTypeRepository serverTypeRepository;

    @Autowired
    ServerTypeCriticalValueRepository serverTypeCriticalValueRepository;

    @Autowired
    ServerResourceRepository serverResourceRepository;

    @Autowired
    MgServerGroupServerRepository mgServerGroupServerRepository;

    @Autowired
    MgServerGroupTitleMapRepository mgServerGroupTitleMapRepository;

    public PagingResVo<MgServerGroupDto> pagingServerGroup(Integer monitoringGroupId, PagingReqVo pagingReqVo, SearchReqVo searchReqVo) {

        Map<String, String> keywords = searchReqVo.getKeywords();
        String key = null;
        String keyword = null;
        if (keywords != null) {
            Iterator<String> keys = keywords.keySet().iterator();
            while (keys.hasNext()) {
                key = keys.next();
                keyword = keywords.get(key);
                keyword = "%" + keyword + "%";
            }
        }
        Page<MgServerGroup> mgServerGroups = null;
        if (key == null) {
            //TODO 전체조회
            mgServerGroups = mgServerGroupRepository.findAllByMonGroupId(pagingReqVo.toPagingRequest(), monitoringGroupId);
        } else {
            switch (key) {
                case "name": {
                    mgServerGroups = mgServerGroupRepository.findAllByMonGroupIdAndNameLike(monitoringGroupId, pagingReqVo.toPagingRequest(), keyword);
                }
                break;
                case "description": {
                    mgServerGroups = mgServerGroupRepository.findAllByMonGroupIdAndDescriptionLike(monitoringGroupId, pagingReqVo.toPagingRequest(), keyword);
                }
                break;
            }
        }

        PagingResVo<MgServerGroupDto> resPage = new PagingResVo<MgServerGroupDto>(mgServerGroups, false);

        List<MgServerGroup> content = mgServerGroups.getContent();

        for(MgServerGroup mgServerGroup : content){
            MgServerGroupDto mgServerGroupDto = new MgServerGroupDto();
            mgServerGroupDto.setId(mgServerGroup.getId());
            mgServerGroupDto.setMonGroupId(mgServerGroup.getMonGroup().getId());
            mgServerGroupDto.setName(mgServerGroup.getName());
            mgServerGroupDto.setServerTypeId(mgServerGroup.getServerType().getId());
            mgServerGroupDto.setServerTypeName(mgServerGroup.getServerType().getName());
            mgServerGroupDto.setDescription(mgServerGroup.getDescription());
            mgServerGroupDto.setMonitoringYn(mgServerGroup.getMonitoringYn());
            mgServerGroupDto.setDashboardYn(mgServerGroup.getDashboardYn());

            resPage.addListItem(mgServerGroupDto);
        }

        return resPage;
    }

    public void createServerGroup(Integer monitoringGroupId, MgServerGroup mgServerGroup) {

        /*MgServerGroup byMonGroupId = mgServerGroupRepository.findByMonGroupId(monitoringGroupId);*/
        MgServerGroup mg = new MgServerGroup();

        mg.setName(mgServerGroup.getName());
        mg.setMonGroupId(monitoringGroupId);
        mg.setServerTypeId(mgServerGroup.getServerTypeId());
        mg.setDescription(mgServerGroup.getDescription());
        mg.setDashboardYn(mgServerGroup.getDashboardYn());
        mg.setMonitoringYn(mgServerGroup.getMonitoringYn());

        MgServerGroup result = mgServerGroupRepository.save(mg);

       ServerType serverType = serverTypeRepository.findById(result.getServerTypeId());
        Collection<Measurement> measurements = serverType.getMeasurements();
        for(Measurement measurement : measurements) {
            Collection<Metric> metrics = measurement.getMetrics();
            for(Metric metric : metrics) {
                Integer id = metric.getId();

                ServerTypeCriticalValuePK serverTypeCriticalValuePK = new ServerTypeCriticalValuePK();

                serverTypeCriticalValuePK.setServerTypeId(serverType.getId());
                serverTypeCriticalValuePK.setMeasurementId(measurement.getId());
                serverTypeCriticalValuePK.setMetricId(id);

                ServerTypeCriticalValue serverTypeCriticalValue = serverTypeCriticalValueRepository.findOne(serverTypeCriticalValuePK);
                if(serverTypeCriticalValue != null) {
                    MgServerGroupCriticalValue mgServerGroupCriticalValue = new MgServerGroupCriticalValue();
                    /*mgServerGroupCriticalValue.setServerGroupId(mgServerGroup.getId());*/
                    mgServerGroupCriticalValue.setServerGroupId(result.getId());
                    mgServerGroupCriticalValue.setMetricId(id);
                    mgServerGroupCriticalValue.setWarning(serverTypeCriticalValue.getWarning());
                    mgServerGroupCriticalValue.setCritical(serverTypeCriticalValue.getCritical());

                    mgServerGroupCriticalValueRepository.save(mgServerGroupCriticalValue);
                }
            }
            MgServerGroupTitleMap map = new MgServerGroupTitleMap();
            map.setMonGroupId(monitoringGroupId);
            map.setMeasurementId(measurement.getId());

            mgServerGroupTitleMapRepository.save(map);
        }
    }

    public MgServerGroupDto getServerGroup(Integer monitoringGroupId, Integer serverGroupId) {

        MgServerGroup mgServerGroup = mgServerGroupRepository.findByMonGroupIdAndId(monitoringGroupId, serverGroupId);

        MgServerGroupDto mgServerGroupDto = new MgServerGroupDto();
        mgServerGroupDto.setId(serverGroupId);
        mgServerGroupDto.setMonGroupId(monitoringGroupId);
        mgServerGroupDto.setName(mgServerGroup.getName());
        mgServerGroupDto.setServerTypeId(mgServerGroup.getServerTypeId());
        mgServerGroupDto.setServerTypeName(mgServerGroup.getServerType().getName());
        mgServerGroupDto.setDescription(mgServerGroup.getDescription());
        mgServerGroupDto.setDashboardYn(mgServerGroup.getDashboardYn());
        mgServerGroupDto.setMonitoringYn(mgServerGroup.getMonitoringYn());

        return mgServerGroupDto;
    }

    public MgServerGroup updateServerGroup(Integer monitoringGroupId, Integer serverGroupId, MgServerGroup mgServerGroup) {

        MgServerGroup updateServerGroup = mgServerGroupRepository.findByMonGroupIdAndId(monitoringGroupId, serverGroupId);

        updateServerGroup.setName(mgServerGroup.getName());
        updateServerGroup.setServerTypeId(mgServerGroup.getServerTypeId());
        updateServerGroup.setMonitoringYn(mgServerGroup.getMonitoringYn());
        updateServerGroup.setDescription(mgServerGroup.getDescription());
        updateServerGroup.setDashboardYn(mgServerGroup.getDashboardYn());

        MgServerGroup updateData = mgServerGroupRepository.save(updateServerGroup);

        return updateData;
    }

    public void deleteMgServerGroups(Integer monitoringGroupId, Integer[] serverGroupIds) {

        mgServerGroupRepository.deleteByMonGroupIdAndIdIn(monitoringGroupId, serverGroupIds);
    }

    public Collection<MgServerGroupCriticalValue> getServerGroupMetrics(Integer monitoringGroupId, Integer serverGroupId) {

        MgServerGroup mgServerGroup = mgServerGroupRepository.findById(serverGroupId);
        Collection<MgServerGroupCriticalValue> mgServerGroupCriticalValues = mgServerGroup.getMgServerGroupCriticalValues();

        return mgServerGroupCriticalValues;
    }

    public MgServerGroupCriticalValue updateServerGroupMetrics(Integer monitoringGroupId, Integer serverGroupId, Integer metricId, MgServerGroupCriticalValue mgServerGroupCriticalValue) {

        MgServerGroupCriticalValue updateServerGroupMetric = mgServerGroupCriticalValueRepository.findByServerGroupIdAndMetricId(serverGroupId, metricId);

        updateServerGroupMetric.setWarning(mgServerGroupCriticalValue.getWarning());
        updateServerGroupMetric.setCritical(mgServerGroupCriticalValue.getCritical());

        MgServerGroupCriticalValue updateData = mgServerGroupCriticalValueRepository.save(updateServerGroupMetric);

        return updateData;
    }

    public Collection<MgServer> getServerGroupServerResource(Integer monitoringGroupId, Integer serverGroupId) {

        MgServerGroup mgServerGroup = mgServerGroupRepository.findByMonGroupIdAndId(monitoringGroupId, serverGroupId);

        Collection<MgServer> mgServers = mgServerGroup.getMgServers();

        return mgServers;
    }

    public void insertServerGroupServerResources(Integer monitoringGroupId, Integer serverGroupId, Integer[] serverResourceIds) {

        for(Integer serverResourceId : serverResourceIds) {
            MgServerGroupServer map = new MgServerGroupServer();
            map.setMonGroupId(monitoringGroupId);
            map.setServerGroupId(serverGroupId);
            map.setServerResourceId(serverResourceId);

            MgServerGroupServer result = mgServerGroupServerRepository.save(map);
        }
    }

    public void deleteServerGroupServerResources(Integer monitoringGroupId, Integer serverGroupId, Integer[] serverResourceIds) {

        mgServerGroupServerRepository.deleteByMonGroupIdAndServerGroupIdAndServerResourceIdIn(monitoringGroupId, serverGroupId, serverResourceIds);
    }

    public void deleteServerGroupServerResource(Integer monitoringGroupId, Integer serverGroupId, Integer serverResourceId) {

        mgServerGroupServerRepository.deleteByMonGroupIdAndServerGroupIdAndServerResourceId(monitoringGroupId, serverGroupId, serverResourceId);
    }
}
