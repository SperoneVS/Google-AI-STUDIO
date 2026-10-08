package com.example.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.example.data.model.AuthMethod
import com.example.data.model.UserProfile
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(private val context: Context) {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id))
    }

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user != null) {
            val userProfile = UserProfile(
                id = user.uid,
                displayName = user.displayName ?: user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "Camper",
                email = user.email,
                phoneNumber = user.phoneNumber,
                authMethod = AuthMethod.GOOGLE
            )
            _currentUser.value = userProfile
            // Load vehicle specs from Firestore
            fetchUserProfileFromFirestore(user.uid)
        } else {
            _currentUser.value = null
        }
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    private fun fetchUserProfileFromFirestore(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val current = _currentUser.value ?: return@addOnSuccessListener
                    _currentUser.value = current.copy(
                        displayName = doc.getString("displayName") ?: current.displayName,
                        vehicleModel = doc.getString("vehicleModel") ?: current.vehicleModel,
                        vehicleHeight = doc.getString("vehicleHeight") ?: current.vehicleHeight,
                        vehicleWeight = doc.getString("vehicleWeight") ?: current.vehicleWeight,
                        licensePlate = doc.getString("licensePlate") ?: current.licensePlate
                    )
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error fetching user profile doc: ${e.message}")
            }
    }

    /**
     * Interactive Google Sign-In using CredentialManager with GetSignInWithGoogleOption.
     */
    suspend fun signInWithGoogle(activity: Activity): Result<UserProfile> {
        val serverClientId = context.getString(R.string.default_web_client_id)
        val credentialManager = CredentialManager.create(activity)

        val googleOption = GetSignInWithGoogleOption.Builder(serverClientId).build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()

        return try {
            val result = credentialManager.getCredential(
                request = request,
                context = activity
            )

            val credential = result.credential
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val firebaseCred = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(firebaseCred).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    val profile = UserProfile(
                        id = firebaseUser.uid,
                        displayName = firebaseUser.displayName ?: googleIdTokenCredential.displayName ?: "Camper",
                        email = firebaseUser.email ?: googleIdTokenCredential.id,
                        phoneNumber = firebaseUser.phoneNumber,
                        authMethod = AuthMethod.GOOGLE
                    )
                    _currentUser.value = profile

                    // Sync to Firestore users collection
                    saveUserProfileToFirestore(profile)

                    Result.success(profile)
                } else {
                    Result.failure(Exception("Failed to obtain Firebase user session."))
                }
            } else {
                Result.failure(Exception("Unsupported credential type received."))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "User cancelled Google Sign-In dialog.", e)
            Result.failure(Exception("Sign-in was cancelled."))
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential Manager error: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Authentication failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Silent / Auto Sign-In attempt using GetGoogleIdOption
     */
    suspend fun trySilentSignIn(activity: Activity): Result<UserProfile> {
        val serverClientId = context.getString(R.string.default_web_client_id)
        val credentialManager = CredentialManager.create(activity)

        val silentOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(silentOption)
            .build()

        return try {
            val result = credentialManager.getCredential(
                request = request,
                context = activity
            )
            val credential = result.credential
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCred = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                val authResult = auth.signInWithCredential(firebaseCred).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val profile = UserProfile(
                        id = firebaseUser.uid,
                        displayName = firebaseUser.displayName ?: "Camper",
                        email = firebaseUser.email,
                        phoneNumber = firebaseUser.phoneNumber,
                        authMethod = AuthMethod.GOOGLE
                    )
                    _currentUser.value = profile
                    fetchUserProfileFromFirestore(firebaseUser.uid)
                    Result.success(profile)
                } else {
                    Result.failure(Exception("No user session"))
                }
            } else {
                Result.failure(Exception("Not authorized"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateVehicleInfo(
        vehicleHeight: String?,
        vehicleWeight: String?,
        vehicleModel: String?,
        licensePlate: String?
    ) {
        val current = _currentUser.value ?: return
        val updated = current.copy(
            vehicleHeight = vehicleHeight?.trim()?.ifBlank { null },
            vehicleWeight = vehicleWeight?.trim()?.ifBlank { null },
            vehicleModel = vehicleModel?.trim()?.ifBlank { null },
            licensePlate = licensePlate?.trim()?.ifBlank { null }
        )
        _currentUser.value = updated
        saveUserProfileToFirestore(updated)
    }

    private fun saveUserProfileToFirestore(user: UserProfile) {
        val data = hashMapOf<String, Any>(
            "userId" to user.id,
            "displayName" to user.displayName,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        user.email?.let { data["email"] = it }
        user.vehicleModel?.let { data["vehicleModel"] = it }
        user.vehicleHeight?.let { data["vehicleHeight"] = it }
        user.vehicleWeight?.let { data["vehicleWeight"] = it }
        user.licensePlate?.let { data["licensePlate"] = it }

        db.collection("users").document(user.id)
            .set(data, com.google.firebase.firestore.SetOptions.merge())
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to persist user profile to Firestore: ${e.message}")
            }
    }

    fun signOut() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Sign out error: ${e.message}")
        }
        _currentUser.value = null
    }

    companion object {
        private const val TAG = "AuthRepository"
    }
}
