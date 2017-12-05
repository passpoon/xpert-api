package com.crossent.monitoring.portal.mongroup.moniotring.service;

import com.crossent.monitoring.portal.common.constants.*;
import com.crossent.monitoring.portal.common.exception.BusinessException;
import com.crossent.monitoring.portal.common.lib.elasticsearch.ElasticsearchTemplate;
import com.crossent.monitoring.portal.common.lib.util.DateUtil;
import com.crossent.monitoring.portal.common.lib.util.MessageUtil;
import com.crossent.monitoring.portal.common.lib.util.StringUtil;
import com.crossent.monitoring.portal.common.properties.ApplicationProperties;
import com.crossent.monitoring.portal.common.vo.*;
import com.crossent.monitoring.portal.jpa.domain.*;
import com.crossent.monitoring.portal.jpa.repository.*;
import com.crossent.monitoring.portal.mongroup.moniotring.dao.MonServerDao;
import com.crossent.monitoring.portal.mongroup.moniotring.dto.*;
import com.crossent.monitoring.portal.mongroup.moniotring.util.MonitoringUtil;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.crossent.monitoring.portal.common.lib.util.DateUtil.DATE_HMS_PATTERN;
import static com.crossent.monitoring.portal.common.lib.util.DateUtil.TIMESTAMP_T_PATTERN;

@Service
public class MonServerService {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(MonServerService.class);

    @Autowired
    MgServerRepository mgServerRepository;

    @Autowired
    MgServerTitleMapRepository mgServerTitleMapRepository;

    @Autowired
    MeasurementRepository measurementRepository;

    @Autowired
    MgServerCriticalValueRepository mgServerCriticalValueRepository;



    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private MonServerDao monServerDao;

    @Autowired
    private ServerResourceRepository serverResourceRepository;

    @Autowired
    private MetricRepository metricRepository;

    @Autowired
    private EventHistoryRepository eventHistoryRepository;



    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;



