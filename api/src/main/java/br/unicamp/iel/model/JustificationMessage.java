package br.unicamp.iel.model;
// Generated Jul 29, 2014 3:21:28 PM by Hibernate Tools 3.2.2.GA


import java.util.Date;

/**
 * JustificationMessage generated by hbm2java
 */
public class JustificationMessage  implements java.io.Serializable {


     private Long id;
     private Date dateSent;
     private String message;
     private String user;
     private Justification justification;

    public JustificationMessage() {
    }

	
    public JustificationMessage(Date dateSent, Justification justification) {
        this.dateSent = dateSent;
        this.justification = justification;
    }
    public JustificationMessage(Date dateSent, String message, String user, Justification justification) {
       this.dateSent = dateSent;
       this.message = message;
       this.user = user;
       this.justification = justification;
    }
   
    public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    public Date getDateSent() {
        return this.dateSent;
    }
    
    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent;
    }
    public String getMessage() {
        return this.message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    public String getUser() {
        return this.user;
    }
    
    public void setUser(String user) {
        this.user = user;
    }
    public Justification getJustification() {
        return this.justification;
    }
    
    public void setJustification(Justification justification) {
        this.justification = justification;
    }




}


