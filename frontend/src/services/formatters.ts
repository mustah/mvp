import * as moment from 'moment';
import * as numeral from 'numeral';

export const FORMAT_NUMBER_DEFAULT = '0,0.0[000]';
export const FORMAT_DATE_DAY_MONTH = 'DD/MM';

export const formatDate = (date: Date, format?: string) => moment(date).format(format || FORMAT_DATE_DAY_MONTH);
export const formatNumber = (num: number | string, format?: string) =>
  numeral(num).format(format || FORMAT_NUMBER_DEFAULT);

// from https://stackoverflow.com/a/14994860/49879
// I want 999 and 1.5k, with numeral(1499).format('0.0a') I get '1.5k', but the same format for 999 gives me 999.0 :(
export const suffix = (num: number): string => {
  if (num >= 1000000000) {
    return (num / 1000000000).toFixed(1).replace(/\.0$/, '') + 'G';
  }
  if (num >= 1000000) {
    return (num / 1000000).toFixed(1).replace(/\.0$/, '') + 'M';
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1).replace(/\.0$/, '') + 'k';
  }
  return num.toFixed(0);
};
