package org.dbu.library.ui

import org.dbu.library.model.Book
import org.dbu.library.model.Patron
import org.dbu.library.repository.LibraryRepository
import org.dbu.library.service.BorrowResult
import org.dbu.library.service.LibraryService
import org.dbu.library.util.display

fun handleMenuAction(
    choice: String,
    service: LibraryService,
    repository: LibraryRepository
): Boolean {

    return when (choice) {

        "1" -> {
            addBook(service)
            true
        }

        "2" -> {
            registerPatron(repository)
            true
        }

        "3" -> {
            borrowBook(service)
            true
        }

        "4" -> {
            returnBook(service)
            true
        }

        "5" -> {
            search(service)
            true
        }

        "6" -> {
            listAllBooks(repository)
            true
        }

        "7" -> {
            listAllPatrons(service)
            true
        }

        "0" -> false

        else -> {
            println("Invalid option")
            true
        }
    }
}

fun addBook(service: LibraryService) {
    print("Enter ISBN: ")
    val isbn = readln().trim()
    print("Enter Title: ")
    val title = readln().trim()
    print("Enter Author: ")
    val author = readln().trim()
    print("Enter Publication Year: ")
    val yearStr = readln().trim()
    val year = yearStr.toIntOrNull()

    if (year == null) {
        println("❌ Invalid year. Book not added.")
        return
    }

    val book = Book(isbn, title, author, year)
    if (service.addBook(book)) {
        println("✅ Book added successfully: ${book.display()}")
    } else {
        println("❌ Failed to add book. ISBN may already exist or input was invalid.")
    }
}

fun registerPatron(repository: LibraryRepository) {
    print("Enter Patron ID: ")
    val id = readln().trim()
    print("Enter Patron Name: ")
    val name = readln().trim()
    print("Enter Patron Email: ")
    val email = readln().trim()
    print("Enter Patron Phone: ")
    val phone = readln().trim()

    if (id.isBlank() || name.isBlank()) {
        println("❌ Patron ID and Name cannot be blank.")
        return
    }

    val patron = Patron(id, name, email, phone)
    if (repository.addPatron(patron)) {
        println("✅ Patron registered successfully: $name (ID: $id)")
    } else {
        println("❌ Failed to register patron. Patron ID already exists.")
    }
}

fun borrowBook(service: LibraryService) {
    print("Enter Patron ID: ")
    val patronId = readln().trim()
    print("Enter Book ISBN: ")
    val isbn = readln().trim()

    when (service.borrowBook(patronId, isbn)) {
        BorrowResult.SUCCESS -> println("✅ Book borrowed successfully!")
        BorrowResult.PATRON_NOT_FOUND -> println("❌ Error: Patron not found.")
        BorrowResult.BOOK_NOT_FOUND -> println("❌ Error: Book not found.")
        BorrowResult.NOT_AVAILABLE -> println("❌ Error: Book is currently borrowed by someone else.")
        BorrowResult.LIMIT_REACHED -> println("❌ Error: Patron has reached their maximum borrow limit.")
    }
}

fun returnBook(service: LibraryService) {
    print("Enter Patron ID: ")
    val patronId = readln().trim()
    print("Enter Book ISBN: ")
    val isbn = readln().trim()

    if (service.returnBook(patronId, isbn)) {
        println("✅ Book returned successfully!")
    } else {
        println("❌ Error: Could not return book. Check Patron ID, ISBN, or whether the book was borrowed.")
    }
}

fun search(service: LibraryService) {
    print("Enter search term (title, author, or ISBN): ")
    val query = readln().trim()
    val results = service.search(query)

    if (results.isEmpty()) {
        println("🔍 No books found matching '$query'.")
    } else {
        println("🔍 Search Results (${results.size}):")
        results.forEach { book ->
            val status = if (book.isAvailable) "[Available]" else "[Borrowed]"
            println(" - ${book.display()} $status")
        }
    }
}

fun listAllBooks(repository: LibraryRepository) {
    val books = repository.getAllBooks()
    if (books.isEmpty()) {
        println("📚 No books in the library.")
    } else {
        println("📚 Library Catalog (${books.size} books):")
        books.forEach { book ->
            val status = if (book.isAvailable) "[Available]" else "[Borrowed]"
            println(" - ISBN: ${book.isbn} | ${book.display()} $status")
        }
    }
}

fun listAllPatrons(service: LibraryService) {
    val patrons = service.getAllPatrons()
    if (patrons.isEmpty()) {
        println("👥 No patrons registered.")
    } else {
        println("👥 Registered Patrons (${patrons.size}):")
        patrons.forEach { patron ->
            val contact = listOf(patron.email, patron.phone).filter { it.isNotBlank() }.joinToString(", ")
            val contactInfo = if (contact.isNotBlank()) " ($contact)" else ""
            val borrowed = if (patron.borrowedBooks.isEmpty()) "None" else patron.borrowedBooks.joinToString(", ")
            println(" - ID: ${patron.id} | Name: ${patron.name}$contactInfo | Borrowed ISBNs: [$borrowed]")
        }
    }
}
