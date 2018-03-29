import * as moment from 'moment-timezone';
import {DateRange, Period} from '../components/dates/dateModels';

moment.tz.load(require('moment-timezone/data/packed/latest.json'));

export const timezoneStockholm = 'Europe/Stockholm';

export const momentWithTimeZone = (input: moment.MomentInput): moment.Moment =>
  moment(input).tz(timezoneStockholm);

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
export const dateRange = (date: Date, period: Period): DateRange => {
  const zonedDate = momentWithTimeZone(date);
  switch (period) {
    case Period.currentMonth:
      return {
        start: zonedDate.startOf('month').toDate(),
        end: zonedDate.endOf('month').toDate(),
      };
    case Period.currentWeek:
      return {
        start: zonedDate.startOf('isoWeek').toDate(),
        end: zonedDate.endOf('isoWeek').toDate(),
      };
    case Period.previous7Days:
      return {
        start: zonedDate.clone().subtract(6, 'days').toDate(),
        end: zonedDate.toDate(),
      };
    case Period.previousMonth:
      const prevMonth = zonedDate.clone().subtract(1, 'month');
      return {
        start: prevMonth.startOf('month').toDate(),
        end: prevMonth.endOf('month').toDate(),
      };
    case Period.latest:
    default:
      const yesterday = zonedDate.clone().subtract(1, 'days');
      return {
        start: yesterday.startOf('day').toDate(),
        end: yesterday.endOf('day').toDate(),
      };
  }
};

const now = (): moment.Moment => moment().tz(timezoneStockholm);

export const currentDateRange = (period: Period): DateRange => dateRange(now().toDate(), period);

const yyyymmdd = 'YYYY-MM-DD';

export const toFriendlyIso8601 = ({start, end}: DateRange): string =>
  `${momentWithTimeZone(start).format(yyyymmdd)} - ${momentWithTimeZone(end).format(yyyymmdd)}`;

export const prettyRange = (period: Period): string =>
  toFriendlyIso8601(dateRange(now().toDate(), period));
