package com.tekin.reciper.ui.User.others

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.tekin.reciper.R
import com.tekin.reciper.data.UserData
import com.tekin.reciper.model.UserViewModel
import com.tekin.reciper.databinding.FragmentRegisterBinding
import com.tekin.reciper.ui.User.UserNotSignedIn
import com.tekin.reciper.ui.User.UserSignedIn

class Register : Fragment(R.layout.fragment_register) {
    private lateinit var binding: FragmentRegisterBinding
    private val viewModel: UserViewModel by activityViewModels()

    private var currentDate = ""
    private val dateDefault = "DDMMYYYY"

    private val termsAndConditionsContent = """
        Welcome to Reciper!

        These terms and conditions outline the rules and regulations for the use of Reciper's application.

        By accessing this application, we assume you accept these terms and conditions. Do not continue to use Reciper if you do not agree to all of the terms and conditions stated on this page.

        1.Recipes and User Content:
           Recipes submitted by users remain their intellectual property, but Reciper retains the right to display, promote, and store the content.
           Content should be respectful and free of offensive materials.

        2.User Privacy:
           User data, such as email and phone numbers, is stored securely and not shared with third parties without consent.

        3.Account Responsibilities:
           Users are responsible for maintaining the security of their accounts and ensuring accurate information.

        4.Usage Restrictions:
           You agree not to use Reciper for any unlawful purposes or to disrupt its operations.

        5.Modifications:
           Reciper reserves the right to update or modify these terms at any time.

        Thank you for using Reciper!
    """.trimIndent()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegisterBinding.bind(view)

        val buttonRegister = binding.buttonRegister
        val buttonRegisterBack = binding.buttonRegisterBack
        val textBirthDate = binding.editTextRegisterBirthDate
        val checkBoxTerms = binding.checkBoxTerms
        val textTermsAndConditions = binding.textTermsAndConditions

        textBirthDate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val inputNumbers = s.toString().replace("[^\\d]".toRegex(), "")
                if (inputNumbers.length <= 8) {
                    val sb = StringBuilder()
                    var index = 0
                    for (i in dateDefault.indices) {
                        if (index < inputNumbers.length) {
                            sb.append(inputNumbers[index])
                            index++
                            if ((i == 1 || i == 3) && index < inputNumbers.length) {
                                sb.append("/")
                            }
                        } else {
                            break
                        }
                    }
                    currentDate = sb.toString()
                    textBirthDate.removeTextChangedListener(this)
                    textBirthDate.setText(currentDate)
                    textBirthDate.setSelection(currentDate.length)
                    textBirthDate.addTextChangedListener(this)
                } else {
                    textBirthDate.removeTextChangedListener(this)
                    textBirthDate.setText(currentDate)
                    textBirthDate.setSelection(currentDate.length)
                    textBirthDate.addTextChangedListener(this)
                }
            }
        })

        textTermsAndConditions.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Terms and Conditions")
                .setMessage(termsAndConditionsContent)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        buttonRegister.setOnClickListener {
            val email = binding.textRegisterMail.text.toString().trim()
            val password = binding.textRegisterPassword.text.toString().trim()
            val confirmPassword = binding.textRegisterConfirmPassword.text.toString().trim()
            val name = binding.textRegisterName.text.toString().trim()
            val surname = binding.textRegisterSurname.text.toString().trim()
            val phone = binding.textRegisterPhone.text.toString().trim()
            val date = binding.editTextRegisterBirthDate.text.toString().trim()

            if (!checkBoxTerms.isChecked) {
                Toast.makeText(context, "Please accept the Terms and Conditions", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(context, "Password must be at least 6 characters long", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty() &&
                surname.isNotEmpty() && phone.isNotEmpty() && date.isNotEmpty()) {

                val phoneLong = phone.toLongOrNull()
                val dateInt = parseDateInt(date)
                val userData = UserData(email, name, surname, phoneLong, dateInt)

                if (phoneLong == null || dateInt == null) {
                    Toast.makeText(context, "Phone or date invalid", Toast.LENGTH_LONG).show()
                } else {
                    viewModel.register(email, password, userData) { success ->
                        if (success) {
                            Toast.makeText(context, "Registration successful!", Toast.LENGTH_LONG).show()
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.frame_layout, UserSignedIn())
                                .commit()
                        } else {
                            Toast.makeText(context, "Registration failed.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } else {
                Toast.makeText(context, "Please fill in all fields.", Toast.LENGTH_LONG).show()
            }
        }

        buttonRegisterBack.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, UserNotSignedIn())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun parseDateInt(dateString: String): Int? {
        val numeric = dateString.replace("/", "")
        return numeric.toIntOrNull()
    }
}
