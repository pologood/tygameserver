puremvc.define(
	{
		name:'drawsomething.view.component.PlayerItem',
		constructor:function(container){
			this.$container=container;
			var source=$("#playerItems-template").html();
			this.template = Handlebars.compile(source);	
			this.isReady=false;		
		}
	},
	{
		setBlank:function(){
			this.avatarId=null;
			var html    = this.template({});
			this.el=$(html);
			this.$container.empty();
			this.$container.append(this.el);
			this.$container.find(".name").hide();
			this.$container.find(".head").hide();
			this.$container.find(".closeBtn").hide();
		},
		setData:function(data,info,userAvatarId){
			var _this=this;
			this.selfData=data;
			this.roomInfo=info;
			this.avatarId=data.avatarId;
			this.userAvatarId=userAvatarId;
			var html    = this.template(data);
			this.el=$(html);
			this.$container.append(this.el);
			this.updateState();			
			
			this.$container.find(".btn-ready").click(function(){
				_this.el.find(".countDown").hide();
				_this.isReady=true;
			})
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
		updateState:function(){
			this.el.find(".closeBtn").show();

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
			this.isReady=false;
			this.avatarId=null;
		}
	},
	{
		NAME:'PlayerItem'
	}
)