    public PagingResVo<ServerStatusesResDto> pageServerStatuses(Integer monitoringGroupId, PagingReqVo paging, SearchReqVo search) {

        String key = null;
        String keyword = null;
        Map<String, Map<String, String>> influxQueryFilters = applicationProperties.getInfluxQueryFilters();

        Map<String, String> keywords = search.getKeywords();
        if (keywords != null) {
            Iterator<String> keys = keywords.keySet().iterator();
            while(keys.hasNext()) {
                key = keys.next();
                keyword = keywords.get(key);
                logger.debug("keyword ::::: " + keyword);
                keyword = "%" + keyword + "%";
            }
        }


        Page<MgServer> pageMgServer = null;

        logger.debug("key : {}", key);
        logger.debug("keyword : {}", keyword);

        if (key == null) {
            pageMgServer = mgServerRepository.findAllByMonGroupIdAndMonitoringYn(paging.toPagingRequest(), monitoringGroupId, "Y");
        } else {

            switch (key) {
                case "hostName": {
                    pageMgServer = mgServerRepository.findAllByMonGroupIdAndMonitoringYnAndServerResource_HostNameLike(paging.toPagingRequest(), monitoringGroupId, "Y", keyword);
                }
                break;
                case "serverName": {
                    pageMgServer = mgServerRepository.findAllByMonGroupIdAndMonitoringYnAndServerResource_NameLike(paging.toPagingRequest(),  monitoringGroupId,"Y" , keyword);
                }
                break;
                default:
                    throw new BusinessException(MessageUtil.getMessage("unDefSearchKey", key));

            }
        }


        PagingResVo<ServerStatusesResDto> pageServerStatusesResDtoPagingResVo = new PagingResVo<ServerStatusesResDto>(pageMgServer, false);


        List<MgServer> mgServers = pageMgServer.getContent();
        if (logger.isDebugEnabled()) {
            logger.debug("mgServers : {}", mgServers);
        }

        logger.debug("mgServer {} ::", mgServers);
        Collection<MgServerTitleMap> mgServerTitleMaps = mgServerTitleMapRepository.findAllByMonGroupId(monitoringGroupId);

        //title셋팅
        for(MgServerTitleMap titleMap : mgServerTitleMaps){

            pageServerStatusesResDtoPagingResVo.addTile(titleMap.getMeasurements().getName());

        }

        //모니터링 대상서버 목록
        for (MgServer mgServer : mgServers) {
            String hostName = mgServer.getServerResource().getHostName();
            ServerStatusesResDto serverStatusesResDto = new ServerStatusesResDto();
            serverStatusesResDto.setServerResourceId(mgServer.getServerResourceId());
            serverStatusesResDto.setServerResourceName(mgServer.getServerResource().getName());
            serverStatusesResDto.setHostName(hostName);



            logger.debug("mgServer {} ::", mgServers);



            for (MgServerTitleMap map : mgServerTitleMaps) {

                StringBuilder contentBuffer = new StringBuilder();
                Integer mId = map.getMeasurementId();
                String mName = map.getMeasurements().getName();

                //title입력
                //serverStatusesResDto.addTile(mName);

                Collection<MgServerCriticalValue> criticalValues = mgServerCriticalValueRepository
                                                                           .findAllByMonGroupIdAndServerResourceIdAndMetric_MeasurementId(monitoringGroupId,
                                                                                   mgServer.getServerResourceId(), mId);


                if(logger.isDebugEnabled()){
                    logger.debug("criticalValues size : {}", criticalValues.size());
                    logger.debug("criticalValues  : {}", criticalValues);
                }


                StatusEnum status = StatusEnum.NA;

                if(criticalValues != null && criticalValues.size() != 0){
                    List<CriticalValueInterface> metriIfs = MonitoringUtil.criticalCollectionToInterface(criticalValues);

                    CriticalValueMapVo cvMapDto = new CriticalValueMapVo(metriIfs);

                    logger.debug("cvMapDto : {}", cvMapDto);


                    Map<String, Object> referenceValueMap = monServerDao.selectReferenceValue(mName, hostName, cvMapDto);

                    if(referenceValueMap != null){




                        logger.debug("referenceValueMap : {}", referenceValueMap);
                        List<String> metricNames = cvMapDto.getMetricNames();
                        for(String metricName : metricNames) {

                            Object val = referenceValueMap.get(metricName);

                            if (val == null) {
                                contentBuffer.append(MessageUtil.getMessage("statusNoRcvData", metricName) + "\n");
                                status = status.max(StatusEnum.Error);
                            } else {
                                Double dVal = MonitoringUtil.toDouble(val);
                                Double criticalVal = cvMapDto.getCriticalVal(metricName);
                                Double warningVal = cvMapDto.getWarningVal(metricName);


                                if (cvMapDto.isCritical(metricName, dVal)) {
                                    status = status.max(StatusEnum.Critical);
                                    contentBuffer.append(MessageUtil.getMessage("statusCritical", metricName, MonitoringUtil.round2ToString(dVal), MonitoringUtil.round2ToString(criticalVal)) + "\n");
                                } else if (cvMapDto.isWarning(metricName, dVal)) {
                                    status = status.max(StatusEnum.Warning);
                                    contentBuffer.append(MessageUtil.getMessage("statusWarning", metricName, MonitoringUtil.round2ToString(dVal), MonitoringUtil.round2ToString(warningVal)) + "\n");
                                } else {
                                    status = status.max(StatusEnum.Nomal);
                                    contentBuffer.append(MessageUtil.getMessage("statusNormal", metricName, MonitoringUtil.round2ToString(dVal)) + "\n");
                                }
                            }
                        }
                    }else{
                        status = StatusEnum.Error;
                        contentBuffer.append(MessageUtil.getMessage("failMetricInfo", mName));

                    }
                }else{
                    //status = StatusEnum.NA;
                    contentBuffer.append(MessageUtil.getMessage("unDefCriticalVal")+"\n"); //정의된 임계치가 없습니다.
                }

                MeasurementStatusDto mentStatusDto = new MeasurementStatusDto();
                mentStatusDto.setMeasurementId(mId);
                mentStatusDto.setContent(contentBuffer.toString());
                mentStatusDto.setStatus(status.getString());
                mentStatusDto.setMeasurementName(mName);

                serverStatusesResDto.addMeasurementStatus(mentStatusDto);



            }




            //process정보 조회
            StatusEnum procStatus = StatusEnum.Nomal;
            StringBuilder procContent = new StringBuilder();
            ProcessStatusDto processStatusDto  = new ProcessStatusDto();

            Map<String, Object> processInfoForServer = monServerDao.selectProcessInfoForServer(hostName);

            if(processInfoForServer == null){
                procStatus = StatusEnum.Error;

                processStatusDto.setTotalCnt(0L);
                processStatusDto.setNormalCnt(0L);
                processStatusDto.setAbnormalCnt(0L);

                procContent.append(MessageUtil.getMessage("failProcessInfo", hostName));

            }else{
                Long normalCnt = Math.round(MonitoringUtil.toDouble(processInfoForServer.get(Constants.PROC_STAT_KEY_NORMAL)));
                Long abnormalCnt = Math.round(MonitoringUtil.toDouble(processInfoForServer.get(Constants.PROC_STAT_KEY_ABNORMAL)));
                Long totalCnt = Math.round(MonitoringUtil.toDouble(processInfoForServer.get(Constants.PROC_STAT_KEY_TOTAL)));

                if(abnormalCnt > 0){
                    procStatus = StatusEnum.Error;
                }
                processStatusDto.setTotalCnt(totalCnt);
                processStatusDto.setNormalCnt(normalCnt);
                processStatusDto.setAbnormalCnt(abnormalCnt);

                for(String keys : Constants.PROC_STAT_KEYS){
                    procContent.append(keys + " : " + processInfoForServer.get(keys)+"\n");
                }


            }

            processStatusDto.setStatus(procStatus.getString());
            processStatusDto.setContent(procContent.toString());

            serverStatusesResDto.setProcessStatus(processStatusDto);

            pageServerStatusesResDtoPagingResVo.addListItem(serverStatusesResDto);




        }


        return pageServerStatusesResDtoPagingResVo;
    }

