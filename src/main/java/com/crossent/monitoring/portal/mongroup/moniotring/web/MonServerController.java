package com.crossent.monitoring.portal.mongroup.moniotring.web;

import com.crossent.monitoring.portal.common.vo.PagingReqVo;
import com.crossent.monitoring.portal.common.vo.PagingResVo;
import com.crossent.monitoring.portal.common.vo.SearchReqVo;
import com.crossent.monitoring.portal.common.web.BaseController;
import com.crossent.monitoring.portal.mongroup.moniotring.dto.EventResDto;
import com.crossent.monitoring.portal.mongroup.moniotring.dto.ServerDetailStatusDto;
import com.crossent.monitoring.portal.mongroup.moniotring.dto.ServerStatusesResDto;
import com.crossent.monitoring.portal.mongroup.moniotring.service.MonServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
public class MonServerController extends BaseController {

    private static Logger logger = LoggerFactory.getLogger(MonServerController.class);

    @Autowired
    MonServerService serverService;


    @Transactional
    @RequestMapping(value = "/monitoring-groups/{monitoringGroupId}/monitoring/server/server-statuses", method = RequestMethod.GET)
    public PagingResVo<ServerStatusesResDto> pageServerStatuses(@PathVariable Integer monitoringGroupId, @ModelAttribute("paging") PagingReqVo paging, @ModelAttribute("search") SearchReqVo search) {


        if(logger.isDebugEnabled()){
            logger.debug("monitoringGroupId : {}", monitoringGroupId);
            logger.debug("paging : {}", paging);
            logger.debug("search : {}", search);
        }

        PagingResVo<ServerStatusesResDto> serverStatuses = serverService.pageServerStatuses(monitoringGroupId, paging, search);


        if(logger.isDebugEnabled()){
            logger.debug("serverStatuses : {}", serverStatuses);
        }

        return serverStatuses;
    }


    @Transactional
    @RequestMapping(value = "/monitoring-groups/{monitoringGroupId}/monitoring/server/server-statuses/{serverResourceId}", method = RequestMethod.GET)
    public ServerDetailStatusDto getServerDetailStatus(@PathVariable Integer monitoringGroupId, @PathVariable Integer serverResourceId, @ModelAttribute("search") SearchReqVo search) {

        if(logger.isDebugEnabled()){
            logger.debug("monitoringGroupId : {}", monitoringGroupId);
            logger.debug("serverResourceId : {}", serverResourceId);
            logger.debug("search : {}", search);
        }

        ServerDetailStatusDto serverDetailStatus = serverService.getServerDetailStatus(monitoringGroupId, serverResourceId, search);


//        if(logger.isDebugEnabled()){
//            logger.debug("serverStatuses : {}", serverStatuses);
//        }

        return serverDetailStatus;
    }

    @Transactional
    @RequestMapping(value = "/monitoring-groups/{monitoringGroupId}/monitoring/server/server-statuses/{serverResourceId}/events", method = RequestMethod.GET)
    public PagingResVo<EventResDto> pageEvent(@PathVariable Integer monitoringGroupId, @PathVariable Integer serverResourceId, @ModelAttribute("paging") PagingReqVo pagingReqVo, @ModelAttribute("search") SearchReqVo search){

        logger.debug("monitoringGroupId : {}", monitoringGroupId);
        logger.debug("serverResourceId : {}", serverResourceId);
        logger.debug("pagingReqVo : {}", pagingReqVo);
        logger.debug("search : {}", search);


        PagingResVo<EventResDto> resDtoPagingResVo = serverService.pageEvent(monitoringGroupId, serverResourceId, pagingReqVo, search);
        logger.debug("resDtoPagingResVo : {}", resDtoPagingResVo);
        return resDtoPagingResVo;
    }









}
