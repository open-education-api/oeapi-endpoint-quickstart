package oeapi.payload;

import java.util.List;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import oeapi.model.Role;

import org.hibernate.validator.constraints.Length;

public class AuthRequest {

    @NotNull
    @Email
    @Length(min = 5, max = 90)
    private String email;

    @NotNull
    @Length(min = 5, max = 40)
    private String password;

    private List<Role> roles;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the roles
     */
    public List<Role> getRoles() {
        return roles;
    }

    /**
     * @param roles the roles to set
     */
    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

}
