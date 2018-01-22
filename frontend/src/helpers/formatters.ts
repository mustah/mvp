import * as moment from 'moment';
import * as numeral from 'numeral';

export const FORMAT_NUMBER_DEFAULT = '0,0.0[000]';
export const FORMAT_DATE_DAY_MONTH = 'DD/MM';
export const FORMAT_DATE_FULL_MINUTE = 'YY-MM-DD hh:mm';

export const formatDate = (date: Date, format?: string): string =>
  moment(date).format(format || FORMAT_DATE_DAY_MONTH);
export const formatNumber = (num: number | string, format?: string): string =>
  numeral(num).format(format || FORMAT_NUMBER_DEFAULT);

export const unixTimestampMillisecondsToDate = (timestamp: number): string =>
  formatDate(new Date(timestamp), FORMAT_DATE_FULL_MINUTE);
