import {default as numeral} from 'numeral';
import {firstUpper, translate} from '../services/translationService';
import {momentAtUtcPlusOneFrom} from './dateHelpers';

const isGreaterThan100 = (collectionPercentage: number): boolean =>
  collectionPercentage !== undefined && collectionPercentage > 100;

export const roundedPercentage = (num: number): string => {
  const rounded = Math.floor(num * 10) / 10;
  return round(rounded, rounded === 100 || rounded === 0 ? '' : '0.0');
};

export const formatPercentage = (num: number): string => `${roundedPercentage(num)}%`;

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
  momentAtUtcPlusOneFrom(date).format(format);

export const roundMeasurement = (num: number | string): string =>
  round(num, '0.000');

export const round = (num: number | string, format: string): string =>
  isNaN(Number(num)) ? num : numeral(num).format(format);

export const cityWithoutCountry = (city: string): string => {
  const cityMatchParts = city.match(/[^,]+,(.+)/);
  return firstUpper(cityMatchParts === null ? city : cityMatchParts[1]);
};

export const formatReadInterval = (minutes: number | undefined): string => {
  if (!minutes) {
    return translate('unknown');
  } else if (minutes >= 60) {
    return (minutes / 60) + translate('hour in short');
  } else {
    return minutes + translate('minute in short');
  }
};
