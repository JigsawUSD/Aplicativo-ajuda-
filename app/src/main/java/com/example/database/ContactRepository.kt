package com.example.database

import com.example.models.PriorityContact
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val contactDao: ContactDao) {
    val allContacts: Flow<List<PriorityContact>> = contactDao.getAllContacts()

    suspend fun getActiveContacts(): List<PriorityContact> = contactDao.getActiveContacts()

    suspend fun getContactById(id: Int): PriorityContact? = contactDao.getContactById(id)

    suspend fun insertContact(contact: PriorityContact) = contactDao.insertContact(contact)

    suspend fun updateContact(contact: PriorityContact) = contactDao.updateContact(contact)

    suspend fun deleteContact(contact: PriorityContact) = contactDao.deleteContact(contact)
}
