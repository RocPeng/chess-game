var beiMiCommon = require("BeiMiCommon");

/**
 * 轮盘游戏，来自于网络找到的代码 ， 版权位置，如有侵权，请联系 本软件作者
 */
cc.Class({
    extends: beiMiCommon,

    properties: {
        spinBtn: {
            default: null,      // The default value will be used only when the component attachin                    // to a node for the first time
            type:cc.Button,     // optional, default is typeof default
            visible: true,      // optional, default is true
            displayName: 'SpinBtn', // optional
        },
        wheelSp:{
            default:null,
            type:cc.Node
        },
        maxSpeed:{
            default:3,
            type:cc.Float,
            max:10,
            min:2,
        },
        duration:{
            default:3,
            type:cc.Float,
            max:5,
            min:1,
            tooltip:"减速前旋转时间"
        },
        acc:{
            default:0.1,
            type:cc.Float,
            max:0.2,
            min:0.01,
            tooltip:"加速度"
        },
        targetID:{
            default:0,
            type:cc.Integer,
            max:17,
            min:0,
            tooltip:"指定结束时的齿轮"
        },
        springback:{
            default:true,
            tooltip:"旋转结束是否回弹"
        },
        effectAudio:{
            default:null,
            url:cc.AudioClip
        }
    },

    // use this for initialization
    onLoad: function () {
        cc.log("....onload");
        this.wheelState = 0;
        this.curSpeed = 0;
        this.spinTime = 0;                   //减速前旋转时间
        this.gearNum = 18;
        this.defaultAngle = 360/18/2;        //修正默认角度
        this.gearAngle = 360/this.gearNum;   //每个齿轮的角度
        this.wheelSp.rotation = this.defaultAngle;
        this.finalAngle = 0;                   //最终结果指定的角度
        this.effectFlag = 0;                 //用于音效播放

        this.spinBtn.node.on(cc.Node.EventType.TOUCH_END,function(event)
        {
            cc.log("begin spin");
            if(this.wheelState !== 0)
            {
                return;
            }
            this.decAngle = 2*360;  // 减速旋转两圈
            this.wheelState = 1;
            this.curSpeed = 0;
            this.spinTime = 0;
            // var act = cc.rotateTo(10, 360*10);
            // this.wheelSp.node.runAction(act.easing(cc.easeSineInOut()));
        }.bind(this));
    },

    start:function()
    {
        // cc.log('....start');
    },

    caculateFinalAngle:function(targetID)
    {
        this.finalAngle = 360-this.targetID*this.gearAngle + this.defaultAngle;
        if(this.springback)
        {
            this.finalAngle += this.gearAngle;
        }
    },
    editBoxDidBegin:function(edit)
    {
    },
    editBoxDidChanged:function(text)
    {
    },
    editBoxDidEndEditing:function(edit)
    {
        var res = parseInt(edit.string);
        if(isNaN(res))
        {
            if(cc.sys.isBrowser)
            {
                alert('please input a number!');
            }else cc.log(".....invalid input");
            this.targetID = Math.round(Math.random()*(this.gearNum-1));
            return;
        }
        this.targetID = res;
    },
    // called every frame, uncomment this function to activate update callback
    update: function (dt) {
        if(this.wheelState === 0)
        {
            return;
        }
        // cc.log('......update');
        // cc.log('......state=%d',this.wheelState);

        this.effectFlag += this.curSpeed;
        if(!cc.sys.isBrowser && this.effectFlag >= this.gearAngle)
        {
            this.effectFlag = 0;
        }
        if(this.wheelState == 1)
        {
            // cc.log('....加速,speed:' + this.curSpeed);
            this.spinTime += dt;
            this.wheelSp.rotation = this.wheelSp.rotation + this.curSpeed;
            if(this.curSpeed <= this.maxSpeed)
            {
                this.curSpeed += this.acc;
            }
            else
            {
                if(this.spinTime<this.duration)
                {
                    return;
                }
                // cc.log('....开始减速');
                //设置目标角度
                this.finalAngle = 360-this.targetID*this.gearAngle + this.defaultAngle;
                this.maxSpeed = this.curSpeed;
                if(this.springback)
                {
                    this.finalAngle += this.gearAngle;
                }
                this.wheelSp.rotation = this.finalAngle;
                this.wheelState = 2;
            }
        }
        else if(this.wheelState == 2)
        {
            // cc.log('......减速');
            var curRo = this.wheelSp.rotation; //应该等于finalAngle
            var hadRo = curRo - this.finalAngle;
            this.curSpeed = this.maxSpeed*((this.decAngle-hadRo)/this.decAngle) + 0.2;
            this.wheelSp.rotation = curRo + this.curSpeed;

            if((this.decAngle-hadRo)<=0)
            {
                // cc.log('....停止');
                this.wheelState = 0;
                this.wheelSp.rotation = this.finalAngle;
                if(this.springback)
                {
                    //倒转一个齿轮
                    var act = new cc.rotateBy(0.5, -this.gearAngle);
                    var seq = cc.sequence(new cc.delayTime(0.3),act,cc.callFunc(this.showRes, this));
                    this.wheelSp.runAction(seq);
                }
                else
                {
                    this.showRes();
                }
            }
        }
    },
    showRes:function()
    {

    }
});