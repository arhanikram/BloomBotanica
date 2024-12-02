package com.example.bloombotanica;

import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.bloombotanica.database.TaskDao;
import com.example.bloombotanica.database.UserPlantDao;
import com.example.bloombotanica.models.Task;
import com.example.bloombotanica.models.UserPlant;
import com.example.bloombotanica.ui.CalendarFragment;
import com.example.bloombotanica.utils.RecurrenceUtils;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

@RunWith(AndroidJUnit4.class)
class CalendarFragmentUnitTest {

    @Mock
    TaskDao mockTaskDao;
    @Mock
    UserPlantDao mockUserPlantDao;
    @Mock
    RecurrenceUtils mockRecurrenceUtils;
    @InjectMocks
    CalendarFragment calendarFragment;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFetchTaskDates() {
        // Given
        Task mockTask = mock(Task.class);
        UserPlant mockUserPlant = mock(UserPlant.class);

        // Mock the behavior
        when(mockTaskDao.getIncompleteTasks()).thenReturn(List.of(mockTask));
        when(mockUserPlantDao.getUserPlantById(anyInt())).thenReturn(mockUserPlant);
        when(mockRecurrenceUtils.calculateFutureDates(any(Date.class), anyInt(), anyInt()))
                .thenReturn(List.of(new Date()));

        // Call the method to be tested
        calendarFragment.fetchTaskDates();

        // Verify database interactions
        verify(mockTaskDao).getIncompleteTasks();
        verify(mockUserPlantDao).getUserPlantById(anyInt());
        verify(mockRecurrenceUtils).calculateFutureDates(any(Date.class), anyInt(), anyInt());
    }

    @Test
    public void testFetchTasksForDate() {
        // Given
        Date selectedDate = new Date();
        Set<Task> mockTasks = Set.of(mock(Task.class), mock(Task.class));

        // Mock the behavior
        when(mockTaskDao.getIncompleteTasksForDate(any(Date.class), any(Date.class)))
                .thenReturn(List.of(mock(Task.class)));

        // Call the method to be tested
        calendarFragment.fetchTasksForDate(selectedDate);

        // Verify that database methods are called
        verify(mockTaskDao).getIncompleteTasksForDate(any(Date.class), any(Date.class));
    }
}
