webpackJsonp([23],{1060:function(e,t){e.exports={render:function(){var e=this,t=e.$createElement,r=e._self._c||t;return r("div",{staticClass:"main"},[r("div",{staticClass:"table",staticStyle:{"margin-left":"20px"}},[r("el-table",{staticStyle:{width:"100%"},attrs:{data:e.servers,border:"",stripe:""}},[r("el-table-column",{attrs:{prop:"server",label:"usedMemory"}})],1)],1)])},staticRenderFns:[]}},522:function(e,t,r){var s=r(198)(r(620),r(1060),null,null);e.exports=s.exports},620:function(e,t,r){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.default={name:"serversInfo",data:function(){return{servers:[]}},created:function(){this.getServersInfo()},methods:{getServersInfo:function(){var e=this;this.$http.get(this.$common.apis.serversInfo).then(function(t){e.servers=t.data.map(function(e){return{server:e}})},function(e){console.log("error")})}}}}});