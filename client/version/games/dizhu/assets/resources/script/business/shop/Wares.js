var beiMiCommon = require("BeiMiCommon");
cc.Class({
    extends: beiMiCommon,

    properties: {
        title:{
            default : null ,
            type : cc.Label
        },
        price:{
            default:null ,
            type:cc.Label
        },
        quantity:{
            default:null ,
            type:cc.Label
        },
        image:{
            default:null ,
            type:cc.Node
        },
        payment:{
            default:null ,
            type:cc.Node
        },
        atlas: {
            default: null,
            type: cc.SpriteAtlas
        },
    },
    onLoad:function(){

    },
    init:function(wares){
        if(wares!=null){
            this.title.string = wares.name ;
            this.price.string = wares.price ;
            this.quantity.string = wares.quantity;
            this.image.getComponent(cc.Sprite).spriteFrame = this.atlas.getSpriteFrame(wares.imageurl);
        }
    }
});
