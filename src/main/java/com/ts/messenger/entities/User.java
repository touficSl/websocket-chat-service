package com.ts.messenger.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

/**
 * The persistent class for the GUI_USER database table.
 * 
 */
@Entity
@Table(name = "utilisateur")
public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GenericGenerator(name = "maxUserId", strategy = "increment")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private long userId;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATE_CREATE_USER")
	private Date dateCreateUser;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATE_MODIFY_PWD")
	private Date dateModifyPwd;

	@Column(name = "DEBUG_MODE", insertable = false)
	private Boolean debugMode;

	@Column(name = "USER_ATTEMPS_CONNECTION")
	private BigDecimal userAttempsConnection;

	@Column(name = "email_address")
	private String userEmail;

	@Column(name = "enabled")
	private Boolean userEnabled;

	@Column(name = "first_name")
	private String userFirstname;

	@Column(name = "remember_me")
	private Boolean remember_me;

	@Column(name = "last_name")
	private String userLastname;

	@Column(name = "USER_LOCKED")
	private Boolean userLocked = false;

	@Column(name = "username")
	private String userLogin;

	@Column(name = "password")
	private String userPassword;

	@Column(name = "USER_PHOTO")
	private String userPhoto;

	@Column(name = "reset_token")
	private String resetToken;

	@Column(name = "confirmation_token")
	private String confirmationToken;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_CONNEXION_TIME")
	private Date lastConnexionTime;

	@Column(name = "my_request_active")
	private Integer myRequestActive;

	@Column(name = "language_id")
	private Integer languageId;
	
	@Column(name = "expiry_days")
	private String expiryDays;

	@Column(name = "expiry_date")
	private Date expirationDate;

	@Column(name = "USER_PHOTO_URL")
	private String userPhotoUrl;

	@Column(name = "USER_PHOTO_ID")
	private int userPhotoId;

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public User() {
	}

	public long getUserId() {
		return this.userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public Date getDateCreateUser() {
		return this.dateCreateUser;
	}

	public void setDateCreateUser(Date dateCreateUser) {
		this.dateCreateUser = dateCreateUser;
	}

	public Date getDateModifyPwd() {
		return this.dateModifyPwd;
	}

	public void setDateModifyPwd(Date dateModifyPwd) {
		this.dateModifyPwd = dateModifyPwd;
	}

	public Boolean getDebugMode() {
		return this.debugMode;
	}

	public void setDebugMode(Boolean debugMode) {
		this.debugMode = debugMode;
	}

	public String getUserPhoto() {
		return userPhoto;
	}

	public void setUserPhoto(String userPhoto) {
		this.userPhoto = userPhoto;
	}

	public BigDecimal getUserAttempsConnection() {
		return this.userAttempsConnection;
	}

	public void setUserAttempsConnection(BigDecimal userAttempsConnection) {
		this.userAttempsConnection = userAttempsConnection;
	}

	public String getUserEmail() {
		return this.userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public Boolean getUserEnabled() {
		return this.userEnabled;
	}

	public void setUserEnabled(Boolean userEnabled) {
		this.userEnabled = userEnabled;
	}

	public String getUserFirstname() {
		return this.userFirstname;
	}

	public void setUserFirstname(String userFirstname) {
		this.userFirstname = userFirstname;
	}

	public String getUserLastname() {
		return this.userLastname;
	}

	public void setUserLastname(String userLastname) {
		this.userLastname = userLastname;
	}

	public Boolean getUserLocked() {
		return this.userLocked;
	}

	public void setUserLocked(Boolean userLocked) {
		this.userLocked = userLocked;
	}

	public String getUserLogin() {
		return this.userLogin;
	}

	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}

	public String getUserPassword() {

		return this.userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public Date getLastConnexionTime() {
		return lastConnexionTime;
	}

	public void setLastConnexionTime(Date lastConnexionTime) {
		this.lastConnexionTime = lastConnexionTime;
	}

	public void setResetToken(String resetToken) {
		this.resetToken = resetToken;
	}

	public String getResetToken() {
		return resetToken;
	}

	public Boolean getRemember_me() {
		return remember_me;
	}

	public void setRemember_me(Boolean remember_me) {
		this.remember_me = remember_me;
	}

	public Integer getMyRequestActive() {
		return myRequestActive;
	}

	public void setMyRequestActive(Integer myRequestActive) {
		this.myRequestActive = myRequestActive;
	}

	public Integer getLanguageId() {
		return languageId;
	}

	public void setLanguageId(Integer languageId) {
		this.languageId = languageId;
	}
	
	public String getExpiryDays() {
		return expiryDays;
	}

	public void setExpiryDays(String expiryDays) {
		this.expiryDays = expiryDays;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (userId ^ (userId >>> 32));
		result = prime * result + ((userLogin == null) ? 0 : userLogin.toUpperCase().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (userId != other.userId)
			return false;
		if (userLogin == null) {
			if (other.userLogin != null)
				return false;
		} else if (!userLogin.equalsIgnoreCase(other.userLogin))
			return false;
		return true;
	}

	public String getUserPhotoUrl() {
		return userPhotoUrl;
	}

	public void setUserPhotoUrl(String userPhotoUrl) {
		this.userPhotoUrl = userPhotoUrl;
	}

	public int getUserPhotoId() {
		return userPhotoId;
	}

	public void setUserPhotoId(int userPhotoId) {
		this.userPhotoId = userPhotoId;
	}

	public String getConfirmationToken() {
		return confirmationToken;
	}

	public void setConfirmationToken(String confirmationToken) {
		this.confirmationToken = confirmationToken;
	}

}
