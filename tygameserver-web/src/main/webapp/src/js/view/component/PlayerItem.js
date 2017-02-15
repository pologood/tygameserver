puremvc.define(
	{
		name:'drawsomething.view.component.PlayerItem',
		constructor:function(){
			this.el=null;
			var source=$("#playerItems-template").html();
			this.template = Handlebars.compile(source);	
			this.isReady=false;		
		}
	},
	{
		getName:function(){

		},
		getElement:function(){

		},
		startCountDown:function(){
			this.el.find(".countDown").show();
			var _this=this;
			var countDown=10;
			function count(){
				countDown--;
				_this.el.find(".second").html(countDown);
				if(countDown>0){
					setTimeout(count,1000);
				}else if(!_this.isReady){
					_this.el.trigger("countDown");
					_this.el.find(".countDown").hide();
				}
			}
			count();
		},
		update:function(data,info,userAvatarId){
			var _this=this;
			this.selfData=data;
			this.roomInfo=info;
			this.avatarId=data.avatarId;
			this.userAvatarId=userAvatarId;
			var html    = this.template(data);
			this.el=$(html);
			this.updateState();			
			
			this.el.find(".btn-ready").click(function(){
				_this.el.find(".countDown").hide();
				_this.isReady=true;
			})
		},
		updateState:function(){
			if(this.userAvatarId==this.selfData.avatarId){
				if(this.selfData.state=="READY"){
					this.el.find(".btn-already").css({"display":"block"});
					this.isReady=true;
				}else{
					this.el.find(".btn-ready").css({"display":"block"});
				}
			}else{
				if(this.selfData.state=="READY"){
					this.el.find(".btn-already").css({"display":"block"});
					this.isReady=true;
				}else{
					this.el.find(".btn-unready").css({"display":"block"});
				}			
			}

			//当前用户不是房主
			if(this.userAvatarId!=this.roomInfo.ownerId){
				this.el.find(".closeBtn").hide();
			}

			//列表用户是房主
			if(this.selfData.avatarId==this.roomInfo.ownerId){
				this.el.find(".ownerIcon").css({"display":"block"});
				this.el.find(".closeBtn").hide();
			}
		},
		ready:function(){
			this.el.find(".btn-unready").css({"display":"none"});
			this.el.find(".btn-ready").css({"display":"none"});
			this.el.find(".btn-already").css({"display":"block"});
			this.isReady=true;
		},
		remove:function(){			
			this.el.find(".btn-ready").unbind("click");
			this.el.remove();
		}
	},
	{
		NAME:'PlayerItem'
	}
)