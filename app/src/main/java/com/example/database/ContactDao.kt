package com.example.database

import androidx.room.*
import com.example.models.PriorityContact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM priority_contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<PriorityContact>>

    @Query("SELECT * FROM priority_contacts WHERE isActive = 1")
    suspend fun getActiveContacts(): List<PriorityContact>

    @Query("SELECT * FROM priority_contacts WHERE id = :id LIMIT 1")
    suspend fun getContactById(id: Int): PriorityContact?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: PriorityContact)

    @Update
    suspend fun updateContact(contact: PriorityContact)

    @Delete
    suspend fun deleteContact(contact: PriorityContact)
}
