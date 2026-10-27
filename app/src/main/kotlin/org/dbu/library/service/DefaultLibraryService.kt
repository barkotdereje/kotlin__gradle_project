package org.dbu.library.service

import org.dbu.library.model.Book
import org.dbu.library.model.Patron
import org.dbu.library.repository.LibraryRepository

class DefaultLibraryService(
    private val repository: LibraryRepository,
    private val maxBorrowLimit: Int = 3
) : LibraryService {

    override fun addBook(book: Book): Boolean {
        if (book.isbn.isBlank() || book.title.isBlank() || book.author.isBlank()) {
            return false
        }
        return repository.addBook(book)
    }

    override fun borrowBook(patronId: String, isbn: String): BorrowResult {
        val patron = repository.findPatron(patronId) ?: return BorrowResult.PATRON_NOT_FOUND
        val book = repository.findBook(isbn) ?: return BorrowResult.BOOK_NOT_FOUND

        if (!book.isAvailable) {
            return BorrowResult.NOT_AVAILABLE
        }

        if (patron.borrowedBooks.size >= maxBorrowLimit) {
            return BorrowResult.LIMIT_REACHED
        }

        repository.updateBook(book.copy(isAvailable = false))
        repository.updatePatron(patron.copy(borrowedBooks = patron.borrowedBooks + isbn))

        return BorrowResult.SUCCESS
    }

    override fun returnBook(patronId: String, isbn: String): Boolean {
        val patron = repository.findPatron(patronId) ?: return false
        if (isbn !in patron.borrowedBooks) {
            return false
        }

        val book = repository.findBook(isbn) ?: return false

        repository.updateBook(book.copy(isAvailable = true))
        repository.updatePatron(patron.copy(borrowedBooks = patron.borrowedBooks - isbn))

        return true
    }

    override fun search(query: String): List<Book> {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return repository.getAllBooks()

        return repository.getAllBooks().filter { book ->
            book.title.contains(cleanQuery, ignoreCase = true) ||
                    book.author.contains(cleanQuery, ignoreCase = true) ||
                    book.isbn.contains(cleanQuery, ignoreCase = true)
        }
    }

    override fun getAllPatrons(): List<Patron> {
        return repository.getAllPatrons()
    }
}
