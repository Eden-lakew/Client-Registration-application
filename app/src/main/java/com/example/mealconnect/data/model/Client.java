package com.example.mealconnect.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
public class Client {
    @SerializedName("id")
    private Long id;

    @SerializedName("clientId")
    private String clientId;

    @SerializedName("name")
    private String name;

    @SerializedName("phone")
    private String phone;

    @SerializedName("address")
    private String address;

    @SerializedName("householdSize")
    private Integer householdSize;

    @SerializedName("dietaryRestrictions")
    private String dietaryRestrictions;

    @SerializedName("authorizedPickupPerson")
    private String authorizedPickupPerson;

    @SerializedName("pickedUp")
    private Boolean pickedUp;

    @SerializedName("firstLanguage")
    private String firstLanguage;

    // 🌟 NEW QUALIFICATION FIELDS 🌟
    @SerializedName("isLowIncome")
    private Boolean isLowIncome;

    @SerializedName("isNewcomer")
    private Boolean isNewcomer;

    @SerializedName("isSenior")
    private Boolean isSenior;

    // 🌟 NEW HOUSEHOLD MEMBERS LIST 🌟
    @SerializedName("members")
    private List<HouseholdMember> members;

    public Client() {}

    public Client(String name, String phone, String address, String firstLanguage,
                  Boolean isLowIncome, Boolean isNewcomer, Boolean isSenior,
                  Integer householdSize, String dietaryRestrictions,
                  String authorizedPickupPerson, List<HouseholdMember> members) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.firstLanguage = firstLanguage;
        this.isLowIncome = isLowIncome;
        this.isNewcomer = isNewcomer;
        this.isSenior = isSenior;
        this.householdSize = householdSize;
        this.dietaryRestrictions = dietaryRestrictions;
        this.authorizedPickupPerson = authorizedPickupPerson;
        this.members = members;
        this.pickedUp = false;
    }

    // Getters and Setters (Updated with all new fields)

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getHouseholdSize() {
        return householdSize;
    }

    public void setHouseholdSize(Integer householdSize) {
        this.householdSize = householdSize;
    }

    public String getDietaryRestrictions() {
        return dietaryRestrictions;
    }

    public void setDietaryRestrictions(String dietaryRestrictions) {
        this.dietaryRestrictions = dietaryRestrictions;
    }

    public String getAuthorizedPickupPerson() {
        return authorizedPickupPerson;
    }

    public void setAuthorizedPickupPerson(String authorizedPickupPerson) {
        this.authorizedPickupPerson = authorizedPickupPerson;
    }

    public Boolean getPickedUp() {
        return pickedUp;
    }

    public void setPickedUp(Boolean pickedUp) {
        this.pickedUp = pickedUp;
    }

    // NEW GETTERS AND SETTERS
    public String getFirstLanguage() {
        return firstLanguage;
    }

    public void setFirstLanguage(String firstLanguage) {
        this.firstLanguage = firstLanguage;
    }

    public Boolean getLowIncome() {
        return isLowIncome;
    }

    public void setLowIncome(Boolean lowIncome) {
        isLowIncome = lowIncome;
    }

    public Boolean getNewcomer() {
        return isNewcomer;
    }

    public void setNewcomer(Boolean newcomer) {
        isNewcomer = newcomer;
    }

    public Boolean getSenior() {
        return isSenior;
    }

    public void setSenior(Boolean senior) {
        isSenior = senior;
    }

    public List<HouseholdMember> getMembers() {
        return members;
    }

    public void setMembers(List<HouseholdMember> members) {
        this.members = members;
    }
}
