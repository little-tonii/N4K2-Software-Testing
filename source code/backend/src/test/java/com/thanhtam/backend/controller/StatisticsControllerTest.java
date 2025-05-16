package com.thanhtam.backend.controller;

import com.thanhtam.backend.dto.StatisticsDashboard;
import com.thanhtam.backend.service.StatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsControllerTest {

    @Mock
    private StatisticsService statisticsService;

    @InjectMocks
    private StatisticsController statisticsController;

    private StatisticsDashboard expectedDashboard;

    @BeforeEach
    void setUp() {
        expectedDashboard = new StatisticsDashboard();
        expectedDashboard.setAccountTotal(100L);
        expectedDashboard.setExamTotal(50L);
        expectedDashboard.setExamUserTotal(200L);
        expectedDashboard.setQuestionTotal(500L);
        expectedDashboard.setChangeQuestion(10.5);
        expectedDashboard.setChangeExam(5.2);
        expectedDashboard.setChangeAccount(2.1);
        expectedDashboard.setChangeExamUser(8.7);
        expectedDashboard.setExamUserLastedSevenDaysTotal(java.util.Collections.singletonList(75L));

        // Mock service calls
        when(statisticsService.countAccountTotal()).thenReturn(expectedDashboard.getAccountTotal());
        when(statisticsService.countExamTotal()).thenReturn(expectedDashboard.getExamTotal());
        when(statisticsService.countExamUserTotal()).thenReturn(expectedDashboard.getExamUserTotal());
        when(statisticsService.countQuestionTotal()).thenReturn(expectedDashboard.getQuestionTotal());
        when(statisticsService.getChangeQuestion()).thenReturn(expectedDashboard.getChangeQuestion());
        when(statisticsService.getChangeExam()).thenReturn(expectedDashboard.getChangeExam());
        when(statisticsService.getChangeAccount()).thenReturn(expectedDashboard.getChangeAccount());
        when(statisticsService.getChangeExamUser()).thenReturn(expectedDashboard.getChangeExamUser());
        when(statisticsService.countExamUserLastedSevenDaysTotal()).thenReturn(expectedDashboard.getExamUserLastedSevenDaysTotal());
    }

    /**
     * Test Case ID: UT_SM_01_TC01
     * Purpose: Verify that getStatistics() correctly retrieves and maps data from StatisticsService to StatisticsDashboard.
     *
     * Prerequisites:
     *  - StatisticsService is mocked.
     *  - Mocked StatisticsService methods return predefined values.
     *
     * Test Steps:
     *  1. Call statisticsController.getStatistics().
     *  2. Assert that the returned StatisticsDashboard object contains the values mocked from StatisticsService.
     *  3. Verify that all relevant methods in StatisticsService were called exactly once.
     *
     * Expected Results:
     *  - The StatisticsDashboard object matches the expectedDashboard.
     *  - All mocked service methods are invoked once.
     */
    @Test
    @DisplayName("UT_SM_01_TC01: Get Statistics - Success")
    void getStatistics_Success_ReturnsDashboard() {
        StatisticsDashboard actualDashboard = statisticsController.getStatistics();

        assertEquals(expectedDashboard.getAccountTotal(), actualDashboard.getAccountTotal());
        assertEquals(expectedDashboard.getExamTotal(), actualDashboard.getExamTotal());
        assertEquals(expectedDashboard.getExamUserTotal(), actualDashboard.getExamUserTotal());
        assertEquals(expectedDashboard.getQuestionTotal(), actualDashboard.getQuestionTotal());
        assertEquals(expectedDashboard.getChangeQuestion(), actualDashboard.getChangeQuestion());
        assertEquals(expectedDashboard.getChangeExam(), actualDashboard.getChangeExam());
        assertEquals(expectedDashboard.getChangeAccount(), actualDashboard.getChangeAccount());
        assertEquals(expectedDashboard.getChangeExamUser(), actualDashboard.getChangeExamUser());
        assertEquals(expectedDashboard.getExamUserLastedSevenDaysTotal(), actualDashboard.getExamUserLastedSevenDaysTotal());

        verify(statisticsService, times(1)).countAccountTotal();
        verify(statisticsService, times(1)).countExamTotal();
        verify(statisticsService, times(1)).countExamUserTotal();
        verify(statisticsService, times(1)).countQuestionTotal();
        verify(statisticsService, times(1)).getChangeQuestion();
        verify(statisticsService, times(1)).getChangeExam();
        verify(statisticsService, times(1)).getChangeAccount();
        verify(statisticsService, times(1)).getChangeExamUser();
        verify(statisticsService, times(1)).countExamUserLastedSevenDaysTotal();
    }
} 