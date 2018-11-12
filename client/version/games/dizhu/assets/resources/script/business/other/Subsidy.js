var beiMiCommon = require("BeiMiCommon");
cc.Class({
    extends: beiMiCommon,

    onLoad:function(){
        let self = this ;
        if(this.ready()) {
            let socket = this.socket();
            socket.on('subsidy', function (result) {
                self.closeOpenWin();
                var data = self.parse(result) ;
                if(data != null){
                    cc.beimi.data.subsidy = data.enable;
                    cc.beimi.data.subtimes = result.subtimes ;
                    cc.beimi.data.subgolds = result.subgolds ;
                    cc.beimi.data.lefttimes = result.subtimes - result.frequency ;

                    self.pva("gold" , result.balance);

                    self.updatepva() ;
                }
                /**
                 * 弹出确认窗口
                 */
                cc.loader.loadRes("prefab/welfare/confirm", function (err, prefab) {
                    cc.beimi.openwin = cc.instantiate(prefab);
                    cc.beimi.openwin.parent = cc.beimi.context.root();
                });
            });
            socket.on('subsidyfaild', function (result) {
                self.closeOpenWin();
                var data = self.parse(result) ;
                if(data.result != null && data.result != ""){
                    self.alertForCallBack(data.result , function(){
                        self.shopDialog();
                    });
                }
            });
        }
    },
    onClick:function(){
        if(this.ready()) {
            let socket = this.socket();
            var param = {
                command : "subsidy" ,
                token   : cc.beimi.authorization ,
                data    : cc.beimi.user.id
            };
            socket.emit("command",JSON.stringify(param));
        }
    },
    onDestroy:function(){
        if(this.ready()) {
            let socket = this.socket();
            socket.on('subsidy', null);
            socket.on('subsidyfaild', null);
        }
    }
});
