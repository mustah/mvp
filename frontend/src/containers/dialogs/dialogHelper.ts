import {Flag} from '../../state/domain-models/flag/flagModels';
import {translate} from '../../services/translationService';

export const renderFlags = (flags: Flag[]): string => {
  if (flags.length) {
    return flags.map((flag) => flag.title).join(', ');
  } else {
    return translate('no');
  }
};
