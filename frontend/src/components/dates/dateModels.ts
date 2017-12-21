import * as moment from 'moment';
import * as R from 'ramda';
import {Maybe} from '../../helpers/Maybe';

const padZero = (aNumber: number): string => {
  return aNumber < 10 ? `0${aNumber}` : aNumber + '';
};

export const enum Period {
  latest = 'latest',
  currentMonth = 'current_month',
  previousMonth = 'previous_month',
  currentWeek = 'current_week',
  previous7Days = 'previous_7_days',
}

interface DateRange {
  start: Maybe<Date>;
  end: Maybe<Date>;
}

export const toApiParameters = (startAndEnd: DateRange): string[] => {
  const parameters: string[] = [];
  startAndEnd.start.map((date) =>
    parameters.push(`after=${encodeURIComponent(date.toISOString())}`));
  startAndEnd.end.map((date) =>
    parameters.push(`before=${encodeURIComponent(date.toISOString())}`));
  return parameters;
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
        start: Maybe.just(moment(now).startOf('month').toDate()),
        end: Maybe.just(moment(now).endOf('month').toDate()),
      };
    case Period.currentWeek:
      return {
        start: Maybe.just(moment(now).startOf('isoWeek').toDate()),
        end: Maybe.just(moment(now).endOf('isoWeek').toDate()),
      };
    case Period.previous7Days:
      return {
        start: Maybe.just(moment(now).subtract(6, 'days').toDate()),
        end: Maybe.just(now),
      };
    case Period.previousMonth:
      const prevMonth = moment(now).subtract(1, 'month');
      return {
        start: Maybe.just(prevMonth.startOf('month').toDate()),
        end: Maybe.just(prevMonth.endOf('month').toDate()),
      };
    case Period.latest:
    default:
      return {
        start: Maybe.nothing(),
        end: Maybe.nothing(),
      };
  }
};

export const currentDateRange = R.curry(dateRange)(new Date());

const formatYyMmDd = (date: Date): string => {
  return `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())}`;
};

export const toFriendlyIso8601 = ({start, end}: DateRange): string => {
  const startDate: string = start.map(formatYyMmDd).orElse('');
  const endDate: string = end.map(formatYyMmDd).orElse('');
  return `${startDate} - ${endDate}`;
};

export const prettyInterval = R.compose(toFriendlyIso8601, currentDateRange);
