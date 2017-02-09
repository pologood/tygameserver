puremvc.define({
        name: 'drawsomething.view.mediator.DrawPanelMediator',
        parent: puremvc.Mediator
    },
    {
        // Notifications this mediator is interested in 
        listNotificationInterests: function() {
            return [drawsomething.AppConstants.GAME_STARTING];
        },
        
        // Code to be executed when the Mediator instance is registered with the View
        onRegister: function() {
            this.setViewComponent( new drawsomething.view.component.DrawPanel);
            this.viewComponent.addEventListener( drawsomething.view.event.AppEvents.SEND_MSG, this );
            this.viewComponent.addEventListener(drawsomething.view.event.AppEvents.DRAWING,this);
        },
        
        // Handle events from the view component
        handleEvent: function ( event ) {
            switch( event.type ) {
                case drawsomething.view.event.AppEvents.SEND_MSG:
                    this.sendNotification( drawsomething.AppConstants.SEND_CHAT_MSG, event.msg );
                break;
                case drawsomething.view.event.AppEvents.DRAWING:
                    this.sendNotification(drawsomething.AppConstants.DRAWING,event.msg);
                break;
             }
            
        },
 
        // Handle notifications from other PureMVC actors
        handleNotification: function( note ) {
            switch ( note.getName() ) {
                case drawsomething.AppConstants.GAME_STARTING:
                    this.viewComponent.show();
                    this.viewComponent.roundStart(note.getBody());
                break;
            }
        },
    },
 
    // STATIC MEMBERS
    {
        NAME: 'DrawPanelMediator'
    }    
);
