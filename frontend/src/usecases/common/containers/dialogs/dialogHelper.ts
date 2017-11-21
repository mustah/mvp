import {Flag} from '../../../../state/domain-models/flag/flagModels';

export const renderFlags = (flags: Flag[]): string => {
  return flags.map((flag) => flag.title).join(', ');
};
