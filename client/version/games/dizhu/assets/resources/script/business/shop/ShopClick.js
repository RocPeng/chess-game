var beiMiCommon = require("BeiMiCommon");
cc.Class({
    extends: beiMiCommon,

    properties: {

    },
    onClick:function(event, data){
        /**
         * 发射事件到 上一级 处理
         */
        this.node.dispatchEvent( new cc.Event.EventCustom(data, true) );
    }
});
