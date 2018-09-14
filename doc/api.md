##  单个用户分群操作请求
- `/ant/usergroup/single`  
**基本信息**   
接口说明:对单个用户分群进行覆盖或者累加计算操作   
请求方式:POST  
请求地址:`/ant/usergroup/single`  
响应类型:application/json  
数据类型:application/json     
url请求参数: 无  
body参数:   

    参数名 | 是否必须 | 类型 | 描述  | 默认值
    ---- | ----- | --- | --- | ----    
    brokerUrl | 是 | string | 表示`tindex|uindex`引擎的broker地址 |
    query| 是 | obeject | `UserGroupQuery`的json对象,具体配置请参考[Sugo-UserGroupQuery查询接口](http://docs.sugo.io/developer/query/query.html#UserGroup) |
    append | 是 | boolean | 表示对用户分群的操作,false表示进行覆盖操作, true表示进行累加操作 |
 
    

    
请求示例:
```
type:post
url:http://localhost:6061/ant/usergroup/single
Body数据:
{
      "brokerUrl": "http://192.168.0.225:8082/druid/v2?pretty",
      "query": {
            "queryType":"user_group",
            "dataSource":"schedule_desc",
            "granularity":"all",
            "intervals": "1000/3000",
            "filter": {
                "type": "selector",
                "dimension": "sugo_province",
                "value": "广东省"
            },
            "dimension":"distinct_id",
            "dataConfig": {
                "hostAndPorts":"192.168.0.223:6379",  
                "clusterMode":false,  
                "groupId":"schedule_desc_sugo_province"  
            },
            "context":{
                "timeout": 180000,
                "useOffheap": true,
                "groupByStrategy": "v2"
            }
      },
      "append" : true
}
```
结果示例:
```
[
    {
        "event": {
            "RowCount": 18075     //RowCount 表示分群内的记录数
        }
    }
]
```


##  多个用户分群操作请求
- `/ant/usergroup/multi`  
**基本信息**   
接口说明:根据对多个用户分群的操作生成一个最终的用户分群   
请求方式:POST  
请求地址:`/ant/usergroup/multi`  
响应类型:application/json  
数据类型:application/json     
url请求参数: 无  
body参数:  body参数为一个对象数组, 每个对象描述了一个用户分群的属性和操作,下表是对象属性的描述  

    参数名 | 是否必须 | 类型 | 描述  | 默认值
    ---- | ----- | --- | --- | ----    
    type | 是 | string | 表示用户分群的类型,有四个可选值`tindex|uindex|usergroup|finalGroup`, 其中前两种类型表示从`tindex|uindex`引擎生成临时用户分群,在操作完成后会被删除,usergroup表示是redis中的用户分群,不会被删除, finalGroup表示是最终要生成的用户分群|
    query| 是 | obeject | `UserGroupQuery`的json对象,具体配置请参考[Sugo-UserGroupQuery查询接口](http://docs.sugo.io/developer/query/query.html#UserGroup) |
    brokerUrl | 否 | string | 表示`tindex|uindex`引擎的broker地址, 在type=`tindex|uindex`时需要配置 |
    op | 否 | string | 表示对用户分群的操作, 可选值为`or|and|exclude`; 在type = `finalGroup`时不需要设置, 数组中的第一个用户分群也不用设置 |
    append | 否 | boolean | 表示对最终用户分群的操作,false表示进行覆盖操作, true表示进行累加操作 |
 
    

    
请求示例:
```
type:post
url:http://localhost:6061/ant/usergroup/multi
Body数据:
[
  {
      "type": "tindex",
      "brokerUrl": "http://192.168.0.225:8082/druid/v2?pretty",
      "query": {
            "queryType":"user_group",
            "dataSource":"schedule_desc",
            "granularity":"all",
            "intervals": "1000/3000",
            "filter": {
                "type": "selector",
                "dimension": "sugo_province",
                "value": "广东省"
            },
            "dimension":"distinct_id",
            "dataConfig": {
                "hostAndPorts":"192.168.0.223:6379",  
                "clusterMode":false,  
                "groupId":"schedule_desc_sugo_province"  
            },
            "context":{
                "timeout": 180000,
                "useOffheap": true,
                "groupByStrategy": "v2"
            }
      }
  },

  {
    "type": "usergroup",
    "query": {
      "dataConfig": { 
            "hostAndPorts": "192.168.0.220:6379",
            "clusterMode": false,
            "sentinelMode": false,
            "groupId": "usergroup_N4pahgU1y"
      }
    },
    "op": "or"
  },
  {
      "type": "uindex",
      "brokerUrl": "http://192.168.0.223:8082/druid/v2?pretty",
      "query": {
                "queryType":"user_group",
                "dataSource":"tag_bank",
                "granularity":"all",
                "intervals": "1000/3000",
                "filter": {
                    "type": "selector",
                    "dimension": "ub_risk",
                    "value": "R4"
                },
                "dimension":"distinct_id",
                "dataConfig": {
                    "hostAndPorts":"192.168.0.223:6379",  
                    "clusterMode":false,  
                    "groupId":"tag_bank_ub_risk"  
                },
                "context":{
                    "timeout": 180000,
                    "useOffheap": true,
                    "groupByStrategy": "v2"
                }
      },
      "op": "exclude"
  },
  {
    "type": "finalGroup",
    "query": {
      "dataConfig": { 
            "hostAndPorts":"192.168.0.223:6379",  
            "clusterMode":false,  
            "groupId":"test_usergroup_multi"  
      }
    },
    "append": true
  }
]
```
结果示例:
```
[
    {
        "event": {
            "RowCount": 14919
        }
    }
]
```

##  路径分析请求
- `/ant/process/pa`  
**基本信息**   
接口说明: 对数据进行路径分析   
请求方式:POST  
请求地址:`/ant/process/pa`  
响应类型:application/json  
数据类型:application/json     
url请求参数: 无  
body参数:   

    参数名 | 是否必须 | 类型 | 描述  | 默认值
    ---- | ----- | --- | --- | ----    
    dataSource | 是 | string | 表示进行路径分析的数据源 |
    dimension | 是 | obeject | 表示路径分析需要查询的维度,需要设置`sessionId|pageName|userId|date`四个字段 |
    homePage | 是 | string | 表示开始分析的路径(页面)名称 |
    startDate | 是 | date-string | 路径分析的开始时间,格式为`yyyy-MM-dd'T'HH:mm:ss.SSS` |
    endDate | 是 | date-string | 路径分析的结束时间,格式为`yyyy-MM-dd'T'HH:mm:ss.SSS` |
    brokerUrl | 是 | string | 表示`tindex|uindex`引擎的broker地址 |
    direction | 否 | string | 表示路径分析的方向是升序还是降序(dimension.date),可选值为`normal|reverse` |normal 
    filters| 否 | array<object> | `fiter`的json对象数组,具体配置请参考后面的`filter配置` |
    pages | 否 | array<string> | 表示只对页面名称(dimension.pageName)在该范围内的页面进行路径分析 |
    limit | 否 | int | 表示路径分析向`tindex|uindex`查询的总记录数 |2000000
    
 
    

    
请求示例:
```
type:post
url:http://localhost:6061/ant/process/pa
Body数据:
{
  
    "dataSource": "com_SJLnjowGe_project_HyErw0VBW",
    "dimension": {
      "sessionId": "session_id",
      "pageName": "path_name",
      "userId": "distinct_id",
      "date": "__time"
    },
    "homePage": "ScanViewController",
    "startDate": "2018-08-31T16:00:00.000Z",
    "endDate": "2018-09-3T15:59:59.999Z",
    "brokerUrl": "http://192.168.0.220:8082/druid/v2?pretty",
    "direction": "reverse"
}
```
结果示例:
```
{
    "pageName": "ScanViewController",
    "layer": 1,
    "userIds": [
        "6E4495EF-B712-4125-9B0B-FC05563A84D0",
        "9F09F14A-5D7C-405C-9275-25BCFB6D5B91",
        "0E18EA3A-187A-461E-A6B4-1C455C44877C",
        "021567F3-14BD-4A86-A5F7-723DB15AB62A",
        "62CF8091-8E18-49EC-A88A-DA5799C965C5"
    ],
    "type": 1,
    "weight": 29,
    "rate": 0,
    "children": [],
    "stayTime": 0
}
```


###  filter配置
说明: 此filter配置仅用于路径分析   
配置字段:

参数名 | 是否必须 | 类型 | 描述  | 默认值
---- | ----- | --- | --- | ----    
dimension | 是 | string | 表示过滤的维度 |
action| 是 | string | 过滤的表达式, 可选值有`"=","!=",">","<",">=","<=","between","in","not in","lookup"` |  
value | 是 | string|array | 表示维度的过滤值, 根据action决定类型是`string|array` |
actionType | 否 | string | 保留字段,暂时不用设置 |
 