package com.example.mealconnect;

// Existing imports
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import com.example.mealconnect.ui.login.ClientRegistrationViewModel;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

// NEW/UPDATED Imports for Firestore Architecture
import com.example.mealconnect.R;
import com.example.mealconnect.data.Result;
import com.example.mealconnect.data.model.Client;
import com.example.mealconnect.data.model.HouseholdMember;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClientRegisterActivity extends AppCompatActivity {

    private static final String TAG = "ClientRegisterActivity";
    // ViewModel Instance
    private ClientRegistrationViewModel viewModel;

    // 1. Core Fields (Existing)
    private EditText etName, etPhone, etAddress, etAuthorizedPickup, etSiteCode;
    private Button btnSubmit;
    private ProgressBar progressBar;

    private CheckBox cbNoChicken, cbNoBeef, cbNoPork, cbNoTurkey, cbNoSausages, cbNone;

    // 2.Fields from XML
    private TextView tvApplicationDate;
    private EditText etFirstLanguage, etNumAdults, etNumChildren;
    private CheckBox cbLowIncome, cbNewcomer, cbSenior;
    private LinearLayout llHouseholdMembersContainer;

    // NEW: QR Code UI Elements
    private LinearLayout qrCodeContainer;
    private TextView tvNewClientId;
    private ImageView ivQrCode;
    private Button btnPrintQr;
    private Button btnClose;

    private Bitmap currentQrBitmap; // Store bitmap to share


    // FIELD: To store references to the dynamically added member views
    private List<HouseholdMemberView> memberViewsList = new ArrayList<>();

    // NESTED CLASS: To easily manage the fields for a single household member
    private static class HouseholdMemberView {
        final TextInputEditText etMemberFirstName;
        final TextInputEditText etMemberLastName;
        final TextInputEditText etMemberDOB;    // Date of Birth
        final TextInputEditText etMemberAllergy; // Allergy Notes
        final TextInputEditText etMemberContact; // Contact Number
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

        // NEW: Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ClientRegistrationViewModel.class);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_client_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 2. UI INITIALIZATION (Binding the views)
        // Existing Bindings
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etAuthorizedPickup = findViewById(R.id.etAuthorizedPickup);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);

        // NEW Bindings
        tvApplicationDate = findViewById(R.id.tvApplicationDate);
        etFirstLanguage = findViewById(R.id.etFirstLanguage);
        etNumAdults = findViewById(R.id.etNumAdults);
        etNumChildren = findViewById(R.id.etNumChildren);
        llHouseholdMembersContainer = findViewById(R.id.llHouseholdMembersContainer);

        // *** NEW: SITE CODE FIELD INITIALIZATION ***
        etSiteCode = findViewById(R.id.etSiteCode);

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

        // NEW: QR Code Bindings
        qrCodeContainer = findViewById(R.id.qrCodeContainer);
        tvNewClientId = findViewById(R.id.tvNewClientId);
        ivQrCode = findViewById(R.id.ivQrCode);
        btnPrintQr = findViewById(R.id.btnPrintQr);
        btnClose = findViewById(R.id.btnClose);


        // Set the Application Date
        setApplicationDate();

        // Setup Logic
        setupCheckboxListeners();
        setupHouseholdSizeListeners();

        // NEW: Observe registration result from ViewModel
        viewModel.getRegistrationStatus().observe(this, result -> {
            progressBar.setVisibility(View.GONE); // Hide loading indicator

            if (result instanceof Result.Success) {
                // SUCCESS
                String generatedId = viewModel.getLastRegisteredClientId();

                if (generatedId != null) {
                    // Use the input fields directly to show context since they haven't been cleared yet
                    String clientName = etName.getText().toString();
                    showSuccessQr(generatedId, clientName);
                } else {
                    Toast.makeText(this, "Client Registered, but ID unavailable for QR.", Toast.LENGTH_SHORT).show();
                    finish();
                }

            } else if (result instanceof Result.Error) {
                btnSubmit.setEnabled(true); // Re-enable submit
                String errorMsg = ((Result.Error) result).getError().getLocalizedMessage();
                Toast.makeText(this, "Registration FAILED: " + errorMsg, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Registration failed: " + errorMsg);
            }
        });

        btnClose.setOnClickListener(v -> finish());

        // NEW: Share/Print Button Logic
        btnPrintQr.setOnClickListener(v -> shareQrCode());

        // Set Toolbar as Action Bar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Register Client");
            }
        }

        // Handle hardware back button
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish(); // Go back to previous screen
            }
        });

        // 3. EVENT LISTENER
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForm()) {

                    // --- UNIQUE ID LOGIC INTEGRATION START ---
                    String siteCode = etSiteCode.getText().toString().trim();
                    if (siteCode.isEmpty()) {
                        Toast.makeText(ClientRegisterActivity.this, "Site Code is required to generate Client ID.", Toast.LENGTH_SHORT).show();
                        etSiteCode.setError("Required");
                        return; // Stop submission
                    }
                    // --- UNIQUE ID LOGIC INTEGRATION END ---

                    // Hide keyboard
                    hideKeyboard();

                    btnSubmit.setEnabled(false); // Disable button during submission
                    progressBar.setVisibility(View.VISIBLE); // Show loading indicator

                    try {
                        // 1. Collect Core and Qualification Data
                        String name = etName.getText().toString().trim();
                        String phone = etPhone.getText().toString().trim();
                        String address = etAddress.getText().toString().trim();
                        String firstLanguage = etFirstLanguage.getText().toString().trim();
                        String authorizedPickup = etAuthorizedPickup.getText().toString().trim();
                        List<String> dietaryRestrictions = getDietaryRestrictionsAsList();

                        // Qualification Status
                        Boolean isLowIncome = cbLowIncome.isChecked();
                        Boolean isNewcomer = cbNewcomer.isChecked();
                        Boolean isSenior = cbSenior.isChecked();

                        // Household size
                        int numAdults = getNumberFromEditText(etNumAdults);
                        int numChildren = getNumberFromEditText(etNumChildren);

                        // Collect Household Member Data into List<HouseholdMember>
                        List<HouseholdMember> membersList = new ArrayList<>();
                        for (HouseholdMemberView memberView : memberViewsList) {
                            HouseholdMember member = new HouseholdMember(
                                    memberView.memberType, // 1. type
                                    memberView.etMemberFirstName.getText().toString().trim(), // 2. firstName
                                    memberView.etMemberLastName.getText().toString().trim(),  // 3. lastName
                                    memberView.etMemberDOB.getText().toString().trim(),       // 4. dateOfBirth
                                    memberView.etMemberAllergy.getText().toString().trim(),   // 5. allergyNotes
                                    memberView.etMemberContact.getText().toString().trim()    // 6. contactNumber
                            );
                            membersList.add(member);
                        }

                        // 3. Instantiate the Firestore Client Object using setters
                        Client newClient = new Client();
                        // Set fields matching the Client.java model
                        newClient.setName(name);
                        newClient.setPhone(phone);
                        newClient.setAddress(address);
                        newClient.setFirstLanguage(firstLanguage);
                        newClient.setAuthorizedPickup(authorizedPickup);
                        newClient.setNumAdults(numAdults);
                        newClient.setNumChildren(numChildren);
                        newClient.setLowIncome(isLowIncome);
                        newClient.setNewcomer(isNewcomer);
                        newClient.setSenior(isSenior);
                        newClient.setDietaryRestrictions(dietaryRestrictions);
                        newClient.setHouseholdMembers(membersList);

                        // 4. Submit to ViewModel for unique ID generation and Firestore saving
                        viewModel.registerClient(newClient, siteCode);

                    } catch (NumberFormatException e) {
                        Toast.makeText(ClientRegisterActivity.this, "Error: Household numbers must be valid integers.", Toast.LENGTH_SHORT).show();
                        btnSubmit.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Go back to previous screen
        return true;
    }

    // ===================================
    // NEW: QR CODE GENERATION & DISPLAY
    // ===================================

    private void showSuccessQr(String clientId, String clientName) {
        // 1. Hide Form
        btnSubmit.setVisibility(View.GONE);

        // 2. Generate QR Bitmap
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            currentQrBitmap = barcodeEncoder.encodeBitmap(clientId, BarcodeFormat.QR_CODE, 400, 400);
            ivQrCode.setImageBitmap(currentQrBitmap);
        } catch (Exception e) {
            Log.e(TAG, "QR Generation failed", e);
            Toast.makeText(this, "Error generating QR code", Toast.LENGTH_SHORT).show();
        }

        // 3. Update Text with Details
        String details = "Client ID: " + clientId + "\nClient: " + clientName;
        tvNewClientId.setText(details);

        // 4. Show Container
        qrCodeContainer.setVisibility(View.VISIBLE);
    }

    /**
     * Shares the generated QR code using the Android Share Intent.
     */
    private void shareQrCode() {
        if (currentQrBitmap == null) {
            Toast.makeText(this, "No QR Code to share", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Create a temporary URI for the bitmap
            String path = MediaStore.Images.Media.insertImage(getContentResolver(), currentQrBitmap, "Client_QR_" + System.currentTimeMillis(), null);
            Uri uri = Uri.parse(path);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "MealConnect Client QR Code\n" + tvNewClientId.getText());
            startActivity(Intent.createChooser(shareIntent, "Share/Print QR Code via"));

        } catch (Exception e) {
            Log.e(TAG, "Error sharing QR", e);
            Toast.makeText(this, "Failed to share QR code.", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // ===================================
    // DYNAMIC VIEW CREATION
    // ===================================

    private void setupHouseholdSizeListeners() {
        TextWatcher householdWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Delay slightly to ensure UI updates are processed before redrawing views
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

        // Limit recursion/looping issues
        if (totalMembers > 15) {
            return;
        }

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

            // Add a visual separator
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

    // ===================================
    // HELPER METHODS
    // ===================================

    private int getNumberFromEditText(EditText editText) {
        String text = editText.getText().toString().trim();
        try {
            return text.isEmpty() ? 0 : Integer.parseInt(text);
        } catch (NumberFormatException e) {
            // Treat invalid number input as 0 for generating views, validation handles error on submit
            return 0;
        }
    }

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

    /**
     * Converts the selected dietary restrictions into a List<String>.
     */
    private List<String> getDietaryRestrictionsAsList() {
        List<String> restrictions = new ArrayList<>();

        if (cbNone.isChecked()) {
            restrictions.add("None");
            return restrictions;
        }

        if (cbNoChicken.isChecked()) restrictions.add("No Chicken");
        if (cbNoBeef.isChecked()) restrictions.add("No Beef");
        if (cbNoPork.isChecked()) restrictions.add("No Pork");
        if (cbNoTurkey.isChecked()) restrictions.add("No Turkey");
        if (cbNoSausages.isChecked()) restrictions.add("No Sausages");

        if (restrictions.isEmpty()) {
            restrictions.add("None Specified");
        }

        return restrictions;
    }


    private boolean validateForm() {
        // Step 1: Validate Full Name
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Name is required");
            return false;
        }
        // Full name validation: letters & spaces only, minimum 3 characters
        if (!name.matches("^[a-zA-Z\\s]{3,}$")) {
            etName.setError("Name must contain only letters and spaces, minimum 3 characters");
            return false;
        }

        // Step 2: Validate Phone Number
        String phone = etPhone.getText().toString().trim();
        if (phone.isEmpty()) {
            etPhone.setError("Phone is required");
            return false;
        }
        // Basic phone validation: must have at least 10 characters
        if (phone.length() < 10) {
            etPhone.setError("Phone number must be at least 10 characters");
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
        // Site Code Validation (Needed for Unique ID generation)
        if (etSiteCode.getText().toString().trim().isEmpty()) {
            etSiteCode.setError("Site Code (for ID) is required");
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
                member.etMemberDOB.setError(member.memberType + "'s date of birth is required (YYYY-MM-DD)");
                return false;
            }
        }

        return true;
    }

}
