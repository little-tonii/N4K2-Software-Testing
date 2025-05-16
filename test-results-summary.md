# Test Results Summary

## AM (Account Management) Module

### UserServiceTest.java
| Test Case ID | Test Method | Status | Issue |
|--------------|-------------|--------|-------|
| UT_AM_01 | testGetAllUsers | ✅ Passed | - |
| UT_AM_02 | testGetUserById | ✅ Passed | - |
| UT_AM_03 | testCreateUser | ✅ Passed | - |
| UT_AM_04 | testUpdateUser | ✅ Passed | - |
| UT_AM_05 | testDeleteUser | ✅ Passed | - |

### AuthenticationControllerTest.java
| Test Case ID | Test Method | Status | Issue |
|--------------|-------------|--------|-------|
| UT_AC_01 | testAuthenticateUser | ✅ Passed | - |
| UT_AC_02 | testRegisterUser | ✅ Passed | - |
| UT_AC_03 | testInvalidLogin | ✅ Passed | - |

### UserControllerTest.java
| Test Case ID | Test Method | Status | Issue |
|--------------|-------------|--------|-------|
| UT_UC_01 | testGetAllUsers | ✅ Passed | - |
| UT_UC_02 | testGetUserById | ✅ Passed | - |
| UT_UC_03 | testCreateUser | ✅ Passed | - |
| UT_UC_04 | testUpdateUser | ✅ Passed | - |
| UT_UC_05 | testDeleteUser | ✅ Passed | - |

## CM (Course Management) Module

### CourseServiceTest.java
| Test Case ID | Test Method | Status | Issue |
|--------------|-------------|--------|-------|
| UT_CM_01 | testGetAllCourses | ✅ Passed | - |
| UT_CM_02 | testGetCourseById | ✅ Passed | - |
| UT_CM_03 | testCreateCourse | ✅ Passed | - |
| UT_CM_04 | testUpdateCourse | ✅ Passed | - |
| UT_CM_05 | testDeleteCourse | ✅ Passed | - |

### PartServiceTest.java
| Test Case ID | Test Method | Status | Issue |
|--------------|-------------|--------|-------|
| UT_CM_06 | testGetAllParts | ✅ Passed | - |
| UT_CM_07 | testGetPartById | ✅ Passed | - |
| UT_CM_08 | testCreatePart | ✅ Passed | - |
| UT_CM_09 | testUpdatePart | ✅ Passed | - |
| UT_CM_10 | testDeletePart | ✅ Passed | - |

### CourseControllerTest.java
| Test Case ID | Test Method | Status | Issue |
|--------------|-------------|--------|-------|
| UT_CC_01 | testGetAllCourses | ✅ Passed | - |
| UT_CC_02 | testGetCourseById | ✅ Passed | - |
| UT_CC_03 | testCreateCourse | ✅ Passed | - |
| UT_CC_04 | testUpdateCourse | ✅ Passed | - |
| UT_CC_05 | testDeleteCourse | ✅ Passed | - |

### PartControllerTest.java
| Test Case ID | Test Method | Status | Issue |
|--------------|-------------|--------|-------|
| UT_CC_06 | testGetAllParts | ✅ Passed | - |
| UT_CC_07 | testGetPartById | ✅ Passed | - |
| UT_CC_08 | testCreatePart | ✅ Passed | - |
| UT_CC_09 | testUpdatePart | ✅ Passed | - |
| UT_CC_10 | testDeletePart | ✅ Passed | - |

## EM (Exam Management) Module

### ExamServiceTest.java
| Test Case ID | Test Method | Status | Issue |
|--------------|-------------|--------|-------|
| UT_EM_01 | testGetAllExams | ✅ Passed | - |
| UT_EM_02 | testGetExamById | ✅ Passed | - |
| UT_EM_03 | testCancelExam | ❌ Failed | Exam cancellation operation returned false when it should have returned true |
| UT_EM_04 | testCreateExam | ✅ Passed | - |
| UT_EM_05 | testUpdateExam | ✅ Passed | - |
| UT_EM_06 | testGetAllExams | ❌ Failed | Exam retrieval operation returned false when it should have returned true |
| UT_EM_07 | testGetExamById | ✅ Passed | - |
| UT_EM_08 | testCreateExam | ✅ Passed | - |
| UT_EM_09 | testUpdateExam | ✅ Passed | - |
| UT_EM_10 | testDeleteExam | ✅ Passed | - |
| UT_EM_11 | testGetExamUserById | ✅ Passed | - |
| UT_EM_12 | testGetChoiceListWithNullExamQuestionPoints | ❌ Failed | The system did not throw a NullPointerException when handling null exam question points as expected |

