package com.crossent.monitoring.portal.jpa.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.catalina.Server;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name = "mon_group", schema = "mondb")
public class MonGroup  implements Serializable {
    private Integer id;
    private String name;
    private String description;
    @JsonIgnore
    private Collection<ServerResource> serverResource;
    @JsonIgnore
    private Collection<AppResource> appResource;
    @JsonIgnore
    private Collection<User> users;
    @JsonIgnore
    private Collection<MgUser> mgUsers;


    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Basic
    @Column(name = "name", nullable = true, length = 100)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "description", nullable = true, length = 200)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "mg_server",
            joinColumns = @JoinColumn(name = "mon_group_id", referencedColumnName="id"),
            inverseJoinColumns = @JoinColumn(name = "server_resource_id", referencedColumnName="id"))
    public Collection<ServerResource> getServerResource() { return serverResource; }

    public void setServerResource(Collection<ServerResource> serverResource) { this.serverResource = serverResource; }


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "mg_app",
            joinColumns = @JoinColumn(name = "mon_group_id", referencedColumnName="id"),
            inverseJoinColumns = @JoinColumn(name = "app_resource_id", referencedColumnName="id"))
    public Collection<AppResource> getAppResource() { return appResource; }

    public void setAppResource(Collection<AppResource> appResource) { this.appResource = appResource; }


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "mg_user",
            joinColumns = @JoinColumn(name = "mon_group_id", referencedColumnName="id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName="id"))
    public Collection<User> getUsers() { return users; }

    public void setUsers(Collection<User> users) { this.users = users; }

    @OneToMany(mappedBy = "monGroup")
    public Collection<MgUser> getMgUsers() {
        return mgUsers;
    }


    public void setMgUsers(Collection<MgUser> mgUsers) {
        this.mgUsers = mgUsers;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MonGroupDto{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