    public ServerDetailStatusDto getServerDetailStatus(Integer monitoringGroupId, Integer serverResourceId, SearchReqVo search) {

        ServerResource serverResource = serverResourceRepository.findById(serverResourceId);

        if(serverResource == null){

            throw new BusinessException(MessageUtil.getMessage("noSearchServer", serverResourceId+""));
        }

        String rangeType = search.getRangeType();

        if(StringUtil.isEmpty(rangeType)){
            rangeType = "5m";
        }
        ServerType serverType = serverResource.getServerType();

        Collection<Measurement> measurements = serverType.getMeasurements();

        ServerDetailStatusDto serverDetailStatusDto = new ServerDetailStatusDto();

        serverDetailStatusDto.setServerResourceId(serverResourceId);
        serverDetailStatusDto.setServerName(serverResource.getName());
        serverDetailStatusDto.setHostName(serverResource.getHostName());
        serverDetailStatusDto.setIp(serverResource.getIp());

        for(Measurement measurement : measurements){
            MeasurementDetail measurementDetail = new MeasurementDetail();

            Integer measurementId = measurement.getId();
            String measurementName = measurement.getName();
            List<String> typeCodes = new ArrayList<String>();
            typeCodes.add(MetricType.INT.getCode());
            typeCodes.add(MetricType.DOUBLE.getCode());
            Collection<Metric> metrics = metricRepository.findAllByMeasurementIdAndMetricTypeCodeIn(measurementId, typeCodes);



            measurementDetail.setMeasurementId(measurementId);
            measurementDetail.setMeasurementName(measurementName);

            for(Metric metric : metrics) {
                measurementDetail.addTitle(metric.getName());
            }


            List<Map<String, String>> listMetrics = monServerDao.listMetrics(serverResource.getHostName(), measurementName, rangeType, metrics);

            logger.debug("listMetrics : {}", listMetrics);
            measurementDetail.setRows(listMetrics);
            serverDetailStatusDto.addMeasurement(measurementDetail);

        }

        return serverDetailStatusDto;
    }


