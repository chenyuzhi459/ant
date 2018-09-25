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
    brokerUrl | 是 | string | 表示`tindex/uindex`引擎的broker地址 |
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
    type | 是 | string | 表示用户分群的类型,有四个可选值`tindex/uindex/usergroup/finalGroup`, 其中前两种类型表示从`tindex,uindex`引擎生成临时用户分群,在操作完成后会被删除,usergroup表示是redis中的用户分群,不会被删除, finalGroup表示是最终要生成的用户分群|
    query| 是 | obeject | `UserGroupQuery`的json对象,具体配置请参考[Sugo-UserGroupQuery查询接口](http://docs.sugo.io/developer/query/query.html#UserGroup) |
    brokerUrl | 否 | string | 表示`tindex/uindex`引擎的broker地址, 在type=`tindex/uindex`时需要配置 |
    op | 否 | string | 表示对用户分群的操作, 可选值为`or/and/exclude`; 在type = `finalGroup`时不需要设置, 数组中的第一个用户分群也不用设置 |
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
