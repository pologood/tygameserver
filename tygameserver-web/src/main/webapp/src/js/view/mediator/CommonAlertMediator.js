puremvc.define({
        name: 'drawsomething.view.mediator.CommonAlertMediator',
        parent: puremvc.Mediator
    },
    {
        // Notifications this mediator is interested in 
        listNotificationInterests: function() {
            return [];
        },
        
        // Code to be executed when the Mediator instance is registered with the View
        onRegister: function() {
            this.setViewComponent( new drawsomething.view.component.ConnectPanel);
        },
        
        // Handle events from the view component
        handleEvent: function ( event ) {            
            switch( event.type ) {

             }
            
        },
 
        // Handle notifications from other PureMVC actors
        handleNotification: function( note ) {
            switch ( note.getName() ) {
                
            }
        },
    },
 
    // STATIC MEMBERS
    {
        NAME: 'CommonAlertMediator'
    }    
);
