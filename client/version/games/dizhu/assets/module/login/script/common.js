var beiMiCommon = require("BeiMiCommon");
cc.Class({
    extends: beiMiCommon,
    properties: {
        toggle: {
            default : null ,
            type : cc.Toggle
        },
        tips:{
            default : null ,
            type : cc.Node
        }
    },

    // use this for initialization
    onLoad: function () {
        this.tips.active = false ;
    },
    login:function(){
        let self = this ;
        if(this.toggle.isChecked){
            this.io = require("IOUtils");
            this.loadding();
            if(this.io.get("token") == null){
                //发送游客注册请求
                var xhr = cc.beimi.http.httpGet("/api/guest", this.sucess , this.error , this);
            }else{
                var token = this.io.get("token") ;
                var xhr = cc.beimi.http.httpGet("/api/guest?token="+token, this.sucess , this.error , this);
            }
        }else{
            this.tips.active = true ;
            setTimeout(function(){
                if(self.tip != null){
                    self.tips.active = false ;
                }
            } , 2000) ;
        }
	},
    sucess:function(result , object){
        var data = JSON.parse(result) ;
        if(data!=null && data.token!=null && data.data!=null){
            //放在全局变量
            object.reset(data , result);
            cc.beimi.gamestatus = data.data.gamestatus ;
            /**
             * 登录成功后即创建Socket链接
             */
            object.connect();
            //预加载场景
            if(cc.beimi.gametype!=null && cc.beimi.gametype != ""){//只定义了单一游戏类型 ，否则 进入游戏大厅
                object.scene(cc.beimi.gametype , object) ;
            }else{
                /**
                 * 暂未实现功能
                 */
            }
        }
    },
    error:function(object){
        object.closeloadding(object.loaddingDialog);
        object.alert("网络异常，服务访问失败");
    }

    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});
