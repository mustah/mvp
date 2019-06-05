export const enum Period {
  now = 'now',
  today = 'today',
  yesterday = 'yesterday',
  currentMonth = 'currentMonth',
  previousMonth = 'previousMonth',
  previous7Days = 'previous7Days',
  custom = 'custom',
}

export const enum TemporalResolution {
  all = 'all',
  hour = 'hour',
  day = 'day',
  month = 'month',
}

export const defaultPeriodResolution: { [p in Period]: TemporalResolution } = {
  now: TemporalResolution.hour,
  today: TemporalResolution.hour,
  yesterday: TemporalResolution.hour,
  currentMonth: TemporalResolution.day,
  previousMonth: TemporalResolution.day,
  previous7Days: TemporalResolution.day,
  custom: TemporalResolution.day,
};

export interface DateRange {
  start: Date;
  end: Date;
}
