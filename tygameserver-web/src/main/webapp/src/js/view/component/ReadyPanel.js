puremvc.define({
		name:'drawsomething.view.component.ReadyPanel',
		constructor:function(event){
			var _this=this;
			this.kickPlayerId;
			this.container=document.querySelector( '#readyPanel');
			this.$container=$('#readyPanel');
			this.$container.find(".startBtn").click(function(){
				if($(this).hasClass("gray")){
					return;
				}
				_this.startGame();
			})
			this.$container.click(function(e){
				if($(e.target).hasClass("btn-ready")){
					_this.setReady();
				}else if($(e.target).hasClass("closeBtn")){
					var avatarId=$(e.target).attr("data-avatarId");
					_this.kickPlayerId=avatarId;
					var member=_this.getMember(_this.members,avatarId);
					if(member!=null){
						_this.$confirmPop.find(".nickName").html(member.name);
					}
					_this.$confirmPop.show();
				}
			})
			this.$container.bind("countDown",function(){
				_this.setReady();
			})
			this.items=[];
			this.$confirmPop=this.$container.find(".confirmPop");
			this.$confirmPop.find(".cancelBtn").click(function(){
				_this.$confirmPop.hide();
			})
			this.$confirmPop.find(".confirmBtn").click(function(){
				_this.kickPlayer(_this.kickPlayerId);
			})
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
			this.roominfo=data.roominfo;
			this.avatarId=data.avatarId;
			this.items=[];
			this.$container.find(".item").empty();
			var members=data.info.members;
			this.members=members;
			for(var i=0;i<8;i++){
				var p=new drawsomething.view.component.PlayerItem(this.$container.find(".item").eq(i));			
				if(members[i]){
					p.setData(members[i],data.roominfo,data.avatarId);
					if(data.avatarId==members[i].avatarId&&data.avatarId!=data.roominfo.ownerId){
						p.startCountDown();
					}
				}else{
					p.setBlank();
				}
				this.items.push(p);
				// this.$container.find(".playerList").append(p.el);
			}

			this.$container.find(".roomId").html("房间号："+data.info.id);
			this.updateStartBtn();
		},
		addPlayer:function(data){
			var position=data.member.position;
			this.items[position].setData(data.member.avatar,data.roominfo,data.avatarId);
			this.updateStartBtn();
		},
		getMember:function(members,avatarId){
	        for(var i in members){
	            if(members[i].avatarId==avatarId){
	                return members[i];
	            }
	        }
	        return null;
	    },
		updateReadyInfo:function(data){
			var readyAvatarId=data.info.avatar.avatarId;
			for(var i=0;i<this.items.length;i++){
				if(this.items[i].avatarId==readyAvatarId){
					this.items[i].ready();
				}
			}
			this.updateStartBtn();
		},
		changeOwner:function(changeAvatarId){
			for(var i=0;i<this.items.length;i++){
				if(this.items[i].avatarId!=null){
					if(this.items[i].avatarId==changeAvatarId){
						this.items[i].selfData.state="READY";
					}
					this.items[i].updateState();
				}				
			}
			this.updateStartBtn();
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
			for(var i=this.items.length-1;i>=0;i--){
				if(removeInfo.avatarId==this.items[i].avatarId){
					this.items[i].remove();
					// this.items.splice(i, 1);					
				}
			}
			this.$container.find(".confirmPop").hide();
			this.updateStartBtn();
		},
		updateStartBtn:function(){
			var readyCount=0;
			var playerCount=0;
			for(var i=0;i<this.items.length;i++){
				if(this.items[i].isReady){
					readyCount++;
				}
				if(this.items[i].avatarId!=null){
					playerCount++;
				}
			}
			if(readyCount>=2&&readyCount==playerCount){
				this.$container.find(".startBtn").removeClass("gray");
			}else{
				this.$container.find(".startBtn").addClass("gray");
			}
			if(this.roominfo.ownerId==this.avatarId){
				this.$container.find(".startBtn").show();
			}else{
				this.$container.find(".startBtn").hide();
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