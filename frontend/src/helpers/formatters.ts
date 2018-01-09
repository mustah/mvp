import * as moment from 'moment';
import * as numeral from 'numeral';
import {translate} from '../services/translationService';

export const FORMAT_NUMBER_DEFAULT = '0,0.0[000]';
export const FORMAT_DATE_DAY_MONTH = 'DD/MM';

export const formatDate = (date: Date, format?: string) => moment(date).format(format || FORMAT_DATE_DAY_MONTH);
export const formatNumber = (num: number | string, format?: string) =>
  numeral(num).format(format || FORMAT_NUMBER_DEFAULT);

const firstUpper = (original: string): string => {
  if (original.length === 0) {
    return original;
  }
  return original[0].toUpperCase() + original.substr(1);
};

export const firstUpperTranslated = (original: string): string => firstUpper(translate(original));
