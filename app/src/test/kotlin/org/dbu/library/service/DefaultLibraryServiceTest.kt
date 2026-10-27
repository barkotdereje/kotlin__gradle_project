package org.dbu.library.service

import org.dbu.library.model.Book
import org.dbu.library.model.Patron
import org.dbu.library.repository.InMemoryLibraryRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultLibraryServiceTest {

    private lateinit var repository: InMemoryLibraryRepository
    private lateinit var service: DefaultLibraryService

    @BeforeEach
    fun setUp() {
        repository = InMemoryLibraryRepository()
        service = DefaultLibraryService(repository, maxBorrowLimit = 2)
    }

    @Test
    fun `borrowBook should succeed when book and patron exist and book is available`() {
        val book = Book("101", "Clean Code", "Robert Martin", 2008)
        val patron = Patron("P1", "Bob")

        repository.addBook(book)
        repository.addPatron(patron)

        val result = service.borrowBook("P1", "101")
        assertEquals(BorrowResult.SUCCESS, result)

        assertFalse(repository.findBook("101")!!.isAvailable)
        assertTrue(repository.findPatron("P1")!!.borrowedBooks.contains("101"))
    }

    @Test
    fun `borrowBook should fail if patron does not exist`() {
        val book = Book("101", "Clean Code", "Robert Martin", 2008)
        repository.addBook(book)

        val result = service.borrowBook("NON_EXISTENT", "101")
        assertEquals(BorrowResult.PATRON_NOT_FOUND, result)
    }

    @Test
    fun `borrowBook should fail if book does not exist`() {
        val patron = Patron("P1", "Bob")
        repository.addPatron(patron)

        val result = service.borrowBook("P1", "NON_EXISTENT")
        assertEquals(BorrowResult.BOOK_NOT_FOUND, result)
    }

    @Test
    fun `borrowBook should fail if book is not available`() {
        val book = Book("101", "Clean Code", "Robert Martin", 2008, isAvailable = false)
        val patron = Patron("P1", "Bob")

        repository.addBook(book)
        repository.addPatron(patron)

        val result = service.borrowBook("P1", "101")
        assertEquals(BorrowResult.NOT_AVAILABLE, result)
    }

    @Test
    fun `borrowBook should enforce max borrow limit`() {
        val b1 = Book("1", "B1", "A1", 2020)
        val b2 = Book("2", "B2", "A2", 2020)
        val b3 = Book("3", "B3", "A3", 2020)
        val patron = Patron("P1", "Bob")

        repository.addBook(b1)
        repository.addBook(b2)
        repository.addBook(b3)
        repository.addPatron(patron)

        assertEquals(BorrowResult.SUCCESS, service.borrowBook("P1", "1"))
        assertEquals(BorrowResult.SUCCESS, service.borrowBook("P1", "2"))
        assertEquals(BorrowResult.LIMIT_REACHED, service.borrowBook("P1", "3"))
    }

    @Test
    fun `returnBook should succeed and restore book availability`() {
        val book = Book("101", "Clean Code", "Robert Martin", 2008)
        val patron = Patron("P1", "Bob")

        repository.addBook(book)
        repository.addPatron(patron)

        service.borrowBook("P1", "101")
        assertTrue(service.returnBook("P1", "101"))

        assertTrue(repository.findBook("101")!!.isAvailable)
        assertFalse(repository.findPatron("P1")!!.borrowedBooks.contains("101"))
    }

    @Test
    fun `search should filter books case-insensitively`() {
        repository.addBook(Book("1", "Clean Code", "Robert Martin", 2008))
        repository.addBook(Book("2", "Effective Java", "Joshua Bloch", 2018))

        val results = service.search("clean")
        assertEquals(1, results.size)
        assertEquals("Clean Code", results[0].title)
    }
}
