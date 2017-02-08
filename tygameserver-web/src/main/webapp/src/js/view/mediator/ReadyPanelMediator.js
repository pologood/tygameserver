puremvc.define({
        name: 'drawsomething.view.mediator.ReadyPanelMediator',
        parent: puremvc.Mediator
    },
    {
        // Notifications this mediator is interested in 
        listNotificationInterests: function() {
            return [drawsomething.AppConstants.BROADCAST_ROOMINFO,drawsomething.AppConstants.BROADCAST_JOIN];
        },
        
        // Code to be executed when the Mediator instance is registered with the View
        onRegister: function() {
            this.setViewComponent( new drawsomething.view.component.ReadyPanel);
            // this.viewComponent.addEventListener( drawsomething.view.event.AppEvents.CONNECT_SOCKET, this );
            this.viewComponent.addEventListener(drawsomething.view.event.AppEvents.READY,this);
        },
        
        // Handle events from the view component
        handleEvent: function ( event ) {            
            switch( event.type ) {
                // case drawsomething.view.event.AppEvents.CONNECT_SOCKET:
                    // this.sendNotification( drawsomething.AppConstants.CONNECT_SOCKET, event.msg );
                // break;
                case drawsomething.view.event.AppEvents.READY:
                    this.sendNotification(drawsomething.AppConstants.READY,event.msg);
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
            }
        },
    },
 
    // STATIC MEMBERS
    {
        NAME: 'ReadyPanelMediator'
    }    
);
