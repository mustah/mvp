import 'CustomPeriodSelector.scss';
import * as React from 'react';
import {DateRangePicker, DateRangePickerShape} from 'react-dates';
import {yyyymmdd} from '../../helpers/dateHelpers';
import {firstUpperTranslated} from '../../services/translationService';

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
    startDatePlaceholderText={firstUpperTranslated('start date')}
    endDatePlaceholderText={firstUpperTranslated('end date')}
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
    displayFormat={yyyymmdd}
  />
);
