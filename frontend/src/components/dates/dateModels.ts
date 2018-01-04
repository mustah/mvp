import {Maybe} from '../../helpers/Maybe';

export const enum Period {
  latest = 'latest',
  currentMonth = 'current_month',
  previousMonth = 'previous_month',
  currentWeek = 'current_week',
  previous7Days = 'previous_7_days',
}

export interface DateRange {
  start: Maybe<Date>;
  end: Maybe<Date>;
}