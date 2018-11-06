import {firstUpperTranslated, translate} from '../services/translationService';
import {ParameterName} from '../state/user-selection/userSelectionModels';
import {Status} from '../types/Types';
import {texts} from './texts';

export const orUnknown = (name?: string) => !name || name.toLowerCase() === 'unknown' ? translate('unknown') : name;

export const getTranslationOrName = (name: string, domainModelName: ParameterName): string => {
  switch (domainModelName) {
    case ParameterName.cities:
    case ParameterName.addresses:
      return orUnknown(name);
    default:
      return name;
  }
};

export const statusTranslation = (name: string): string => {
  const statuses = {
    [Status.ok]: translate('ok'),
    [Status.error]: translate('error'),
    [Status.unknown]: translate('unknown'),
    [Status.warning]: translate('warning'),
  };
  return statuses[name] || statuses[Status.unknown];
};

export const translatedErrorMessage = (message: string): string => {
  if (message === texts.invalidToken) {
    return firstUpperTranslated('token missing or invalid');
  } else if (message === texts.badCredentials) {
    return firstUpperTranslated('bad credentials');
  } else {
    return message;
  }
};
