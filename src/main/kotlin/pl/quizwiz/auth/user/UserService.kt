package pl.quizwiz.auth.user

import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import pl.quizwiz.auth.CredentialsNotValidException
import pl.quizwiz.auth.EmailAlreadyExistException
import pl.quizwiz.auth.config.HashService
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val hashService: HashService
) {

    fun findByPublicId(userPublicId: UUID): UserRepresentation {
        return userRepository.findByUserPublicId(userPublicId)?.let { UserRepresentation(it) }
            ?: throw UsernameNotFoundException("User $userPublicId not found")
    }

    fun createUser(email: String, password: String): UserRepresentation {
        if (existsByEmail(email)) {
            throw EmailAlreadyExistException(email)
        }

        val user = User(UUID.randomUUID(), email, hashService.hash(password), true)
        userRepository.save(user)
        return UserRepresentation(user)
    }

    private fun existsByEmail(email: String): Boolean {
        val user = userRepository.findByEmail(email)
        return user != null
    }

    fun findUserForCredentials(email: String, password: String): UserRepresentation {
        val user = userRepository.findByEmail(email) ?: throw CredentialsNotValidException()
        if (!hashService.check(password, user.password)) {
            throw CredentialsNotValidException()
        }
        return UserRepresentation(user)
    }
}