puremvc.define({
		name:'drawsomething.view.component.ReadyPanel',
		constructor:function(event){
			var _this=this;
			this.container=document.querySelector( '#readyPanel');
			this.$container=$('#readyPanel');
			this.$container.find(".startBtn").click(function(){
				_this.startGame();
			})
			this.$container.click(function(e){
				if($(e.target).hasClass("btn-ready")){
					_this.setReady();
				}else if($(e.target).hasClass("closeBtn")){
					var avatarId=$(e.target).attr("data-avatarId");
					_this.kickPlayer(avatarId);
				}
			})
			this.$container.bind("countDown",function(){
				_this.setReady();
			})
			this.items=[];
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
		initRoom:function(data){
			var members=data.info.members;
			for(var i=0;i<members.length;i++){
				var p=new drawsomething.view.component.PlayerItem();
				p.update(members[i],data.roominfo,data.avatarId);
				this.items.push(p);
				this.$container.find(".playerList").append(p.el);
				if(data.avatarId==members[i].avatarId&&data.avatarId!=data.roominfo.ownerId){
					p.startCountDown();
				}				
			}
			this.$container.find(".roomId").html("房间号："+data.info.id);
			if(data.roominfo.ownerId==data.avatarId){
				this.$container.find(".startBtn").show();
			}else{
				this.$container.find(".startBtn").hide();
			}
		},
		addPlayer:function(data){
			var p=new drawsomething.view.component.PlayerItem();
			p.update(data.member.avatar,data.roominfo,data.avatarId);
			this.items.push(p);
			this.$container.find(".playerList").append(p.el);
		},
		updateReadyInfo:function(data){
			var readyAvatarId=data.info.avatar.avatarId;
			for(var i=0;i<this.items.length;i++){
				if(this.items[i].avatarId==readyAvatarId){
					this.items[i].ready();
				}
			}
		},
		updateMembers:function(){
			
		},
		startGame:function(){
			var e = this.createEvent( drawsomething.view.event.AppEvents.START_GAME);
			e.msg={};
			this.dispatchEvent(e);
		},
		setReady:function(){
			var e = this.createEvent( drawsomething.view.event.AppEvents.READY);
			e.msg={};
			this.dispatchEvent(e);
		},
		kickPlayer:function(avatarId){			
			var e = this.createEvent( drawsomething.view.event.AppEvents.REMOVE_PLAYER);
			e.msg={
				avatarId:avatarId
			};
			this.dispatchEvent(e);
		},
		receiveRemovePlayer:function(removeInfo){
			for(var i=this.items.length-1;i>0;i--){
				if(removeInfo.avatarId==this.items[i].avatarId){
					this.items[i].remove();
					this.items.splice(i, 1);					
				}
			}
		},
		hide:function(){
			this.$container.hide();
		},
		show:function(){
			this.$container.show();
		}
	},
	{
		NAME:'ReadyPanel'
	}
)