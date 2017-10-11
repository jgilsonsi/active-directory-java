package com.jjdev.activedirectory;

import com.jjdev.activedirectory.JActiveDirectory.JUser;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

/**
 *
 * @author jgilson
 */
public class JMain {

    public static void main(String[] args) {

        try {
            LdapContext context = JActiveDirectory.getConnection("TIAGO\\Administrador", "master", "192.168.9.144", null);
            System.out.println("Success connection!");

            System.out.println("\n====> User");
            JUser singleUser = JActiveDirectory.getUser(context, "tiago.local", "gilson");
            System.out.println(singleUser != null ? singleUser.getEmail() : "Not found");

            System.out.println("\n====> Users");
            JActiveDirectory.getUsers(context, "tiago.local").forEach(user
                    -> System.out.println(user.getEmail()));

            context.close();
        } catch (NamingException e) {
            System.out.println("Error: " + e);
        }

    }

}
