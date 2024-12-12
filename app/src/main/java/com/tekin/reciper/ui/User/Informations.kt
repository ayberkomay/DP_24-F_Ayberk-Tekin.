package com.tekin.reciper.ui.User

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.tekin.reciper.R
import com.tekin.reciper.UserViewModel
import com.tekin.reciper.databinding.FragmentInformationsBinding

class Informations : Fragment(R.layout.fragment_informations) {
    private val viewModel: UserViewModel by activityViewModels()
    private lateinit var binding: FragmentInformationsBinding

    private var isEditing = false

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

        viewModel.currentUserData.observe(viewLifecycleOwner) { userData ->
            if (userData != null) {
                emailTextView.text = userData.email
                emailEditText.setText(userData.email)

                nameTextView.text = userData.name
                nameEditText.setText(userData.name)

                surnameTextView.text = userData.surname
                surnameEditText.setText(userData.surname)

                phoneTextView.text = userData.phone
                phoneEditText.setText(userData.phone)

                dateTextView.text = userData.date
                dateEditText.setText(userData.date)
            } else {
                Toast.makeText(context, "User data not available", Toast.LENGTH_SHORT).show()
            }
        }

        editEmailButton.setOnClickListener {
            EditMode(emailTextView, emailEditText)
            saveButton.visibility = View.VISIBLE
        }

        editNameButton.setOnClickListener {
            EditMode(nameTextView, nameEditText)
            saveButton.visibility = View.VISIBLE
        }

        editSurnameButton.setOnClickListener {
            EditMode(surnameTextView, surnameEditText)
            saveButton.visibility = View.VISIBLE
        }

        editPhoneButton.setOnClickListener {
            EditMode(phoneTextView, phoneEditText)
            saveButton.visibility = View.VISIBLE
        }

        editDateButton.setOnClickListener {
            EditMode(dateTextView, dateEditText)
            saveButton.visibility = View.VISIBLE
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
                val newPhone = phoneEditText.text.toString()
                if (newPhone != phoneTextView.text.toString()) {
                    updatedFields["phone"] = newPhone
                }
            }

            if (dateEditText.visibility == View.VISIBLE) {
                val newDate = dateEditText.text.toString()
                if (newDate != dateTextView.text.toString()) {
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
                                        saveButton.visibility = View.GONE
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
                            saveButton.visibility = View.GONE
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
            if (!isEditMode()) {
                binding.saveButton.visibility = View.GONE
            }
        }
    }

    private fun isEditMode(): Boolean {
        with(binding) {
            return emailEditText.visibility == View.VISIBLE ||
                    nameEditText.visibility == View.VISIBLE ||
                    surnameEditText.visibility == View.VISIBLE ||
                    phoneEditText.visibility == View.VISIBLE ||
                    dateEditText.visibility == View.VISIBLE
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
}
