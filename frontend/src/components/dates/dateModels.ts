export const enum Period {
  latest = 'latest',
  currentMonth = 'current_month',
  previousMonth = 'previous_month',
  currentWeek = 'current_week',
  previous7Days = 'previous_7_days',
  custom = 'custom',
}

export interface DateRange {
  start: Date;
  end: Date;
}
