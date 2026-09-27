package io.github.matoklimm.domain.bookcopy

import io.github.matoklimm.domain.bookcopy.commands.*
import io.github.matoklimm.domain.bookcopy.events.*
import io.github.matoklimm.domain.shared.AggregateRoot
import java.time.Instant
import java.time.LocalDate
import kotlin.uuid.Uuid

data class BookCopyId(val id: Uuid)

class BookCopy : AggregateRoot<BookCopyEvent>() {

    lateinit var id: BookCopyId
        private set

    lateinit var isbn: String
        private set

    lateinit var bookCopyStatus: BookCopyStatus
        private set

    var bookCopyLoan: BookCopyLoan? = null
        private set

    fun handle(command: BookCopyCommand) {
        when (command) {
            is AddBookCopyCommand -> raise(BookCopyAddedEvent(bookCopyId = BookCopyId(Uuid.random()), isbn = command.isbn))

            is BorrowBookCopyCommand -> {
                checkStatus(bookCopyId = command.bookCopyId, BookCopyStatus.AVAILABLE)

                val borrowedUntil = command.borrowedUntil ?: LocalDate.now().plusDays(14)
                check(borrowedUntil < LocalDate.now().plusDays(31)) {
                    "BookCopy(${id.id}) must be borrowed for 30 days or less. $borrowedUntil is exceeding that range"
                }

                raise(
                    BookCopyBorrowedEvent(
                        bookCopyId = command.bookCopyId,
                        userId = command.userId,
                        borrowedAt = Instant.now(),
                        borrowedUntil = borrowedUntil
                    )
                )
            }

            is ReturnBookCopyCommand -> {
                checkStatus(bookCopyId = command.bookCopyId, BookCopyStatus.BORROWED)

                raise(BookCopyReturnedEvent(bookCopyId = command.bookCopyId, returnedAt = Instant.now()))
            }

            is ReportBookCopyDamageCommand -> {
                checkStatus(bookCopyId = command.bookCopyId, BookCopyStatus.AVAILABLE, BookCopyStatus.BORROWED)
                raise(BookCopyDamagedEvent(bookCopyId = command.bookCopyId, description = command.description))
            }

            is RepairBookCopyCommand -> {
                checkStatus(bookCopyId = command.bookCopyId, BookCopyStatus.DAMAGED)
                raise(
                    BookCopyRepairedEvent(
                        bookCopyId = command.bookCopyId,
                        description = command.description,
                        isDamageRepaired = command.isDamageRepaired
                    )
                )
            }

            is MarkBookCopyAsLostCommand -> {
                checkStatus(bookCopyId = command.bookCopyId, BookCopyStatus.AVAILABLE, BookCopyStatus.BORROWED, BookCopyStatus.DAMAGED)
                raise(BookCopyLostEvent(bookCopyId = command.bookCopyId, lostAt = Instant.now()))
            }

            is MarkBookCopyAsFoundCommand -> {
                checkStatus(bookCopyId = command.bookCopyId, BookCopyStatus.LOST)
                raise(BookCopyFoundEvent(bookCopyId = command.bookCopyId, foundAt = Instant.now()))
            }

            is RetireBookCopyCommand -> {
                checkStatus(bookCopyId = command.bookCopyId, BookCopyStatus.AVAILABLE, BookCopyStatus.DAMAGED)
                raise(BookCopyRetiredEvent(bookCopyId = command.bookCopyId, retiredAt = Instant.now()))
            }

            is ExtendBookCopyLoanCommand -> {
                checkStatus(bookCopyId = command.bookCopyId, BookCopyStatus.BORROWED)
                val loan = checkNotNull(bookCopyLoan) {
                    "BookCopy(${id.id}) cannot be extended without an active loan"
                }
                check(loan.borrowedBy == command.userId) {
                    "BookCopy(${id.id}) is borrowed by User(${loan.borrowedBy}) and therefore must not be extended by ${command.userId}"
                }
                check(loan.extendCount < 2) {
                    "BookCopy(${id.id}) maximum number of loan extends has been reached."
                }

                raise(BookCopyLoanExtendedEvent(bookCopyId = command.bookCopyId, extendedUntil = loan.borrowedUntil.plusDays(14)))
            }
        }
    }

    override fun apply(event: BookCopyEvent) {
        when (event) {
            is BookCopyAddedEvent -> {
                id = event.bookCopyId;
                isbn = event.isbn;
                bookCopyStatus = BookCopyStatus.AVAILABLE
            }

            is BookCopyBorrowedEvent -> {
                bookCopyStatus = BookCopyStatus.BORROWED
                bookCopyLoan = BookCopyLoan(
                    borrowedBy = event.userId, borrowedAt = event.borrowedAt, borrowedUntil = event.borrowedUntil, extendCount = 0
                )
            }

            is BookCopyReturnedEvent -> {
                bookCopyStatus = BookCopyStatus.AVAILABLE
                bookCopyLoan = null
            }

            is BookCopyDamagedEvent -> bookCopyStatus = BookCopyStatus.DAMAGED
            is BookCopyRepairedEvent -> if (event.isDamageRepaired) bookCopyStatus = BookCopyStatus.AVAILABLE
            is BookCopyLostEvent -> bookCopyStatus = BookCopyStatus.LOST
            is BookCopyFoundEvent -> bookCopyStatus = BookCopyStatus.AVAILABLE
            is BookCopyRetiredEvent -> bookCopyStatus = BookCopyStatus.RETIRED
            is BookCopyLoanExtendedEvent -> {
                val loan = checkNotNull(bookCopyLoan) {
                    "BookCopy(${id.id}) cannot be extended without an active loan"
                }
                loan.extendCount += 1
                loan.borrowedUntil = event.extendedUntil
            }
        }
    }

    private fun checkStatus(bookCopyId: BookCopyId, vararg allowed: BookCopyStatus) {
        check(id == bookCopyId) {
            "BookCopy(${id.id}) was handled/applied with incorrect call argument $bookCopyId.\nThis must not happen and is a serious error."
        }
        check(bookCopyStatus in allowed) {
            "BookCopy(${id.id}) must be in one of states ${allowed.toList()} but is in '$bookCopyStatus'"
        }
    }


}