package com.andreromano.devjobboard.service

import at.favre.lib.crypto.bcrypt.BCrypt

interface PasswordService {
    fun hash(password: String): String
    fun verify(password: String, hash: String): Boolean
}

class DefaultPasswordService : PasswordService {
    override fun hash(password: String): String =
        BCrypt.withDefaults().hashToString(12, password.toCharArray())


    override fun verify(password: String, hash: String): Boolean =
        BCrypt.verifyer().verify(password.toCharArray(), hash).verified
}