package Securite;

import java.security.*;

public class ClientPolicy extends Policy {
    private Permissions perms;

    public ClientPolicy() {
        perms = new Permissions();
        perms.add(new java.security.AllPermission());
    }

    @Override
    public PermissionCollection getPermissions(CodeSource codesource) {
        return perms;
    }

    @Override
    public PermissionCollection getPermissions(ProtectionDomain domain) {
        return perms;
    }

    @Override
    public boolean implies(ProtectionDomain domain, Permission permission) {
        return perms.implies(permission);
    }
}
