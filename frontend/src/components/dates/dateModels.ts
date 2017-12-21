import * as moment from 'moment';
import {Maybe} from '../../helpers/Maybe';

/*
type iso8601Instant = string;

interface ApiFriendlyInterval {
  after?: iso8601Instant;
  before?: iso8601Instant;
}
*/

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

interface StartAndEnd {
  start: Maybe<Date>;
  end: Maybe<Date>;
}

const formatYyMmDd = (date: Date): string => {
  return `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())}`;
};

export const toFriendlyIso8601 = (startAndEnd: StartAndEnd): string => {
  const {start, end} = startAndEnd;
  const startDate: string = start.map(formatYyMmDd).orElse('');
  const endDate: string = end.map(formatYyMmDd).orElse('');
  return `${startDate} - ${endDate}`;
};

export const toApiParameters = (startAndEnd: StartAndEnd): string[] => {
  const parameters: string[] = [];
  startAndEnd.start.map((date) =>
    parameters.push(`after=${encodeURIComponent(date.toISOString())}`));
  startAndEnd.end.map((date) =>
    parameters.push(`before=${encodeURIComponent(date.toISOString())}`));
  return parameters;
};

// We work with Period and Date, to not expose moment() to our application
export const startAndEnd = (period: Period): StartAndEnd => {
  const now = new Date();
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
