package io.sugo.services.pathanalysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.sugo.common.utils.HttpClinetUtil;
import io.sugo.server.http.AntServer;
import io.sugo.services.pathanalysis.model.AccessPath;
import io.sugo.services.pathanalysis.model.AccessTree;
import io.sugo.services.pathanalysis.model.PathNode;
import io.sugo.services.pathanalysis.vo.PageAccessRecordVo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import java.util.*;

/**
 */
public class PathAnalyzer {

    private static final Logger log = LogManager.getLogger(AntServer.class);
    private final ObjectMapper jsonMapper;
    private final TreePlanter planter;

    @Inject
    public PathAnalyzer(TreePlanter planter, ObjectMapper jsonMapper) {
        this.planter = planter;
        this.jsonMapper = jsonMapper;
    }

    public AccessTree getAccessTree(String queryStr, String homePage, boolean reversed, String broker) {
        long before = System.currentTimeMillis();
        log.info(String.format(
                "Begin to path analysis...\n" +
                ">>>>>>>>>>>>>>>>[ScanQuery]\n " +
                "broker= %s \n param= %s\n" +
                "<<<<<<<<<<<<<<<<", broker, queryStr));

        int depth = reversed ? PathAnalysisConstant.TREE_DEPTH_REVERSE : PathAnalysisConstant.TREE_DEPTH_NORMAL;

        List<Map> queryResult = HttpClinetUtil.getQueryResult(broker, queryStr);
        Iterator<Map> iterator = queryResult.iterator();

        while (iterator.hasNext()) {
            Map resultValue = iterator.next();
            if (resultValue != null) {
                List<PageAccessRecordVo> records = Lists.newArrayList();
                List<List<Object>> events = (List<List<Object>>) resultValue.get("events");
                for (List<Object> event : events) {
                    try {
                        PageAccessRecordVo record = new PageAccessRecordVo();
                        record.setSessionId(event.get(1).toString());
                        record.setUserId(event.get(2).toString());
                        record.setPageName(event.get(3).toString());
                        Object accessTime = event.get(4);
                        if (accessTime != null) {
                            record.setAccessTime(new Date((Long) (accessTime)));
                        } else { // If the data generation time is null, then use the data ingestion time.
                            accessTime = event.get(0);
                            record.setAccessTime(new DateTime(accessTime).toDate());
                        }
                        records.add(record);
                    } catch (Exception ignore) {}
                }
                records.sort(reversed ? PageAccessRecordVo.DESC_COMPARATOR : PageAccessRecordVo.ASC_COMPARATOR);
                analyze(records, homePage, depth);
            }
        }

        long after = System.currentTimeMillis();
        log.info(String.format("Path analysis total cost %d ms.", after - before));

        return planter.getRoot();
    }

    private void analyze(List<PageAccessRecordVo> records, String homePage, int depth) {
        if (records.size() > 0) {
            boolean startAnalysis = false;
            PageAccessRecordVo preRecord = null;
            AccessPath path = null;
            int layer = 0;
            Long preAccessTime = null;

            for (int i = 0, size = records.size(); i < size; i++) {
                PageAccessRecordVo record = records.get(i);
                if (record.getPageName().equals(homePage)) {
                    startAnalysis = true;
                }
                if (startAnalysis) {
                    if (path == null) {
                        path = new AccessPath();
                    }

                    if (sameSession(preRecord, record, homePage)) {
                        // Discard the nodes whose layer greater than depth
                        if (layer <= depth) {
                            if (!sameNode(preRecord, record)) {
                                PathNode node = new PathNode(record.getPageName(), ++layer);
                                node.setUserId(record.getUserId());
                                if (preAccessTime != null) {
                                    node.setStayTime(record.getAccessTime().getTime() - preAccessTime);
                                }
                                path.addNode(node);
                            }
                            preRecord = record;
                            preAccessTime = record.getAccessTime().getTime();
                        }
                    } else {
                        // Add path for growing tree
                        planter.addPath(path);

                        // Clear all variables to initiate status
                        startAnalysis = false;
                        preRecord = null;
                        path = null;
                        layer = 0;
                        preAccessTime = null;

                        // Index rollback, otherwise current record will be missed
                        i--;
                    }
                }
            }

            // The last path that is not cleaned
            if (path != null) {
                planter.addPath(path);
            }
        }
    }

    private boolean sameSession(PageAccessRecordVo preRecord, PageAccessRecordVo currentRecord, String firstPage) {
        // First page
        if (preRecord == null && currentRecord.getPageName().equals(firstPage)) {
            return true;
        }

        Date preTime = preRecord.getAccessTime();
        Date currentTime = currentRecord.getAccessTime();
        long interval = Math.abs(currentTime.getTime() - preTime.getTime());

        return preRecord.getSessionId().equals(currentRecord.getSessionId()) &&
                interval <= PathAnalysisConstant.SESSION_EXPIRED_TIME;
    }

    private boolean sameNode(PageAccessRecordVo preRecord, PageAccessRecordVo currentRecord) {
        if (preRecord == null) {
            return false;
        }

        return preRecord.getPageName().equals(currentRecord.getPageName());
    }
}
