package security;

public enum Permission {
    // Admin permissions
    ADD_PARKING_SPOT,
    REMOVE_PARKING_SPOT,
    VIEW_ALL_TICKETS,
    MANAGE_USERS,
    VIEW_REPORTS,
    
    // Entry Operator permissions
    ISSUE_TICKET,
    VIEW_FREE_SPOTS,
    
    // Exit Operator permissions
    PROCESS_EXIT,
    CALCULATE_PAYMENT,
    VIEW_TICKET
}
