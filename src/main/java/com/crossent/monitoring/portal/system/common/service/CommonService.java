package com.crossent.monitoring.portal.system.common.service;

import com.crossent.monitoring.portal.jpa.domain.AppInfo;
import com.crossent.monitoring.portal.jpa.domain.ServerType;
import com.crossent.monitoring.portal.jpa.repository.AppInfoRepository;
import com.crossent.monitoring.portal.jpa.repository.ServerTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class CommonService {

    @Autowired
    ServerTypeRepository serverTypeRepository;

    @Autowired
    AppInfoRepository appInfoRepository;

    // 서버 유형 조회
    public Collection<ServerType> getServerTypes(Integer serverResourceId, String name) {

        Collection<ServerType> serverTypes = serverTypeRepository.findAllByNameLikeAndServerResources_Id("%"+name+"%", serverResourceId);

        return serverTypes;
    }

    // 어플리케이션 정보 조회
    public Collection<AppInfo> getAppInfos(Integer appResourceId, String name) {

        Collection<AppInfo> appInfos = appInfoRepository.findAllByNameLikeAndAppResources_Id("%"+name+"%", appResourceId);

        return appInfos;
    }
}