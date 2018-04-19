import * as moment from 'moment-timezone';
import {DateRange, Period} from '../components/dates/dateModels';
import {EncodedUriParameters} from '../types/Types';
import {Maybe} from './Maybe';

moment.tz.load(require('moment-timezone/data/packed/latest.json'));
const timezoneUtc = 'UTC';

// TODO: This should more general or change name to momentWithTimeZoneStockholm
export const momentWithTimeZone = (input: moment.MomentInput): moment.Moment =>
  moment(input).tz(timezoneUtc);

/**
 * Calculate absolute start- and end dates based on an input date and a relative time period.
 *
 * We work with Date and Period, to not expose moment() to our application.
 */
const dateRange = (now: Date, period: Period, customDateRange: Maybe<DateRange>): DateRange => {
  const zonedDate = momentWithTimeZone(now);
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
    case Period.custom:
      return customDateRange.map(({start, end}) => ({
        start: momentWithTimeZone(start).startOf('day').toDate(),
        end: momentWithTimeZone(end).endOf('day').toDate(),
      })).orElse({start: zonedDate.toDate(), end: zonedDate.toDate()});
    case Period.latest:
    default:
      const yesterday = zonedDate.clone().subtract(1, 'days');
      return {
        start: yesterday.startOf('day').toDate(),
        end: yesterday.endOf('day').toDate(),
      };
  }
};

export const now = (): Date => moment().tz(timezoneUtc).toDate();

const currentDateRange = (
  now: Date,
  period: Period,
  customDateRange: Maybe<DateRange>,
): DateRange => dateRange(now, period, customDateRange);

const toApiParameters = ({start, end}: DateRange): EncodedUriParameters[] => {
  return [
    `after=${encodeURIComponent(start.toISOString())}`,
    `before=${encodeURIComponent(momentWithTimeZone(end).add(1, 'ms').toISOString())}`,
  ];
};

export interface CurrentPeriod {
  now: Date;
  period: Period;
  customDateRange: Maybe<DateRange>;
}

export const toPeriodApiParameters = (
  {
    now,
    period,
    customDateRange,
  }: CurrentPeriod): EncodedUriParameters[] =>
  toApiParameters(currentDateRange(now, period, customDateRange));

const yyyymmdd = 'YYYY-MM-DD';

const toFriendlyIso8601 = ({start, end}: DateRange): string =>
  `${momentWithTimeZone(start).format(yyyymmdd)} - ${momentWithTimeZone(end).format(yyyymmdd)}`;

export const prettyRange = ({now, period, customDateRange}: CurrentPeriod): string =>
  toFriendlyIso8601(currentDateRange(now, period, customDateRange));

export const formatLabelTimeStamp = (input: moment.MomentInput): string =>
  momentWithTimeZone(input).format('MMM D, HH:mm');
