puremvc.define({name:"drawsomething.view.mediator.ConnectPanelMediator",parent:puremvc.Mediator},{listNotificationInterests:function(){return[drawsomething.AppConstants.CONNECT_SUCCESS]},onRegister:function(){this.setViewComponent(new drawsomething.view.component.ConnectPanel),this.viewComponent.addEventListener(drawsomething.view.event.AppEvents.CONNECT_SOCKET,this)},handleEvent:function(e){switch(e.type){case drawsomething.view.event.AppEvents.CONNECT_SOCKET:this.sendNotification(drawsomething.AppConstants.CONNECT_SOCKET,e.msg)}},handleNotification:function(e){switch(e.getName()){case drawsomething.AppConstants.CONNECT_SUCCESS:this.viewComponent.hide()}}},{NAME:"ConnectPanelMediator"});