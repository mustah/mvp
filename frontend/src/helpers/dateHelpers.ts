import {default as moment} from 'moment-timezone';
import 'moment/locale/en-gb';
import 'moment/locale/sv';
import {DateRange, Period} from '../components/dates/dateModels';
import {EncodedUriParameters} from '../types/Types';
import {Maybe} from './Maybe';

/**
 * We work with Date and Period, to not expose moment() to our application.
 */
moment.tz.load(require('moment-timezone/data/packed/latest.json'));

export const momentFrom = (input?: moment.MomentInput): moment.Moment => moment(input).tz('UTC');

export const changeLocale = (language: string): string => moment.locale(language);

/**
 * Calculate absolute start- and end dates based on an input date and a relative time period.*
 */
const dateRange = (now: Date, period: Period, customDateRange: Maybe<DateRange>): DateRange => {
  const zonedDate = momentFrom(now);
  switch (period) {
    case Period.currentMonth:
      return {
        start: zonedDate.startOf('month').toDate(),
        end: zonedDate.endOf('month').startOf('day').toDate(),
      };
    case Period.currentWeek:
      return {
        start: zonedDate.startOf('isoWeek').toDate(),
        end: zonedDate.endOf('isoWeek').add(1, 'days').startOf('day').toDate(),
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
        end: prevMonth.endOf('month').startOf('day').toDate(),
      };
    case Period.custom:
      return customDateRange.map(({start, end}) => ({
        start: momentFrom(start).startOf('day').toDate(),
        end: momentFrom(end).clone().add(1, 'days').startOf('day').toDate(),
      })).orElse({start: zonedDate.toDate(), end: zonedDate.toDate()});
    case Period.latest:
    default:
      const yesterday = zonedDate.clone().subtract(1, 'days');
      return {
        start: yesterday.startOf('day').toDate(),
        end: zonedDate.clone().startOf('day').toDate(),
      };
  }
};

const currentDateRange = (
  start: Date = momentFrom().toDate(),
  period: Period,
  customDateRange: Maybe<DateRange>,
): DateRange => dateRange(start, period, customDateRange);

const toApiParameters = ({start, end}: DateRange): EncodedUriParameters[] => {
  return [
    `after=${encodeURIComponent(start.toISOString())}`,
    `before=${encodeURIComponent(end.toISOString())}`,
  ];
};

export interface CurrentPeriod {
  start?: Date;
  period: Period;
  customDateRange: Maybe<DateRange>;
}

export const toPeriodApiParameters = ({
  start,
  period,
  customDateRange,
}: CurrentPeriod): EncodedUriParameters[] =>
  toApiParameters(currentDateRange(start, period, customDateRange));

export const yyyymmdd = 'YYYY-MM-DD';

const toFriendlyIso8601 = ({start, end}: DateRange): string =>
  `${momentFrom(start).format(yyyymmdd)} - ${momentFrom(end).format(yyyymmdd)}`;

export const prettyRange = ({start, period, customDateRange}: CurrentPeriod): string =>
  toFriendlyIso8601(currentDateRange(start, period, customDateRange));

const yyyymmddhhMm = `${yyyymmdd} HH:mm`;
const utcOffsetHours = 1;

export const displayDate = (input: moment.MomentInput, format: string = yyyymmddhhMm): string => {
  const date = momentFrom(input).add(utcOffsetHours, 'hours');
  return `${date.format(format)}`;
};

export const timestamp = (input: moment.MomentInput): string =>
  displayDate(input, 'MMM D, HH:mm');
