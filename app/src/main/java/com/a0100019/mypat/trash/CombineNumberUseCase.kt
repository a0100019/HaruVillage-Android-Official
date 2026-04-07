package com.a0100019.mypat.trash

interface CombineNumberUseCase {

    suspend operator fun invoke(
        firstNumber: String,
        secondNumber: String,
        operation: String
    ):String

}