    public PagingResVo<EventResDto> pageServerEvent(Integer monGroupId, Integer serverResourceId, PagingReqVo pagingReqVo, SearchReqVo search){

        PagingResVo<EventResDto> eventResPage = null;

        List<String> serverResourceTypes = new ArrayList<>();
        List<String> stateCodes = new ArrayList<>();

        Page<EventHistory> eventHistoryPage = null;

        SearchVo searchVo = new SearchVo(search);

        if (searchVo.isHaveKeyworkd()) {
            List<String> keys = searchVo.getKeys();
            for (String key : keys) {
                String keyword = searchVo.getKeyword(key);
                switch (key) {
                    case "RESOURCE-TYPE":
                        switch (keyword) {
                            case "SERVER":
                                serverResourceTypes.add(ResourceType.SERVER.getCode());
                            case "LOG":
                                serverResourceTypes.add(ResourceType.LOG.getCode());
                        }

                    case "STATE":
                        switch (keyword) {
                            case "NORMAL":
                                stateCodes.add(ServerState.NORMAL.getCode());
                            case "WARNING":
                                stateCodes.add(ServerState.WARNING.getCode());
                            case "CRITICAL":
                                stateCodes.add(ServerState.CRITICAL.getCode());
                                break;
                            case "START":
                                stateCodes.add(LogState.START.getCode());
                                break;
                            case "STOP":
                                stateCodes.add(LogState.STOP.getCode());
                                break;
                            case "DEBUG":
                                stateCodes.add(LogState.DEBUG.getCode());
                            case "INFO":
                                stateCodes.add(LogState.INFO.getCode());
                            case "WARN":
                                stateCodes.add(LogState.WARN.getCode());
                            case "ERROR":
                                stateCodes.add(LogState.ERROR.getCode());
                                break;
                        }
                }


            }
        }

        if(serverResourceTypes.size() == 0){
            serverResourceTypes.add(ResourceType.SERVER.getCode());
            serverResourceTypes.add(ResourceType.LOG.getCode());
        }

        if(stateCodes.size() == 0){
            stateCodes.add(ServerState.NORMAL.getCode());
                stateCodes.add(ServerState.WARNING.getCode());
                stateCodes.add(ServerState.CRITICAL.getCode());
                stateCodes.add(LogState.START.getCode());
                stateCodes.add(LogState.STOP.getCode());
                stateCodes.add(LogState.DEBUG.getCode());
                stateCodes.add(LogState.INFO.getCode());
                stateCodes.add(LogState.WARN.getCode());
                stateCodes.add(LogState.ERROR.getCode());
        }



        logger.debug("stateCodes : {}", stateCodes);



        if(searchVo.isHaveRange()){
            String startDttm = searchVo.getStartDttm();
            String endDttm = searchVo.getEndDttm();

            //findAllByMonGroupIdAndResourceIdAndResourceTypeInAndStateCodeCodeInAndUpdateDttmGreaterThanAndUpdateDttmLessThanOrderByUpdateDttmDescIdDesc

            eventHistoryPage = eventHistoryRepository.findAllByMonGroupIdAndResourceIdAndResourceTypeInAndStateCodeCodeInAndUpdateDttmGreaterThanEqualAndUpdateDttmLessThanEqualOrderByUpdateDttmDescIdDesc(pagingReqVo.toPagingRequest(), monGroupId, serverResourceId, serverResourceTypes, stateCodes, startDttm, endDttm);

        }else {
            eventHistoryPage = eventHistoryRepository.findAllByMonGroupIdAndResourceIdAndResourceTypeInAndStateCodeCodeInOrderByUpdateDttmDescIdDesc(pagingReqVo.toPagingRequest(), monGroupId, serverResourceId, serverResourceTypes, stateCodes);
        }

        eventResPage = new PagingResVo<EventResDto>(eventHistoryPage, false);

        List<EventHistory> content = eventHistoryPage.getContent();

        for(EventHistory eventHistory : content){
            EventResDto eventResDto = new EventResDto();
            eventResDto.setHostName(eventHistory.getHostname());
            eventResDto.setContents(eventHistory.getContents());
            eventResDto.setId(eventHistory.getId());
            eventResDto.setIp(eventHistory.getIp());
            eventResDto.setMonGroupId(eventHistory.getMonGroupId());
            eventResDto.setProgram(eventHistory.getProgram());
            eventResDto.setRegiDttm(DateUtil.convertDateFormat(eventHistory.getRegistDttm(), DATE_HMS_PATTERN, DateUtil.DATE_TIME_PATTERN));
            eventResDto.setUpdateDttm(DateUtil.convertDateFormat(eventHistory.getUpdateDttm(), DATE_HMS_PATTERN, DateUtil.DATE_TIME_PATTERN));

            //typeCodeRepo

            eventResDto.setResourceType(eventHistory.getResourceTypeCode().getType().toUpperCase());

            eventResDto.setResourceUuid(eventHistory.getResourceUuid());
            eventResDto.setState(eventHistory.getStateCode().getState());

            eventResPage.addListItem(eventResDto);
        }

        return eventResPage;
    }




