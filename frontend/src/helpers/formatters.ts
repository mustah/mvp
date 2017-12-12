import * as moment from 'moment';
import * as numeral from 'numeral';

export const FORMAT_NUMBER_DEFAULT = '0,0.0[000]';
export const FORMAT_DATE_DAY_MONTH = 'DD/MM';

export const formatDate = (date: Date, format?: string) => moment(date).format(format || FORMAT_DATE_DAY_MONTH);
export const formatNumber = (num: number | string, format?: string) =>
  numeral(num).format(format || FORMAT_NUMBER_DEFAULT);
