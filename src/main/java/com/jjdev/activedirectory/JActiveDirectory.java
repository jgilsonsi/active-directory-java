package com.jjdev.activedirectory;

import java.util.ArrayList;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import static javax.naming.directory.SearchControls.SUBTREE_SCOPE;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.InitialLdapContext;

public class JActiveDirectory {

    private static final String[] userAttributes = {
        "distinguishedName", "cn", "sn", "givenName", "memberOf",
        "sAMAccountName", "userPrincipalName", "mail"
    };

    private JActiveDirectory() {
    }

    public static LdapContext getConnection(String userName, String password, String serverAddress) {

        System.out.println("\nAuthenticating " + userName + "@" + serverAddress);

        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        props.put(Context.SECURITY_AUTHENTICATION, "simple");
        props.put(Context.SECURITY_PRINCIPAL, userName);
        props.put(Context.SECURITY_CREDENTIALS, password);
        props.put(Context.PROVIDER_URL, "ldap://" + serverAddress + "/");

        try {
            return new InitialLdapContext(props, null);
        } catch (javax.naming.CommunicationException e) {
            System.out.println("Failed to connect to " + serverAddress + "\n" + e);
        } catch (NamingException e) {
            System.out.println("Failed to authenticate " + userName + "@" + serverAddress + "\n" + e);
        }
        return null;
    }

    public static JUser getUser(LdapContext context, String domainName, String userName) {

        String principalName = userName + "@" + domainName;
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SUBTREE_SCOPE);
        controls.setReturningAttributes(userAttributes);
        try {
            NamingEnumeration<SearchResult> answer = context.search(toDC(domainName),
                    "(& (userPrincipalName=" + principalName + ")(objectClass=user))", controls);
            if (answer.hasMore()) {
                Attributes attr = ((SearchResult) answer.next()).getAttributes();
                Attribute user = attr.get("userPrincipalName");
                if (user != null) {
                    return new JUser(attr);
                }
            }
        } catch (NamingException e) {
        }
        return null;
    }

    public static ArrayList<JUser> getUsers(LdapContext context, String domainName) {

        ArrayList<JUser> users = new ArrayList<>();
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SUBTREE_SCOPE);
        controls.setReturningAttributes(userAttributes);
        try {
            NamingEnumeration answer = context.search(toDC(domainName),
                    "(objectClass=user)", controls);
            while (answer.hasMore()) {
                Attributes attr = ((SearchResult) answer.next()).getAttributes();
                Attribute user = attr.get("userPrincipalName");
                if (user != null) {
                    users.add(new JUser(attr));
                }
            }
        } catch (NamingException e) {
        }
        return users;
    }

    private static String toDC(String domainName) {
        StringBuilder buf = new StringBuilder();
        for (String token : domainName.split("\\.")) {
            if (token.length() == 0) {
                continue;
            }
            if (buf.length() > 0) {
                buf.append(",");
            }
            buf.append("DC=").append(token);
        }
        return buf.toString();
    }

    public static class JUser {

        private final String distinguishedName;
        private final String userPrincipal;
        private final String commonName;
        private final String email;

        public JUser(Attributes attr) throws javax.naming.NamingException {
            distinguishedName = attr.get("distinguishedName") != null ? (String) attr.get("distinguishedName").get() : "";
            userPrincipal = attr.get("userPrincipalName") != null ? (String) attr.get("userPrincipalName").get() : "";
            commonName = attr.get("cn") != null ? (String) attr.get("cn").get() : "";
            email = attr.get("mail") != null ? (String) attr.get("mail").get() : "";
        }

        public String getUserPrincipal() {
            return userPrincipal;
        }

        public String getCommonName() {
            return commonName;
        }

        public String getDistinguishedName() {
            return distinguishedName;
        }

        public String getEmail() {
            return email;
        }

        @Override
        public String toString() {
            return "distinguishedName=" + distinguishedName + "\nuserPrincipal=" + userPrincipal
                    + "\ncommonName=" + commonName + "\nemail=" + email;
        }

    }
}
