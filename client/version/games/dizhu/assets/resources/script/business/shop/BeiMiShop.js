var beiMiCommon = require("BeiMiCommon");
cc.Class({
    extends: beiMiCommon,
    properties: {
        panel:{
            default:null ,
            type : cc.Node
        },
        wares:{
            default : null ,
            type:cc.Prefab
        },
        goldcoinsscroll:{
            default : null ,
            type:cc.Node
        },
        diamondsscroll:{
            default : null ,
            type:cc.Node
        },
        propscroll:{
            default : null ,
            type:cc.Node
        },
        goldcoinscontent:{
            default : null ,
            type:cc.Node
        },
        diamondscontent:{
            default : null ,
            type:cc.Node
        },
        propcontent:{
            default : null ,
            type:cc.Node
        },
        coins:{
            default : null ,
            type:cc.Node
        },
        diamonds:{
            default : null ,
            type:cc.Node
        },
        prop:{
            default : null ,
            type:cc.Node
        },
        atlas: {
            default: null,
            type: cc.SpriteAtlas
        }

    },

    onLoad: function () {
        let self = this ;
        this.wareslist = new Array();
        if(this.panel!=null){
            this.swatchmenu("goldcoins");
            if(cc.beimi.loadding.size() > 0){
                this.loaddingDialog = cc.beimi.loadding.get();
                this.loaddingDialog.parent = this.panel;

                this._animCtrl = this.loaddingDialog.getComponent(cc.Animation);
                var animState = this._animCtrl.play("loadding");
                animState.wrapMode = cc.WrapMode.Loop;
                this.closeShopLoadding(false);
            }
            if(cc.beimi.wares == null) {
                cc.beimi.http.httpGet("/api/wares", this.shop, this.error, this);
            }else{
                this.shop(cc.beimi.wares , this) ;
            }
        }
        /**
         * 商城菜单点击发射出来的 事件，用于在这个位置统一处理
         */
        this.node.on("goldcoins",function(event){
            self.swatchmenu("goldcoins")
            event.stopPropagation();
        });
        this.node.on("diamonds",function(event){
            self.swatchmenu("diamonds")
            event.stopPropagation();
        });
        this.node.on("prop",function(event){
            self.swatchmenu("prop")
            event.stopPropagation();
        });
    },
    shop:function(result , object){
        object.closeShopLoadding(true);
        var data = JSON.parse(result) ;
        if(cc.beimi.wares == null && data !=null && data.length > 0) {
            cc.beimi.wares == result;
        }
        if(data!=null && data.length > 0){
            for(var i=0 ; i<data.length ; i++){
                let temp = data[i] ;
                if(object.wares!=null){
                    for(var inx=0 ; inx<temp.wares.length ; inx++){
                        var wares = cc.instantiate(object.wares);
                        var script = wares.getComponent("Wares");
                        script.init(temp.wares[inx]);
                        if(temp.code == "goldcoins"){
                            wares.parent = object.goldcoinscontent ;
                        }else if(temp.code == "diamonds"){
                            wares.parent = object.diamondscontent ;
                        }else if(temp.code == "prop"){
                            wares.parent = object.propcontent ;
                        }
                        object.wareslist.push(wares) ;

                    }
                }
                if(temp.status != null && temp.status == "0"){
                    if(temp.code == "goldcoins"){
                        object.coins.active = false ;
                    }else if(temp.code == "diamonds"){
                        object.diamonds.active = false ;
                    }else if(temp.code == "prop"){
                        object.prop.active = false ;
                    }
                }
            }
        }
    },
    error:function(){

    },
    closeShopLoadding:function(close){
        let self = this ;

        if(this.loaddingDialog!=null){
            if(close == true){
                cc.beimi.loadding.put(self.loaddingDialog);
            }else{
                setTimeout(function(){
                    cc.beimi.loadding.put(self.loaddingDialog);
                },3000);
            }
        }
    },
    swatchmenu:function(model){
        if(model == "goldcoins"){
            this.coins.getComponent(cc.Sprite).spriteFrame = this.atlas.getSpriteFrame("tab_selected");
            this.diamonds.getComponent(cc.Sprite).spriteFrame = this.atlas.getSpriteFrame("tab_unselected");
            this.prop.getComponent(cc.Sprite).spriteFrame = this.atlas.getSpriteFrame("tab_unselected");
            this.diamondsscroll.active = false ; //钻石模块隐藏
            this.propscroll.active = false ; //道具模块隐藏
            this.goldcoinsscroll.active = true ; //金币模块显示
        }else if(model == "diamonds"){
            this.coins.getComponent(cc.Sprite).spriteFrame = this.atlas.getSpriteFrame("tab_unselected");
            this.diamonds.getComponent(cc.Sprite).spriteFrame = this.atlas.getSpriteFrame("tab_selected");
            this.prop.getComponent(cc.Sprite).spriteFrame = this.atlas.getSpriteFrame("tab_unselected");
            this.propscroll.active = false ; //道具模块隐藏
            this.goldcoinsscroll.active = false ; //金币模块显示
            this.diamondsscroll.active = true ; //钻石模块隐藏
        }else if(model == "prop"){
            this.coins.getComponent(cc.Sprite).spriteFrame = this.atlas.getSpriteFrame("tab_unselected");
            this.diamonds.getComponent(cc.Sprite).spriteFrame = this.atlas.getSpriteFrame("tab_unselected");
            this.prop.getComponent(cc.Sprite).spriteFrame = this.atlas.getSpriteFrame("tab_selected");
            this.goldcoinsscroll.active = false ; //金币模块显示
            this.diamondsscroll.active = false ; //钻石模块隐藏
            this.propscroll.active = true ; //道具模块隐藏
        }
    }
});
