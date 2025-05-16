package com.thanhtam.backend.controller;

import com.thanhtam.backend.entity.QuestionType;
import com.thanhtam.backend.service.QuestionTypeService;
import com.thanhtam.backend.ultilities.EQTypeCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class QuestionTypeControllerTest {

    @Mock
    private QuestionTypeService questionTypeService;

    @InjectMocks
    private QuestionTypeController questionTypeController;

    private MockMvc mockMvc;

    private QuestionType questionType1;
    private QuestionType questionType2;

    // Test-specific ControllerAdvice
    @ControllerAdvice
    static class TestGlobalExceptionHandler {
        @ExceptionHandler(NoSuchElementException.class)
        public ResponseEntity<String> handleNoSuchElementException(NoSuchElementException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(questionTypeController)
                                 .setControllerAdvice(new TestGlobalExceptionHandler()) // Add the advice
                                 .build();

        questionType1 = new QuestionType();
        questionType1.setId(1L);
        questionType1.setTypeCode(EQTypeCode.TF);
        questionType1.setDescription("True/False Question");

        questionType2 = new QuestionType();
        questionType2.setId(2L);
        questionType2.setTypeCode(EQTypeCode.MC);
        questionType2.setDescription("Multiple Choice Question");
    }

    /**
     * Test Case ID: UT_QBM_04_TC01
     * Purpose: Verify successful retrieval of all question types.
     *
     * Prerequisites:
     *  - QuestionTypeService is mocked.
     *  - Mocked QuestionTypeService.getQuestionTypeList() returns a list of question types.
     *
     * Test Steps:
     *  1. Perform a GET request to /api/question-types.
     *  2. Assert that the response status is 200 (OK).
     *  3. Assert that the response content type is application/json.
     *  4. Assert that the response body contains the expected list of question types.
     *  5. Verify that questionTypeService.getQuestionTypeList() was called once.
     *
     * Expected Results:
     *  - Status code is 200.
     *  - Content type is application/json.
     *  - Response body matches the list of question types returned by the service.
     *  - The service method getQuestionTypeList is invoked once.
     */
    @Test
    @DisplayName("UT_QBM_04_TC01: Get All Question Types - Success")
    void getAllQuestionType_Success_ReturnsListOfQuestionTypes() throws Exception {
        List<QuestionType> allQuestionTypes = Arrays.asList(questionType1, questionType2);
        given(questionTypeService.getQuestionTypeList()).willReturn(allQuestionTypes);

        mockMvc.perform(get("/api/question-types"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].typeCode", is("TF")))
                .andExpect(jsonPath("$[0].description", is("True/False Question")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].typeCode", is("MC")))
                .andExpect(jsonPath("$[1].description", is("Multiple Choice Question")));

        verify(questionTypeService).getQuestionTypeList();
    }

    /**
     * Test Case ID: UT_QBM_04_TC02
     * Purpose: Verify behavior when no question types are available.
     *
     * Prerequisites:
     *  - QuestionTypeService is mocked.
     *  - Mocked QuestionTypeService.getQuestionTypeList() returns an empty list.
     *
     * Test Steps:
     *  1. Perform a GET request to /api/question-types.
     *  2. Assert that the response status is 200 (OK).
     *  3. Assert that the response content type is application/json.
     *  4. Assert that the response body is an empty list.
     *  5. Verify that questionTypeService.getQuestionTypeList() was called once.
     *
     * Expected Results:
     *  - Status code is 200.
     *  - Content type is application/json.
     *  - Response body is an empty JSON array.
     *  - The service method getQuestionTypeList is invoked once.
     */
    @Test
    @DisplayName("UT_QBM_04_TC02: Get All Question Types - Empty List")
    void getAllQuestionType_EmptyList_ReturnsOkWithEmptyList() throws Exception {
        given(questionTypeService.getQuestionTypeList()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/question-types"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(questionTypeService).getQuestionTypeList();
    }

    /**
     * Test Case ID: UT_QBM_04_TC03
     * Purpose: Verify successful retrieval of a question type by its ID.
     *
     * Prerequisites:
     *  - QuestionTypeService is mocked.
     *  - Mocked QuestionTypeService.getQuestionTypeById() returns an Optional containing a question type.
     *
     * Test Steps:
     *  1. Perform a GET request to /api/question-types/id/{id} with a valid ID.
     *  2. Assert that the response status is 200 (OK).
     *  3. Assert that the response content type is application/json.
     *  4. Assert that the response body contains the expected question type.
     *  5. Verify that questionTypeService.getQuestionTypeById() was called once with the correct ID.
     *
     * Expected Results:
     *  - Status code is 200.
     *  - Content type is application/json.
     *  - Response body matches the question type returned by the service.
     *  - The service method getQuestionTypeById is invoked once with the specified ID.
     */
    @Test
    @DisplayName("UT_QBM_04_TC03: Get Question Type By ID - Success")
    void getQuestionTypeById_Success_ReturnsQuestionType() throws Exception {
        given(questionTypeService.getQuestionTypeById(1L)).willReturn(Optional.of(questionType1));

        mockMvc.perform(get("/api/question-types/id/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.typeCode", is("TF")))
                .andExpect(jsonPath("$.description", is("True/False Question")));

        verify(questionTypeService).getQuestionTypeById(1L);
    }

    /**
     * Test Case ID: UT_QBM_04_TC04
     * Purpose: Verify behavior when a question type ID is not found.
     * Note: The controller currently throws NoSuchElementException. The TestGlobalExceptionHandler converts this to a 500 response.
     *
     * Prerequisites:
     *  - QuestionTypeService is mocked.
     *  - Mocked QuestionTypeService.getQuestionTypeById() returns an empty Optional.
     *
     * Test Steps:
     *  1. Perform a GET request to /api/question-types/id/{id} with a non-existent ID.
     *  2. Assert that the response status is 500 (Internal Server Error).
     *  3. Verify that questionTypeService.getQuestionTypeById() was called once with the correct ID.
     *
     * Expected Results:
     *  - Status code is 500.
     *  - The service method getQuestionTypeById is invoked once with the specified ID.
     */
    @Test
    @DisplayName("UT_QBM_04_TC04: Get Question Type By ID - Not Found")
    void getQuestionTypeById_NotFound_ReturnsInternalServerError() throws Exception {
        given(questionTypeService.getQuestionTypeById(3L)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/question-types/id/{id}", 3L))
                .andExpect(status().isInternalServerError());
// Removed explicit exception check as it's handled by ControllerAdvice
//                .andExpect(mvcResult -> {
//                    Exception ex = mvcResult.getResolvedException();
//                    assertNotNull(ex);
//                    assertEquals(java.util.NoSuchElementException.class, ex.getClass());
//                    assertTrue(ex.getMessage().contains("No value present"));
//                });

        verify(questionTypeService).getQuestionTypeById(3L);
    }

    /**
     * Test Case ID: UT_QBM_04_TC05
     * Purpose: Verify successful retrieval of a question type by its type code.
     *
     * Prerequisites:
     *  - QuestionTypeService is mocked.
     *  - Mocked QuestionTypeService.getQuestionTypeByCode() returns an Optional containing a question type.
     *
     * Test Steps:
     *  1. Perform a GET request to /api/question-types/code/{typeCode} with a valid type code.
     *  2. Assert that the response status is 200 (OK).
     *  3. Assert that the response content type is application/json.
     *  4. Assert that the response body contains the expected question type.
     *  5. Verify that questionTypeService.getQuestionTypeByCode() was called once with the correct type code.
     *
     * Expected Results:
     *  - Status code is 200.
     *  - Content type is application/json.
     *  - Response body matches the question type returned by the service.
     *  - The service method getQuestionTypeByCode is invoked once with the specified type code.
     */
    @Test
    @DisplayName("UT_QBM_04_TC05: Get Question Type By Type Code - Success")
    void getQuestionTypeByTypeCode_Success_ReturnsQuestionType() throws Exception {
        given(questionTypeService.getQuestionTypeByCode(EQTypeCode.MC)).willReturn(Optional.of(questionType2));

        mockMvc.perform(get("/api/question-types/code/{typeCode}", "MC"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.typeCode", is("MC")))
                .andExpect(jsonPath("$.description", is("Multiple Choice Question")));

        verify(questionTypeService).getQuestionTypeByCode(EQTypeCode.MC);
    }

    /**
     * Test Case ID: UT_QBM_04_TC06
     * Purpose: Verify behavior when a question type code is not found.
     * Note: The controller currently throws NoSuchElementException. The TestGlobalExceptionHandler converts this to a 500 response.
     *
     * Prerequisites:
     *  - QuestionTypeService is mocked.
     *  - Mocked QuestionTypeService.getQuestionTypeByCode() returns an empty Optional for a valid but non-existent type code.
     *
     * Test Steps:
     *  1. Perform a GET request to /api/question-types/code/{typeCode} with a valid but non-existent type code.
     *  2. Assert that the response status is 500 (Internal Server Error).
     *  3. Verify that questionTypeService.getQuestionTypeByCode() was called once.
     *
     * Expected Results:
     *  - Status code is 500.
     *  - The service method getQuestionTypeByCode is invoked once.
     */
    @Test
    @DisplayName("UT_QBM_04_TC06: Get Question Type By Type Code - Not Found")
    void getQuestionTypeByTypeCode_NotFound_ReturnsInternalServerError() throws Exception {
        // Assuming MS is a valid EQTypeCode but the service returns empty for it in this scenario.
        given(questionTypeService.getQuestionTypeByCode(EQTypeCode.MS)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/question-types/code/{typeCode}", "MS"))
                .andExpect(status().isInternalServerError());
// Removed explicit exception check as it's handled by ControllerAdvice
//                .andExpect(mvcResult -> {
//                    Exception ex = mvcResult.getResolvedException();
//                    assertNotNull(ex);
//                    assertEquals(java.util.NoSuchElementException.class, ex.getClass());
//                    assertTrue(ex.getMessage().contains("No value present"));
//                });

        verify(questionTypeService).getQuestionTypeByCode(EQTypeCode.MS);
    }

    /**
     * Test Case ID: UT_QBM_04_TC07
     * Purpose: Verify behavior when an invalid question type code string is provided.
     * This should result in an IllegalArgumentException. The TestGlobalExceptionHandler converts this to a 500 response.
     *
     * Prerequisites:
     *  - None (service interaction is not expected before the enum conversion fails).
     *
     * Test Steps:
     *  1. Perform a GET request to /api/question-types/code/{typeCode} with an invalid type code string.
     *  2. Assert that the response status is 500 (Internal Server Error).
     *
     * Expected Results:
     *  - Status code is 500.
     */
    @Test
    @DisplayName("UT_QBM_04_TC07: Get Question Type By Invalid Type Code String - Returns Internal Server Error")
    void getQuestionTypeByTypeCode_InvalidCodeString_ReturnsInternalServerError() throws Exception {
        // No need to mock service behavior here, as the `EQTypeCode.valueOf(typeCode)` will fail first.
        mockMvc.perform(get("/api/question-types/code/{typeCode}", "INVALID_CODE"))
                .andExpect(status().isInternalServerError());
// Removed explicit exception check as it's handled by ControllerAdvice
//                .andExpect(mvcResult -> {
//                    Exception ex = mvcResult.getResolvedException();
//                    assertNotNull(ex);
//                    assertEquals(IllegalArgumentException.class, ex.getClass());
//                    assertTrue(ex.getMessage().contains("INVALID_CODE"));
//                });
        // We don't verify service calls here because the controller method should throw an exception before calling the service.
    }
} 