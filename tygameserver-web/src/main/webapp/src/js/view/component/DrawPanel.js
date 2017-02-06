puremvc.define({
	name:'drawsomething.view.component.DrawPanel',
	constructor:function(event){
		var _this=this;
		this.selectedColor=3;
        this.selectedBrush=1;
        this.colors=['','#ffffff','#322322','#bf5ebc','#6649b0','#3f71ae','#32c9e3','#9bea57','#eaca57','#e89054','#e85454'];
        this.brushes=[,2,5,10,20,30];
		this.app=document.querySelector( '#connectPanel');
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
        var scroll=new drawsomething.view.component.Scroll("nihao");
        scroll.getName();
	}
},
{
	addEventListener:function(type,listener,useCapture){
		drawsomething.view.event.AppEvents.addEventListener(this.app,type,listener,useCapture);
	},
	createEvent:function(eventName){
		return drawsomething.view.event.AppEvents.createEvent(eventName);
	},
	dispatchEvent:function(event){
		drawsomething.view.event.AppEvents.dispatchEvent(this.app,event);
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
	},
	show:function(){
		$("#drawPanel").show();
	}
},
{
	NAME:'DrawPanel'
})