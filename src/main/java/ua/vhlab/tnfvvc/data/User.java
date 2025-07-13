package ua.vhlab.tnfvvc.data;

import java.util.Set;

public class User {

    private String username;

    private String name;

    private String hashedPassword;

    private Set<Role> roles;

    private byte[] profilePicture;

    public User(String username, String name, String pwd, Set<Role> roles, byte[] profilePicture) {
        this.username = username;
        this.name = name;
        this.hashedPassword = pwd;
        this.roles = roles;
        this.profilePicture = profilePicture;
    }

    public User() {

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }

}
