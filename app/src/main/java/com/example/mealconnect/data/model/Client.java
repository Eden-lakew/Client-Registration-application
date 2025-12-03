package com.example.mealconnect.data.model;

import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.List;

/**
 * Data class representing a registered client for the MealConnect service.
 * Matches Firestore fields exactly using @PropertyName.
 */
public class Client {

    @PropertyName("clientId")
    private String clientId;

    @PropertyName("name")
    private String name;

    @PropertyName("phone")
    private String phone;

    @PropertyName("address")
    private String address;

    @PropertyName("firstLanguage")
    private String firstLanguage;

    @PropertyName("authorizedPickup")
    private String authorizedPickup;

    @PropertyName("numAdults")
    private int numAdults;

    @PropertyName("numChildren")
    private int numChildren;

    @PropertyName("isLowIncome")
    private boolean isLowIncome;

    @PropertyName("isNewcomer")
    private boolean isNewcomer;

    @PropertyName("isSenior")
    private boolean isSenior;

    @PropertyName("dietaryRestrictions")
    private List<String> dietaryRestrictions;

    @PropertyName("householdMembers")
    private List<HouseholdMember> householdMembers;

    @PropertyName("siteCode")
    private String siteCode;

    @PropertyName("registrationTimestamp")
    private Long registrationTimestamp;

    // Required empty constructor for Firestore
    public Client() {
        this.dietaryRestrictions = new ArrayList<>();
        this.householdMembers = new ArrayList<>();
        this.siteCode = "";
        this.registrationTimestamp = 0L;
    }

    // ----- Getters / Setters -----

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

    public String getFirstLanguage() {
        return firstLanguage;
    }

    public void setFirstLanguage(String firstLanguage) {
        this.firstLanguage = firstLanguage;
    }

    public String getAuthorizedPickup() {
        return authorizedPickup;
    }

    public void setAuthorizedPickup(String authorizedPickup) {
        this.authorizedPickup = authorizedPickup;
    }

    public int getNumAdults() {
        return numAdults;
    }

    public void setNumAdults(int numAdults) {
        this.numAdults = numAdults;
    }

    public int getNumChildren() {
        return numChildren;
    }

    public void setNumChildren(int numChildren) {
        this.numChildren = numChildren;
    }

    @PropertyName("isLowIncome")
    public boolean isLowIncome() {
        return isLowIncome;
    }

    @PropertyName("isLowIncome")
    public void setLowIncome(boolean lowIncome) {
        this.isLowIncome = lowIncome;
    }

    @PropertyName("isNewcomer")
    public boolean isNewcomer() {
        return isNewcomer;
    }

    @PropertyName("isNewcomer")
    public void setNewcomer(boolean newcomer) {
        this.isNewcomer = newcomer;
    }

    @PropertyName("isSenior")
    public boolean isSenior() {
        return isSenior;
    }

    @PropertyName("isSenior")
    public void setSenior(boolean senior) {
        this.isSenior = senior;
    }

    public List<String> getDietaryRestrictions() {
        return dietaryRestrictions;
    }

    public void setDietaryRestrictions(List<String> dietaryRestrictions) {
        this.dietaryRestrictions = dietaryRestrictions != null ? dietaryRestrictions : new ArrayList<>();
    }

    public List<HouseholdMember> getHouseholdMembers() {
        return householdMembers;
    }

    public void setHouseholdMembers(List<HouseholdMember> householdMembers) {
        this.householdMembers = (householdMembers != null) ? householdMembers : new ArrayList<>();
    }

    public String getSiteCode() {
        return siteCode;
    }

    public void setSiteCode(String siteCode) {
        this.siteCode = siteCode != null ? siteCode : "";
    }

    public Long getRegistrationTimestamp() {
        return registrationTimestamp;
    }

    public void setRegistrationTimestamp(Long registrationTimestamp) {
        this.registrationTimestamp = registrationTimestamp != null ? registrationTimestamp : 0L;
    }
}
