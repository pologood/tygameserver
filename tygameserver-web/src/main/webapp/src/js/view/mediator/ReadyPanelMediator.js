puremvc.define({
        name: 'drawsomething.view.mediator.ReadyPanelMediator',
        parent: puremvc.Mediator
    },
    {
        // Notifications this mediator is interested in 
        listNotificationInterests: function() {
            return [
                drawsomething.AppConstants.BROADCAST_ROOMINFO,
                drawsomething.AppConstants.BROADCAST_JOIN,
                drawsomething.AppConstants.BROADCAST_READY,
                drawsomething.AppConstants.RECEIVE_REMOVE_PLAYER,
                drawsomething.AppConstants.GAME_OVER,
                drawsomething.AppConstants.CHANGE_OWNER,
            ];
        },
        
        // Code to be executed when the Mediator instance is registered with the View
        onRegister: function() {
            this.setViewComponent( new drawsomething.view.component.ReadyPanel);
            // this.viewComponent.addEventListener( drawsomething.view.event.AppEvents.CONNECT_SOCKET, this );
            this.viewComponent.addEventListener(drawsomething.view.event.AppEvents.READY,this);
            this.viewComponent.addEventListener(drawsomething.view.event.AppEvents.START_GAME,this);
            this.viewComponent.addEventListener(drawsomething.view.event.AppEvents.REMOVE_PLAYER,this);
        },
        
        // Handle events from the view component
        handleEvent: function ( event ) {            
            switch( event.type ) {
                case drawsomething.view.event.AppEvents.READY:
                    this.sendNotification(drawsomething.AppConstants.READY,event.msg);
                break;
                case drawsomething.view.event.AppEvents.START_GAME:
                    this.sendNotification(drawsomething.AppConstants.START_GAME,event.msg);
                break;
                case drawsomething.view.event.AppEvents.REMOVE_PLAYER:
                    this.sendNotification(drawsomething.AppConstants.REMOVE_PLAYER,event.msg);
                break;
             }
            
        },
 
        // Handle notifications from other PureMVC actors
        handleNotification: function( note ) {
            switch ( note.getName() ) {
                case drawsomething.AppConstants.BROADCAST_ROOMINFO:
                    this.viewComponent.show();
                    this.viewComponent.initRoom(note.getBody());
                break;
                case drawsomething.AppConstants.BROADCAST_JOIN:
                    this.viewComponent.addPlayer(note.getBody());
                break;
                case drawsomething.AppConstants.BROADCAST_READY:
                    this.viewComponent.updateReadyInfo(note.getBody());
                break;
                case drawsomething.AppConstants.RECEIVE_REMOVE_PLAYER:
                    this.viewComponent.receiveRemovePlayer(note.getBody());
                break;
                case drawsomething.AppConstants.GAME_OVER:
                    this.viewComponent.show();
                break;
                case drawsomething.AppConstants.CHANGE_OWNER:
                    this.viewComponent.changeOwner(note.getBody());
                break;
            }
        },
    },
 
    // STATIC MEMBERS
    {
        NAME: 'ReadyPanelMediator'
    }    
);
