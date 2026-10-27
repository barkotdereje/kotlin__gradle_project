package org.dbu.library.repository

import org.dbu.library.model.Book
import org.dbu.library.model.Patron
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InMemoryLibraryRepositoryTest {

    private lateinit var repository: InMemoryLibraryRepository

    @BeforeEach
    fun setUp() {
        repository = InMemoryLibraryRepository()
    }

    @Test
    fun `addBook should store book and return true when ISBN is unique`() {
        val book = Book("123", "Kotlin in Action", "Dmitry Jemerov", 2017)
        assertTrue(repository.addBook(book))
        assertEquals(book, repository.findBook("123"))
    }

    @Test
    fun `addBook should return false when ISBN already exists`() {
        val book = Book("123", "Kotlin in Action", "Dmitry Jemerov", 2017)
        repository.addBook(book)
        assertFalse(repository.addBook(book))
    }

    @Test
    fun `updateBook should modify existing book record`() {
        val book = Book("123", "Kotlin in Action", "Dmitry Jemerov", 2017, isAvailable = true)
        repository.addBook(book)

        val updatedBook = book.copy(isAvailable = false)
        repository.updateBook(updatedBook)

        val retrieved = repository.findBook("123")
        assertNotNull(retrieved)
        assertFalse(retrieved!!.isAvailable)
    }

    @Test
    fun `addPatron and findPatron should handle patron data correctly`() {
        val patron = Patron("P1", "Abebe", "abebe@example.com", "123-456-7890")
        assertTrue(repository.addPatron(patron))
        assertFalse(repository.addPatron(patron))
        assertEquals(patron, repository.findPatron("P1"))
    }

    @Test
    fun `getAllBooks should return all stored books`() {
        val b1 = Book("1", "Book One", "Author A", 2020)
        val b2 = Book("2", "Book Two", "Author B", 2021)

        repository.addBook(b1)
        repository.addBook(b2)

        val all = repository.getAllBooks()
        assertEquals(2, all.size)
        assertTrue(all.contains(b1))
        assertTrue(all.contains(b2))
    }

    @Test
    fun `getAllPatrons should return all stored patrons`() {
        val p1 = Patron("P1", "Abebe")
        val p2 = Patron("P2", "Bob")

        repository.addPatron(p1)
        repository.addPatron(p2)

        val all = repository.getAllPatrons()
        assertEquals(2, all.size)
        assertTrue(all.contains(p1))
        assertTrue(all.contains(p2))
    }
}
