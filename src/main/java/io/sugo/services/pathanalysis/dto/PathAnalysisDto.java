package io.sugo.services.pathanalysis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.sugo.services.query.ScanQuery;
import io.sugo.services.query.filter.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.sugo.common.utils.Constants.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PathAnalysisDto {
    private static final Logger log = LogManager.getLogger(PathAnalysisDto.class);
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    public static final String NORMAL_DIRECTION = "normal";
    public static final String REVERSE_DIRECTION = "reverse";

    @Inject @Named(Sys.PATH_ANALYSIS_SCAN_QUERY_BATCH_SIZE)
    private static int SCAN_QUERY_BATCH_SIZE;

    @Inject @Named(Sys.PATH_ANALYSIS_SCAN_QUERY_LIMIT_SIZE)
    private static int SCAN_QUERY_LIMIT_SIZE;

    @Inject @Named(Sys.PATH_ANALYSIS_SCAN_QUERY_TIMEOUT_MILLIS)
    public static int SCAN_QUERY_TIMOUT_MILLIS;


    @JsonProperty
    private String dataSource;
    @JsonProperty
    private ColumnName dimension;
    @JsonProperty
    private List<String> pages;
    @JsonProperty
    private Object filters;
    @JsonProperty
    private String homePage;
    @JsonProperty
    private Integer limit;

    /**
     * yyyy-mm-dd
     */
    @JsonProperty
    private String startDate;

    /**
     * yyyy-mm-dd
     */
    @JsonProperty
    private String endDate;

    @JsonProperty
    private String broker;

    @JsonProperty(defaultValue = NORMAL_DIRECTION)
    private String direction;

    public static class ColumnName {
        @JsonProperty
        String sessionId;
        @JsonProperty
        String userId;
        @JsonProperty
        String pageName;
        @JsonProperty
        String date;

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getPageName() {
            return pageName;
        }

        public void setPageName(String pageName) {
            this.pageName = pageName;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public ColumnName getDimension() {
        return dimension;
    }

    public void setDimension(ColumnName dimension) {
        this.dimension = dimension;
    }

    public List<String> getPages() {
        return pages;
    }

    public void setPages(List<String> pages) {
        this.pages = pages;
    }

    public String getHomePage() {
        return homePage;
    }

    public void setHomePage(String homePage) {
        this.homePage = homePage;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Object getFilters() {
        return filters;
    }

    public void setFilters(Object filters) {
        this.filters = filters;
    }

    public String getBroker() {
        return broker;
    }

    public String getDirection() {
        return direction;
    }

    public PathAnalysisDto setDirection(String direction) {
        this.direction = direction;
        return this;
    }

    public PathAnalysisDto setBroker(String broker) {
        this.broker = broker;
        return this;
    }

    public String buildScanQuery() throws Exception {
        ScanQuery query = new ScanQuery();
        query.setDataSource(this.dataSource);
        query.setBatchSize(SCAN_QUERY_BATCH_SIZE);
        query.setLimit(this.limit == null ? SCAN_QUERY_LIMIT_SIZE : this.limit);

        // Set filters
        if (this.filters != null) {
            if (filters instanceof List) { // Compatible with previous versions
                AndFilter andFilter = new AndFilter();
                query.setFilter(andFilter);
                if (this.pages != null && !this.pages.isEmpty()) {
                    InFilter InFilter = new InFilter();
                    InFilter.setDimension(this.getDimension().getPageName());
                    InFilter.setValues(this.pages);
                    andFilter.getFields().add(InFilter);
                }

                try {
                    List<FilterDimension> dimFilters = Lists.transform((List) filters, new Function<Object, FilterDimension>() {
                        @Nullable
                        @Override
                        public FilterDimension apply(@Nullable Object input) {
                            Map obj = (Map) input;
                            FilterDimension filterDimension = new FilterDimension();
                            filterDimension.setDimension(obj.get("dimension").toString());
                            filterDimension.setAction(obj.get("action").toString());
                            filterDimension.setValue(obj.get("value"));
                            if (obj.get("actionType") != null) {
                                filterDimension.setActionType(obj.get("actionType").toString());
                            }
                            return filterDimension;
                        }
                    });
                    if (dimFilters.size() > 0) {
                        andFilter.getFields().addAll(buildFilterFields(dimFilters));
                    }
                } catch (Exception e) {
                    log.error(String.format("Deserialize path analysis filters %s failed: %s", filters.toString(), e.getMessage()));
                }
            } else { // New version
                query.setFilter(filters);
            }
        }

        // Set columns
        query.getColumns().add(this.getDimension().getSessionId());
        query.getColumns().add(this.getDimension().getUserId());
        query.getColumns().add(this.getDimension().getPageName());
        query.getColumns().add(this.getDimension().getDate());

        // Set intervals
        query.setIntervals(this.startDate + "/" + this.endDate);

        String queryStr = "";
        try {
            queryStr = jsonMapper.writeValueAsString(query);
        } catch (JsonProcessingException e) {
            log.error("Serialize path analysis query object failed.", e);
            throw new Exception("Parse query json failed.");
        }

        return queryStr;
    }



    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FilterDimension {
        @JsonProperty
        String dimension;
        @JsonProperty
        String action;
        @JsonProperty
        Object value;
        @JsonProperty
        String actionType;

        public String getDimension() {
            return dimension;
        }

        public void setDimension(String dimension) {
            this.dimension = dimension;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public String getActionType() {
            return actionType;
        }

        public void setActionType(String actionType) {
            this.actionType = actionType;
        }
    }

    private List<FieldType> buildFilterFields(List<FilterDimension> filters) {
        List<FieldType> fields = new ArrayList<>(filters.size());
        for (FilterDimension filter : filters) {
            switch (filter.getAction()) {
                case "=":
                    EqualFilter EqualFilter = new EqualFilter();
                    EqualFilter.setDimension(filter.getDimension());
                    EqualFilter.setValue(filter.getValue().toString());
                    fields.add(EqualFilter);
                    break;
                case "!=":
                    NotEqualFilter notEqualFilter = new NotEqualFilter();
                    notEqualFilter.getField().setDimension(filter.getDimension());
                    notEqualFilter.getField().setValue(filter.getValue().toString());
                    fields.add(notEqualFilter);
                    break;
                case ">":
                    GreaterThanFilter greaterThanField = new GreaterThanFilter();
                    greaterThanField.setDimension(filter.getDimension());
                    greaterThanField.setLower(filter.getValue().toString());
                    fields.add(greaterThanField);
                    break;
                case "<":
                    LessThanFilter lessThanField = new LessThanFilter();
                    lessThanField.setDimension(filter.getDimension());
                    lessThanField.setUpper(filter.getValue().toString());
                    fields.add(lessThanField);
                    break;
                case ">=":
                    GreaterThanEqualFilter greaterThanEqualFilter = new GreaterThanEqualFilter();
                    greaterThanEqualFilter.setDimension(filter.getDimension());
                    greaterThanEqualFilter.setLower(filter.getValue().toString());
                    fields.add(greaterThanEqualFilter);
                    break;
                case "<=":
                    LessThanEqualFilter lessThanEqualFilter = new LessThanEqualFilter();
                    lessThanEqualFilter.setDimension(filter.getDimension());
                    lessThanEqualFilter.setUpper(filter.getValue().toString());
                    fields.add(lessThanEqualFilter);
                    break;
                case "between":
                    List valuePair = (List) filter.getValue();
                    BetweenFilter betweenField = new BetweenFilter();
                    betweenField.setDimension(filter.getDimension());
                    betweenField.setLower(valuePair.get(0));
                    betweenField.setUpper(valuePair.get(1));
                    fields.add(betweenField);
                    break;
                case "in":
                    List listValues = (List) filter.getValue();
                    InFilter InFilter = new InFilter();
                    InFilter.setDimension(filter.getDimension());
                    InFilter.setValues(listValues);
                    fields.add(InFilter);
                    break;
                case "not in":
                    List listValuesNotIn = (List) filter.getValue();
                    NotInFilter notInFilter = new NotInFilter();
                    notInFilter.getField().setDimension(filter.getDimension());
                    notInFilter.getField().setValues(listValuesNotIn);
                    fields.add(notInFilter);
                    break;
                case "lookup":
                    LookupFilter lookupField = new LookupFilter();
                    lookupField.setDimension(filter.getDimension());
                    lookupField.setLookup(filter.getValue().toString());
                    fields.add(lookupField);
                    break;
            }
        }

        return fields;
    }

    public static void main(String[] args) {
        PathAnalysisDto dto = new PathAnalysisDto();
        dto.setDataSource("com_SJLnjowGe_project_H1sSFD36g");
        dto.setStartDate("2017-01-01");
        dto.setEndDate("2017-05-05");
        dto.setHomePage("蠢蠢欲动");
        dto.setPages(Lists.asList("蠢蠢欲动", "激情无限", new String[]{"欲罢不能", "爷不行了"}));

        ColumnName columnName = new ColumnName();
        columnName.setSessionId("SugoSessionId");
        columnName.setPageName("Page");
        columnName.setDate("AccessTime");
        columnName.setUserId("SugoUserId");
        dto.setDimension(columnName);

        List<FilterDimension> filters = new ArrayList<>();
        FilterDimension filterDimension = new FilterDimension();
        filterDimension.setDimension("sugo_id");
        filterDimension.setAction("=");
        filterDimension.setValue("1001");
        filters.add(filterDimension);

        filterDimension = new FilterDimension();
        filterDimension.setDimension("age");
        filterDimension.setAction(">");
        filterDimension.setValue("18");
        filters.add(filterDimension);

        dto.setFilters(filters);

        try {
            System.out.println(jsonMapper.writeValueAsString(dto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }

}
