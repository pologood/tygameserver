puremvc.define({
	name:'drawsomething.view.component.DrawPanel',
	constructor:function(event){
		var _this=this;
        this.type=1;//1画笔，2橡皮
		this.selectedColor=3;
        this.selectedBrush=1;
        this.colors=['','#ffffff','#322322','#bf5ebc','#6649b0','#3f71ae','#32c9e3','#9bea57','#eaca57','#e89054','#e85454'];
        this.brushes=[,2,5,10,20,30];
		this.container=document.querySelector( '#connectPanel');
        this.$container=$("#drawPanel");
		this.stage=new createjs.Stage("drawingBoard");
		this.drawingCanvas=new createjs.Shape();	
        this.stage.addChild(this.drawingCanvas);
        this.stage.addEventListener("stagemousedown", function(){
        	_this.handleMouseDown(_this);
        });
        this.stage.addEventListener("stagemouseup", function(){
        	_this.handleMouseUp(_this);
        });
        this.drawingCanvas.cache(0,0,900,600);
        this.el=$("#drawPanel");
        this.el.find(".colorcard").find(".circle").click(function(){
            var index=$(this).parent().index();
            _this.el.find(".cursor").removeClass("cursor-1 cursor-2 cursor-3 cursor-4 cursor-5 cursor-6 cursor-7 cursor-8 cursor-9 cursor-10");
            _this.el.find(".cursor").addClass("cursor-"+index);
            _this.selectedColor=index;
        })
        this.$container.find(".eraser").click(function(){
            _this.type=2;
        })
        this.$container.find(".delete").click(function(){
            _this.dispatchDelete();
        })
        this.$container.find(".sendBtn").click(function(){
            _this.dispatchMsg();
        })
        this.$container.find('.msgIpt').bind('keypress',function(event){  
            if(event.keyCode == "13")
            {  
                _this.dispatchMsg();
            }  
        });
        
        this.isDrawer=false;
        this.countDown=60;
        this.$lineSet=this.$container.find(".lineSet");
        this.$endPop=this.$container.find(".endPop");
        this.$endPop.find(".zanBtn").click(function(){
            _this.sendLike();
        })
        this.$endPop.find(".caiBtn").click(function(){
            _this.sendUnlike();
        })
        var scroll=new drawsomething.view.component.Scroll("nihao");
        scroll.getName();

	}
},
{
	addEventListener:function(type,listener,useCapture){
		drawsomething.view.event.AppEvents.addEventListener(this.container,type,listener,useCapture);
	},
	createEvent:function(eventName){
		return drawsomething.view.event.AppEvents.createEvent(eventName);
	},
	dispatchEvent:function(event){
		drawsomething.view.event.AppEvents.dispatchEvent(this.container,event);
	},
    dispatchMsg:function(){
        var msgStr=this.$container.find(".msgIpt").val();
        var e = this.createEvent( drawsomething.view.event.AppEvents.SEND_MSG);
        e.msg={
            text:msgStr
        };
        this.dispatchEvent(e);
        this.$container.find(".msgIpt").val("");
    },
    dispatchDelete:function(){
        this.stage.clear();
        this.drawingCanvas.cache(0,0,900,600);
        var e = this.createEvent( drawsomething.view.event.AppEvents.DELETE);
        e.msg={type:2,drawInfo:null};
        this.dispatchEvent(e);
    },
	handleMouseDown:function(_this){
		_this.oldPt = new createjs.Point(_this.stage.mouseX, _this.stage.mouseY);
        _this.oldMidPt = _this.oldPt;
        _this.isDrawing = true; 
        _this.stage.addEventListener("stagemousemove",function(){
        	_this.handleMouseMove(_this);
        });
	},
	handleMouseUp:function(_this){
		_this.isDrawing = false; 
        _this.stage.removeEventListener("stagemousemove",function(){
        	_this.handleMouseMove(_this);
        });

	},
	handleMouseMove:function(_this){
        if(!_this.isDrawer){
            return;
        }
		if (!_this.isDrawing) {
            return;
        }

        var midPoint = new createjs.Point(_this.oldPt.x + _this.stage.mouseX >> 1, _this.oldPt.y + _this.stage.mouseY >> 1);
        var color = _this.colors[_this.selectedColor];
        var brush = _this.brushes[_this.selectedBrush];
        _this.drawingCanvas.graphics.setStrokeStyle(brush, "round", "round")
                .beginStroke(color)
                .moveTo(midPoint.x, midPoint.y)
                .curveTo(_this.oldPt.x, _this.oldPt.y, _this.oldMidPt.x, _this.oldMidPt.y);	
        _this.drawingCanvas.updateCache(_this.type==2 ? "destination-out" : "source-over");
        _this.drawingCanvas.graphics.clear();               

        var drawInfo={
            brush:brush,
            color:color,
            mtx:midPoint.x,
            mty:midPoint.y,
            ctOldx:_this.oldPt.x,
            ctOldy:_this.oldPt.y,
            ctOldMidx:_this.oldMidPt.x,
            ctOldMidy:_this.oldMidPt.y,
            type:_this.type
        }

        _this.oldPt.x = _this.stage.mouseX;
        _this.oldPt.y = _this.stage.mouseY;
        _this.oldMidPt.x = midPoint.x;
        _this.oldMidPt.y = midPoint.y;
        _this.stage.update();

        _this.dispatchDrawing({type:1,drawInfo:drawInfo});
	},
    drawingHandle:function(data){
        if(this.isDrawer){
            return;
        }
        var info=data.drawInfo;
        if(data.type==1){
            //绘画
            this.drawingCanvas.graphics.setStrokeStyle(info.brush, "round", "round")
                .beginStroke(info.color)
                .moveTo(info.mtx, info.mty)
                .curveTo(info.ctOldx, info.ctOldy, info.ctOldMidx, info.ctOldMidy); 
            this.drawingCanvas.updateCache(info.type==2 ? "destination-out" : "source-over");
            this.drawingCanvas.graphics.clear();
            this.stage.update();

        }else if(data.type==2){
            //清除           
            this.stage.clear();
            this.drawingCanvas.cache(0,0,900,600);
        }
        
    },
    dispatchDrawing:function(data){
        var e = this.createEvent( drawsomething.view.event.AppEvents.DRAWING);
        e.msg=data;
        this.dispatchEvent(e);
    },
    roundStart:function(data){
        var gameInfo=data.gameInfo;
        var avatarId=data.avatarId;
        if(avatarId==gameInfo.drawerId){
            this.$container.find(".colorDisc").show();
            this.$container.find(".tools").show();
            this.$container.find(".iptCnt").hide();
            this.isDrawer=true;
        }else{
            this.$container.find(".colorDisc").hide();
            this.$container.find(".tools").hide();
            this.$container.find(".iptCnt").show();
            this.isDrawer=false;
        }
        var source=$("#drawingPlayerItem-template").html();
        var template = Handlebars.compile(source); 
        var html    = template({rolesList:data.roominfo.members});
        this.$container.find(".members").html(html);
        var owner=this.getMember(data.roominfo.members,gameInfo.drawerId);
        if(owner){
            this.$container.find(".drawerName").html(owner.name);
        }
        this.stage.clear();
        this.drawingCanvas.cache(0,0,900,600);
        this.startCountdown();
    },
    getMember:function(members,avatarId){
        for(var i=0;i<members.length;i++){
            if(members[i].avatarId==avatarId){
                return members[i];
            }
        }
        return null;
    },
    updateAnswerInfo:function(answerInfo){
        this.$container.find(".answerTxt").html("作品："+answerInfo.answer);
    },
    receiveMsg:function(answerInfo){   
        var msg;
        if(answerInfo.isCorrect){
            msg='<p><span class="u-name">'+answerInfo.avatarName+'：</span>√</p>';
            this.countDown-=5;
            this.countDown=this.countDown>0?this.countDown:0;
        }else{
            msg='<p><span class="u-name">'+answerInfo.avatarName+'：</span>'+answerInfo.answer+'</p>';
        }        
        this.$container.find(".chatBox").find(".content").append(msg);
    },
    receiveHint:function(hintInfo){
        if(hintInfo.type==1){
            this.$container.find(".hint1").html('提示1：<span>'+hintInfo.hint+'</span>');
        }else{
            this.$container.find(".hint2").html('提示2：<span>'+hintInfo.hint+'个字</span>');
        }
    },
    roundOver:function(roundInfo){
        var dataUrl=this.stage.toDataURL();
        this.$endPop.find("img").attr("src",dataUrl);
        var _this=this;
        this.$endPop.show();
        this.$endPop.find(".title").html("答案："+roundInfo.answer);
        var countDown=6;
        function count(){            
            countDown--;
            _this.$endPop.find(".timeTips").html(countDown+"秒后自动关闭");
            if(countDown>0){
                setTimeout(count,1000);
            }else{
                _this.$endPop.hide();
            }
        }
        count();
    },
    startCountdown:function(){
        this.countDown=60;
        function count(_this){            
            _this.countDown--;            
            if(_this.countDown>0){
                _this.$container.find(".roundCountDown").html(_this.countDown+"秒");
                setTimeout(function(){
                    count(_this);
                },1000);
            }else{
                _this.$container.find(".roundCountDown").html("此轮结束");
            }
        }
        count(this);
    },
    sendLike:function(){
        var e = this.createEvent( drawsomething.view.event.AppEvents.LIKE);
        e.msg={};
        this.dispatchEvent(e);
    },
    sendUnlike:function(){
        var e = this.createEvent( drawsomething.view.event.AppEvents.UNLIKE);
        e.msg={};
        this.dispatchEvent(e);
    },
	show:function(){
		$("#drawPanel").show();
	}
},
{
	NAME:'DrawPanel'
})