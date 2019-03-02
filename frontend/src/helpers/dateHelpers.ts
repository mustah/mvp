import {default as moment} from 'moment-timezone';
import 'moment/locale/en-gb';
import 'moment/locale/sv';
import {DateRange, Period} from '../components/dates/dateModels';
import {SelectionInterval} from '../state/user-selection/userSelectionModels';
import {EncodedUriParameters} from '../types/Types';
import {Maybe} from './Maybe';
import {RequestParameter, RequestParameters} from './urlFactory';

moment.tz.load(require('moment-timezone/data/packed/latest.json'));

export const momentAtUtcPlusOneFrom = (input?: moment.MomentInput): moment.Moment => moment(input).utcOffset('+0100');

export const changeLocale = (language: string): string => moment.locale(language);

/**
 * Calculate absolute start- and end dates based on an input date and a relative time period.
 */
const dateRange = (now: Date, period: Period, customDateRange: Maybe<DateRange>): DateRange => {
  const zonedDate = momentAtUtcPlusOneFrom(now);
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
        start: zonedDate.clone().startOf('day').subtract(6, 'days').toDate(),
        end: zonedDate.clone().startOf('day').toDate(),
      };
    case Period.previousMonth:
      const prevMonth = zonedDate.clone().subtract(1, 'month');
      return {
        start: prevMonth.startOf('month').toDate(),
        end: prevMonth.endOf('month').startOf('day').toDate(),
      };
    case Period.custom:
      return customDateRange.map(({start, end}) => ({
        start: momentAtUtcPlusOneFrom(start).startOf('day').toDate(),
        end: momentAtUtcPlusOneFrom(end).add(1, 'days').startOf('day').toDate(),
      })).orElse({
        start: zonedDate.clone().startOf('day').toDate(),
        end: zonedDate.clone().startOf('day').toDate()
      });
    case Period.latest:
    default:
      const yesterday = zonedDate.clone().subtract(1, 'days');
      return {
        start: yesterday.startOf('day').toDate(),
        end: zonedDate.clone().startOf('day').toDate(),
      };
  }
};

export const newDateRange = (
  period: Period,
  customDateRange: Maybe<DateRange> = Maybe.nothing(),
  start: Date = momentAtUtcPlusOneFrom().toDate(),
): DateRange => dateRange(start, period, customDateRange);

export const yyyymmdd = 'YYYY-MM-DD';
const yyyymmddhhMm = `${yyyymmdd} HH:mm`;

const hhmmss = 'HH:mm:ss.sss';
const apiFormat = `${yyyymmdd}T${hhmmss}+01:00`;

const toApiParameters = ({start, end}: DateRange): EncodedUriParameters[] => [
  `after=${encodeURIComponent(momentAtUtcPlusOneFrom(start).format(apiFormat))}`,
  `before=${encodeURIComponent(momentAtUtcPlusOneFrom(end).format(apiFormat))}`,
];

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
  toApiParameters(newDateRange(period, customDateRange, start));

export const queryParametersOfDateRange = ({
  period,
  customDateRange,
}: SelectionInterval): RequestParameters => {
  const {start, end}: DateRange = newDateRange(period, Maybe.maybe(customDateRange));
  return {
    [RequestParameter.after]: momentAtUtcPlusOneFrom(start).format(apiFormat),
    [RequestParameter.before]: momentAtUtcPlusOneFrom(end).format(apiFormat),
  };
};

const toFriendlyIso8601 = ({start, end}: DateRange): string =>
  `${momentAtUtcPlusOneFrom(start).format(yyyymmdd)} - ${momentAtUtcPlusOneFrom(end).format(yyyymmdd)}`;

export const prettyRange = ({start, period, customDateRange}: CurrentPeriod): string =>
  toFriendlyIso8601(newDateRange(period, customDateRange, start));

export const shortTimestamp = (input: moment.MomentInput): string =>
  displayDate(input, 'MMM D, HH:mm');

export const displayDate = (input: moment.MomentInput, format: string = yyyymmddhhMm): string =>
  momentAtUtcPlusOneFrom(input).format(format);
