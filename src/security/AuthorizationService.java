package security;

import model.Role;
import model.User;

import java.util.*;

public class AuthorizationService {
    private static AuthorizationService instance;
    private Map<String, Set<Permission>> rolePermissions;
    private User currentUser;

    private AuthorizationService() {
        rolePermissions = new HashMap<>();
        initializePermissions();
    }

    public static AuthorizationService getInstance() {
        if (instance == null) {
            instance = new AuthorizationService();
        }
        return instance;
    }

    private void initializePermissions() {
        // Admin permissions
        Set<Permission> adminPerms = new HashSet<>(Arrays.asList(
            Permission.ADD_PARKING_SPOT,
            Permission.REMOVE_PARKING_SPOT,
            Permission.VIEW_ALL_TICKETS,
            Permission.MANAGE_USERS,
            Permission.VIEW_REPORTS,
            Permission.VIEW_FREE_SPOTS,
            Permission.VIEW_TICKET
        ));
        rolePermissions.put(Role.ADMIN, adminPerms);

        // Entry Operator permissions
        Set<Permission> entryPerms = new HashSet<>(Arrays.asList(
            Permission.ISSUE_TICKET,
            Permission.VIEW_FREE_SPOTS
        ));
        rolePermissions.put(Role.ENTRY_OPERATOR, entryPerms);

        // Exit Operator permissions
        Set<Permission> exitPerms = new HashSet<>(Arrays.asList(
            Permission.PROCESS_EXIT,
            Permission.CALCULATE_PAYMENT,
            Permission.VIEW_TICKET
        ));
        rolePermissions.put(Role.EXIT_OPERATOR, exitPerms);
    }

    public void login(User user) {
        this.currentUser = user;
        System.out.println("User " + user.getName() + " logged in with role: " + user.getRole());
    }

    public void logout() {
        if (currentUser != null) {
            System.out.println("User " + currentUser.getName() + " logged out");
            currentUser = null;
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean hasPermission(Permission permission) {
        if (currentUser == null) {
            System.out.println("Access Denied: No user logged in");
            return false;
        }

        Set<Permission> permissions = rolePermissions.get(currentUser.getRole());
        if (permissions != null && permissions.contains(permission)) {
            return true;
        }

        System.out.println("Access Denied: User " + currentUser.getName() + 
                         " does not have permission " + permission);
        return false;
    }

    public boolean checkPermission(Permission permission) throws UnauthorizedException {
        if (!hasPermission(permission)) {
            throw new UnauthorizedException("User does not have permission: " + permission);
        }
        return true;
    }
}
