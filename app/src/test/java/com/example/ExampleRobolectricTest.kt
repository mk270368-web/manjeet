package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.AccountStatus
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.repository.AppRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: AppRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = AppRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testAppNameResourceMatchesInstituteName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Vidyut Gyan Kendra", appName)
    }

    @Test
    fun testPasswordHashingSHA256() {
        val hash1 = AppDatabase.hashPassword("admin@123")
        val hash2 = AppDatabase.hashPassword("admin@123")
        val hashDifferent = AppDatabase.hashPassword("pass123")

        assertEquals(hash1, hash2)
        assertTrue(hash1.length == 64)
        assertFalse(hash1 == hashDifferent)
    }

    @Test
    fun testAdminAuthenticationSuccess() = runBlocking {
        val adminUser = UserEntity(
            userId = "admin_01",
            loginId = "ADM001",
            passwordHash = AppDatabase.hashPassword("admin@123"),
            role = UserRole.ADMIN,
            name = "Dr. Arvind Swaminathan",
            email = "admin@gk.edu.in",
            phone = "+91 98765 43210",
            status = AccountStatus.ACTIVE
        )
        db.userDao().insertUser(adminUser)

        val result = repository.authenticate("ADM001", "admin@123")
        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals("ADM001", user?.loginId)
        assertEquals(UserRole.ADMIN, user?.role)
    }

    @Test
    fun testInvalidPasswordAuthenticationFailure() = runBlocking {
        val studentUser = UserEntity(
            userId = "stu_01",
            loginId = "STU202401",
            passwordHash = AppDatabase.hashPassword("pass123"),
            role = UserRole.STUDENT,
            name = "Aarav Patel",
            email = "aarav@gk.edu.in",
            phone = "+91 99000 11111",
            status = AccountStatus.ACTIVE
        )
        db.userDao().insertUser(studentUser)

        val result = repository.authenticate("STU202401", "wrong_password")
        assertTrue(result.isFailure)
    }

    @Test
    fun testDeactivatedAccountFailsLogin() = runBlocking {
        val inactiveUser = UserEntity(
            userId = "tch_01",
            loginId = "TCH101",
            passwordHash = AppDatabase.hashPassword("pass123"),
            role = UserRole.TEACHER,
            name = "Prof. Sharma",
            email = "sharma@gk.edu.in",
            phone = "+91 98111 22334",
            status = AccountStatus.INACTIVE
        )
        db.userDao().insertUser(inactiveUser)

        val result = repository.authenticate("TCH101", "pass123")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("deactivated") == true)
    }

    @Test
    fun testPasswordChangeValidation() = runBlocking {
        val user = UserEntity(
            userId = "stu_02",
            loginId = "STU202402",
            passwordHash = AppDatabase.hashPassword("pass123"),
            role = UserRole.STUDENT,
            name = "Priya Sharma",
            email = "priya@gk.edu.in",
            phone = "+91 99000 22222",
            status = AccountStatus.ACTIVE
        )
        db.userDao().insertUser(user)

        // Invalid old password
        val failResult = repository.changePassword("stu_02", "wrong_old", "newPass999")
        assertTrue(failResult.isFailure)

        // Successful password change
        val successResult = repository.changePassword("stu_02", "pass123", "newPass999")
        assertTrue(successResult.isSuccess)

        // Old password no longer works
        val oldLogin = repository.authenticate("STU202402", "pass123")
        assertTrue(oldLogin.isFailure)

        // New password works
        val newLogin = repository.authenticate("STU202402", "newPass999")
        assertTrue(newLogin.isSuccess)
    }
}