### ExamControllerTest.java
| Test Case ID | Test Method | Status | Issue |
|--------------|-------------|--------|-------|
| UT_EC_01 | getExamsByPage_ShouldReturnPage_WhenAdminUser | ❌ Failed | Wrong exception type (NoSuchElementException instead of NullPointerException) |
| UT_EC_02 | getExamById_ShouldReturnExam_WhenValidId | ✅ Passed | - |
| UT_EC_03 | getExamById_ShouldReturnNotFound_WhenInvalidId | ✅ Passed | - |
| UT_EC_04 | createExam_ShouldReturnBadRequest_WhenInvalidData | ✅ Passed | - |
| UT_EC_05 | updateExam_ShouldReturnNotFound_WhenInvalidId | ✅ Passed | - |
| UT_EC_06 | createExam_ShouldCreateExam_WhenValidData | ❌ Failed | Server error (500) instead of success (200) |
| UT_EC_07 | updateExam_ShouldUpdateExam_WhenValidData | ✅ Passed | - |
| UT_EC_08 | deleteExam_ShouldDeleteExam_WhenValidId | ✅ Passed | - |
| UT_EC_09 | getExamUserById_ShouldReturnExamUser_WhenValidId | ✅ Passed | - |
| UT_EC_10 | getExamCalendar_ShouldReturnCalendar | ❌ Failed | Expected NullPointerException not thrown |
| UT_EC_11 | cancelExam_ShouldCancelExam_WhenValidId | ❌ Failed | Service method not called |
| UT_EC_12 | getExamUserById_ShouldReturnBadRequest_WhenExamLocked | ❌ Failed | Wrong HTTP status (200 instead of 400) |
| UT_EC_13 | getExamUserById_ShouldReturnNotFound_WhenInvalidId | ✅ Passed | - |
| UT_EC_14 | getExamUserById_ShouldReturnForbidden_WhenUnauthorized | ✅ Passed | - |
| UT_EC_15 | createExam_ShouldHandleInvalidIntake | ❌ Failed | Service method called when it shouldn't be |

## QBM (Question Bank Management) Module

### ChoiceServiceImplTest.java
| Test Case ID | Test Method | Status | Issue |
|--------------|-------------|--------|-------|
| UT_QBM_01 | testGetAllChoices | ✅ Passed | - |
| UT_QBM_02 | testGetChoiceById | ✅ Passed | - |
| UT_QBM_03 | testCreateChoice | ✅ Passed | - |
| UT_QBM_04 | testUpdateChoice | ✅ Passed | - |
| UT_QBM_05 | testDeleteChoice | ✅ Passed | - |
| UT_QBM_06 | testGetChoicesByQuestionId | ✅ Passed | - |

### QuestionServiceImplTest.java
| Test Case ID | Test Method | Status | Issue |
|--------------|-------------|--------|-------|
| UT_QBM_07 | testGetAllQuestions | ✅ Passed | - |
| UT_QBM_08 | testGetQuestionById | ✅ Passed | - |
| UT_QBM_09 | testCreateQuestion | ✅ Passed | - |
| UT_QBM_10 | testUpdateQuestion | ✅ Passed | - |
| UT_QBM_11 | testDeleteQuestion | ✅ Passed | - |
| UT_QBM_12 | testGetQuestionsByPartId | ✅ Passed | - |
| UT_QBM_13 | testGetQuestionsByTypeId | ✅ Passed | - |
| UT_QBM_14 | testGetQuestionsByDifficulty | ✅ Passed | - |
| UT_QBM_15 | testGetQuestionsByCourseId | ✅ Passed | - |
| UT_QBM_16 | testGetRandomQuestions | ✅ Passed | - |

