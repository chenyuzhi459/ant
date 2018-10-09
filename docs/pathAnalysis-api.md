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
    dimension | 是 | obeject | 表示路径分析需要查询的维度,需要设置`sessionId/pageName/userId/date`四个字段 |
    homePage | 是 | string | 表示开始分析的路径(页面)名称 |
    startDate | 是 | date-string | 路径分析的开始时间,格式为`yyyy-MM-dd'T'HH:mm:ss.SSS` |
    endDate | 是 | date-string | 路径分析的结束时间,格式为`yyyy-MM-dd'T'HH:mm:ss.SSS` |
    broker | 是 | string | 表示`tindex/uindex`引擎的broker地址,支持以`,`分隔符传入多个broker |
    direction | 否 | string | 表示路径分析的方向是升序还是降序(dimension.date),可选值为`normal/reverse` |normal 
    filters| 否 | array<object> | `fiter`的json对象数组,具体配置请参考后面的`filter配置` |
    pages | 否 | array<string> | 表示只对页面名称(dimension.pageName)在该范围内的页面进行路径分析 |
    limit | 否 | int | 表示路径分析向`tindex/uindex`查询的总记录数 |2000000
    
 
    

    
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
    "broker": "192.168.0.220:8082",
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
value | 是 | `string/array` | 表示维度的过滤值, 根据action决定类型是`string/array` |
actionType | 否 | string | 保留字段,暂时不用设置 |