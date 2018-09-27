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