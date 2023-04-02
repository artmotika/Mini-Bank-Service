package com.bank.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
class ExistingNameException: RuntimeException()