import 'CustomPeriodSelector.scss';
import * as React from 'react';
import {DateRangePicker, DateRangePickerShape} from 'react-dates';

type MappedProps = 'startDate' | 'endDate' | 'focusedInput';
type MappedEventListeners = 'onDatesChange' | 'onFocusChange';

type EventListeners = Pick<DateRangePickerShape, MappedEventListeners>;
export type CustomPeriodSelectorProps = Pick<DateRangePickerShape, MappedProps>;

const makeAllDaysSelectable = () => false;

export const CustomPeriodSelector = (
  {
    startDate,
    endDate,
    focusedInput,
    onDatesChange,
    onFocusChange,
  }: CustomPeriodSelectorProps & EventListeners) => (
  <DateRangePicker
    startDate={startDate || null}
    startDateId="customPeriodStartDateId"
    endDate={endDate || null}
    endDateId="customPeriodEndDateId"
    onDatesChange={onDatesChange}
    focusedInput={focusedInput}
    onFocusChange={onFocusChange}
    minimumNights={0}
    hideKeyboardShortcutsPanel={true}
    isOutsideRange={makeAllDaysSelectable}
  />
);
