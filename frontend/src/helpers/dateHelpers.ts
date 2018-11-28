import {default as moment} from 'moment-timezone';
import 'moment/locale/en-gb';
import 'moment/locale/sv';
import {DateRange, Period} from '../components/dates/dateModels';
import {SelectionInterval} from '../state/user-selection/userSelectionModels';
import {EncodedUriParameters} from '../types/Types';
import {Maybe} from './Maybe';
import {RequestParameter, RequestParameters} from './urlFactory';
import StartOf = moment.unitOfTime.StartOf;

moment.tz.load(require('moment-timezone/data/packed/latest.json'));

export const momentFrom = (input?: moment.MomentInput): moment.Moment => moment(input).tz('UTC');

const intervalMinutesToString = (minutes: number): StartOf => minutes === 60 ? 'hour' : 'day';
export const startOfLatestInterval = (now: moment.MomentInput, intervalInMinutes: number): Date =>
  momentFrom(now).startOf(intervalMinutesToString(intervalInMinutes)).toDate();

export const changeLocale = (language: string): string => moment.locale(language);

/**
 * Calculate absolute start- and end dates based on an input date and a relative time period.
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

export const yyyymmdd = 'YYYY-MM-DD';
const yyyymmddhhMm = `${yyyymmdd} HH:mm`;

const hhmmss = 'HH:mm:ss.sss';
const apiFormat = `${yyyymmdd}T${hhmmss}+01:00`;

const toApiParameters = ({start, end}: DateRange): EncodedUriParameters[] => [
  `after=${encodeURIComponent(momentFrom(start).format(apiFormat))}`,
  `before=${encodeURIComponent(momentFrom(end).format(apiFormat))}`,
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
  toApiParameters(currentDateRange(start, period, customDateRange));

export const queryParametersOfDateRange = ({
  period,
  customDateRange,
}: SelectionInterval): RequestParameters => {
  const {start, end}: DateRange = currentDateRange(undefined, period, Maybe.maybe(customDateRange));
  return {
    [RequestParameter.after]: momentFrom(start).format(apiFormat),
    [RequestParameter.before]: momentFrom(end).format(apiFormat),
  };
};

const toFriendlyIso8601 = ({start, end}: DateRange): string =>
  `${momentFrom(start).format(yyyymmdd)} - ${momentFrom(end).format(yyyymmdd)}`;

export const prettyRange = ({start, period, customDateRange}: CurrentPeriod): string =>
  toFriendlyIso8601(currentDateRange(start, period, customDateRange));

export const timestamp = (input: moment.MomentInput): string =>
  displayDate(input, 'MMM D, HH:mm');

export const displayDate = (input: moment.MomentInput, format: string = yyyymmddhhMm): string =>
  momentFrom(input).add(1, 'hours').format(format);