    public PagingResVo<LogResDto> pageServerLog(Integer serverResourceId, PagingReqVo page, SearchReqVo search){

        ServerResource serverResource = serverResourceRepository.findById(serverResourceId);
        if(serverResource == null){
            throw new BusinessException(MessageUtil.getMessage("noSearchServer", serverResource+""));
        }

        String hostName = serverResource.getHostName();
        String timeZone = ApplicationProperties.elasticsearchDateTimezone;
        //String nApplicationProperties.
        String dateForamte = ApplicationProperties.elasticsearchDateFormat;
        String index = ApplicationProperties.elasticsearchIndexLog;
        String type = ApplicationProperties.elasticsearchTypeLog;


        SearchVo searchVo = new SearchVo(search);
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        QueryBuilder hostMatch = QueryBuilders.matchQuery("host", hostName);

        query.must(hostMatch);


        QueryBuilder rangeQuery = null;
        QueryBuilder postFilter = null;
        String from  = null;
        String to = null;



        if(searchVo.isHaveRange()){
//            from  = DateUtil.convertDateFormat(searchVo.getStartDttm(), SearchVo.DATE_FORMAT, TIMESTAMP_T_PATTERN);
            from  = searchVo.getStartDttm(TIMESTAMP_T_PATTERN);
            to = searchVo.getEndDttm(TIMESTAMP_T_PATTERN);
            rangeQuery = QueryBuilders.rangeQuery("@timestamp").from(from).to(to).timeZone(timeZone).format(dateForamte);
        }else{

            from  = DateUtil.dateToString(new Date(System.currentTimeMillis() - (ApplicationProperties.logDefaultTerm*60*60*1000)), TIMESTAMP_T_PATTERN);
           // to = DateUtil.convertDateFormat(searchVo.getEndDttm(), DATE_HMS_PATTERN, TIMESTAMP_T_PATTERN);
            rangeQuery = QueryBuilders.rangeQuery("@timestamp").from(from).to(to).timeZone(timeZone).format(dateForamte);

        }

        query.must(rangeQuery);





        if(searchVo.isHaveKeyworkd()){
            List<String> keys = searchVo.getKeys();
            for(String key : keys){
                query.must(QueryBuilders.matchQuery(keys.get(0), searchVo.getKeyword(keys.get(0))));

            }

        }





        SearchResponse searchResponse = null;

        if(logger.isDebugEnabled()) {
            logger.debug("index : {}", index);
            logger.debug("type : {}", type);
            logger.debug("query : {}", query);
            logger.debug("postFilter : {}", postFilter);
            logger.debug("index : {}", index);
        }

        searchResponse = elasticsearchTemplate.query(index, type, query, postFilter, "@timestamp", SortOrder.DESC, page.getPage(), page.getPageSize());


        //searchResponse.

        //Set<String> result = new HashSet<String>();

        SearchHits hits = searchResponse.getHits();
        Long total = hits.totalHits;

        PagingResVo<LogResDto> pagingResVo = new PagingResVo<LogResDto>(page.getPage(), page.getPageSize(), total.intValue());

        for (SearchHit hit : searchResponse.getHits()) {
            LogResDto logResDto = new LogResDto();
            logResDto.setId(hit.getId());
            logResDto.setIndex(hit.getIndex());
            Map<String, Object> source = hit.getSource();
            //String host = (String)source.get("host");

            logResDto.setHost(StringUtil.nullToString((String)source.get("host")));
            logResDto.setMessage(StringUtil.nullToString((String)source.get("message").toString()));
            logResDto.setProgram(StringUtil.nullToString((String)source.get("program").toString()));
            logResDto.setSource(StringUtil.nullToString((String)source.get("source").toString()));

            try {
                String time = DateUtil.utcTimestampToString(source.get("@timestamp").toString(), DateUtil.DATE_TIME_PATTERN);
                logResDto.setTime(time);
            }catch(Exception ex){

            }
            pagingResVo.addListItem(logResDto);
        }


        if(logger.isDebugEnabled()) {
            logger.debug("pagingResVo : {}", pagingResVo);
        }


        return pagingResVo;
    }
}

