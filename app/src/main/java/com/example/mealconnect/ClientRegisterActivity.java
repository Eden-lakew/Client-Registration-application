package com.example.mealconnect;

import com.example.mealconnect.data.ClientRepository;
import com.example.mealconnect.data.local.ClientEntity;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mealconnect.data.model.Client;
import com.example.mealconnect.data.model.HouseholdMember; // 🌟 NEW IMPORT
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClientRegisterActivity extends AppCompatActivity {
    // 1. Core Fields (Existing)
    private EditText etName, etPhone, etAddress, etAuthorizedPickup;
    private Button btnSubmit;
    private CheckBox cbNoChicken, cbNoBeef, cbNoPork, cbNoTurkey, cbNoSausages, cbNone;

    // 2. NEW Fields from XML
    private TextView tvApplicationDate;
    private EditText etFirstLanguage, etNumAdults, etNumChildren;
    private CheckBox cbLowIncome, cbNewcomer, cbSenior;
    private LinearLayout llHouseholdMembersContainer;

    // 🌟 FIELD: To store references to the dynamically added member views
    private List<HouseholdMemberView> memberViewsList = new ArrayList<>();

    private ClientRepository clientRepository;

    // 🌟 NESTED CLASS: To easily manage the fields for a single household member
    private static class HouseholdMemberView {
        final TextInputEditText etMemberFirstName;
        final TextInputEditText etMemberLastName;
        final TextInputEditText etMemberDOB;
        final TextInputEditText etMemberAllergy;
        final TextInputEditText etMemberContact;
        final String memberType;

        public HouseholdMemberView(TextInputEditText firstName, TextInputEditText lastName, TextInputEditText dob, TextInputEditText allergy, TextInputEditText contact, String type) {
            this.etMemberFirstName = firstName;
            this.etMemberLastName = lastName;
            this.etMemberDOB = dob;
            this.etMemberAllergy = allergy;
            this.etMemberContact = contact;
            this.memberType = type;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_client_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        clientRepository = new ClientRepository(this);

        // 2. UI INITIALIZATION (Binding the views)
        // Existing Bindings
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etAuthorizedPickup = findViewById(R.id.etAuthorizedPickup);
        btnSubmit = findViewById(R.id.btnSubmit);

        // NEW Bindings
        tvApplicationDate = findViewById(R.id.tvApplicationDate);
        etFirstLanguage = findViewById(R.id.etFirstLanguage);
        etNumAdults = findViewById(R.id.etNumAdults);
        etNumChildren = findViewById(R.id.etNumChildren);
        llHouseholdMembersContainer = findViewById(R.id.llHouseholdMembersContainer);

        // Checkbox Bindings
        cbNoChicken = findViewById(R.id.cbNoChicken);
        cbNoBeef = findViewById(R.id.cbNoBeef);
        cbNoPork = findViewById(R.id.cbNoPork);
        cbNoTurkey = findViewById(R.id.cbNoTurkey);
        cbNoSausages = findViewById(R.id.cbNoSausages);
        cbNone = findViewById(R.id.cbNone);
        cbLowIncome = findViewById(R.id.cbLowIncome);
        cbNewcomer = findViewById(R.id.cbNewcomer);
        cbSenior = findViewById(R.id.cbSenior);

        // Set the Application Date
        setApplicationDate();

        // Setup Logic
        setupCheckboxListeners();
        setupHouseholdSizeListeners();

        // 3. EVENT LISTENER
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForm()) {
                    try {
                        // 1. Collect Core and Qualification Data
                        String name = etName.getText().toString().trim();
                        String phone = etPhone.getText().toString().trim();
                        String address = etAddress.getText().toString().trim();
                        String firstLanguage = etFirstLanguage.getText().toString().trim();
                        String authorizedPickup = etAuthorizedPickup.getText().toString().trim();
                        String restrictions = getDietaryRestrictions();

                        // Qualification Status
                        Boolean isLowIncome = cbLowIncome.isChecked();
                        Boolean isNewcomer = cbNewcomer.isChecked();
                        Boolean isSenior = cbSenior.isChecked();

                        // Household size
                        int numAdults = getNumberFromEditText(etNumAdults);
                        int numChildren = getNumberFromEditText(etNumChildren);
                        Integer householdSize = numAdults + numChildren;

                        // 2. 🌟 Collect Household Member Data into List<HouseholdMember> 🌟
                        List<HouseholdMember> membersList = new ArrayList<>();
                        // We iterate through the list of View references (memberViewsList)
                        // and create actual data model objects (HouseholdMember)
                        for (HouseholdMemberView memberView : memberViewsList) {
                            HouseholdMember member = new HouseholdMember(
                                    memberView.memberType, // "Adult" or "Child"
                                    memberView.etMemberFirstName.getText().toString().trim(),
                                    memberView.etMemberLastName.getText().toString().trim(),
                                    memberView.etMemberDOB.getText().toString().trim(),
                                    memberView.etMemberAllergy.getText().toString().trim(),
                                    memberView.etMemberContact.getText().toString().trim()
                            );
                            membersList.add(member);
                        }


                        // 3. 🌟 Instantiate the Client Object with ALL fields (using the new constructor) 🌟
                        Client newClient = new Client(
                                name,
                                phone,
                                address,
                                firstLanguage,       // NEW
                                isLowIncome,         // NEW
                                isNewcomer,          // NEW
                                isSenior,            // NEW
                                householdSize,
                                restrictions,
                                authorizedPickup,
                                membersList          // NEW
                        );
                        ClientEntity entity = new ClientEntity();
                        entity.name = newClient.getName();
                        entity.phone = newClient.getPhone();
                        entity.address = newClient.getAddress();
                        entity.firstLanguage = newClient.getFirstLanguage();
                        entity.lowIncome = isLowIncome;
                        entity.newcomer = isNewcomer;
                        entity.senior = isSenior;
                        entity.numAdults = numAdults;
                        entity.numChildren = numChildren;
                        entity.dietaryRestrictions = restrictions;
                        entity.authorizedPickup = authorizedPickup;

                        // You already built the application date text:
                        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(new Date());
                        entity.dateApplied = date;

                        // Hardcode a site code for now (e.g. Surrey)
                        String siteCode = "SRY";     // or "SITE_A", etc.
                        entity.siteCode = siteCode;

                        // This actually inserts into Room in a background thread
                        clientRepository.addClient(entity, siteCode);

                        // Success message
                        Toast.makeText(ClientRegisterActivity.this,
                                "Client saved! ID will show in list screen.",
                                Toast.LENGTH_LONG).show();
                        finish();

                        // TODO: Implement API call or database insertion here
                    } catch (NumberFormatException e) {
                        Toast.makeText(ClientRegisterActivity.this, "Error: Household numbers must be valid integers.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    // ===================================
    // DYNAMIC VIEW CREATION (UNCHANGED)
    // ===================================

    private void setupHouseholdSizeListeners() {
        // ... (unchanged)
        TextWatcher householdWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                llHouseholdMembersContainer.postDelayed(ClientRegisterActivity.this::generateHouseholdMemberFields, 100);
            }
        };

        etNumAdults.addTextChangedListener(householdWatcher);
        etNumChildren.addTextChangedListener(householdWatcher);
    }

    /**
     * Helper to create a TextInputLayout containing a TextInputEditText
     */
    private TextInputLayout createInputLayout(Context context, String hint, int inputType) {
        // Layout Params for the TextInputLayout
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.bottomMargin = (int) (8 * context.getResources().getDisplayMetrics().density); // 8dp bottom margin

        // 1. Create TextInputLayout (The container for the hint/floating label)
        TextInputLayout textInputLayout = new TextInputLayout(context);
        textInputLayout.setLayoutParams(layoutParams);
        textInputLayout.setHint(hint);

        // 2. Create TextInputEditText (The actual input field)
        TextInputEditText editText = new TextInputEditText(context);
        // Set match_parent for width since it's inside the TextInputLayout
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        editText.setInputType(inputType);

        // 3. Add EditText to the Layout
        textInputLayout.addView(editText);
        return textInputLayout;
    }

    /**
     * Dynamically generates all EditText fields for each household member.
     */
    private void generateHouseholdMemberFields() {
        llHouseholdMembersContainer.removeAllViews();
        memberViewsList.clear();

        int numAdults = getNumberFromEditText(etNumAdults);
        int numChildren = getNumberFromEditText(etNumChildren);
        int totalMembers = numAdults + numChildren;

        if (totalMembers == 0) return;

        Context context = this;

        for (int i = 0; i < totalMembers; i++) {
            boolean isAdult = i < numAdults;
            String memberType = isAdult ? "Adult" : "Child";
            int memberIndex = i + 1;

            // 1. Add a Section Header
            TextView header = new TextView(context);
            header.setText(String.format("\n👤 Member %d: %s Details", memberIndex, memberType));
            header.setTextSize(16);
            header.setTypeface(null, android.graphics.Typeface.BOLD);
            llHouseholdMembersContainer.addView(header);

            // 2. Create all the input fields dynamically
            // First Name
            TextInputLayout firstNameLayout = createInputLayout(context, "Member First Name", InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            llHouseholdMembersContainer.addView(firstNameLayout);
            TextInputEditText etFirstName = (TextInputEditText) firstNameLayout.getEditText();

            // Last Name
            TextInputLayout lastNameLayout = createInputLayout(context, "Member Last Name", InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            llHouseholdMembersContainer.addView(lastNameLayout);
            TextInputEditText etLastName = (TextInputEditText) lastNameLayout.getEditText();

            // DOB (using text for simplicity, could add a Date Picker later)
            TextInputLayout dobLayout = createInputLayout(context, "Member DOB (YYYY-MM-DD)", InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
            llHouseholdMembersContainer.addView(dobLayout);
            TextInputEditText etDOB = (TextInputEditText) dobLayout.getEditText();

            // Allergy
            TextInputLayout allergyLayout = createInputLayout(context, "Member Allergy/Notes", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            llHouseholdMembersContainer.addView(allergyLayout);
            TextInputEditText etAllergy = (TextInputEditText) allergyLayout.getEditText();

            // Contact Number (Optional, only useful for adults or older children)
            TextInputLayout contactLayout = createInputLayout(context, "Member Contact Number (Optional)", InputType.TYPE_CLASS_PHONE);
            llHouseholdMembersContainer.addView(contactLayout);
            TextInputEditText etContact = (TextInputEditText) contactLayout.getEditText();

            // Add a visual separator (similar to the View in your original member detail XML)
            View separator = new View(context);
            LinearLayout.LayoutParams separatorParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) (1 * context.getResources().getDisplayMetrics().density) // 1dp height
            );
            separatorParams.topMargin = (int) (8 * context.getResources().getDisplayMetrics().density); // 8dp top margin
            separator.setLayoutParams(separatorParams);
            separator.setBackgroundColor(0xFFCCCCCC); // Light gray color
            llHouseholdMembersContainer.addView(separator);


            // 3. Store the member view references in the list
            HouseholdMemberView member = new HouseholdMemberView(etFirstName, etLastName, etDOB, etAllergy, etContact, memberType);
            memberViewsList.add(member);
        }
    }

    private int getNumberFromEditText(EditText editText) {
        String text = editText.getText().toString().trim();
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }

    // ===================================
    // HELPER METHODS (UNCHANGED)
    // ===================================
    private void setApplicationDate() {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        tvApplicationDate.setText(String.format("Application Date: %s", date));
    }

    private void setupCheckboxListeners() {
        cbNone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onNoneChecked(isChecked);
        });

        View.OnClickListener restrictionListener = v -> {
            if (((CheckBox) v).isChecked() && cbNone.isChecked()) {
                cbNone.setChecked(false);
                onNoneChecked(false);
            }
        };

        cbNoChicken.setOnClickListener(restrictionListener);
        cbNoBeef.setOnClickListener(restrictionListener);
        cbNoPork.setOnClickListener(restrictionListener);
        cbNoTurkey.setOnClickListener(restrictionListener);
        cbNoSausages.setOnClickListener(restrictionListener);
    }

    private void onNoneChecked(boolean isChecked) {
        CheckBox[] restrictionBoxes = {cbNoChicken, cbNoBeef, cbNoPork, cbNoTurkey, cbNoSausages};

        for (CheckBox cb : restrictionBoxes) {
            if (isChecked) {
                cb.setChecked(false);
                cb.setEnabled(false);
            } else {
                cb.setEnabled(true);
            }
        }
    }

    private String getDietaryRestrictions() {
        StringBuilder restrictions = new StringBuilder();

        if (cbNone.isChecked()) {
            return "None";
        }

        if (cbNoChicken.isChecked()) restrictions.append("No Chicken, ");
        if (cbNoBeef.isChecked()) restrictions.append("No Beef, ");
        if (cbNoPork.isChecked()) restrictions.append("No Pork, ");
        if (cbNoTurkey.isChecked()) restrictions.append("No Turkey, ");
        if (cbNoSausages.isChecked()) restrictions.append("No Sausages, ");

        if (restrictions.length() > 0) {
            restrictions.setLength(restrictions.length() - 2);
        } else {
            return "None Specified";
        }

        return restrictions.toString();
    }
    // ===================================

    // ===================================
    // VALIDATION (UNCHANGED)
    // ===================================

    private boolean validateForm() {
        // Basic Client Info
        if (etName.getText().toString().trim().isEmpty()) {
            etName.setError("Name is required");
            return false;
        }
        if (etPhone.getText().toString().trim().isEmpty()) {
            etPhone.setError("Phone is required");
            return false;
        }
        if (etAddress.getText().toString().trim().isEmpty()) {
            etAddress.setError("Address is required");
            return false;
        }
        if (etFirstLanguage.getText().toString().trim().isEmpty()) {
            etFirstLanguage.setError("First Language is required");
            return false;
        }
        if (etAuthorizedPickup.getText().toString().trim().isEmpty()) {
            etAuthorizedPickup.setError("Authorized pickup person is required");
            return false;
        }

        // Household Member Counts
        if (getNumberFromEditText(etNumAdults) + getNumberFromEditText(etNumChildren) == 0) {
            etNumAdults.setError("Household must have at least one member");
            return false;
        }

        // Validation logic for all dynamically created household member fields
        for (HouseholdMemberView member : memberViewsList) {
            if (member.etMemberFirstName.getText().toString().trim().isEmpty()) {
                member.etMemberFirstName.setError(member.memberType + "'s first name is required");
                return false;
            }
            if (member.etMemberLastName.getText().toString().trim().isEmpty()) {
                member.etMemberLastName.setError(member.memberType + "'s last name is required");
                return false;
            }
            if (member.etMemberDOB.getText().toString().trim().isEmpty()) {
                member.etMemberDOB.setError(member.memberType + "'s date of birth is required");
                return false;
            }
        }

        return true;
    }
}