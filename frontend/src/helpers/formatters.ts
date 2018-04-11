import * as moment from 'moment';
import * as numeral from 'numeral';

export const FORMAT_DATE_DAY_MONTH = 'DD/MM';
export const FORMAT_DATE_FULL_MINUTE = 'YY-MM-DD hh:mm';

export const formatDate = (date: Date, format?: string): string =>
  moment(date).format(format || FORMAT_DATE_DAY_MONTH);
export const roundMeasurement = (num: number | string): string =>
  isNaN(Number(num)) ? num : numeral(num).format('0.000');

export const unixTimestampMillisecondsToDate = (timestamp: number): string =>
  formatDate(new Date(timestamp), FORMAT_DATE_FULL_MINUTE);
