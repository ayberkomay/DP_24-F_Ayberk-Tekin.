package com.tekin.reciper.ui.User.others

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.tekin.reciper.R
import com.tekin.reciper.model.UserViewModel
import com.tekin.reciper.databinding.FragmentInformationsBinding

class Informations : Fragment(R.layout.fragment_informations) {
    private val viewModel: UserViewModel by activityViewModels()
    private lateinit var binding: FragmentInformationsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInformationsBinding.bind(view)

        val emailTextView = binding.emailTextView
        val emailEditText = binding.emailEditText
        val editEmailButton = binding.editEmailButton

        val nameTextView = binding.nameTextView
        val nameEditText = binding.nameEditText
        val editNameButton = binding.editNameButton

        val surnameTextView = binding.surnameTextView
        val surnameEditText = binding.surnameEditText
        val editSurnameButton = binding.editSurnameButton

        val phoneTextView = binding.phoneTextView
        val phoneEditText = binding.phoneEditText
        val editPhoneButton = binding.editPhoneButton

        val dateTextView = binding.dateTextView
        val dateEditText = binding.dateEditText
        val editDateButton = binding.editDateButton

        val saveButton = binding.saveButton
        val changePasswordButton = binding.changePasswordButton
        val backButton = binding.backButton

        dateEditText.addTextChangedListener(object : TextWatcher {
            private var current = ""
            private val dateDefault = "dd/MM/yyyy"

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
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
                        current = sb.toString()
                        dateEditText.removeTextChangedListener(this)
                        dateEditText.setText(current)
                        dateEditText.setSelection(current.length)
                        dateEditText.addTextChangedListener(this)
                    } else {
                        dateEditText.removeTextChangedListener(this)
                        dateEditText.setText(current)
                        dateEditText.setSelection(current.length)
                        dateEditText.addTextChangedListener(this)
                    }
                }
            }
        })

        viewModel.currentUserData.observe(viewLifecycleOwner) { userData ->
            if (userData != null) {
                emailTextView.text = userData.email
                emailEditText.setText(userData.email)

                nameTextView.text = userData.name
                nameEditText.setText(userData.name)

                surnameTextView.text = userData.surname
                surnameEditText.setText(userData.surname)

                val phoneStr = userData.phone?.toString() ?: ""
                phoneTextView.text = phoneStr
                phoneEditText.setText(phoneStr)

                val dateStr = userData.date?.toString() ?: ""
                val formattedDate = formatDateString(dateStr)
                dateTextView.text = formattedDate
                dateEditText.setText(formattedDate)
            } else {
                Toast.makeText(context, "User data not available", Toast.LENGTH_SHORT).show()
            }
        }

        editEmailButton.setOnClickListener {
            EditMode(emailTextView, emailEditText)
        }

        editNameButton.setOnClickListener {
            EditMode(nameTextView, nameEditText)
        }

        editSurnameButton.setOnClickListener {
            EditMode(surnameTextView, surnameEditText)
        }

        editPhoneButton.setOnClickListener {
            EditMode(phoneTextView, phoneEditText)
        }

        editDateButton.setOnClickListener {
            EditMode(dateTextView, dateEditText)
        }

        saveButton.setOnClickListener {
            val updatedFields = mutableMapOf<String, Any?>()
            var requiresReauth = false

            if (emailEditText.visibility == View.VISIBLE) {
                val newEmail = emailEditText.text.toString()
                if (newEmail != emailTextView.text.toString()) {
                    updatedFields["email"] = newEmail
                    requiresReauth = true
                }
            }
            if (nameEditText.visibility == View.VISIBLE) {
                val newName = nameEditText.text.toString()
                if (newName != nameTextView.text.toString()) {
                    updatedFields["name"] = newName
                }
            }
            if (surnameEditText.visibility == View.VISIBLE) {
                val newSurname = surnameEditText.text.toString()
                if (newSurname != surnameTextView.text.toString()) {
                    updatedFields["surname"] = newSurname
                }
            }
            if (phoneEditText.visibility == View.VISIBLE) {
                val newPhone = phoneEditText.text.toString().toLongOrNull()
                val oldPhone = phoneTextView.text.toString().toLongOrNull()
                if (newPhone != null && newPhone != oldPhone) {
                    updatedFields["phone"] = newPhone
                }
            }
            if (dateEditText.visibility == View.VISIBLE) {
                val newDateStr = dateEditText.text.toString().replace("/", "")
                val newDate = newDateStr.toIntOrNull()
                val oldDateStr = dateTextView.text.toString().replace("/", "")
                val oldDate = oldDateStr.toIntOrNull()
                if (newDate != null && newDate != oldDate) {
                    updatedFields["date"] = newDate
                }
            }

            if (updatedFields.isNotEmpty()) {
                if (requiresReauth) {
                    val passwordInput = EditText(context)
                    val dialog = AlertDialog.Builder(requireContext())
                        .setTitle("Re-authentication Required")
                        .setMessage("Please enter your password to update email")
                        .setView(passwordInput)
                        .setPositiveButton("Confirm") { _, _ ->
                            val password = passwordInput.text.toString()
                            if (password.isNotEmpty()) {
                                viewModel.updateUserData(updatedFields, password) { success, errorMessage ->
                                    if (success) {
                                        Toast.makeText(context, "Information updated", Toast.LENGTH_SHORT).show()
                                        reset()
                                    } else {
                                        Toast.makeText(context, "Update failed: $errorMessage", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Password cannot be empty", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .create()
                    dialog.show()
                } else {
                    viewModel.updateUserData(updatedFields, null) { success, errorMessage ->
                        if (success) {
                            Toast.makeText(context, "Information updated", Toast.LENGTH_SHORT).show()
                            reset()
                        } else {
                            Toast.makeText(context, "Update failed: $errorMessage", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } else {
                Toast.makeText(context, "No changes made", Toast.LENGTH_SHORT).show()
            }
        }

        changePasswordButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, ChangePassword())
                .addToBackStack(null)
                .commit()
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun EditMode(textView: View, editText: View) {
        if (editText.visibility == View.GONE) {
            textView.visibility = View.GONE
            editText.visibility = View.VISIBLE
        } else {
            textView.visibility = View.VISIBLE
            editText.visibility = View.GONE
        }
    }

    private fun reset() {
        with(binding) {
            emailTextView.visibility = View.VISIBLE
            emailEditText.visibility = View.GONE

            nameTextView.visibility = View.VISIBLE
            nameEditText.visibility = View.GONE

            surnameTextView.visibility = View.VISIBLE
            surnameEditText.visibility = View.GONE

            phoneTextView.visibility = View.VISIBLE
            phoneEditText.visibility = View.GONE

            dateTextView.visibility = View.VISIBLE
            dateEditText.visibility = View.GONE
        }
    }

    private fun formatDateString(dateStr: String): String {
        val inputNumbers = dateStr.replace("[^\\d]".toRegex(), "")
        if (inputNumbers.length == 8) {
            val day = inputNumbers.substring(0, 2)
            val month = inputNumbers.substring(2, 4)
            val year = inputNumbers.substring(4, 8)
            return "$day/$month/$year"
        }
        return dateStr
    }
}
