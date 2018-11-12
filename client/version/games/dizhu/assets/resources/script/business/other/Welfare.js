var beiMiCommon = require("BeiMiCommon");

/**
 * 福利中心 ， 根据后台设置的启用哪些福利，初始化的时候，根据设定的启用的福利类型加载
 */
cc.Class({
    extends: beiMiCommon,
    properties: {
        over:{
            default     : null ,
            type         :   cc.Prefab
        },
        sign:{
            default     : null ,
            type         :   cc.Prefab
        },
        turn:{
            default     : null ,
            type         :   cc.Prefab
        },
        overnode:{
            default     : null ,
            type         :   cc.Node
        },
        signnode:{
            default     : null ,
            type         :   cc.Node
        },
        turnnode:{
            default     : null ,
            type         :   cc.Node
        }
    },
    onLoad:function(){
        /**
         * 根据后端配置的参数确定哪些福利活动启用或不启用，登录的时候，从后端传入进来的参数
         */
        if(cc.beimi != null && cc.beimi.data != null){
            if(cc.beimi.data.welfare != null && cc.beimi.data.welfare.indexOf("over") >= 0){
                this.overnode.active = true ;
            }else{
                this.overnode.active = false ;
            }

            if(cc.beimi.data.welfare != null && cc.beimi.data.welfare.indexOf("sign") >= 0){
                this.signnode.active = true ;
            }else{
                this.signnode.active = false ;
            }

            if(cc.beimi.data.welfare != null && cc.beimi.data.welfare.indexOf("turn") >= 0){
                this.turnnode.active = true ;
            }else{
                this.turnnode.active = false ;
            }
        }
    },
    onOverClick:function(){
        this.closeOpenWin();
        this.openWin(this.over);
    },
    onSignClick:function(){
        this.closeOpenWin();
        this.openWin(this.sign);
    },
    onTurnClick:function(){
        this.closeOpenWin();
        this.openWin(this.turn);
    }

});
