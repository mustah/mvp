import * as numeral from 'numeral';
import {momentWithTimeZone} from './dateHelpers';

const isGreaterThan100 = (collectionPercentage: number): boolean =>
  collectionPercentage !== undefined && collectionPercentage > 100;

const formatPercentage = (num: number): string =>
  round(num, '0.0') + '%';

export const formatCollectionPercentage =
  (collectionPercentage?, readIntervalMinutes?, isSuperAdmin?: boolean): string => {
    if (readIntervalMinutes === 0.0
        || collectionPercentage === undefined
        || readIntervalMinutes === undefined) {
      return '-';
    }
    const greaterThan100 = isGreaterThan100(collectionPercentage);
    const percentage = greaterThan100 ? 100 : collectionPercentage;
    return isSuperAdmin && greaterThan100
      ? `${formatPercentage(percentage)} *`
      : formatPercentage(percentage);
  };

export const formatDate = (date: Date, format: string = 'DD/MM'): string =>
  momentWithTimeZone(date).format(format);

export const roundMeasurement = (num: number | string): string =>
  round(num, '0.000');

export const round = (num: number | string, format: string): string =>
  isNaN(Number(num)) ? num : numeral(num).format(format);