### QuestionTypeServiceImplTest.java
| Test Case ID | Test Method | Status | Issue |
|--------------|-------------|--------|-------|
| UT_QBM_17 | testGetAllQuestionTypes | ✅ Passed | - |
| UT_QBM_18 | testGetQuestionTypeById | ✅ Passed | - |
| UT_QBM_19 | testCreateQuestionType | ✅ Passed | - |
| UT_QBM_20 | testUpdateQuestionType | ✅ Passed | - |
| UT_QBM_21 | testDeleteQuestionType | ✅ Passed | - |
| UT_QBM_22 | testGetQuestionTypeByCode | ✅ Passed | - |
| UT_QBM_23 | testGetQuestionTypesByCategory | ✅ Passed | - |
| UT_QBM_24 | testGetQuestionTypesByDifficulty | ✅ Passed | - |
| UT_QBM_25 | testGetQuestionTypesByStatus | ✅ Passed | - |

### QuestionControllerTest.java
| Test Case ID | Test Method | Status | Issue |
|--------------|-------------|--------|-------|
| UT_QC_01 | testGetAllQuestions | ✅ Passed | - |
| UT_QC_02 | testGetQuestionById | ✅ Passed | - |
| UT_QC_03 | testCreateQuestion | ✅ Passed | - |
| UT_QC_04 | testUpdateQuestion | ✅ Passed | - |
| UT_QC_05 | testDeleteQuestion | ✅ Passed | - |
| UT_QC_06 | testGetQuestionsByPartId | ✅ Passed | - |
| UT_QC_07 | testGetQuestionsByTypeId | ✅ Passed | - |
| UT_QC_08 | testGetQuestionsByDifficulty | ✅ Passed | - |
| UT_QC_09 | testGetQuestionsByCourseId | ✅ Passed | - |
| UT_QC_10 | testGetRandomQuestions | ✅ Passed | - |
| UT_QC_11 | testGetQuestionById_NotFound | ✅ Passed | - |
| UT_QC_12 | testCreateQuestion_InvalidData | ✅ Passed | - |
| UT_QC_13 | testUpdateQuestion_NotFound | ✅ Passed | - |
| UT_QC_14 | testDeleteQuestion_NotFound | ✅ Passed | - |
| UT_QC_15 | testGetQuestionsByPartId_NotFound | ✅ Passed | - |
| UT_QC_16 | testGetQuestionsByTypeId_NotFound | ✅ Passed | - |
| UT_QC_17 | testGetQuestionsByDifficulty_Invalid | ✅ Passed | - |
| UT_QC_18 | testGetQuestionsByCourseId_NotFound | ✅ Passed | - |
| UT_QC_19 | testGetRandomQuestions_InvalidCount | ✅ Passed | - |
| UT_QC_20 | testGetRandomQuestions_NoQuestions | ✅ Passed | - |
| UT_QC_21 | testGetRandomQuestions_NotEnoughQuestions | ✅ Passed | - |
| UT_QC_22 | testGetRandomQuestions_ZeroCount | ✅ Passed | - |
| UT_QC_23 | testGetRandomQuestions_NegativeCount | ✅ Passed | - |
| UT_QC_24 | testGetRandomQuestions_NullPartId | ✅ Passed | - |
| UT_QC_25 | testGetRandomQuestions_InvalidPartId | ✅ Passed | - |

### QuestionTypeControllerTest.java
| Test Case ID | Test Method | Status | Issue |
|--------------|-------------|--------|-------|
| UT_QTC_01 | testGetAllQuestionTypes | ✅ Passed | - |
| UT_QTC_02 | testGetQuestionTypeById | ✅ Passed | - |
| UT_QTC_03 | testCreateQuestionType | ✅ Passed | - |
| UT_QTC_04 | testUpdateQuestionType | ✅ Passed | - |
| UT_QTC_05 | testDeleteQuestionType | ✅ Passed | - |
| UT_QTC_06 | testGetQuestionTypeByCode | ✅ Passed | - |
| UT_QTC_07 | testGetQuestionTypesByCategory | ✅ Passed | - |

## SM (Statistics Management) Module

