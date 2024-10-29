package com.example.bloombotanica;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bloombotanica.databinding.FragmentCalendarBinding;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import androidx.core.content.ContextCompat;

import java.util.Calendar;
import java.util.Objects;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialCalendarView calendarView = binding.calendarView;
        calendarView.addDecorator(new TaskDayDecorator(requireContext()));

        calendarView.setDateSelected(CalendarDay.today(), true);
    }

    public static class TaskDayDecorator implements DayViewDecorator {

        private final Context context;

        public TaskDayDecorator(Context context) {
            this.context = context;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            Calendar today = Calendar.getInstance();
            int currentMonth = today.get(Calendar.MONTH) + 1; // Calendar.MONTH is zero-based
            int currentYear = today.get(Calendar.YEAR);

            // Compare day’s month and year to current month and year
            return day.getMonth() == currentMonth && day.getYear() == currentYear;
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.water_drop_svgrepo_com)));

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
