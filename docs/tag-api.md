##  为用户分群的数据更新标签
- `/ant/tag/usergroup/batchUpdate`  
**基本信息**   
接口说明: 为用户分群的数据更新标签   
请求方式:POST  
请求地址:`/ant/tag/usergroup/batchUpdate`  
响应类型:application/json  
数据类型:application/json     
url请求参数: 无  
body参数:   

    参数名 | 是否必须 | 类型 | 描述  | 默认值
    ---- | ----- | --- | --- | ----
    hproxyUrl | 是 | string | 表示向hproxy服务请求的更新地址 |
    dataSource | 是 | string | 表示要更新的数据源 |
    primaryColumn | 是 | string | 表示数据源的主键列名 |
    userGroupConfig | 是 | object | 表示用户分群的redis配置 |
    dimData | 是 | object | 表示要更新的标签维度值 |
    appendFlags | 否 | object | 表示是否是追加操作,仅对多值列有效|
    
 
    

    
请求示例:
```
type:post
url:http://localhost:6061/ant/tag/usergroup/batchUpdate
Body数据:
{
	"hproxyUrl":"http://192.168.0.225:8085/druid/proxy/batchupdate/ssss",
	"dataSource":"ssss",
	"primaryColumn" : "distinct_id",
	"userGroupConfig" : { 
            "hostAndPorts":"192.168.0.223:6379",  
            "clusterMode":false,  
            "groupId":"test_usergroup_multi2"  
      },
      "dimData":{
      	"s_test2":"sstt2233",
      	"mi_testmi": "5,9"
      },
      "appendFlags":{
      	"s_test2":false,
      	"mi_testmi":false
      }
}
```
结果示例:
```
{
    "success": 5,
    "failed": 0,
    "errors": []
}
```

##  根据行为数据计算标签
- `/ant/tag/query/batchUpdate`  
**基本信息**   
接口说明: 根据行为数据直接计算标签   
请求方式:POST  
请求地址:`/ant/tag/query/batchUpdate`  
响应类型:application/json  
数据类型:application/json     
url请求参数: 无  
body参数:   

    参数名 | 是否必须 | 类型 | 描述  | 默认值
    ---- | ----- | --- | --- | ----
    brokerUrl | 是 | string | 表示行为数据源所在集群的broker地址 |
    hproxyUrl | 是 | string | 表示向hproxy服务请求的更新地址 |
    dataSource | 是 | string | 表示要更新的数据源 |
    dimMap | 是 | object | 表示查询指标和标签维度的关系映射表 |
    query | 是 | object | 表示对行为数据的查询json, 一般为`GroupBy`查询,具体配置请参考[Sugo-GrouByQuery查询接口](http://docs.sugo.io/developer/query/query.html#GroupBy) |
    appendFlags | 否 | object | 表示是否对标签维度做追加操作,仅对多值列有效|
    
 
    

    
请求示例:
```
type:post
url:http://localhost:6061/ant/tag/query/batchUpdate
Body数据:
{
    "brokerUrl":"http://192.168.0.225:8082/druid/v2?pretty",
    "hproxyUrl":"http://192.168.0.225:8085/druid/proxy/batchupdate/ssss",
    "dataSource":"ssss",
    "dimMap":{"distinct_id":"distinct_id", "count":"i_count","sum_age":"d_sum_age"},
    "appendFlags": {"i_count":false,"l_sum_age" : false},
    "query":{
        "queryType": "lucene_groupBy",
        "dataSource": "schedule_desc",
        "intervals": "1000/3000",
        "granularity": "all",
        "context": {
            "timeout": 180000,
            "useOffheap": true,
            "groupByStrategy": "v2"
        },
        "dimensions": [
            {
                "type": "default",
                "dimension": "distinct_id",
                "outputName": "distinct_id"
            }
        ],
        "aggregations": [
            {
                "name": "sum_age",
                "type": "lucene_doubleSum",
                "fieldName": "age"
            },
                {
            "type":"lucene_count",
            "name":"count"
        }
        ],
        "limitSpec": {
            "type": "default",
            "columns": [
                {
                    "dimension": "distinct_id"
                }
            ],
            "limit": 3
        }
    }
}
```
结果示例:
```
{
    "success": 3,
    "failed": 0,
    "errors": []
}
```