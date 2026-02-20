package youglish.anki.demo.domain.service

import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import youglish.anki.demo.application.dto.AuthResponse
import youglish.anki.demo.application.dto.LoginRequest
import youglish.anki.demo.application.dto.RegisterRequest
import youglish.anki.demo.domain.entity.User
import youglish.anki.demo.domain.repository.UserRepository
import youglish.anki.demo.infrastructure.security.JwtTokenProvider

@Service
@Transactional
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun register(request: RegisterRequest): AuthResponse {
        logger.info("Registering new user: {}", request.username)

        if (userRepository.existsByUsername(request.username)) {
            throw IllegalArgumentException("Username already exists")
        }

        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already exists")
        }

        val user = User(
            username = request.username,
            email = request.email,
            password = passwordEncoder.encode(request.password)
        )

        userRepository.save(user)

        val token = jwtTokenProvider.generateToken(user.username)

        logger.info("User registered successfully: {}", user.username)

        return AuthResponse(
            token = token,
            username = user.username,
            email = user.email
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        logger.info("User login attempt: {}", request.username)

        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.username, request.password)
        )

        val user = userRepository.findByUsername(request.username)
            .orElseThrow { IllegalArgumentException("User not found") }

        val token = jwtTokenProvider.generateToken(user.username)

        logger.info("User logged in successfully: {}", user.username)

        return AuthResponse(
            token = token,
            username = user.username,
            email = user.email
        )
    }
}

