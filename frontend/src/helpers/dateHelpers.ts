import * as moment from 'moment-timezone';
import {DateRange, Period} from '../components/dates/dateModels';

export const timezoneStockholm = 'Europe/Stockholm';

const momentWithTimeZone = (date: Date): moment.Moment => moment(date).tz(timezoneStockholm);

const padZero = (aNumber: number): string => {
  return aNumber < 10 ? `0${aNumber}` : aNumber + '';
};

export const toApiParameters = ({start, end}: DateRange): string[] => {
  return [
    `after=${encodeURIComponent(start.toISOString())}`,
    `before=${encodeURIComponent(end.toISOString())}`,
  ];
};

/**
 * Calculate absolute start- and end dates based on an input date and a relative time period.
 *
 * We work with Date and Period, to not expose moment() to our application.
 */
export const dateRange = (now: Date, period: Period): DateRange => {
  switch (period) {
    case Period.currentMonth:
      return {
        start: momentWithTimeZone(now).startOf('month').toDate(),
        end: momentWithTimeZone(now).endOf('month').toDate(),
      };
    case Period.currentWeek:
      return {
        start: momentWithTimeZone(now).startOf('isoWeek').toDate(),
        end: momentWithTimeZone(now).endOf('isoWeek').toDate(),
      };
    case Period.previous7Days:
      return {
        start: momentWithTimeZone(now).subtract(6, 'days').toDate(),
        end: now,
      };
    case Period.previousMonth:
      const prevMonth = momentWithTimeZone(now).subtract(1, 'month');
      return {
        start: prevMonth.startOf('month').toDate(),
        end: prevMonth.endOf('month').toDate(),
      };
    case Period.latest:
    default:
      const yesterday = momentWithTimeZone(now).subtract(1, 'days');
      return {
        start: yesterday.startOf('day').toDate(),
        end: yesterday.endOf('day').toDate(),
      };
  }
};

export const currentDateRange = (period: Period): DateRange => dateRange(new Date(), period);

const formatYyMmDd = (date: Date): string =>
  `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())}`;

export const toFriendlyIso8601 = ({start, end}: DateRange): string =>
  `${formatYyMmDd(start)} - ${formatYyMmDd(end)}`;

export const prettyRange = (period: Period): string =>
  toFriendlyIso8601(dateRange(new Date(), period));
