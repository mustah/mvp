import * as numeral from 'numeral';
import {momentWithTimeZone} from './dateHelpers';

export const FORMAT_DATE_DAY_MONTH = 'DD/MM';
export const FORMAT_DATE_FULL_MINUTE = 'YY-MM-DD HH:mm';
export const FORMAT_COLLECTION_PERCENT = '0.0';
const FORMAT_MEASUREMENT = '0.000';

export const formatDate = (date: Date, format: string = FORMAT_DATE_DAY_MONTH): string =>
  momentWithTimeZone(date).format(format);

export const formatCollectionPercentage =
  (collectionPercentage?: number, readIntervalMinutes?: number): string => {
    if (readIntervalMinutes === 0.0 || collectionPercentage === undefined || readIntervalMinutes === undefined) {
      return '-';
    }
    return roundCollectionPercentage(collectionPercentage);
  };

const roundCollectionPercentage = (num: number): string =>
  round(num, FORMAT_COLLECTION_PERCENT) + '%';

export const roundMeasurement = (num: number | string): string =>
  round(num, FORMAT_MEASUREMENT);

export const round = (num: number | string, format: string): string =>
  isNaN(Number(num)) ? num : numeral(num).format(format);
