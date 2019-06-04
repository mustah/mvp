export const enum Period {
  now = 'now',
  today = 'today',
  yesterday = 'yesterday',
  currentMonth = 'current_month',
  previousMonth = 'previous_month',
  previous7Days = 'previous_7_days',
  custom = 'custom',
}

export const enum TemporalResolution {
  all = 'all',
  hour = 'hour',
  day = 'day',
  month = 'month',
}

export interface DateRange {
  start: Date;
  end: Date;
}
