package com.crossent.monitoring.portal.mongroup.monitoring.web;

import com.crossent.monitoring.portal.common.constants.ResourceType;
import com.crossent.monitoring.portal.common.vo.PagingReqVo;
import com.crossent.monitoring.portal.common.vo.PagingResVo;
import com.crossent.monitoring.portal.common.vo.SearchReqVo;
import com.crossent.monitoring.portal.common.web.BaseController;
import com.crossent.monitoring.portal.mongroup.monitoring.dto.EventResDto;
import com.crossent.monitoring.portal.mongroup.monitoring.dto.LogResDto;
import com.crossent.monitoring.portal.mongroup.monitoring.dto.ServerDetailStatusDto;
import com.crossent.monitoring.portal.mongroup.monitoring.dto.ServerStatusesResDto;
import com.crossent.monitoring.portal.mongroup.monitoring.service.MonCommonService;
import com.crossent.monitoring.portal.mongroup.monitoring.service.MonServerService;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
public class MonServerController extends BaseController {

    private static Logger logger = LoggerFactory.getLogger(MonServerController.class);

    @Autowired
    MonServerService monServerService;

    @Autowired
    MonCommonService monCommonService;

        //path, query, body, header

    @ApiOperation(value = "모니터링그룹의 서버상태 상태 조회")
    @ApiImplicitParams({
                               @ApiImplicitParam(name = "monitoringGroupId", value = "모니터링 그룹 ID", required = true, dataType = "int", paramType = "path"),
                               @ApiImplicitParam(name = "paging", value = "페이징 정보", required = false, dataType = "string", paramType = "query"),
                               @ApiImplicitParam(name = "search", value = "검색 정보", required = false, dataType = "string", paramType = "query"),
    })
    @Transactional
    @RequestMapping(value = "/monitoring-groups/{monitoringGroupId}/monitoring/server/server-statuses", method = RequestMethod.GET)
    public PagingResVo<ServerStatusesResDto> pageServerStatuses(@PathVariable Integer monitoringGroupId, @ModelAttribute("paging") PagingReqVo paging, @ModelAttribute("search") SearchReqVo search) {


        if(logger.isDebugEnabled()){
            logger.debug("monitoringGroupId : {}", monitoringGroupId);
            logger.debug("paging : {}", paging);
            logger.debug("search : {}", search);
        }

        PagingResVo<ServerStatusesResDto> serverStatuses = monServerService.pageServerStatuses(monitoringGroupId, paging, search);


        if(logger.isDebugEnabled()){
            logger.debug("serverStatuses : {}", serverStatuses);
        }

        return serverStatuses;
    }

    @ApiOperation(value = "서버 상세 조회")
    @ApiImplicitParams({
                               @ApiImplicitParam(name = "monitoringGroupId", value = "모니터링 그룹 ID", required = true, dataType = "int", paramType = "path"),
                               @ApiImplicitParam(name = "serverResourceId", value = "서버 리소스 ID", required = true, dataType = "int", paramType = "path"),
                               @ApiImplicitParam(name = "search", value = "검색 정보", required = false, dataType = "string", paramType = "query"),
    })
    @Transactional
    @RequestMapping(value = "/monitoring-groups/{monitoringGroupId}/monitoring/server/server-statuses/{serverResourceId}", method = RequestMethod.GET)
    public ServerDetailStatusDto getServerDetailStatus(@PathVariable Integer monitoringGroupId, @PathVariable Integer serverResourceId, @ModelAttribute("search") SearchReqVo search) {

        if(logger.isDebugEnabled()){
            logger.debug("monitoringGroupId : {}", monitoringGroupId);
            logger.debug("serverResourceId : {}", serverResourceId);
            logger.debug("search : {}", search);
        }

        ServerDetailStatusDto serverDetailStatus = monCommonService.getServerDetailStatus(serverResourceId, search);


//        if(logger.isDebugEnabled()){
//            logger.debug("serverStatuses : {}", serverStatuses);
//        }

        return serverDetailStatus;
    }

    @ApiOperation(value = "서버 이벤트 조회")
    @ApiImplicitParams({
                               @ApiImplicitParam(name = "monitoringGroupId", value = "모니터링 그룹 ID", required = true, dataType = "int", paramType = "path"),
                               @ApiImplicitParam(name = "serverResourceId", value = "서버 리소스 ID", required = true, dataType = "int", paramType = "path"),
                               @ApiImplicitParam(name = "paging", value = "페이징 정보", required = false, dataType = "string", paramType = "query"),
                               @ApiImplicitParam(name = "search", value = "검색 정보", required = false, dataType = "string", paramType = "query"),
    })
    @Transactional
    @RequestMapping(value = "/monitoring-groups/{monitoringGroupId}/monitoring/server/server-statuses/{serverResourceId}/events", method = RequestMethod.GET)
    public PagingResVo<EventResDto> pageEvent(@PathVariable Integer monitoringGroupId, @PathVariable Integer serverResourceId, @ModelAttribute("paging") PagingReqVo pagingReqVo, @ModelAttribute("search") SearchReqVo search){

        logger.debug("monitoringGroupId : {}", monitoringGroupId);
        logger.debug("serverResourceId : {}", serverResourceId);
        logger.debug("pagingReqVo : {}", pagingReqVo);
        logger.debug("search : {}", search);


        PagingResVo<EventResDto> resDtoPagingResVo = monCommonService.pageEvent(monitoringGroupId, ResourceType.SERVER, serverResourceId, pagingReqVo, search);
        logger.debug("resDtoPagingResVo : {}", resDtoPagingResVo);
        return resDtoPagingResVo;
    }

    @ApiOperation(value = "서버 로그 조회")
    @ApiImplicitParams({
                               @ApiImplicitParam(name = "monitoringGroupId", value = "모니터링 그룹 ID", required = true, dataType = "int", paramType = "path"),
                               @ApiImplicitParam(name = "serverResourceId", value = "서버 리소스 ID", required = true, dataType = "int", paramType = "path"),
                               @ApiImplicitParam(name = "paging", value = "페이징 정보", required = false, dataType = "string", paramType = "query"),
                               @ApiImplicitParam(name = "search", value = "검색 정보", required = false, dataType = "string", paramType = "query"),
    })
    @Transactional
    @RequestMapping(value = "/monitoring-groups/{monitoringGroupId}/monitoring/server/server-statuses/{serverResourceId}/logs", method = RequestMethod.GET)
    public PagingResVo<LogResDto> pageLog(@PathVariable Integer monitoringGroupId, @PathVariable Integer serverResourceId, @ModelAttribute("paging") PagingReqVo pagingReqVo, @ModelAttribute("search") SearchReqVo search){

        logger.debug("monitoringGroupId : {}", monitoringGroupId);
        logger.debug("serverResourceId : {}", serverResourceId);
        logger.debug("pagingReqVo : {}", pagingReqVo);
        logger.debug("search : {}", search);

        PagingResVo<LogResDto> resDtoPagingResVo = monCommonService.pageServerLog(serverResourceId, pagingReqVo, search);
        logger.debug("resDtoPagingResVo : {}", resDtoPagingResVo);
        return resDtoPagingResVo;
    }
}