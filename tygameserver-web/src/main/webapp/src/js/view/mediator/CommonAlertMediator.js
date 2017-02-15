puremvc.define({
        name: 'drawsomething.view.mediator.CommonAlertMediator',
        parent: puremvc.Mediator
    },
    {
        // Notifications this mediator is interested in 
        listNotificationInterests: function() {
            return [drawsomething.AppConstants.SHOW_ALERT];
        },
        
        // Code to be executed when the Mediator instance is registered with the View
        onRegister: function() {
            this.setViewComponent( new drawsomething.view.component.CommonAlert);
        },
        
        // Handle events from the view component
        handleEvent: function ( event ) {            
            switch( event.type ) {

             }
            
        },
 
        // Handle notifications from other PureMVC actors
        handleNotification: function( note ) {
            switch ( note.getName() ) {
                case drawsomething.AppConstants.SHOW_ALERT:
                    this.viewComponent.show(note.getBody());
                break;
            }
        },
    },
 
    // STATIC MEMBERS
    {
        NAME: 'CommonAlertMediator'
    }    
);
