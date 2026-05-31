package com.mango;

import com.mango.control.api.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReserveDemoApplicationTests {

    @Test
    void apiResponseSuccessCreatesStandardPayload() {
        ApiResponse<String> response = ApiResponse.success("ok-data");

        assertTrue(response.isSuccess());
        assertEquals("ok", response.getMessage());
        assertEquals("ok-data", response.getData());
    }
}
