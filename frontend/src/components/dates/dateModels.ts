export const enum Period {
  now = 'now',
  latest = 'latest',
  currentMonth = 'current_month',
  previousMonth = 'previous_month',
  currentWeek = 'current_week',
  previous7Days = 'previous_7_days',
  custom = 'custom',
}

export const enum TemporalResolution {
  hour = 'hour',
  day = 'day',
  month = 'month',
}

export interface DateRange {
  start: Date;
  end: Date;
}