### StatisticsServiceTest.java
| Test Case ID | Test Method | Status | Issue |
|--------------|-------------|--------|-------|
| UT_SM_01 | testCountExamTotal | ❌ Failed | Expected 3, got 51 exams |
| UT_SM_02 | testCountQuestionTotal | ❌ Failed | Expected 3, got 113 questions |
| UT_SM_03 | testCountAccountTotal | ❌ Failed | Expected 3, got 158 accounts |
| UT_SM_04 | testCountExamUserTotal | ❌ Failed | Expected 3, got 160 exam users |
| UT_SM_05 | testGetChangeExamUser | ❌ Error | Missing Course entity (id 12) |
| UT_SM_06 | testCountExamUserLastedSevenDaysTotal | ❌ Error | Missing Course entity (id 12) |
| UT_SM_07 | testGetChangeQuestion | ❌ Error | Missing Course entity (id 12) |
| UT_SM_08 | testGetChangeAccount | ❌ Failed | Expected 100.0%, got 6500.0% |
| UT_SM_09 | testGetChangeExam | ❌ Error | Missing Course entity (id 9) |
| UT_SM_10 | testGetChangeExamUserBothZero | ❌ Error | Missing Course entity (id 12) |
| UT_SM_11 | testGetChangeExamUserOnlyLastWeek | ❌ Error | Missing Course entity (id 12) |
| UT_SM_12 | testGetChangeExamUserOnlyCurrentWeek | ❌ Error | Missing Course entity (id 12) |
| UT_SM_13 | testGetChangeQuestionBothZero | ❌ Error | Missing Course entity (id 12) |
| UT_SM_14 | testGetChangeAccountBothZero | ❌ Failed | Expected 0.0%, got 6500.0% |
| UT_SM_15 | testGetChangeExamBothZero | ❌ Error | Missing Course entity (id 9) |
| UT_SM_16 | testCountExamUserLastedSevenDaysTotalEmpty | ❌ Error | Missing Course entity (id 12) |
| UT_SM_17 | testIsSameDayNull | ✅ Passed | - |
| UT_SM_18 | testIsSameWeekNull | ✅ Passed | - |
| UT_SM_19 | testIsLastWeekNull | ✅ Passed | - |

### ChartControllerTest.java
| Test Case ID | Test Method | Status | Issue |
|--------------|-------------|--------|-------|
| UT_CC_01 | testGetExamCompletionChart | ✅ Passed | - |
| UT_CC_02 | testGetExamCompletionChart_NoData | ✅ Passed | - |
| UT_CC_03 | testGetExamCompletionChart_CurrentWeekOnly | ✅ Passed | - |
| UT_CC_04 | testGetExamCompletionChart_LastWeekOnly | ✅ Passed | - |
| UT_CC_05 | testGetExamCompletionChart_BothWeeks | ✅ Passed | - |
| UT_CC_06 | testGetExamCompletionChart_ZeroValues | ✅ Passed | - |
| UT_CC_07 | testGetExamCompletionChart_NullValues | ✅ Passed | - |
| UT_CC_08 | testGetExamCompletionChart_InvalidData | ✅ Passed | - |
| UT_CC_09 | testGetExamCompletionChart_EmptyData | ✅ Passed | - |
| UT_CC_10 | testGetExamCompletionChart_MaxValues | ✅ Passed | - |
| UT_CC_11 | testGetExamCompletionChart_MinValues | ✅ Passed | - |
| UT_CC_12 | testGetExamCompletionChart_BoundaryValues | ✅ Passed | - |
| UT_CC_13 | testGetExamCompletionChart_EdgeCases | ✅ Passed | - |
| UT_CC_14 | testGetExamCompletionChart_StressTest | ✅ Passed | - |
| UT_CC_15 | testGetExamCompletionChart_ConcurrentAccess | ✅ Passed | - |
| UT_CC_16 | testGetExamCompletionChart_ErrorHandling | ✅ Passed | - |

### StatisticsControllerTest.java
| Test Case ID | Test Method | Status | Issue |
|--------------|-------------|--------|-------|
| UT_SC_01 | testGetStatistics | ✅ Passed | - |

## Common Issues Summary

| Issue Type | Description | Affected Modules |
|------------|-------------|------------------|
| Data | Missing Course entities (IDs 9 and 12) | EM, SM |
| Logic | Incorrect entity counting | SM |
| Logic | Wrong percentage calculations | SM |
| Logic | Exam cancellation issues | EM |
| Validation | Exception handling problems | EM |
| Validation | HTTP status code mismatches | EM |

Legend:
- ✅ Passed
- ❌ Failed/Error 